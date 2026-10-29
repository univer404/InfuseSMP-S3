package com.universe404.infusesmp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class InfuseItemListener implements Listener {
    private final InfuseSMP plugin;
    private final InfuseItems items;
    public InfuseItemListener(InfuseSMP plugin,InfuseItems items){this.plugin=plugin;this.items=items;}
    private boolean hasEffect(Player p,String effect){
        if(plugin.isDisabled(p.getUniqueId()) || plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName()) || plugin.isInDisabledRegion(p.getLocation()) || !plugin.getConfig().getBoolean("effects."+effect+".enabled",true)) return false;
        for(String slot:List.of("primary","secondary")){String value=plugin.getEquippedEffect(p.getUniqueId(),slot);if(value!=null&&value.replace("_augmented","").equals(effect))return true;}
        return false;
    }
    private boolean isFilteredConsumable(Material m){
        return EnumSet.of(Material.BREAD,Material.COOKED_BEEF,Material.COOKED_CHICKEN,Material.COOKED_MUTTON,Material.COOKED_PORKCHOP,Material.COOKED_RABBIT,Material.GOLDEN_CARROT,Material.GOLDEN_APPLE,Material.ENCHANTED_GOLDEN_APPLE,Material.COOKIE,Material.MELON_SLICE,Material.PUMPKIN_PIE,Material.BEETROOT_SOUP,Material.MUSHROOM_STEW,Material.RABBIT_STEW,Material.SUSPICIOUS_STEW,Material.BAKED_POTATO,Material.CARROT,Material.POTATO,Material.APPLE,Material.DRIED_KELP,Material.CHORUS_FRUIT,Material.SWEET_BERRIES,Material.GLOW_BERRIES,Material.HONEY_BOTTLE,Material.COD,Material.SALMON,Material.COOKED_COD,Material.COOKED_SALMON,Material.TROPICAL_FISH,Material.PUFFERFISH,Material.BEEF,Material.PORKCHOP,Material.MUTTON,Material.CHICKEN,Material.RABBIT,Material.POISONOUS_POTATO,Material.SPIDER_EYE,Material.ROTTEN_FLESH).contains(m)||m==Material.POTION||m==Material.SPLASH_POTION||m==Material.LINGERING_POTION;
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onConsume(PlayerItemConsumeEvent event){
        Player p=event.getPlayer();ItemStack item=event.getItem();String effect=items.getEffect(item);
        if(effect==null){
            if(hasEffect(p,"heart")){
                boolean enchantedApple=item.getType()==Material.ENCHANTED_GOLDEN_APPLE;
                int tier=plugin.getConfig().getInt(enchantedApple?"effects.heart.egap_absorption_tier":"effects.heart.food_item_absorption_tier",enchantedApple?5:1);
                int seconds=plugin.getConfig().getInt(enchantedApple?"effects.heart.egap_item_duration_seconds":"effects.heart.food_item_duration_seconds",60);
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,Math.max(1,seconds)*20,Math.max(0,tier-1),true,false,true));
            }
            if(hasEffect(p,"emerald")&&isFilteredConsumable(item.getType())){
                double chance=plugin.isSparkActive(p.getUniqueId(),"emerald")?plugin.getConfig().getDouble("effects.emerald.spark_no_consume_chance",0.75):plugin.getConfig().getDouble("effects.emerald.no_consume_chance",0.5);
                if(ThreadLocalRandom.current().nextDouble()<Math.max(0,Math.min(1,chance))){
                    event.setCancelled(true);p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_YES,1f,1f);return;
                }
            }
            if(hasEffect(p,"regeneration"))org.bukkit.Bukkit.getScheduler().runTask(plugin,()->{if(p.isOnline())p.setFoodLevel(10);});
            return;
        }
        event.setCancelled(true);
        ItemStack one=item.clone();one.setAmount(1);p.getInventory().removeItem(one);
        String slot=p.isSneaking()?"secondary":"primary";
        if(plugin.getConfig().getBoolean("lock_effects",false)){
            List<String> primary=plugin.getConfig().getStringList("primary_effects");
            List<String> secondary=plugin.getConfig().getStringList("secondary_effects");
            if(primary.contains(effect))slot="primary";else if(secondary.contains(effect))slot="secondary";
        }
        String other=slot.equals("primary")?"secondary":"primary";
        String current=plugin.getEquippedEffect(p.getUniqueId(),slot);
        String otherEffect=plugin.getEquippedEffect(p.getUniqueId(),other);
        if(current!=null&&plugin.isSparkActive(p.getUniqueId(),current.replace("_augmented",""))){
            ItemStack refund=item.clone();refund.setAmount(1);plugin.giveOrDrop(p,refund);plugin.tellRaw(p,"<red>ᴡᴀɪᴛ ᴜɴᴛɪʟ ᴛʜᴇ ᴄᴜʀʀᴇɴᴛ sᴘᴀʀᴋ ғɪɴɪsʜᴇs.");return;
        }
        if(!plugin.getConfig().getBoolean("allow_same_effects",false)&&effect.equals(otherEffect==null?null:otherEffect.replace("_augmented",""))){
            ItemStack refund=item.clone();refund.setAmount(1);plugin.giveOrDrop(p,refund);plugin.tellRaw(p,"<red>ʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴛʜɪs ᴇғғᴇᴄᴛ ɪɴ ʏᴏᴜʀ "+other+" sʟᴏᴛ.");return;
        }
        if(effect.equals(current==null?null:current.replace("_augmented",""))){
            ItemStack refund=item.clone();refund.setAmount(1);plugin.giveOrDrop(p,refund);plugin.tellRaw(p,"<red>ʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴛʜɪs ᴇғғᴇᴄᴛ ᴇǫᴜɪᴘᴘᴇᴅ.");return;
        }
        if(current!=null){ItemStack old=items.create(current.replace("_augmented",""),current.endsWith("_augmented"),false);if(old!=null)plugin.giveOrDrop(p,old);}
        plugin.setEquippedEffect(p.getUniqueId(),slot,effect,items.isAugmented(item));
        p.playSound(p.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,1f,slot.equals("primary")?1.1f:1.4f);
        plugin.tellRaw(p,"<white>ᴀᴘᴘʟɪᴇᴅ <green>"+effect+" <gray>ᴛᴏ "+slot+" sʟᴏᴛ.");
    }
}
