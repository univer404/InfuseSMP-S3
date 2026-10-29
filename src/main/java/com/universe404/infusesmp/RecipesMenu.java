package com.universe404.infusesmp;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

/** Recipe index copied from infuse_new/recipes.sk; every click in the top GUI is cancelled. */
public final class RecipesMenu implements Listener {
    private final JavaPlugin plugin;
    private final MiniMessage mm=MiniMessage.miniMessage();
    private final NamespacedKey menuKey;
    private final InfuseItems items;
    private final YamlConfiguration gui;
    private static final List<String> EFFECTS=List.of("emerald","feather","fire","frost","haste","heart","invisibility","ocean","regeneration","speed","strength","thunder","ender");
    private static final int[] MAIN_SLOTS={4,12,13,14,20,21,23,24,30,31,32,40,22};
    private static final Map<String,List<Material>> RECIPES=new LinkedHashMap<>();
    static {
        put("emerald","WILD_ARMOR_TRIM_SMITHING_TEMPLATE","SNIFFER_EGG","WILD_ARMOR_TRIM_SMITHING_TEMPLATE","OMINOUS_BOTTLE","EMERALD_BLOCK","OMINOUS_BOTTLE","ENDER_CHEST","ENCHANTING_TABLE","ENDER_CHEST");
        put("feather","BREEZE_ROD","PHANTOM_MEMBRANE","BREEZE_ROD","HEAVY_CORE","FEATHER","HEAVY_CORE","BREEZE_ROD","PHANTOM_MEMBRANE","BREEZE_ROD");
        put("fire","NETHERITE_UPGRADE_SMITHING_TEMPLATE","RESPAWN_ANCHOR","NETHERITE_UPGRADE_SMITHING_TEMPLATE","NETHERITE_INGOT","MUSIC_DISC_PIGSTEP","NETHERITE_INGOT","NETHERITE_UPGRADE_SMITHING_TEMPLATE","RESPAWN_ANCHOR","NETHERITE_UPGRADE_SMITHING_TEMPLATE");
        put("frost","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","VERDANT_FROGLIGHT","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","GOAT_HORN","MUSIC_DISC_CREATOR","GOAT_HORN","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE","VERDANT_FROGLIGHT","FLOW_ARMOR_TRIM_SMITHING_TEMPLATE");
        put("haste","DIAMOND_BLOCK","BEACON","DIAMOND_BLOCK","GOLD_BLOCK","DEEPSLATE_EMERALD_ORE","GOLD_BLOCK","DIAMOND_BLOCK","NETHERITE_PICKAXE","DIAMOND_BLOCK");
        put("heart","WATER_BOTTLE","TOTEM_OF_UNDYING","WATER_BOTTLE","ENCHANTED_GOLDEN_APPLE","BEETROOT","ENCHANTED_GOLDEN_APPLE","WATER_BOTTLE","TOTEM_OF_UNDYING","WATER_BOTTLE");
        put("invisibility","ENDER_EYE","MUSIC_DISC_5","ENDER_EYE","SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE","OMINOUS_TRIAL_KEY","SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE","ENDER_EYE","RECOVERY_COMPASS","ENDER_EYE");
        put("ocean","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","HEART_OF_THE_SEA","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","TRIDENT","CONDUIT","TRIDENT","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE","HEART_OF_THE_SEA","TIDE_ARMOR_TRIM_SMITHING_TEMPLATE");
        put("regeneration","END_CRYSTAL","HONEY_BLOCK","END_CRYSTAL","VERDANT_FROGLIGHT","AXOLOTL_BUCKET","VERDANT_FROGLIGHT","END_CRYSTAL","MUSIC_DISC_CREATOR","END_CRYSTAL");
        put("speed","RABBIT_FOOT","SADDLE","RABBIT_FOOT","DUNE_ARMOR_TRIM_SMITHING_TEMPLATE","DIAMOND_HORSE_ARMOR","EYE_ARMOR_TRIM_SMITHING_TEMPLATE","RABBIT_FOOT","NETHERITE_BOOTS","RABBIT_FOOT");
        put("strength","PLAYER_HEAD","RIB_ARMOR_TRIM_SMITHING_TEMPLATE","PLAYER_HEAD","NETHERITE_SWORD","WITHER_ROSE","NETHERITE_AXE","PLAYER_HEAD","RIB_ARMOR_TRIM_SMITHING_TEMPLATE","PLAYER_HEAD");
        put("thunder","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","LODESTONE","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","TRIDENT","CREEPER_HEAD","TRIDENT","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE","LODESTONE","BOLT_ARMOR_TRIM_SMITHING_TEMPLATE");
        put("ender","BLACK_SHULKER_BOX","DRAGON_HEAD","BLACK_SHULKER_BOX","SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE","DRAGON_EGG","SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE","BLACK_SHULKER_BOX","ELYTRA","BLACK_SHULKER_BOX");
    }
    private static void put(String e,String... mats){ RECIPES.put(e,Arrays.stream(mats).map(Material::matchMaterial).filter(Objects::nonNull).toList()); }
    public RecipesMenu(JavaPlugin plugin,InfuseItems items){this.plugin=plugin;this.items=items;this.menuKey=new NamespacedKey(plugin,"recipes_menu");this.gui=YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"gui.yml"));}
    public void openMain(Player p){
        Inventory inv=Bukkit.createInventory(p,45,mm.deserialize("<gradient:#A2C81C:#21CD76>ʀᴇᴄɪᴘᴇs").decoration(TextDecoration.ITALIC,false));
        fill(inv,Material.BLACK_STAINED_GLASS_PANE);
        for(int i=0;i<EFFECTS.size();i++){String e=EFFECTS.get(i);inv.setItem(MAIN_SLOTS[i],effectIcon(e));}
        inv.setItem(44,button(Material.BARRIER,"<#E13030><bold>ᴇxɪᴛ", "exit"));
        openTagged(p,inv,"main");
    }
    private void openView(Player p,String effect){
        Inventory inv=Bukkit.createInventory(p,45,mm.deserialize("<gradient:#A2C81C:#21CD76>ʀᴇᴄɪᴘᴇs").decoration(TextDecoration.ITALIC,false));
        fill(inv,Material.BLACK_STAINED_GLASS_PANE);
        List<Material> recipe=RECIPES.get(effect);int[] slots={11,12,13,20,21,22,29,30,31};
        if(recipe!=null)for(int i=0;i<Math.min(9,recipe.size());i++)inv.setItem(slots[i],new ItemStack(recipe.get(i)));
        inv.setItem(23,button(Material.ARROW,"<gray>▶ <white>ʀᴇsᴜʟᴛ","result"));
        inv.setItem(24,effectIcon(effect));
        inv.setItem(36,button(Material.ARROW,"<gray><bold>← ʙᴀᴄᴋ","back"));
        inv.setItem(44,button(Material.BARRIER,"<#E13030><bold>ᴇxɪᴛ","exit"));
        openTagged(p,inv,effect);
    }
    private void openTagged(Player p,Inventory inv,String state){
        // state is stored in the inventory holder via a custom holder
        p.openInventory(inv);
        p.getOpenInventory().getTopInventory().setItem(44, inv.getItem(44));
        p.setMetadata("infuse-recipes-state",new org.bukkit.metadata.FixedMetadataValue(plugin,state));
    }
    private Material material(String path,Material fallback){Material m=Material.matchMaterial(gui.getString(path,""));return m==null?fallback:m;}
    private void fill(Inventory inv,Material m,String name){ItemStack filler=button(m,name,"filler");for(int i=0;i<inv.getSize();i++)inv.setItem(i,filler);}
    private ItemStack effectIcon(String effect){
        Material mat=switch(effect){case "emerald"->Material.EMERALD;case "feather"->Material.FEATHER;case "fire"->Material.BLAZE_POWDER;case "frost"->Material.PACKED_ICE;case "haste"->Material.DIAMOND_PICKAXE;case "heart"->Material.ENCHANTED_GOLDEN_APPLE;case "invisibility"->Material.ENDER_EYE;case "ocean"->Material.HEART_OF_THE_SEA;case "regeneration"->Material.GHAST_TEAR;case "speed"->Material.RABBIT_FOOT;case "strength"->Material.NETHERITE_SWORD;case "thunder"->Material.TRIDENT;default->Material.DRAGON_EGG;};
        ItemStack custom=items.create(effect,false,true); return custom==null?button(mat,"<#A2C81C>"+effect.toUpperCase(Locale.ROOT),"effect:"+effect):tag(custom,"effect:"+effect);
    }
    private ItemStack tag(ItemStack item,String action){ItemMeta meta=item.getItemMeta();meta.getPersistentDataContainer().set(menuKey,PersistentDataType.STRING,action);item.setItemMeta(meta);return item;}
    private ItemStack button(Material mat,String name,String action){
        ItemStack item=new ItemStack(mat);ItemMeta meta=item.getItemMeta();
        meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC,false));
        meta.getPersistentDataContainer().set(menuKey,PersistentDataType.STRING,action);
        item.setItemMeta(meta);return item;
    }
    @EventHandler public void click(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p)||!p.hasMetadata("infuse-recipes-state"))return;
        if(e.getView().getTopInventory()!=e.getClickedInventory() && e.getClickedInventory()!=null)return;
        if(e.getClickedInventory()==null)return;
        e.setCancelled(true);
        if(e.getClickedInventory()!=e.getView().getTopInventory())return;
        ItemStack item=e.getCurrentItem();if(item==null||!item.hasItemMeta())return;
        String action=item.getItemMeta().getPersistentDataContainer().get(menuKey,PersistentDataType.STRING);
        if(action==null)return;
        String state=p.getMetadata("infuse-recipes-state").getFirst().asString();
        switch(action){
            case "exit"->{p.removeMetadata("infuse-recipes-state",plugin);p.closeInventory();}
            case "back"->openMain(p);
            default->{if(action.startsWith("effect:"))openView(p,action.substring(7));}
        }
    }
    @EventHandler public void drag(InventoryDragEvent e){
        if(e.getWhoClicked() instanceof Player p&&p.hasMetadata("infuse-recipes-state")&&e.getRawSlots().stream().anyMatch(s->s<e.getView().getTopInventory().getSize()))e.setCancelled(true);
    }
    @EventHandler public void close(InventoryCloseEvent e){if(e.getPlayer().hasMetadata("infuse-recipes-state"))e.getPlayer().removeMetadata("infuse-recipes-state",plugin);}
}
