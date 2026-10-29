package com.universe404.infusesmp;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/** Optional WorldGuard region mapping, implemented reflectively so WorldGuard remains optional. */
public final class DisabledRegions implements Listener {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final Map<UUID,String> pending=new HashMap<>();
    public DisabledRegions(JavaPlugin plugin){
        this.plugin=plugin;file=new File(plugin.getDataFolder(),"regions.yml");data=YamlConfiguration.loadConfiguration(file);
    }
    public boolean isDisabled(Location loc){
        if(loc==null||loc.getWorld()==null)return false;
        ConfigurationSection section=data.getConfigurationSection("regions");if(section==null)return false;
        for(String id:section.getKeys(false)){
            String path="regions."+id+".";
            if(!loc.getWorld().getName().equals(data.getString(path+"world")))continue;
            if(loc.getBlockX()>=data.getInt(path+"min-x")&&loc.getBlockX()<=data.getInt(path+"max-x")
              &&loc.getBlockY()>=data.getInt(path+"min-y")&&loc.getBlockY()<=data.getInt(path+"max-y")
              &&loc.getBlockZ()>=data.getInt(path+"min-z")&&loc.getBlockZ()<=data.getInt(path+"max-z"))return true;
        }
        return false;
    }
    public boolean command(CommandSender sender,String[] args){
        if(!sender.hasPermission("infuse.admin")){sender.sendMessage("§cYou do not have permission.");return true;}
        if(args.length==0){usage(sender);return true;}
        String sub=args[0].toLowerCase(Locale.ROOT);
        switch(sub){
            case "add" -> {
                if(!(sender instanceof Player p)){sender.sendMessage("§cOnly players can map regions.");return true;}
                if(args.length!=2||args[1].contains(" ")){sender.sendMessage("§cUsage: /mapregion add <region-id>");return true;}
                if(!worldGuardAvailable()){sender.sendMessage("§cWorldGuard is required to map protected regions.");return true;}
                pending.put(p.getUniqueId(),args[1]);
                sender.sendMessage("§7Right-click a block inside WorldGuard region §f'"+args[1]+"§7' to map it.");
            }
            case "delete" -> {
                if(args.length!=2)return usage(sender);
                ConfigurationSection section=data.getConfigurationSection("regions");
                if(section==null||!section.contains(args[1])){sender.sendMessage("§cRegion '"+args[1]+"' is not mapped.");return true;}
                data.set("regions."+args[1],null);save();sender.sendMessage("§7Removed disabled region §f'"+args[1]+"§7'.");
            }
            case "list" -> {
                ConfigurationSection section=data.getConfigurationSection("regions");
                if(section==null||section.getKeys(false).isEmpty()){sender.sendMessage("§7No regions mapped.");return true;}
                sender.sendMessage("§eDisabled Infuse regions:");
                for(String id:section.getKeys(false)){
                    String p="regions."+id+".";
                    sender.sendMessage("§7 • §f"+id+" §8("+data.getString(p+"world")+") §7from §f("+data.getInt(p+"min-x")+", "+data.getInt(p+"min-y")+", "+data.getInt(p+"min-z")+") §7to §f("+data.getInt(p+"max-x")+", "+data.getInt(p+"max-y")+", "+data.getInt(p+"max-z")+")");
                }
            }
            case "clear" -> {data.set("regions",null);pending.clear();save();sender.sendMessage("§7All mapped disabled regions cleared.");}
            default -> usage(sender);
        }
        return true;
    }
    private boolean usage(CommandSender s){usage(s);return true;}
    private void usage(CommandSender s){s.sendMessage("§e/mapregion add <region-id>");s.sendMessage("§e/mapregion delete <region-id>");s.sendMessage("§e/mapregion list");s.sendMessage("§e/mapregion clear");}
    @EventHandler(ignoreCancelled=true)
    public void onClick(PlayerInteractEvent e){
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null)return;
        Player p=e.getPlayer();String id=pending.get(p.getUniqueId());if(id==null)return;
        try{
            Object region=worldGuardRegion(p.getWorld(),id);
            if(region==null){p.sendMessage("§cWorldGuard region '"+id+"' was not found.");return;}
            Location clicked=e.getClickedBlock().getLocation();
            Object vector=asBlockVector(clicked);
            Object contained=invokeCompatible(region,"contains",vector);
            if(!(contained instanceof Boolean ok)||!ok){p.sendMessage("§cThat block is not inside WorldGuard region '"+id+"'.");return;}
            Object min=region.getClass().getMethod("getMinimumPoint").invoke(region);
            Object max=region.getClass().getMethod("getMaximumPoint").invoke(region);
            String path="regions."+id+".";
            data.set(path+"world",p.getWorld().getName());
            data.set(path+"min-x",number(min,"getBlockX"));data.set(path+"min-y",number(min,"getBlockY"));data.set(path+"min-z",number(min,"getBlockZ"));
            data.set(path+"max-x",number(max,"getBlockX"));data.set(path+"max-y",number(max,"getBlockY"));data.set(path+"max-z",number(max,"getBlockZ"));
            pending.remove(p.getUniqueId());save();
            p.sendMessage("§aMapped disabled Infuse region §f'"+id+"§a'.");
        }catch(Exception ex){plugin.getLogger().warning("WorldGuard region mapping failed: "+ex.getMessage());p.sendMessage("§cCould not read that WorldGuard region. Check the console.");}
    }
    private boolean worldGuardAvailable(){return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");}
    private Object worldGuardRegion(World world,String id)throws Exception{
        Class<?> wg=Class.forName("com.sk89q.worldguard.WorldGuard");
        Object instance=wg.getMethod("getInstance").invoke(null);
        Object platform=wg.getMethod("getPlatform").invoke(instance);
        Object container=platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Class<?> adapter=Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Object adaptedWorld=adapter.getMethod("adapt",World.class).invoke(null,world);
        Object manager=invokeCompatible(container,"get",adaptedWorld);
        if(manager==null)return null;
        return manager.getClass().getMethod("getRegion",String.class).invoke(manager,id);
    }
    private Object asBlockVector(Location loc)throws Exception{
        Class<?> adapter=Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        return adapter.getMethod("asBlockVector",Location.class).invoke(null,loc);
    }
    private Object invokeCompatible(Object target,String name,Object arg)throws Exception{
        for(Method m:target.getClass().getMethods())if(m.getName().equals(name)&&m.getParameterCount()==1&&m.getParameterTypes()[0].isInstance(arg))return m.invoke(target,arg);
        throw new NoSuchMethodException(name);
    }
    private int number(Object obj,String method)throws Exception{return ((Number)obj.getClass().getMethod(method).invoke(obj)).intValue();}
    private void save(){try{data.save(file);}catch(IOException e){plugin.getLogger().severe("Could not save regions.yml: "+e.getMessage());}}
}
