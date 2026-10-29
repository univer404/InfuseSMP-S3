package com.universe404.infusesmp;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.boss.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class RitualCrafting implements Listener {
    private final InfuseSMP plugin;
    private final InfuseItems items;
    private final MiniMessage mm=MiniMessage.miniMessage();
    private final NamespacedKey key;
    private final File tallyFile;
    private final org.bukkit.configuration.file.YamlConfiguration tally;
    private final org.bukkit.configuration.file.YamlConfiguration gui;
    private final Map<UUID,Location> ritualBlocks=new HashMap<>();
    private final Map<UUID,BukkitTask> rituals=new HashMap<>();
    private static final int[] SLOTS={12,13,14,21,22,23,30,31,32};
    private static final Map<String,List<Material>> RECIPES=new LinkedHashMap<>();
    static {
        recipe("emerald","WILD_ARMOR_TRIM_SMITHING_TEMPLATE","SNIFFER_EGG","WILD_ARMOR_TRIM_SMITHING_TEMPLATE","OMINOUS_BOTTLE","EMERALD_BLOCK","OMINOUS_BOTTLE","ENDER_CHEST","ENCHANTING_TABLE","ENDER_CHEST");
        recipe("feather","BREEZE_ROD","PHANTOM_MEMBRANE","BREEZE_ROD","HEAVY_CORE","FEATHER","HEAVY_CORE","BREEZE_ROD","PHANTOM_MEMBRANE","BREEZE_ROD");
        recipe("fire","NETHERITE_UPGRADE_SMITHING_TEMPLATE","RESPAWN_ANCHOR","NETHERITE_UPGRADE_SMITHING_TEMPLATE","NETHERITE_INGOT","MUSIC_DISC_PIGSTEP","NETHERITE_INGOT","NETHERITE_UPGRADE_SMITHING_TEMPLATE","RESPAWN_ANCHOR","NETHERITE_UPGRADE_SMITHING_TEMPLATE");
        recipe("frost","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","VERDANT_FROGLIGHT","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","GOAT_HORN","MUSIC_DISC_CREATOR","GOAT_HORN","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","VERDANT_FROGLIGHT","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE");
        recipe("haste","DIAMOND_BLOCK","BEACON","DIAMOND_BLOCK","GOLD_BLOCK","DEEPSLATE_EMERALD_ORE","GOLD_BLOCK","DIAMOND_BLOCK","NETHERITE_PICKAXE","DIAMOND_BLOCK");
        recipe("heart","WATER_BOTTLE","TOTEM_OF_UNDYING","WATER_BOTTLE","ENCHANTED_GOLDEN_APPLE","BEETROOT","ENCHANTED_GOLDEN_APPLE","WATER_BOTTLE","TOTEM_OF_UNDYING","WATER_BOTTLE");
        recipe("invisibility","ENDER_EYE","MUSIC_DISC_5","ENDER_EYE","SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE","OMINOUS_TRIAL_KEY","SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE","ENDER_EYE","RECOVERY_COMPASS","ENDER_EYE");
        recipe("ocean","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","HEART_OF_THE_SEA","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","TRIDENT","CONDUIT","TRIDENT","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","HEART_OF_THE_SEA","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE");
        recipe("regeneration","END_CRYSTAL","HONEY_BLOCK","END_CRYSTAL","VERDANT_FROGLIGHT","AXOLOTL_BUCKET","VERDANT_FROGLIGHT","END_CRYSTAL","MUSIC_DISC_CREATOR","END_CRYSTAL");
        recipe("speed","RABBIT_FOOT","SADDLE","RABBIT_FOOT","DUNE_ARMOR_TRIM_SMITHING_TEMPLATE","DIAMOND_HORSE_ARMOR","EYE_ARMOR_TRIM_SMITHING_TEMPLATE","RABBIT_FOOT","NETHERITE_BOOTS","RABBIT_FOOT");
        recipe("strength","PLAYER_HEAD","RIB_ARMOR_TRIM_SMITHING_TEMPLATE","PLAYER_HEAD","NETHERITE_SWORD","WITHER_ROSE","NETHERITE_AXE","PLAYER_HEAD","RIB_ARMOR_TRIM_SMITHING_TEMPLATE","PLAYER_HEAD");
        recipe("thunder","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","LODESTONE","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","TRIDENT","CREEPER_HEAD","TRIDENT","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","LODESTONE","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE");
        recipe("ender","BLACK_SHULKER_BOX","DRAGON_HEAD","BLACK_SHULKER_BOX","SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE","DRAGON_EGG","SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE","BLACK_SHULKER_BOX","ELYTRA","BLACK_SHULKER_BOX");
    }
    private static void recipe(String e,String... names){RECIPES.put(e,Arrays.stream(names).map(Material::matchMaterial).toList());}
    public RitualCrafting(InfuseSMP plugin,InfuseItems items){
        this.plugin=plugin;this.items=items;this.key=new NamespacedKey(plugin,"ritual_action");
        tallyFile=new File(plugin.getDataFolder(),"rituals.yml");
        tally=org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tallyFile);
        gui=org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"gui.yml"));
    }
    @EventHandler(ignoreCancelled=true)
    public void onBrewingStand(PlayerInteractEvent e){
        if(!gui.getBoolean("ritual.enabled",true)||e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null||e.getClickedBlock().getType()!=Material.BREWING_STAND||!e.getPlayer().isSneaking())return;
        e.setCancelled(true);ritualBlocks.put(e.getPlayer().getUniqueId(),e.getClickedBlock().getLocation());open(e.getPlayer());
    }
    private int[] ingredientSlots(){
        List<Integer> configured=gui.getIntegerList("ritual.ingredient_slots");
        if(configured.size()!=9)return SLOTS.clone();
        return configured.stream().mapToInt(Integer::intValue).toArray();
    }
    private int craftSlot(){return gui.getInt("ritual.craft.slot",40);}
    private int exitSlot(){return gui.getInt("ritual.exit.slot",44);}
    private void open(Player p){
        int rows=Math.max(1,Math.min(6,gui.getInt("ritual.rows",5)));
        Inventory inv=Bukkit.createInventory(p,rows*9,mm.deserialize(gui.getString("ritual.title","<gradient:#A2C81C:#21CD76>ᴘᴏᴛɪᴏɴ ᴄʀᴀғᴛᴇʀ")).decoration(TextDecoration.ITALIC,false));
        ItemStack filler=button(material("ritual.filler.material",Material.PURPLE_STAINED_GLASS_PANE),gui.getString("ritual.filler.name"," "),"filler");
        for(int i=0;i<inv.getSize();i++)inv.setItem(i,filler);
        for(int slot:ingredientSlots())if(slot>=0&&slot<inv.getSize())inv.setItem(slot,null);
        if(craftSlot()>=0&&craftSlot()<inv.getSize())inv.setItem(craftSlot(),button(material("ritual.craft.material",Material.CRAFTING_TABLE),gui.getString("ritual.craft.name","<#81D681><bold>ᴄʀᴀғᴛ"),"craft"));
        if(exitSlot()>=0&&exitSlot()<inv.getSize())inv.setItem(exitSlot(),button(material("ritual.exit.material",Material.BARRIER),gui.getString("ritual.exit.name","<#E13030><bold>ᴇxɪᴛ"),"exit"));
        p.setMetadata("infuse-ritual-gui",new org.bukkit.metadata.FixedMetadataValue(plugin,true));
        p.openInventory(inv);
    }
    private Material material(String path,Material fallback){Material m=Material.matchMaterial(gui.getString(path,""));return m==null?fallback:m;}
    private ItemStack button(Material mat,String title,String action){
        ItemStack i=new ItemStack(mat);ItemMeta m=i.getItemMeta();m.displayName(mm.deserialize(title).decoration(TextDecoration.ITALIC,false));
        m.getPersistentDataContainer().set(key,PersistentDataType.STRING,action);i.setItemMeta(m);return i;
    }
    @EventHandler public void click(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p)||!p.hasMetadata("infuse-ritual-gui"))return;
        if(e.getClickedInventory()==null)return;
        if(e.getClickedInventory()!=e.getView().getTopInventory()){e.setCancelled(true);return;}
        int slot=e.getRawSlot();
        if(slot==exitSlot()){e.setCancelled(true);p.closeInventory();return;}
        if(slot==craftSlot()){e.setCancelled(true);craft(p,e.getView().getTopInventory());return;}
        if(Arrays.stream(ingredientSlots()).noneMatch(s->s==slot)){e.setCancelled(true);return;}
        Bukkit.getScheduler().runTask(plugin,()->clamp(e.getView().getTopInventory(),p));
    }
    @EventHandler public void drag(InventoryDragEvent e){
        if(!(e.getWhoClicked() instanceof Player p)||!p.hasMetadata("infuse-ritual-gui"))return;
        if(e.getRawSlots().stream().anyMatch(s->s<e.getView().getTopInventory().getSize()&&!Arrays.stream(ingredientSlots()).anyMatch(v->v==s)))e.setCancelled(true);
    }
    private void clamp(Inventory inv,Player p){
        for(int slot:ingredientSlots()){ItemStack i=inv.getItem(slot);if(i!=null&&i.getAmount()>1){ItemStack extra=i.clone();extra.setAmount(i.getAmount()-1);i.setAmount(1);inv.setItem(slot,i);p.getInventory().addItem(extra);}}
    }
    private void craft(Player p,Inventory inv){
        String effect=match(inv);
        if(effect==null){p.sendMessage(mm.deserialize("<red>✖ ɪɴᴠᴀʟɪᴅ ʀᴇᴄɪᴘᴇ!").decoration(TextDecoration.ITALIC,false));p.playSound(p,Sound.BLOCK_NOTE_BLOCK_BASS,.5f,1);return;}
        int crafted=tally.getInt(effect+".crafted",0);
        int craftLimit=plugin.getConfig().getInt("effects."+effect+".craft_limit",-1);
        if(craftLimit>=0&&crafted>=craftLimit){p.sendMessage(mm.deserialize("<red>✖ ᴄʀᴀғᴛ ʟɪᴍɪᴛ ʀᴇᴀᴄʜᴇᴅ ғᴏʀ "+effect+"!").decoration(TextDecoration.ITALIC,false));return;}
        for(int s:ingredientSlots())inv.setItem(s,null);
        tally.set(effect+".crafted",crafted+1);saveTally();
        p.closeInventory();p.removeMetadata("infuse-ritual-gui",plugin);
        int augmentLimit=plugin.getConfig().getInt("effects."+effect+".augment_limit",0);
        if(augmentLimit>crafted){
            Location block=ritualBlocks.get(p.getUniqueId());
            if(block==null||block.getBlock().getType()!=Material.BREWING_STAND){plugin.giveOrDrop(p,items.create(effect,false,false));return;}
            startRitual(p,effect,block);
        }else{
            plugin.giveOrDrop(p,items.create(effect,false,false));p.playSound(p,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,.5f,1);
            p.sendMessage(mm.deserialize("<green>✔ ᴄʀᴀғᴛɪɴɢ ᴄᴏᴍᴘʟᴇᴛᴇ!").decoration(TextDecoration.ITALIC,false));
        }
    }
    private String match(Inventory inv){
        for(var entry:RECIPES.entrySet()){
            List<Material> recipe=entry.getValue();boolean ok=recipe.size()==9;
            int[] ingredientSlots=ingredientSlots();
            for(int i=0;i<9&&ok;i++){ItemStack item=inv.getItem(ingredientSlots[i]);ok=item!=null&&item.getType()==recipe.get(i)&&item.getAmount()==1;}
            if(ok)return entry.getKey();
        }
        return null;
    }
    private void startRitual(Player p,String effect,Location loc){
        int duration=Math.max(1,plugin.getConfig().getInt("ritual_duration_seconds",600));
        BossBar bar=Bukkit.createBossBar("Ritual for "+effect+" effect",BarColor.YELLOW,BarStyle.SOLID);
        bar.addPlayer(p);bar.setProgress(1);
        p.sendMessage(mm.deserialize("<gold>★ ʀɪᴛᴜᴀʟ sᴛᴀʀᴛɪɴɢ!").decoration(TextDecoration.ITALIC,false));
        BukkitTask task=Bukkit.getScheduler().runTaskTimer(plugin,new Runnable(){
            int remaining=duration;
            @Override public void run(){
                if(!p.isOnline()){bar.removeAll();stopRitual(p.getUniqueId());return;}
                if(loc.getBlock().getType()!=Material.BREWING_STAND){
                    bar.setTitle("Ritual Failed");bar.setColor(BarColor.RED);bar.setProgress(0);
                    tally.set(effect+".crafted",Math.max(0,tally.getInt(effect+".crafted",1)-1));saveTally();
                    p.sendMessage(mm.deserialize("<red>ʀɪᴛᴜᴀʟ ғᴀɪʟᴇᴅ — ʙʀᴇᴡɪɴɢ sᴛᴀɴᴅ ʀᴇᴍᴏᴠᴇᴅ.").decoration(TextDecoration.ITALIC,false));
                    bar.removeAll();stopRitual(p.getUniqueId());return;
                }
                remaining--;bar.setProgress(Math.max(0,(double)remaining/duration));
                if(remaining%5==0){loc.getWorld().spawnParticle(Particle.END_ROD,loc.clone().add(.5,1,.5),8,.3,.4,.3,.02);}
                if(remaining<=0){
                    ItemStack result=items.create(effect,true,false);plugin.giveOrDrop(p,result);
                    loc.getWorld().strikeLightningEffect(loc);loc.getWorld().playSound(loc,Sound.ENTITY_LIGHTNING_BOLT_THUNDER,1,1);
                    loc.getWorld().playSound(loc,Sound.BLOCK_ENCHANTMENT_TABLE_USE,1,1);
                    p.sendMessage(mm.deserialize("<green>ᴛʜᴇ ʀɪᴛᴜᴀʟ ᴡᴀs sᴜᴄᴄᴇssғᴜʟ!").decoration(TextDecoration.ITALIC,false));
                    bar.removeAll();stopRitual(p.getUniqueId());
                }
            }
        },20L,20L);
        rituals.put(p.getUniqueId(),task);
    }
    @EventHandler public void close(InventoryCloseEvent e){
        if(!(e.getPlayer() instanceof Player p)||!p.hasMetadata("infuse-ritual-gui"))return;
        Inventory inv=e.getInventory();
        for(int s:ingredientSlots()){ItemStack item=inv.getItem(s);if(item!=null&&item.getType()!=Material.AIR)p.getInventory().addItem(item);}
        p.removeMetadata("infuse-ritual-gui",plugin);
    }
    public boolean resetCraft(String effect){
        if(effect==null){for(String e:RECIPES.keySet())tally.set(e+".crafted",0);saveTally();return true;}
        if(!RECIPES.containsKey(effect.toLowerCase(Locale.ROOT)))return false;
        tally.set(effect.toLowerCase(Locale.ROOT)+".crafted",0);saveTally();return true;
    }
    private void stopRitual(UUID id){BukkitTask running=rituals.remove(id);if(running!=null)running.cancel();}
    private void saveTally(){try{tally.save(tallyFile);}catch(IOException ex){plugin.getLogger().severe("Could not save rituals.yml: "+ex.getMessage());}}
}
