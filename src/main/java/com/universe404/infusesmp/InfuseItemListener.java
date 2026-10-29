package com.universe404.infusesmp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class InfuseItemListener implements Listener {
    private final InfuseSMP plugin;
    private final InfuseItems items;
    public InfuseItemListener(InfuseSMP plugin,InfuseItems items){this.plugin=plugin;this.items=items;}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onConsume(PlayerItemConsumeEvent event){
        Player p=event.getPlayer();ItemStack item=event.getItem();String effect=items.getEffect(item);
        if(effect==null)return;
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
            p.getInventory().addItem(item.clone());plugin.tellRaw(p,"<red>ᴡᴀɪᴛ ᴜɴᴛɪʟ ᴛʜᴇ ᴄᴜʀʀᴇɴᴛ sᴘᴀʀᴋ ғɪɴɪsʜᴇs.");return;
        }
        if(!plugin.getConfig().getBoolean("allow_same_effects",false)&&effect.equals(otherEffect==null?null:otherEffect.replace("_augmented",""))){
            p.getInventory().addItem(item.clone());plugin.tellRaw(p,"<red>ʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴛʜɪs ᴇғғᴇᴄᴛ ɪɴ ʏᴏᴜʀ "+other+" sʟᴏᴛ.");return;
        }
        if(effect.equals(current==null?null:current.replace("_augmented",""))){
            p.getInventory().addItem(item.clone());plugin.tellRaw(p,"<red>ʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴛʜɪs ᴇғғᴇᴄᴛ ᴇǫᴜɪᴘᴘᴇᴅ.");return;
        }
        if(current!=null){ItemStack old=items.create(current.replace("_augmented",""),current.endsWith("_augmented"),false);if(old!=null)plugin.giveOrDrop(p,old);}
        plugin.setEquippedEffect(p.getUniqueId(),slot,effect,items.isAugmented(item));
        p.playSound(p.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,1f,slot.equals("primary")?1.1f:1.4f);
        plugin.tellRaw(p,"<white>ᴀᴘᴘʟɪᴇᴅ <green>"+effect+" <gray>ᴛᴏ "+slot+" sʟᴏᴛ.");
    }
}
