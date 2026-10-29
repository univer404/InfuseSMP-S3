package com.universe404.infusesmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Runtime mechanics port for the common passive effects and spark activations. */
public final class InfuseMechanics implements Listener {
    private final InfuseSMP plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> activeUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> hitCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Double> maxHealthBeforeHeart = new ConcurrentHashMap<>();

    public InfuseMechanics(InfuseSMP plugin) { this.plugin = plugin; }

    public boolean resetCooldown(Player p,String slot){
        String value=plugin.getEquippedEffect(p.getUniqueId(),slot);if(value==null)return true;
        String effect=value.replace("_augmented","");
        if(isActive(p.getUniqueId(),effect))return false;
        cooldowns.computeIfAbsent(p.getUniqueId(),k->new ConcurrentHashMap<>()).remove(effect);return true;
    }
    public boolean isActive(UUID id,String effect){return activeUntil.getOrDefault(id,Map.of()).getOrDefault(effect,0L)>System.currentTimeMillis();}

    public void startSpark(Player p, String slot) {
        if (plugin.isDisabled(p.getUniqueId()) || plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())) { plugin.tellKey(p, "disabled"); return; }
        String effect = plugin.getEquippedEffect(p.getUniqueId(), slot);
        if (effect == null) { plugin.tellKey(p, "effects-empty"); return; }
        effect = effect.replace("_augmented", "");
        long now = System.currentTimeMillis();
        long active = activeUntil.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>()).getOrDefault(effect, 0L);
        if (active > now) { plugin.tellRaw(p, "<red>ᴛʜɪs sᴘᴀʀᴋ ɪs ᴀʟʀᴇᴀᴅʏ ᴀᴄᴛɪᴠᴇ."); return; }
        long cd = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>()).getOrDefault(effect, 0L);
        if (cd > now) { plugin.tellRaw(p, "<red>ᴄᴏᴏʟᴅᴏᴡɴ: " + Math.max(1, (cd-now+999)/1000) + "s"); return; }
        long duration = Math.max(1, plugin.getConfig().getLong("effects."+effect+".spark_seconds", defaultSpark(effect)));
        long cooldown = Math.max(0, plugin.getConfig().getLong("effects."+effect+".cooldown_seconds", defaultCooldown(effect)));
        activeUntil.get(p.getUniqueId()).put(effect, now + duration * 1000);
        cooldowns.get(p.getUniqueId()).put(effect, now + (duration + cooldown) * 1000);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.3f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0,1,0), 35, .7,.8,.7,.1);
        activate(p, effect, duration);
        plugin.recordHistory(p.getUniqueId(), "spark " + slot + " " + effect);
        plugin.saveStatePublic();
    }

    private long defaultSpark(String e) {
        return switch(e) { case "heart" -> 60; case "fire" -> 4; case "thunder","emerald","frost","ocean","invisibility" -> 10; case "speed","ender" -> 1; case "feather" -> 5; default -> 15; };
    }
    private long defaultCooldown(String e) {
        return switch(e) { case "heart" -> 120; case "fire","thunder","ocean","invisibility" -> 90; case "frost","haste","regeneration","strength" -> 60; case "feather" -> 40; case "emerald","ender" -> 30; case "speed" -> 20; default -> 60; };
    }
    private void activate(Player p, String e, long seconds) {
        int ticks=(int)Math.min(Integer.MAX_VALUE, seconds*20);
        switch(e) {
            case "heart" -> {
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth()+p.getMaxHealth()*.2));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,ticks,4,true,false,true));
            }
            case "fire" -> {
                p.setFireTicks(0);
                for(Entity ent:p.getNearbyEntities(6,6,6)) if(ent instanceof LivingEntity target && target!=p && isTargetAllowed(p,target)) {
                    target.setFireTicks(200);
                    target.setVelocity(target.getVelocity().add(new Vector(0,.65,0)));
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,ticks,0,true,false,true));
            }
            case "frost" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,ticks,1,true,false,true));
                for(Entity ent:p.getNearbyEntities(6,4,6)) if(ent instanceof LivingEntity target && target!=p && isTargetAllowed(p,target)) {
                    target.setFreezeTicks(Math.min(target.getMaxFreezeTicks(), 140));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,2,true,false,true));
                }
                p.getWorld().spawnParticle(Particle.SNOWFLAKE,p.getLocation().add(0,1,0),80,3,1,3,.03);
            }
            case "speed" -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,ticks,2,true,false,true));
            case "haste" -> p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,ticks,2,true,false,true));
            case "invisibility" -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,ticks,0,true,false,true));
            case "ocean" -> { p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,ticks,0,true,false,true)); p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE,ticks,1,true,false,true)); }
            case "regeneration" -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,ticks,2,true,false,true));
            case "strength" -> p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,ticks,1,true,false,true));
            case "feather" -> p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,ticks,0,true,false,true));
            case "thunder" -> {
                for(Entity ent:p.getNearbyEntities(12,8,12)) if(ent instanceof LivingEntity target && target!=p && isTargetAllowed(p,target)) {
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(plugin.getConfig().getDouble("effects.thunder.true_damage", 1.0), p);
                }
            }
            case "emerald" -> { p.giveExp((int)plugin.getConfig().getLong("effects.emerald.spark_xp_gain",2)); p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE,ticks,2,true,false,true)); }
            case "ender" -> { Entity target=p.getTargetEntity(30); if(target!=null) { Location to=target.getLocation().clone().add(target.getLocation().getDirection().multiply(-2)); to.setDirection(p.getLocation().getDirection()); p.teleport(to); } else p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(6))); }
            case "earth" -> { p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,ticks,2,true,false,true)); p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,ticks,0,true,false,true)); }
        }
    }

    private boolean isTargetAllowed(Player owner, LivingEntity target) {
        return !(target instanceof Player other) || (!other.equals(owner) && !plugin.isTrusted(owner.getUniqueId(), other.getUniqueId()));
    }

    private boolean activateBound(Player p,String action){
        boolean activated=false;
        for(String slot:List.of("primary","secondary"))if(plugin.getBind(p.getUniqueId(),slot).equalsIgnoreCase(action)){
            startSpark(p,slot);activated=true;
        }
        return activated;
    }
    @EventHandler(ignoreCancelled=true) public void onSwap(PlayerSwapHandItemsEvent e){
        String action=e.getPlayer().isSneaking()?"sneak-swap":"swap";
        if(activateBound(e.getPlayer(),action))e.setCancelled(true);
    }
    @EventHandler(ignoreCancelled=true) public void onDrop(PlayerDropItemEvent e){
        String action=e.getPlayer().isSneaking()?"sneak-drop":"drop";
        if(activateBound(e.getPlayer(),action))e.setCancelled(true);
    }
    @EventHandler public void onSneak(PlayerToggleSneakEvent e){
        if(e.isSneaking())activateBound(e.getPlayer(),"sneak");
    }
    @EventHandler public void onDeath(PlayerDeathEvent e){
        Player p=e.getEntity();UUID id=p.getUniqueId();
        for(String slot:List.of("primary","secondary")){
            String value=plugin.getEquippedEffect(id,slot);
            if(value==null)continue;
            ItemStack item=plugin.createInfuseItem(value.replace("_augmented",""),value.endsWith("_augmented"));
            if(item!=null)e.getDrops().add(item);
            plugin.clearEquippedEffect(id,slot);
        }
    }
    @EventHandler public void onJoin(PlayerJoinEvent e) { Bukkit.getScheduler().runTaskLater(plugin, () -> applyPassives(e.getPlayer()), 2L); }
    @EventHandler public void onQuit(PlayerQuitEvent e) {
        UUID id=e.getPlayer().getUniqueId();
        cooldowns.remove(id); activeUntil.remove(id); hitCounts.remove(id);
        Double old=maxHealthBeforeHeart.remove(id);
        if(old!=null && e.getPlayer().isOnline()) e.getPlayer().setMaxHealth(old);
    }

    /** Keep permanent slot passives refreshed, and end temporary spark windows. */
    public void tick() {
        long now=System.currentTimeMillis();
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(plugin.isDisabled(p.getUniqueId()) || plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())) continue;
            applyPassives(p);
            Map<String,Long> active=activeUntil.get(p.getUniqueId());
            if(active!=null) active.entrySet().removeIf(en -> en.getValue()<=now);
        }
    }
    private void applyPassives(Player p) {
        for(String slot:List.of("primary","secondary")) {
            String value=plugin.getEquippedEffect(p.getUniqueId(),slot);
            if(value==null) continue;
            String e=value.replace("_augmented","");
            switch(e) {
                case "fire" -> p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,80,0,true,false,true));
                case "emerald" -> p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE,80,2,true,false,true));
                case "ocean" -> {p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,80,0,true,false,true));p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE,80,0,true,false,true));}
                case "invisibility" -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,80,0,true,false,true));
                case "regeneration" -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,80,0,true,false,true));
                case "speed" -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,80,0,true,false,true));
                case "haste" -> p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,80,0,true,false,true));
                case "feather" -> { if(p.getFallDistance()>3) p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,60,0,true,false,true)); }
            }
        }
    }

    @EventHandler(ignoreCancelled=true) public void onCombat(EntityDamageByEntityEvent event) {
        Player attacker=null;
        if(event.getDamager() instanceof Player p) attacker=p;
        else if(event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player p) attacker=p;
        if(attacker==null || plugin.isDisabled(attacker.getUniqueId())) return;
        for(String slot:List.of("primary","secondary")) {
            String effect=plugin.getEquippedEffect(attacker.getUniqueId(),slot);
            if(effect==null) continue;
            effect=effect.replace("_augmented","");
            if(effect.equals("strength") && event.getEntity() instanceof LivingEntity target && isTargetAllowed(attacker,target)) event.setDamage(event.getDamage()*1.25);
            if(effect.equals("thunder")) {
                Map<String,Integer> perEffect=hitCounts.computeIfAbsent(attacker.getUniqueId(),k->new ConcurrentHashMap<>());
                int hits=perEffect.merge(effect,1,Integer::sum);
                int threshold=plugin.getConfig().getInt("effects.thunder.trigger_hits",10);
                if(hits>=threshold) { perEffect.put(effect,0); attacker.getWorld().strikeLightningEffect(event.getEntity().getLocation()); }
            }
        }
    }
}
