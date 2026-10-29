package com.universe404.infusesmp;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class InfuseItems {
    private final JavaPlugin plugin;
    private final NamespacedKey effectKey, augmentedKey;
    private final MiniMessage mm=MiniMessage.miniMessage();
    private static final Map<String,Integer> IDS=Map.of("emerald",1,"feather",2,"fire",3,"frost",4,"thunder",5,"haste",6,"heart",7,"invisibility",8,"ocean",9,"regeneration",10,"speed",11,"strength",12,"ender",13);
    private static final Map<String,String> COLORS=Map.ofEntries(
      Map.entry("emerald","#0DFC00"),Map.entry("feather","#BEA3CA"),Map.entry("fire","#FC7803"),Map.entry("frost","#7AE3FC"),Map.entry("thunder","#FCED00"),Map.entry("haste","#B96C00"),Map.entry("heart","#FC0046"),Map.entry("invisibility","#7E00FC"),Map.entry("ocean","#005AFC"),Map.entry("regeneration","#FC00DD"),Map.entry("speed","#D1A44B"),Map.entry("strength","#9E2020"),Map.entry("ender","#831177"));
    private static final Map<String,String> NAMES=Map.ofEntries(
      Map.entry("emerald","ᴇᴍᴇʀᴀʟᴅ"),Map.entry("feather","ғᴇᴀᴛʜᴇʀ"),Map.entry("fire","ғɪʀᴇ"),Map.entry("frost","ғʀᴏsᴛ"),Map.entry("thunder","ᴛʜᴜɴᴅᴇʀ"),Map.entry("haste","ʜᴀsᴛᴇ"),Map.entry("heart","ʜᴇᴀʀᴛ"),Map.entry("invisibility","ɪɴᴠɪsɪʙɪʟɪᴛʏ"),Map.entry("ocean","ᴏᴄᴇᴀɴ"),Map.entry("regeneration","ʀᴇɢᴇɴᴇʀᴀᴛɪᴏɴ"),Map.entry("speed","sᴘᴇᴇᴅ"),Map.entry("strength","sᴛʀᴇɴɢᴛʜ"),Map.entry("ender","ᴇɴᴅᴇʀ"));
    public InfuseItems(JavaPlugin plugin){this.plugin=plugin;effectKey=new NamespacedKey(plugin,"infuse_effect");augmentedKey=new NamespacedKey(plugin,"infuse_augmented");}
    public ItemStack create(String effect,boolean augmented,boolean recipeInfo){
        effect=effect.toLowerCase(Locale.ROOT);
        if(!IDS.containsKey(effect))return null;
        ItemStack item=new ItemStack(Material.POTION);PotionMeta meta=(PotionMeta)item.getItemMeta();
        String color=COLORS.get(effect);meta.setColor(Color.fromRGB(Integer.parseInt(color.substring(1,3),16),Integer.parseInt(color.substring(3,5),16),Integer.parseInt(color.substring(5,7),16)));
        meta.setCustomModelData(414001+IDS.get(effect)-1+(augmented?100:0));
        meta.displayName(mm.deserialize(color+NAMES.get(effect)+" ᴇғғᴇᴄᴛ").decoration(TextDecoration.ITALIC,false));
        List<net.kyori.adventure.text.Component> lore=new ArrayList<>();
        lore.add(mm.deserialize("<dark_gray>ɪɴғᴜsᴇ s3").decoration(TextDecoration.ITALIC,false));
        lore.add(mm.deserialize(augmented?"<gold>ᴀᴜɢᴍᴇɴᴛᴇᴅ":"<gray>ɴᴏʀᴍᴀʟ").decoration(TextDecoration.ITALIC,false));
        lore.add(mm.deserialize("<gray>ᴅʀɪɴᴋ ᴛᴏ ᴇǫᴜɪᴘ ᴘʀɪᴍᴀʀʏ").decoration(TextDecoration.ITALIC,false));
        lore.add(mm.deserialize("<gray>ᴄʀᴏᴜᴄʜ ᴀɴᴅ ᴅʀɪɴᴋ ᴛᴏ ᴇǫᴜɪᴘ sᴇᴄᴏɴᴅᴀʀʏ").decoration(TextDecoration.ITALIC,false));
        lore.add(mm.deserialize("<dark_gray>sᴘᴀʀᴋ: <gray>"+plugin.getConfig().getLong("effects."+effect+".spark_seconds",10)+"s").decoration(TextDecoration.ITALIC,false));
        lore.add(mm.deserialize("<dark_gray>ᴄᴏᴏʟᴅᴏᴡɴ: <gray>"+plugin.getConfig().getLong("effects."+effect+".cooldown_seconds",60)+"s").decoration(TextDecoration.ITALIC,false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(effectKey,PersistentDataType.STRING,effect);
        meta.getPersistentDataContainer().set(augmentedKey,PersistentDataType.BYTE,(byte)(augmented?1:0));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);return item;
    }
    public String getEffect(ItemStack item){if(item==null||!item.hasItemMeta())return null;return item.getItemMeta().getPersistentDataContainer().get(effectKey,PersistentDataType.STRING);}
    public boolean isAugmented(ItemStack item){return item!=null&&item.hasItemMeta()&&item.getItemMeta().getPersistentDataContainer().getOrDefault(augmentedKey,PersistentDataType.BYTE,(byte)0)==1;}
}
