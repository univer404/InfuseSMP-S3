package com.universe404.infusesmp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InfuseSMP extends JavaPlugin {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Map<UUID, Map<String,String>> effects = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> history = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String,List<String>>> slotHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> trusted = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String,String>> binds = new ConcurrentHashMap<>();
    private final Set<UUID> simpleDisplay = ConcurrentHashMap.newKeySet();
    private File playersFile;
    private YamlConfiguration players;
    private YamlConfiguration messages;
    private InfuseMechanics mechanics;
    private RecipesMenu recipesMenu;
    private InfuseItems infuseItems;
    private RitualCrafting ritualCrafting;
    private DisabledRegions disabledRegions;
    private final Set<UUID> disabled = ConcurrentHashMap.newKeySet();

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("gui.yml", false);
        playersFile = new File(getDataFolder(), "players.yml");
        players = YamlConfiguration.loadConfiguration(playersFile);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        loadState();
        disabledRegions = new DisabledRegions(this);
        getServer().getPluginManager().registerEvents(disabledRegions, this);

        mechanics = new InfuseMechanics(this);
        getServer().getPluginManager().registerEvents(mechanics, this);
        getServer().getScheduler().runTaskTimer(this, () -> mechanics.tick(), 1L, 1L);

        infuseItems = new InfuseItems(this);
        getServer().getPluginManager().registerEvents(new InfuseItemListener(this, infuseItems), this);
        ritualCrafting = new RitualCrafting(this, infuseItems);
        getServer().getPluginManager().registerEvents(ritualCrafting, this);
        recipesMenu = new RecipesMenu(this, infuseItems);
        getServer().getPluginManager().registerEvents(recipesMenu, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new InfusePlaceholderExpansion(this, mechanics).register();
        }
        getLogger().info("InfuseSMP-S3 enabled. Text rendering disables italics.");
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
    public void tellRaw(CommandSender sender,String value) { sender.sendMessage(render(value)); }
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
        if(cmd.equals("mapregion"))return disabledRegions.command(sender,args);
        if(cmd.equals("infuse"))return infuse(sender,args);
        if(cmd.equals("recipes")) { if(playerOnly(sender))return true; if(!recipesMenu.isEnabled()){tellRaw(sender,"<red>ʀᴇᴄɪᴘᴇ ɢᴜɪ ɪs ᴅɪsᴀʙʟᴇᴅ.");return true;} recipesMenu.openMain((Player)sender); return true; }
        if(cmd.equals("item")) { if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;} if(playerOnly(sender))return true; if(args.length<1)return usage(sender,"/item <effect|all> [true|false]"); Player p=(Player)sender; if(args[0].equalsIgnoreCase("all")){for(String e:List.of("emerald","feather","fire","frost","thunder","haste","heart","invisibility","ocean","regeneration","speed","strength")){giveOrDrop(p,infuseItems.create(e,false,false));giveOrDrop(p,infuseItems.create(e,true,false));}}else{boolean aug=args.length>1&&Boolean.parseBoolean(args[1]);ItemStack result=infuseItems.create(args[0],aug,false);if(result==null){tell(sender,"invalid-effect");return true;}giveOrDrop(p,result);}return true; }
        if(cmd.equals("swap")) {
            if(playerOnly(sender))return true; UUID id=((Player)sender).getUniqueId(); Map<String,String> m=slots(id);
            String p=m.get("primary"),s=m.get("secondary");
            if(s==null)m.remove("primary");else m.put("primary",s);
            if(p==null)m.remove("secondary");else m.put("secondary",p);
            log(id,"set primary "+m.getOrDefault("primary","none"));log(id,"set secondary "+m.getOrDefault("secondary","none"));saveState();tell(sender,"swapped");return true;
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
            if(args.length>1|| (args.length==1&&!isSlot(args[0])))return usage(sender,"/activate [primary|secondary]");
            mechanics.startSpark((Player)sender,args.length==0?"primary":slot(args[0]));return true;
        }
        if(Set.of("ldrain","pdrain","rdrain","sdrain").contains(cmd)) {
            if(playerOnly(sender))return true;Player p=(Player)sender;
            String sl=(cmd.equals("ldrain")||cmd.equals("pdrain"))?"primary":"secondary";
            String old=slots(p.getUniqueId()).get(sl);
            if(old==null){tell(p,"effects-empty");return true;}
            if(mechanics.isBusy(p.getUniqueId(),old.replace("_augmented",""))){tellRaw(p,"<red>ʏᴏᴜ ᴄᴀɴɴᴏᴛ ʀᴇᴍᴏᴠᴇ ᴛʜɪs ᴇғғᴇᴄᴛ ᴡʜɪʟᴇ ɪᴛ ɪs ᴀᴄᴛɪᴠᴇ ᴏʀ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ!");return true;}
            if(p.getInventory().firstEmpty()==-1){tellRaw(p,"<red>ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ɪs ғᴜʟʟ!");return true;}
            ItemStack item=infuseItems.create(old.replace("_augmented",""),old.endsWith("_augmented"),false);
            if(item!=null)p.getInventory().addItem(item);
            slots(p.getUniqueId()).remove(sl);log(p.getUniqueId(),"drained "+sl+" "+old);tellRaw(p,"<green>ᴅʀᴀɪɴᴇᴅ "+old+" ғʀᴏᴍ ʏᴏᴜʀ "+sl+" sʟᴏᴛ.");saveState();
            return true;
        }
        if(cmd.equals("trust"))return trust(sender,args);
        if(cmd.equals("resetcooldown")) {
            if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            if(playerOnly(sender))return true;
            if(args.length>1||(args.length==1&&!isSlot(args[0])))return usage(sender,"/resetcooldown [primary|secondary]");
            Player p=(Player)sender;List<String> chosen=args.length==0?List.of("primary","secondary"):List.of(slot(args[0]));
            for(String sl:chosen){if(!mechanics.resetCooldown(p,sl)){tellRaw(sender,"<red>ᴛʜᴇ "+sl+" sᴘᴀʀᴋ ɪs sᴛɪʟʟ ᴀᴄᴛɪᴠᴇ.");return true;}}
            tellRaw(sender,"<green>ʀᴇsᴇᴛ ᴄᴏᴏʟᴅᴏᴡɴ ғᴏʀ "+String.join(" ᴀɴᴅ ",chosen)+".");return true;
        }
        if(cmd.equals("resetcraft")) {
            if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            if(args.length>1)return usage(sender,"/resetcraft [effect]");
            if(!ritualCrafting.resetCraft(args.length==0?null:args[0])){tell(sender,"invalid-effect");return true;}
            tellRaw(sender,"<green>ʀᴇsᴇᴛ ᴄʀᴀғᴛ ᴛᴀʟʟɪᴇs.");return true;
        }
        if(cmd.equals("conf")) {
            if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            if(args.length<2)return usage(sender,"/conf <get|set> <key> [subkey] [value]");
            String path=args[1];int valueStart=2;
            if(args[0].equalsIgnoreCase("get")){
                if(args.length>=3){path=args[1]+"."+args[2];}
                if(!getConfig().contains(path)){tellRaw(sender,"<red>ᴋᴇʏ ɴᴏᴛ ғᴏᴜɴᴅ: "+path);return true;}
                tellRaw(sender,"<yellow>"+path+" <gray>ɪs: <white>"+String.valueOf(getConfig().get(path)));return true;
            }
            if(args[0].equalsIgnoreCase("set")){
                if(args.length<3)return usage(sender,"/conf set <key> <value> or /conf set <category> <key> <value>");
                if(args.length>=4){path=args[1]+"."+args[2];valueStart=3;}
                String value=String.join(" ",Arrays.copyOfRange(args,valueStart,args.length));Object parsed=value;
                if(value.equalsIgnoreCase("true")||value.equalsIgnoreCase("false"))parsed=Boolean.parseBoolean(value);
                else try{parsed=Integer.parseInt(value);}catch(NumberFormatException ignored){try{parsed=Double.parseDouble(value);}catch(NumberFormatException ignored2){}}
                getConfig().set(path,parsed);saveConfig();tellRaw(sender,"<green>ᴜᴘᴅᴀᴛᴇᴅ "+path+".");return true;
            }
            return usage(sender,"/conf <get|set> <key> [subkey] [value]");
        }
        return false;
    }
    private boolean usage(CommandSender s,String usage){tell(s,"usage",Map.of("usage",usage));return true;}

    private boolean infuse(CommandSender sender,String[] args) {
        if(args.length==0)return usage(sender,"/infuse <add|remove|get|history|toggle>");
        String sub=args[0].toLowerCase(Locale.ROOT);
        String sl=null,effect=null,targetName=null; boolean augmented=false;
        for(String arg:args){
            if(arg.startsWith("-s="))sl=arg.substring(3);
            else if(arg.startsWith("-e="))effect=arg.substring(3).toLowerCase(Locale.ROOT);
            else if(arg.startsWith("-p="))targetName=arg.substring(3);
            else if(arg.equalsIgnoreCase("-a=true"))augmented=true;
        }
        Player target=targetName==null?(sender instanceof Player?(Player)sender:null):Bukkit.getPlayerExact(targetName);
        if(target==null){tell(sender,"player-only");return true;}
        UUID id=target.getUniqueId();
        if(sub.equals("get")){
            if(target!=sender&&!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            Map<String,String> m=slots(id);
            if(sl!=null){
                if(!isSlot(sl))return usage(sender,"/infuse get -p=<player> [-s=primary|secondary]");
                String value=m.get(slot(sl));tellRaw(sender,"<yellow>"+slot(sl)+" <gray>» <white>"+(value==null?"none":value));return true;
            }
            tellRaw(sender,"<yellow>ᴘʀɪᴍᴀʀʏ <gray>» <white>"+m.getOrDefault("primary","none"));
            tellRaw(sender,"<yellow>sᴇᴄᴏɴᴅᴀʀʏ <gray>» <white>"+m.getOrDefault("secondary","none"));return true;
        }
        if(sub.equals("history")){
            if(target!=sender&&!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
            if(sl==null||!isSlot(sl))return usage(sender,"/infuse history -s=<primary|secondary> [-p=<player>]");
            List<String> h=history.getOrDefault(id,List.of());
            if(h.isEmpty()){tellRaw(sender,"<gray>ɴᴏ ʜɪsᴛᴏʀʏ ғᴏᴜɴᴅ.");return true;}
            h.forEach(line->tellRaw(sender,"<gray>"+line));return true;
        }
        if(!sender.hasPermission("infuse.admin")){tell(sender,"no-permission");return true;}
        if(!Set.of("add","remove","toggle").contains(sub))return usage(sender,"/infuse <add|remove|get|history|toggle> [-p=<player>] [-e=<effect>] [-s=<slot>] [-a=true]");
        if(sub.equals("toggle")){
            if(!disabled.add(id))disabled.remove(id);
            tellRaw(sender,disabled.contains(id)?"<yellow>ɪɴғᴜsᴇ ᴅɪsᴀʙʟᴇᴅ.":"<green>ɪɴғᴜsᴇ ᴇɴᴀʙʟᴇᴅ.");
            if(target!=sender)tellRaw(target,disabled.contains(id)?"<red>ʏᴏᴜʀ ɪɴғᴜsᴇ ᴇғғᴇᴄᴛs ʜᴀᴠᴇ ʙᴇᴇɴ ᴅɪsᴀʙʟᴇᴅ.":"<green>ʏᴏᴜʀ ɪɴғᴜsᴇ ᴇғғᴇᴄᴛs ʜᴀᴠᴇ ʙᴇᴇɴ ᴇɴᴀʙʟᴇᴅ.");
            saveState();return true;
        }
        if(sl!=null&&!isSlot(sl))return usage(sender,"Slot must be primary or secondary.");
        List<String> targetSlots=sl==null?List.of("primary","secondary"):List.of(slot(sl));
        Map<String,String> m=slots(id);
        if(sub.equals("remove")){
            for(String s:targetSlots){String old=m.remove(s);if(old!=null)log(id,"removed "+s+" "+old);}
            tellRaw(sender,"<green>ʀᴇᴍᴏᴠᴇᴅ ᴇғғᴇᴄᴛs ғʀᴏᴍ "+(sl==null?"ʙᴏᴛʜ sʟᴏᴛs":slot(sl)+" sʟᴏᴛ")+" ᴏғ "+target.getName()+".");
        } else {
            if(effect==null)return usage(sender,"/infuse add -p=<player> -e=<effect> [-s=<slot>] [-a=true]");
            if(effect.equals("regen"))effect="regeneration";
            if(!Set.of("earth","emerald","ender","feather","fire","frost","haste","heart","invisibility","ocean","regeneration","speed","strength","thunder").contains(effect)){tell(sender,"invalid-effect");return true;}
            if((effect.equals("earth")||effect.equals("ender"))&&!augmented)return usage(sender,"earth and ender require -a=true");
            if(!getConfig().getBoolean("allow_same_effects",false)&&targetSlots.size()==1){
                String other=m.get(targetSlots.get(0).equals("primary")?"secondary":"primary");
                if(effect.equals(other==null?null:other.replace("_augmented",""))){tellRaw(sender,"<red>ᴇғғᴇᴄᴛ ᴀʟʀᴇᴀᴅʏ ᴇǫᴜɪᴘᴘᴇᴅ ɪɴ ᴏᴛʜᴇʀ sʟᴏᴛ.");return true;}
            }
            for(String s:targetSlots){String value=effect+(augmented?"_augmented":"");m.put(s,value);log(id,"set "+s+" "+value);}
            tellRaw(sender,"<green>ᴀᴘᴘʟɪᴇᴅ "+effect+" ᴛᴏ "+(sl==null?"ʙᴏᴛʜ sʟᴏᴛs":slot(sl)+" sʟᴏᴛ")+" ᴏғ "+target.getName()+".");
        }
        saveState();return true;
    }

    private boolean trust(CommandSender sender,String[] args) {
        if(playerOnly(sender))return true;
        Player p=(Player)sender;UUID owner=p.getUniqueId();
        Set<UUID> set=trusted.computeIfAbsent(owner,k->ConcurrentHashMap.newKeySet());
        if(args.length==0)return usage(sender,"/trust <add|remove|list|clear> [player]");
        String action=args[0].toLowerCase(Locale.ROOT);
        if(action.equals("list")){
            if(set.isEmpty()){tellRaw(sender,"<gray>ʏᴏᴜʀ ᴛʀᴜsᴛ ʟɪsᴛ ɪs ᴇᴍᴘᴛʏ.");return true;}
            tellRaw(sender,"<yellow>ᴛʀᴜsᴛ ʟɪsᴛ <dark_gray>("+set.size()+"/"+getConfig().getInt("trust_list_max",3)+")");
            int i=0;for(UUID id:set){i++;tellRaw(sender,"<yellow>"+i+". <white>"+Optional.ofNullable(Bukkit.getOfflinePlayer(id).getName()).orElse(id.toString())+" <dark_gray>("+id+")");}
            return true;
        }
        if(!Set.of("add","remove","clear").contains(action))return usage(sender,"/trust <add|remove|list|clear> [player]");
        UUID targetId=null;
        if(!action.equals("clear")){
            if(args.length<2)return usage(sender,"/trust "+action+" <player>");
            targetId=Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        }
        if(action.equals("add")){
            if(set.contains(targetId)){tellRaw(sender,"<gray>ᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪs ᴀʟʀᴇᴀᴅʏ ᴛʀᴜsᴛᴇᴅ.");return true;}
            if(set.size()>=getConfig().getInt("trust_list_max",3)&&!sender.hasPermission("trust.bypass_limit")){tellRaw(sender,"<red>ʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ᴛʀᴜsᴛ ʟɪsᴛ ʟɪᴍɪᴛ.");return true;}
        }
        if(action.equals("remove")&&!set.contains(targetId)){tellRaw(sender,"<gray>ᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪs ɴᴏᴛ ᴛʀᴜsᴛᴇᴅ.");return true;}
        tellRaw(sender,"<yellow>ᴀᴄᴛɪᴏɴ ᴘᴇɴᴅɪɴɢ...");
        long delay=Math.max(0,getConfig().getLong("trust_list_delay_seconds",10))*20L;
        UUID finalTargetId=targetId;
        Bukkit.getScheduler().runTaskLater(this,()->{
            if(action.equals("clear")){set.clear();tellRaw(p,"<gray>ᴛʀᴜsᴛ ʟɪsᴛ ᴄʟᴇᴀʀᴇᴅ.");}
            else if(action.equals("add")){set.add(finalTargetId);tellRaw(p,"<gray>ᴀᴅᴅᴇᴅ <white>"+Optional.ofNullable(Bukkit.getOfflinePlayer(finalTargetId).getName()).orElse(args[1])+" <gray>ᴛᴏ ʏᴏᴜʀ ᴛʀᴜsᴛ ʟɪsᴛ.");}
            else {set.remove(finalTargetId);tellRaw(p,"<gray>ʀᴇᴍᴏᴠᴇᴅ <white>"+Optional.ofNullable(Bukkit.getOfflinePlayer(finalTargetId).getName()).orElse(args[1])+" <gray>ғʀᴏᴍ ʏᴏᴜʀ ᴛʀᴜsᴛ ʟɪsᴛ.");}
            saveState();
        },delay);
        return true;
    }
    public String getEquippedEffect(UUID id,String slot){ return slots(id).get(slot); }
    public boolean isSimpleDisplay(UUID id){return simpleDisplay.contains(id);}
    public boolean isInDisabledRegion(Location location){return disabledRegions!=null&&disabledRegions.isDisabled(location);}
    public String getBind(UUID id,String slot){return binds.getOrDefault(id,Map.of()).getOrDefault(slot,"none");}
    public void clearEquippedEffect(UUID id,String slot){String old=slots(id).remove(slot);if(old!=null)log(id,"cleared "+slot+" "+old);saveState();}
    public void setEquippedEffect(UUID id,String slot,String effect,boolean augmented){String value=effect+(augmented?"_augmented":"");slots(id).put(slot,value);log(id,"equipped "+slot+" "+value);saveState();}
    public boolean isSparkActive(UUID id,String effect){return mechanics!=null&&mechanics.isActive(id,effect);}
    public ItemStack createInfuseItem(String effect,boolean augmented){return infuseItems.create(effect,augmented,false);}
    public void giveOrDrop(Player player,ItemStack item){if(item==null)return;Map<Integer,ItemStack> leftover=player.getInventory().addItem(item);leftover.values().forEach(stack->player.getWorld().dropItemNaturally(player.getLocation(),stack));}
    public boolean isDisabled(UUID id){ return disabled.contains(id); }
    public boolean isTrusted(UUID owner,UUID target){ return trusted.getOrDefault(owner,Set.of()).contains(target); }
    public void tellKey(CommandSender sender,String key){ tell(sender,key); }
    public void recordHistory(UUID id,String line){ log(id,line); }
    public void saveStatePublic(){ saveState(); }

    private void loadState() {
        for(String key:players.getKeys(false))try{
            UUID id=UUID.fromString(key);Map<String,String> m=new ConcurrentHashMap<>();
            for(String sl:List.of("primary","secondary")){String v=players.getString(key+".effects."+sl);if(v!=null)m.put(sl,v);}effects.put(id,m);
            Set<UUID> tr=ConcurrentHashMap.newKeySet();for(String v:players.getStringList(key+".trusted"))try{tr.add(UUID.fromString(v));}catch(IllegalArgumentException ignored){}trusted.put(id,tr);
            Map<String,String> b=new ConcurrentHashMap<>();for(String sl:List.of("primary","secondary")){String v=players.getString(key+".binds."+sl);if(v!=null)b.put(sl,v);}binds.put(id,b);
            if(players.getBoolean(key+".simple-display"))simpleDisplay.add(id);if(players.getBoolean(key+".disabled"))disabled.add(id);history.put(id,new ArrayList<>(players.getStringList(key+".history")));
        }catch(IllegalArgumentException ignored){getLogger().warning("Ignoring invalid UUID in players.yml: "+key);}
    }
    private void saveState() {
        if(players==null)return;Set<UUID> ids=new HashSet<>(effects.keySet());ids.addAll(trusted.keySet());ids.addAll(binds.keySet());ids.addAll(simpleDisplay);ids.addAll(history.keySet());ids.addAll(slotHistory.keySet());ids.addAll(disabled);
        for(UUID id:ids){String k=id.toString();for(String sl:List.of("primary","secondary"))players.set(k+".effects."+sl,effects.getOrDefault(id,Map.of()).get(sl));
            players.set(k+".trusted",trusted.getOrDefault(id,Set.of()).stream().map(UUID::toString).toList());
            for(var e:binds.getOrDefault(id,Map.of()).entrySet())players.set(k+".binds."+e.getKey(),e.getValue());
            players.set(k+".simple-display",simpleDisplay.contains(id));players.set(k+".history",history.getOrDefault(id,List.of())); players.set(k+".disabled",disabled.contains(id));}
        try{players.save(playersFile);}catch(IOException e){getLogger().severe("Could not save players.yml: "+e.getMessage());}
    }
}