package com.universe404.infusesmp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InfuseSMP extends JavaPlugin {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Map<UUID, Map<String,String>> effects = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> history = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> trusted = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String,String>> binds = new ConcurrentHashMap<>();
    private final Set<UUID> simpleDisplay = ConcurrentHashMap.newKeySet();
    private File playersFile;
    private YamlConfiguration players;
    private YamlConfiguration messages;\n    private InfuseMechanics mechanics;\n    private final Set<UUID> disabled = ConcurrentHashMap.newKeySet();

    @Override public void onEnable() {
        saveDefaultConfig(); saveResource("messages.yml", false); saveResource("gui.yml", false);
        playersFile = new File(getDataFolder(), "players.yml");
        players = YamlConfiguration.loadConfiguration(playersFile);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        loadState();
        getLogger().info("InfuseSMP-S3 loaded. Text rendering disables italics.");
    }
    @Override public void onDisable() { saveState(); }

    private Component render(String value) {
        return mini.deserialize(value == null ? "" : value).decoration(TextDecoration.ITALIC, false);
    }
    private void tell(CommandSender sender, String key) { sender.sendMessage(render(messages.getString(key, "<red>Missing message: "+key))); }
    private void tell(CommandSender sender, String key, Map<String,String> vars) {
        String value=messages.getString(key, "<red>Missing message: "+key);
        for(var e:vars.entrySet()) value=value.replace("{"+e.getKey()+"}",e.getValue());
        tellRaw(sender,value);
    }
    private void tellRaw(CommandSender sender,String value) { sender.sendMessage(render(value)); }
    private boolean playerOnly(CommandSender s) { if(s instanceof Player)return false; tell(s,"player-only"); return true; }
    private boolean isSlot(String s) { return Set.of("primary","secondary","p","s").contains(s.toLowerCase(Locale.ROOT)); }
    private String slot(String s) { return (s.equalsIgnoreCase("p")||s.equalsIgnoreCase("primary"))?"primary":"secondary"; }
    private Map<String,String> slots(UUID id) { return effects.computeIfAbsent(id,k->new ConcurrentHashMap<>()); }
    private void log(UUID id,String entry) {
        List<String> h=history.computeIfAbsent(id,k->Collections.synchronizedList(new ArrayList<>()));
        h.add(0,System.currentTimeMillis()+" "+entry); while(h.size()>30)h.remove(h.size()-1);
    }

    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        String cmd=command.getName().toLowerCase(Locale.ROOT);
        if(cmd.equals("infuse"))return infuse(sender,args);
        if(cmd.equals("swap")) {
            if(playerOnly(sender))return true; UUID id=((Player)sender).getUniqueId(); Map<String,String> m=slots(id);
            String p=m.get("primary"),s=m.get("secondary");
            if(s==null)m.remove("primary");else m.put("primary",s);
            if(p==null)m.remove("secondary");else m.put("secondary",p);
            log(id,"swapped primary and secondary");saveState();tell(sender,"swapped");return true;
        }
        if(cmd.equals("toggledisplay")) {
            if(playerOnly(sender))return true; UUID id=((Player)sender).getUniqueId();
            if(!simpleDisplay.add(id))simpleDisplay.remove(id);
            tell(sender,"display-toggled",Map.of("display",simpleDisplay.contains(id)?"simple":"icon"));saveState();return true;
        }
        if(cmd.equals("bind")) {
            if(playerOnly(sender))return true;
            if(args.length!=2||!isSlot(args[0]))return usage(sender,"/bind <primary|secondary> <swap|drop|sneak|sneak-drop|sneak-swap|none>");
            String action=args[1].toLowerCase(Locale.ROOT);
            if(!Set.of("swap","drop","sneak","sneak-drop","sneak-swap","none").contains(action))return usage(sender,"/bind <primary|secondary> <swap|drop|sneak|sneak-drop|sneak-swap|none>");
            UUID id=((Player)sender).getUniqueId();binds.computeIfAbsent(id,k->new ConcurrentHashMap<>()).put(slot(args[0]),action);
            tell(sender,"bind-set",Map.of("slot",slot(args[0]),"bind",action));saveState();return true;
        }
        if(cmd.equals("activate")) {
            if(playerOnly(sender))return true;
            if(args.length!=1||!isSlot(args[0]))return usage(sender,"/activate <primary|secondary>");
            String effect=slots(((Player)sender).getUniqueId()).get(slot(args[0]));
            if(effect==null)tell(sender,"effects-empty");else tell(sender,"not-implemented");
            return true;
        }
        if(Set.of("ldrain","pdrain","rdrain","sdrain").contains(cmd)) {
            if(playerOnly(sender))return true;Player p=(Player)sender;
            String sl=(cmd.equals("ldrain")||cmd.equals("pdrain"))?"primary":"secondary";
            String old=slots(p.getUniqueId()).remove(sl);
            if(old==null)tell(p,"effects-empty");else{log(p.getUniqueId(),"drained "+sl+" "+old);tell(p,"effect-removed",Map.of("slot",sl));saveState();}
            return true;
        }
        if(cmd.equals("trust"))return trust(sender,args);
        if(cmd.equals("conf")) {
            if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            if(args.length<2)return usage(sender,"/conf <get|set> <key> [value]");
            if(args[0].equalsIgnoreCase("get")){tellRaw(sender,"<gray>"+args[1]+" = <white>"+getConfig().getString(args[1],"undefined"));return true;}
            if(args[0].equalsIgnoreCase("set")&&args.length>=3){
                String value=String.join(" ",Arrays.copyOfRange(args,2,args.length));Object parsed=value;
                if(value.equalsIgnoreCase("true")||value.equalsIgnoreCase("false"))parsed=Boolean.parseBoolean(value);
                else try{parsed=Integer.parseInt(value);}catch(NumberFormatException ignored){}
                getConfig().set(args[1],parsed);saveConfig();tellRaw(sender,"<green>ᴜᴘᴅᴀᴛᴇᴅ "+args[1]+".");return true;
            }
            return usage(sender,"/conf <get|set> <key> [value]");
        }
        return false;
    }
    private boolean usage(CommandSender s,String usage){tell(s,"usage",Map.of("usage",usage));return true;}

    private boolean infuse(CommandSender sender,String[] args) {
        if(args.length==0)return usage(sender,"/infuse <add|remove|get|history>");
        String sub=args[0].toLowerCase(Locale.ROOT);
        if(sub.equals("get")||sub.equals("history")) {
            if(args.length>1&&!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            Player target=args.length>1?Bukkit.getPlayerExact(args[1]):(sender instanceof Player?(Player)sender:null);
            if(target==null){tell(sender,"player-only");return true;}
            if(sub.equals("get")){
                Map<String,String> m=slots(target.getUniqueId());if(m.isEmpty()){tell(sender,"effects-empty");return true;}
                tell(sender,"effects-header");m.forEach((k,v)->tellRaw(sender,"<gray>"+k+": <white>"+v));
            }else history.getOrDefault(target.getUniqueId(),List.of()).forEach(v->tellRaw(sender,"<gray>"+v));
            return true;
        }
        if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
        if(!sub.equals("add")&&!sub.equals("remove")&&!sub.equals("toggle"))return usage(sender,"/infuse <add|remove|get|history|toggle>");
        String sl=null,effect=null,targetName=null;boolean augmented=false;
        for(String arg:args){if(arg.startsWith("-s="))sl=arg.substring(3);else if(arg.startsWith("-e="))effect=arg.substring(3).toLowerCase(Locale.ROOT);else if(arg.startsWith("-p="))targetName=arg.substring(3);else if(arg.equalsIgnoreCase("-a=true"))augmented=true;}
        if(sl==null||!isSlot(sl))return usage(sender,"/infuse "+sub+" -s=<primary|secondary> -e=<effect> [-p=<player>] [-a=true]");
        Player target=targetName==null?(sender instanceof Player?(Player)sender:null):Bukkit.getPlayerExact(targetName);
        if(target==null){tell(sender,"player-only");return true;}
        String s=slot(sl);Map<String,String> m=slots(target.getUniqueId());
        if(sub.equals("toggle")) { if(args.length<2) return usage(sender,"/infuse toggle -p=<player>"); UUID tid=target.getUniqueId(); if(!disabled.add(tid))disabled.remove(tid); tellRaw(sender, disabled.contains(tid)?"<yellow>ɪɴғᴜsᴇ ᴅɪsᴀʙʟᴇᴅ.":"<green>ɪɴғᴜsᴇ ᴇɴᴀʙʟᴇᴅ."); saveState(); return true; }\n        if(sub.equals("remove")){String old=m.remove(s);if(old==null)tell(sender,"effects-empty");else{log(target.getUniqueId(),"removed "+s+" "+old);tell(sender,"effect-removed",Map.of("slot",s));}}
        else {
            if(effect==null||!Set.of("fire","frost","speed","haste","heart","ocean","invis","invisibility","regeneration","regen","thunder","ender","earth","feather","emerald","strength","apophis","thief").contains(effect)){tell(sender,"invalid-effect");return true;}
            if((effect.equals("earth")||effect.equals("ender"))&&!augmented)return usage(sender,"earth and ender require -a=true");
            if(effect.equals("regen"))effect="regeneration"; String value=effect+(augmented?"_augmented":"");m.put(s,value);log(target.getUniqueId(),"set "+s+" "+value);tell(sender,"effect-set",Map.of("slot",s,"effect",value));
        }
        saveState();return true;
    }
    private boolean trust(CommandSender sender,String[] args) {
        if(playerOnly(sender))return true;Player p=(Player)sender;Set<UUID> set=trusted.computeIfAbsent(p.getUniqueId(),k->ConcurrentHashMap.newKeySet());
        if(args.length==0)return usage(sender,"/trust <add|remove|list|clear> [player]");
        switch(args[0].toLowerCase(Locale.ROOT)){
            case "list" -> set.forEach(id->tellRaw(sender,"<gray>"+Optional.ofNullable(Bukkit.getOfflinePlayer(id).getName()).orElse(id.toString())));
            case "clear" -> set.clear();
            case "add","remove" -> {
                if(args.length<2)return usage(sender,"/trust "+args[0]+" <player>");
                UUID id=Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                if(args[0].equalsIgnoreCase("add")){if(set.size()>=getConfig().getInt("trust_list_max",10)){tellRaw(sender,"<red>ᴛʀᴜsᴛ ʟɪsᴛ ɪs ғᴜʟʟ.");return true;}set.add(id);}else set.remove(id);
            }
            default -> {return usage(sender,"/trust <add|remove|list|clear> [player]");}
        }
        saveState();return true;
    }
    public String getEquippedEffect(UUID id,String slot){ return slots(id).get(slot); }\n    public boolean isDisabled(UUID id){ return disabled.contains(id); }\n    public boolean isTrusted(UUID owner,UUID target){ return trusted.getOrDefault(owner,Set.of()).contains(target); }\n    public void tellKey(CommandSender sender,String key){ tell(sender,key); }\n    public void tellRaw(CommandSender sender,String value){ sender.sendMessage(render(value)); }\n    public void recordHistory(UUID id,String line){ log(id,line); }\n    public void saveStatePublic(){ saveState(); }\n\n    private void loadState() {
        for(String key:players.getKeys(false))try{
            UUID id=UUID.fromString(key);Map<String,String> m=new ConcurrentHashMap<>();
            for(String sl:List.of("primary","secondary")){String v=players.getString(key+".effects."+sl);if(v!=null)m.put(sl,v);}effects.put(id,m);
            Set<UUID> tr=ConcurrentHashMap.newKeySet();for(String v:players.getStringList(key+".trusted"))try{tr.add(UUID.fromString(v));}catch(IllegalArgumentException ignored){}trusted.put(id,tr);
            Map<String,String> b=new ConcurrentHashMap<>();for(String sl:List.of("primary","secondary")){String v=players.getString(key+".binds."+sl);if(v!=null)b.put(sl,v);}binds.put(id,b);
            if(players.getBoolean(key+".simple-display"))simpleDisplay.add(id);if(players.getBoolean(key+".disabled"))disabled.add(id);history.put(id,new ArrayList<>(players.getStringList(key+".history")));
        }catch(IllegalArgumentException ignored){getLogger().warning("Ignoring invalid UUID in players.yml: "+key);}
    }
    private void saveState() {
        if(players==null)return;Set<UUID> ids=new HashSet<>(effects.keySet());ids.addAll(trusted.keySet());ids.addAll(binds.keySet());ids.addAll(simpleDisplay);ids.addAll(history.keySet());
        for(UUID id:ids){String k=id.toString();for(String sl:List.of("primary","secondary"))players.set(k+".effects."+sl,effects.getOrDefault(id,Map.of()).get(sl));
            players.set(k+".trusted",trusted.getOrDefault(id,Set.of()).stream().map(UUID::toString).toList());
            for(var e:binds.getOrDefault(id,Map.of()).entrySet())players.set(k+".binds."+e.getKey(),e.getValue());
            players.set(k+".simple-display",simpleDisplay.contains(id));players.set(k+".history",history.getOrDefault(id,List.of())); players.set(k+".disabled",disabled.contains(id));}
        try{players.save(playersFile);}catch(IOException e){getLogger().severe("Could not save players.yml: "+e.getMessage());}
    }
}