package com.universe404.infusesmp;

import org.bukkit.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
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
    private final Map<UUID,Map<UUID,Integer>> hitsFrom = new ConcurrentHashMap<>();
    private final Map<UUID,Double> recentDamage = new ConcurrentHashMap<>();
    private final Map<UUID,Integer> enderCurses = new ConcurrentHashMap<>();
    private final Map<UUID,Double> speedStacks = new ConcurrentHashMap<>();
    private final Map<UUID,Double> attackStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> maxHealthBeforeHeart = new ConcurrentHashMap<>();

    public InfuseMechanics(InfuseSMP plugin) { this.plugin = plugin; }

    public long getSparkRemainingMillis(UUID id,String effect){return Math.max(0,activeUntil.getOrDefault(id,Map.of()).getOrDefault(effect,0L)-System.currentTimeMillis());}
    public long getCooldownRemainingMillis(UUID id,String effect){return Math.max(0,cooldowns.getOrDefault(id,Map.of()).getOrDefault(effect,0L)-System.currentTimeMillis());}
    public boolean isBusy(UUID id,String effect){
        long now=System.currentTimeMillis();
        return isActive(id,effect)||cooldowns.getOrDefault(id,Map.of()).getOrDefault(effect,0L)>now;
    }
    public boolean resetCooldown(Player p,String slot){
        String value=plugin.getEquippedEffect(p.getUniqueId(),slot);if(value==null)return true;
        String effect=value.replace("_augmented","");
        if(isActive(p.getUniqueId(),effect))return false;
        cooldowns.computeIfAbsent(p.getUniqueId(),k->new ConcurrentHashMap<>()).remove(effect);return true;
    }
    public boolean isActive(UUID id,String effect){return activeUntil.getOrDefault(id,Map.of()).getOrDefault(effect,0L)>System.currentTimeMillis();}

    public void startSpark(Player p, String slot) {
        if (plugin.isDisabled(p.getUniqueId()) || plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName()) || plugin.isInDisabledRegion(p.getLocation())) { plugin.tellKey(p, "disabled"); return; }
        String effect = plugin.getEquippedEffect(p.getUniqueId(), slot);
        if (effect == null) { plugin.tellKey(p, "effects-empty"); return; }
        effect = effect.replace("_augmented", "");
        if (!plugin.getConfig().getBoolean("effects." + effect + ".enabled", true)) { plugin.tellRaw(p, "<red>ᴛʜɪs ᴇғғᴇᴄᴛ ɪs ᴅɪsᴀʙʟᴇᴅ."); return; }
        long now = System.currentTimeMillis();
        long active = activeUntil.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>()).getOrDefault(effect, 0L);
        if (active > now) { plugin.tellRaw(p, "<red>ᴛʜɪs sᴘᴀʀᴋ ɪs ᴀʟʀᴇᴀᴅʏ ᴀᴄᴛɪᴠᴇ."); return; }
        long cd = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>()).getOrDefault(effect, 0L);
        if (cd > now) { plugin.tellRaw(p, "<red>ᴄᴏᴏʟᴅᴏᴡɴ: " + Math.max(1, (cd-now+999)/1000) + "s"); return; }
        long duration = Math.max(1, plugin.getConfig().getLong("effects."+effect+".spark_seconds", defaultSpark(effect)));
        long cooldown = Math.max(0, plugin.getConfig().getLong("effects."+effect+".cooldown_seconds", defaultCooldown(effect)));
        int factor=0;
        for(String equippedSlot:List.of("primary","secondary")){
            String equipped=plugin.getEquippedEffect(p.getUniqueId(),equippedSlot);
            if(equipped!=null&&equipped.replace("_augmented","").equals(effect)){
                factor++;
                if(equipped.endsWith("_augmented"))factor++;
            }
        }
        int denominator=Math.max(1,Math.min(4,factor));
        cooldown=Math.max(0,cooldown/denominator);
        activeUntil.get(p.getUniqueId()).put(effect, now + duration * 1000);
        cooldowns.get(p.getUniqueId()).put(effect, now + cooldown * 1000);
        p.playSound(p.getLocation(),plugin.getConfig().getString("use_sound","custom.activate"),1f,1.3f);
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
                double heal=recentDamage.getOrDefault(p.getUniqueId(),0.0)*plugin.getConfig().getDouble("effects.heart.spark_heal_percentage",0.2);
                p.setHealth(Math.min(p.getMaxHealth(),p.getHealth()+Math.max(0,heal)));
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
            case "speed" -> startSpeedDash(p);
            case "haste" -> p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,ticks,2,true,false,true));
            case "invisibility" -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,ticks,0,true,false,true));
            case "ocean" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,ticks,0,true,false,true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE,ticks,1,true,false,true));
                startOceanPull(p,0);
            }
            case "regeneration" -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,ticks,2,true,false,true));
            case "strength" -> p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,ticks,1,true,false,true));
            case "feather" -> startFeatherSpark(p);
            case "thunder" -> startThunderSpark(p);
            case "emerald" -> { p.giveExp((int)plugin.getConfig().getLong("effects.emerald.spark_xp_gain",2)); p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE,ticks,2,true,false,true)); }
            case "ender" -> {
                int radius=plugin.getConfig().getInt("effects.ender.teleport_max_radius",12);
                Block targetBlock=p.getTargetBlockExact(radius);
                if(targetBlock!=null){
                    Location destination=targetBlock.getLocation().add(0.5,1,0.5);
                    destination.setDirection(p.getLocation().getDirection());
                    if(destination.getBlock().isPassable()&&destination.clone().add(0,1,0).getBlock().isPassable())p.teleport(destination);
                }else{
                    Vector direction=p.getLocation().getDirection().normalize();
                    Location destination=p.getLocation().add(direction.multiply(radius)).add(0,0.5,0);
                    if(destination.getBlock().isPassable())p.teleport(destination);
                }
                for(Entity entity:p.getNearbyEntities(2,2,2)){
                    if(entity instanceof Player victim&&!victim.equals(p)&&!plugin.isTrusted(p.getUniqueId(),victim.getUniqueId())){
                        enderCurses.merge(victim.getUniqueId(),1,Integer::sum);
                        Bukkit.getScheduler().runTaskLater(plugin,()->enderCurses.computeIfPresent(victim.getUniqueId(),(id,count)->count<=1?null:count-1),Math.max(1,plugin.getConfig().getLong("effects.ender.curse_duration_seconds",30))*20L);
                    }else if(entity instanceof LivingEntity mob&&!(mob instanceof Player)&&mob.getType()!=EntityType.ENDER_DRAGON&&mob.getType()!=EntityType.WITHER&&mob.getType()!=EntityType.WARDEN)mob.setHealth(0);
                }
            }
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
        if(plugin.getConfig().getBoolean("head_drop",false)&&p.getKiller()!=null&&p.getKiller()!=p){
            ItemStack head=new ItemStack(Material.PLAYER_HEAD);
            if(head.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta){meta.setOwningPlayer(p);head.setItemMeta(meta);}
            e.getDrops().add(head);
        }
    }
    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Player p=e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin,()->applyPassives(p),2L);
        if(p.hasPlayedBefore()||!plugin.getConfig().getBoolean("roll_first_join",false))return;
        List<String> choices=List.of("emerald","feather","fire","frost","haste","heart","invisibility","ocean","regeneration","speed","strength","thunder");
        if(plugin.getConfig().getBoolean("do_roll_animation",true)){
            new org.bukkit.scheduler.BukkitRunnable(){
                int elapsed=0;
                @Override public void run(){
                    if(!p.isOnline()){cancel();return;}
                    String roll=choices.get(new Random().nextInt(choices.size()));
                    p.sendTitle(roll.toUpperCase(Locale.ROOT),"ᴇғғᴇᴄᴛ",0,5,0);
                    elapsed+=5;
                    if(elapsed>=40){
                        String chosen=choices.get(new Random().nextInt(choices.size()));
                        plugin.setEquippedEffect(p.getUniqueId(),"primary",chosen,false);
                        p.sendTitle(chosen.toUpperCase(Locale.ROOT),"ᴇғғᴇᴄᴛ",10,30,10);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin,40L,5L);
        }else{
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                String chosen=choices.get(new Random().nextInt(choices.size()));
                plugin.setEquippedEffect(p.getUniqueId(),"primary",chosen,false);
                p.sendTitle(chosen.toUpperCase(Locale.ROOT),"ᴇғғᴇᴄᴛ",10,30,10);
            },40L);
        }
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        Player p=e.getPlayer();UUID id=p.getUniqueId();
        cooldowns.remove(id);activeUntil.remove(id);hitCounts.remove(id);recentDamage.remove(id);enderCurses.remove(id);
        hitsFrom.remove(id);for(Map<UUID,Integer> map:hitsFrom.values())map.remove(id);
        removeAttributeModifier(p,Attribute.MOVEMENT_SPEED,"speed_stack");
        removeAttributeModifier(p,Attribute.ATTACK_SPEED,"attack_speed_stack");
        speedStacks.remove(id);attackStacks.remove(id);maxHealthBeforeHeart.remove(id);
    }
    private void removeAttributeModifier(Player p,Attribute attribute,String key){
        var instance=p.getAttribute(attribute);if(instance==null)return;NamespacedKey namespaced=new NamespacedKey(plugin,key);
        for(AttributeModifier modifier:new ArrayList<>(instance.getModifiers()))if(modifier.getKey().equals(namespaced))instance.removeModifier(modifier);
    }

    /** Keep permanent slot passives refreshed, and end temporary spark windows. */
    public void tick() {
        long now=System.currentTimeMillis();
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(plugin.isDisabled(p.getUniqueId()) || plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName()) || plugin.isInDisabledRegion(p.getLocation())) {
                p.sendActionBar(Component.empty());
                if(p.getMaxHealth()!=20){p.setMaxHealth(20);if(p.getHealth()>20)p.setHealth(20);}
                continue;
            }
            applyHeartHealth(p);
            applyPassives(p);
            applyToolPassives(p);
            updateDisplay(p);
            Map<String,Long> active=activeUntil.get(p.getUniqueId());
            if(active!=null) active.entrySet().removeIf(en -> en.getValue()<=now);
            Map<String,Long> playerCooldowns=cooldowns.get(p.getUniqueId());
            if(playerCooldowns!=null){
                for(var entry:new ArrayList<>(playerCooldowns.entrySet()))if(entry.getValue()>0&&entry.getValue()<=now){
                    playerCooldowns.remove(entry.getKey(),entry.getValue());
                    p.playSound(p.getLocation(),plugin.getConfig().getString("recharge_sound","custom.health"),1f,1f);
                }
            }
        }
    }
    private void applyHeartHealth(Player p){
        double maximum=20;
        if(hasEffect(p,"heart"))maximum=isActive(p.getUniqueId(),"heart")?plugin.getConfig().getDouble("effects.heart.spark_hearts",20):plugin.getConfig().getDouble("effects.heart.passive_hearts",15);
        maximum=Math.max(1,maximum);
        if(Math.abs(p.getMaxHealth()-maximum)>0.01){p.setMaxHealth(maximum);if(p.getHealth()>maximum)p.setHealth(maximum);}
    }
    private void applyToolPassives(Player p){
        boolean haste=hasEffect(p,"haste"),emerald=hasEffect(p,"emerald");
        if(!haste&&!emerald)return;
        boolean overwrite=plugin.getConfig().getBoolean("overwrite_enchants",true);
        for(ItemStack item:p.getInventory().getContents()){
            if(item==null||item.getType()==Material.AIR)continue;
            String type=item.getType().name();
            boolean tool=type.endsWith("_PICKAXE")||type.endsWith("_AXE")||type.endsWith("_SHOVEL")||type.endsWith("_HOE");
            boolean weapon=type.endsWith("_SWORD")||type.endsWith("_AXE");
            if(haste&&tool){
                if(overwrite||!item.containsEnchantment(Enchantment.FORTUNE))item.addUnsafeEnchantment(Enchantment.FORTUNE,5);
                if(overwrite||!item.containsEnchantment(Enchantment.UNBREAKING))item.addUnsafeEnchantment(Enchantment.UNBREAKING,10);
            }
            if(emerald&&weapon&&(overwrite||!item.containsEnchantment(Enchantment.LOOTING)))item.addUnsafeEnchantment(Enchantment.LOOTING,5);
        }
    }
    private boolean hasEffect(Player p,String effect){
        if(plugin.isDisabled(p.getUniqueId())||plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())||plugin.isInDisabledRegion(p.getLocation()))return false;
        for(String slot:List.of("primary","secondary")){
            String value=plugin.getEquippedEffect(p.getUniqueId(),slot);
            if(value!=null&&value.replace("_augmented","").equals(effect)
                &&plugin.getConfig().getBoolean("effects."+effect+".enabled",true))return true;
        }
        return false;
    }
    private void updateDisplay(Player p){
        String primary=plugin.getEquippedEffect(p.getUniqueId(),"primary");
        String secondary=plugin.getEquippedEffect(p.getUniqueId(),"secondary");
        if(primary==null&&secondary==null){p.sendActionBar(Component.empty());return;}
        boolean simple=plugin.isSimpleDisplay(p.getUniqueId());
        String left=displayEffect(p,primary,simple),right=displayEffect(p,secondary,simple);
        String separator=simple?" <dark_gray>• ":" <dark_gray>│ ";
        net.kyori.adventure.text.minimessage.MiniMessage mm=net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        String rendered=(left.isEmpty()?"":left)+(left.isEmpty()||right.isEmpty()?"":separator)+(right.isEmpty()?"":right);
        p.sendActionBar(mm.deserialize(rendered).decoration(TextDecoration.ITALIC,false));
    }
    private String displayEffect(Player p,String value,boolean simple){
        if(value==null)return "";
        String effect=value.replace("_augmented","");
        if(!plugin.getConfig().getBoolean("effects."+effect+".enabled",true))return "";
        long spark=getSparkRemainingMillis(p.getUniqueId(),effect),cooldown=getCooldownRemainingMillis(p.getUniqueId(),effect);
        long remain=spark>0?spark:cooldown;
        String timer=remain>0?String.format(Locale.ROOT,"%d:%02d",remain/60000,(remain/1000)%60):"";
        if(simple)return "<white>"+effect.toUpperCase(Locale.ROOT)+(value.endsWith("_augmented")?" <gold>★":"")+(timer.isEmpty()?"":" <gray>"+timer);
        int cp=iconCodepoint(effect,value.endsWith("_augmented"));
        String icon=cp<0?"":new String(Character.toChars(cp));
        return "<white>"+icon+(timer.isEmpty()?"":" <gray>"+timer);
    }
    private int iconCodepoint(String effect,boolean augmented){
        List<String> order=List.of("earth","emerald","ender","feather","fire","frost","haste","heart","invisibility","ocean","regeneration","speed","strength","thunder");
        int cp=0xE000;
        for(String current:order){
            if(current.equals("earth")||current.equals("ender")){if(current.equals(effect))return cp+20;cp+=42;}
            else{if(current.equals(effect))return cp+(augmented?41:20);cp+=84;}
        }
        return -1;
    }

    private void applyPassives(Player p) {
        for(String slot:List.of("primary","secondary")) {
            String value=plugin.getEquippedEffect(p.getUniqueId(),slot);
            if(value==null) continue;
            String e=value.replace("_augmented","");
            if (!plugin.getConfig().getBoolean("effects." + e + ".enabled", true)) continue;
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

    @EventHandler(ignoreCancelled=true) public void onExperience(PlayerExpChangeEvent event){
        Player p=event.getPlayer();
        if(!hasEffect(p,"emerald"))return;
        double bonus=plugin.getConfig().getDouble("effects.emerald.xp_gain",1.0);
        if(isActive(p.getUniqueId(),"emerald"))bonus+=plugin.getConfig().getDouble("effects.emerald.spark_xp_gain",2.0);
        event.setAmount(Math.max(0,(int)Math.round(event.getAmount()*(1+bonus))));
    }
    @EventHandler(ignoreCancelled=true) public void onFood(FoodLevelChangeEvent event){
        if(event.getEntity() instanceof Player p&&hasEffect(p,"regeneration"))event.setFoodLevel(10);
    }
    @EventHandler(ignoreCancelled=true) public void onFall(EntityDamageEvent event){
        if(event.getEntity() instanceof Player p && event.getCause()==EntityDamageEvent.DamageCause.FALL
          && !plugin.isDisabled(p.getUniqueId())
          && !plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())
          && !plugin.isInDisabledRegion(p.getLocation()) && hasEffect(p,"feather"))event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled=true) public void onCombat(EntityDamageByEntityEvent event) {
        Player attacker=null;
        if(event.getDamager() instanceof Player p)attacker=p;
        else if(event.getDamager() instanceof Projectile projectile&&projectile.getShooter() instanceof Player p)attacker=p;
        if(attacker==null||attacker.hasMetadata("infuse-isolate-hit")||plugin.isDisabled(attacker.getUniqueId())
          ||plugin.getConfig().getStringList("disabled_worlds").contains(attacker.getWorld().getName())
          ||plugin.isInDisabledRegion(attacker.getLocation()))return;
        if(!(event.getEntity() instanceof LivingEntity target))return;
        UUID attackerId=attacker.getUniqueId();
        Map<String,Integer> counts=hitCounts.computeIfAbsent(attackerId,k->new ConcurrentHashMap<>());
        int globalHits=counts.merge("hits",1,Integer::sum);
        double dealt=Math.max(0,event.getFinalDamage());
        recentDamage.merge(attackerId,dealt,Double::sum);
        Bukkit.getScheduler().runTaskLater(plugin,()->recentDamage.computeIfPresent(attackerId,(id,value)->Math.max(0,value-dealt)),600L);
        if(hasEffect(attacker,"feather")&&attacker.getFallDistance()>plugin.getConfig().getDouble("effects.feather.wind_burst_fall_min",10)){
            double fall=attacker.getFallDistance();Vector velocity=attacker.getVelocity();
            attacker.setVelocity(new Vector(velocity.getX(),plugin.getConfig().getDouble("effects.feather.wind_burst_power",1.5),velocity.getZ()));
            event.setDamage(event.getDamage()+fall*plugin.getConfig().getDouble("effects.feather.wind_burst_damage_multiplier",0.5));
            attacker.getWorld().spawnParticle(Particle.CLOUD,target.getLocation().add(0,1,0),1);
        }
        if(hasEffect(attacker,"fire")){
            if(isActive(attackerId,"fire")){
                target.setFireTicks(Math.max(target.getFireTicks(),plugin.getConfig().getInt("effects.fire.eternal_ticks",32767)));
                target.getWorld().spawnParticle(Particle.FLAME,target.getLocation().add(0,1,0),15,.3,.5,.3,.05);
            }else if(globalHits%Math.max(1,plugin.getConfig().getInt("effects.fire.trigger_hits",5))==0){
                target.setFireTicks(Math.max(target.getFireTicks(),plugin.getConfig().getInt("effects.fire.ignite_ticks",100)));
                target.getWorld().spawnParticle(Particle.FLAME,target.getLocation().add(0,1,0),10,.3,.5,.3,.05);
            }
        }
        if(hasEffect(attacker,"frost")&&(isActive(attackerId,"frost")||globalHits%Math.max(1,plugin.getConfig().getInt("effects.frost.trigger_hits",10))==0)){
            target.setFreezeTicks(Math.max(target.getFreezeTicks(),plugin.getConfig().getInt("effects.frost.freeze_ticks",600)));
            target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,plugin.getConfig().getInt("effects.frost.mining_fatigue_ticks",200),1,true,false,true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,1,true,false,true));
            target.getWorld().spawnParticle(Particle.SNOWFLAKE,target.getLocation().add(0,1,0),20,.5,.6,.5,.02);
        }
        if(hasEffect(attacker,"strength")){
            if(!(target instanceof Player))event.setDamage(event.getDamage()*plugin.getConfig().getDouble("effects.strength.mob_damage",2.0));
            event.setDamage(event.getDamage()+Math.max(0,attacker.getMaxHealth()-attacker.getHealth())*plugin.getConfig().getDouble("effects.strength.hp_loss_atk_bonus",0.3));
            if(isActive(attackerId,"strength")){
                boolean critical=attacker.getFallDistance()>0&&!attacker.isOnGround()&&attacker.getAttackCooldown()>.9;
                event.setDamage(event.getDamage()*(critical?plugin.getConfig().getDouble("effects.strength.double_crit_mult",1.334):1.5));
                target.getWorld().spawnParticle(Particle.CRIT,target.getLocation().add(0,1,0),10,.3,.4,.3,.2);
                if(target instanceof Player victim&&victim.isBlocking()&&attacker.getInventory().getItemInMainHand().getType().name().endsWith("_AXE")){
                    victim.setCooldown(Material.SHIELD,plugin.getConfig().getInt("effects.strength.shield_disable_ticks",200));
                    victim.playSound(victim.getLocation(),Sound.ITEM_SHIELD_BREAK,1f,1f);
                }
            }
        }
        if(hasEffect(attacker,"thunder")&&globalHits%Math.max(1,attacker.getWorld().hasStorm()?plugin.getConfig().getInt("effects.thunder.trigger_hits",10)/2:plugin.getConfig().getInt("effects.thunder.trigger_hits",10))==0)
            lightningChain(attacker,target,plugin.getConfig().getDouble("effects.thunder.true_damage",1),plugin.getConfig().getInt("effects.thunder.lightning_chain_max",10),plugin.getConfig().getDouble("effects.thunder.lightning_chain_radius",12),new HashSet<>());
        if(hasEffect(attacker,"haste")){
            changeAttributeStack(attacker,Attribute.ATTACK_SPEED,attackStacks,"attack_speed_stack",plugin.getConfig().getDouble("effects.haste.attack_speed_stack",0.2),0,1);
            Bukkit.getScheduler().runTaskLater(plugin,()->{if(target.isValid())target.setNoDamageTicks(Math.max(0,plugin.getConfig().getInt("effects.haste.iframe_duration",19)));},1L);
        }
        if(hasEffect(attacker,"invisibility")&&globalHits%Math.max(1,plugin.getConfig().getInt("effects.invisibility.trigger_hits",10))==0)
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,plugin.getConfig().getInt("effects.invisibility.blindness_ticks",200),0,true,false,true));
        if(hasEffect(attacker,"regeneration")){
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,plugin.getConfig().getInt("effects.regeneration.duration_hit_ticks",60),Math.max(0,plugin.getConfig().getInt("effects.regeneration.effect_hit_tier",2)-1),true,false,true));
            if(globalHits%Math.max(1,plugin.getConfig().getInt("effects.regeneration.trigger_hits",10))==0&&target instanceof Player victim)victim.setSaturation(Math.max(0,victim.getSaturation()-1));
        }
        if(hasEffect(attacker,"speed"))changeAttributeStack(attacker,Attribute.MOVEMENT_SPEED,speedStacks,"speed_stack",plugin.getConfig().getDouble("effects.speed.hit_increase",0.01),plugin.getConfig().getDouble("effects.speed.max_penalty",-0.2),plugin.getConfig().getDouble("effects.speed.stack_cap",0.2));
        if(target instanceof Player victim&&hasEffect(victim,"speed"))changeAttributeStack(victim,Attribute.MOVEMENT_SPEED,speedStacks,"speed_stack",plugin.getConfig().getDouble("effects.speed.hit_decrease",-0.02),plugin.getConfig().getDouble("effects.speed.max_penalty",-0.2),plugin.getConfig().getDouble("effects.speed.stack_cap",0.2));
        if(target instanceof Player victim){
            int hits=hitsFrom.computeIfAbsent(victim.getUniqueId(),k->new ConcurrentHashMap<>()).merge(attackerId,1,Integer::sum);
            if(hasEffect(attacker,"emerald")){
                int xp=plugin.getConfig().getInt(plugin.isTrusted(attackerId,victim.getUniqueId())?"effects.emerald.xp_share_amount":"effects.emerald.xp_steal_amount",2);
                if(plugin.isTrusted(attackerId,victim.getUniqueId()))event.setCancelled(true);else victim.giveExp(-xp);
                attacker.giveExp(xp);
            }
            if(hasEffect(victim,"emerald")&&hits%Math.max(1,plugin.getConfig().getInt("effects.emerald.trigger_hits",10))==0)
                for(Material mat:consumableCooldownMaterials())attacker.setCooldown(mat,plugin.getConfig().getInt("effects.emerald.hit_item_cooldown_ticks",200));
            if(hasEffect(attacker,"heart")&&plugin.isTrusted(attackerId,victim.getUniqueId())){
                double transfer=Math.min(attacker.getHealth()-10,10);
                if(transfer>0){
                    event.setCancelled(true);int amplifier=Math.max(0,(int)(transfer/2)-1);
                    int duration=(int)Math.max(20,480/transfer*20);
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,duration,amplifier,true,false,true));
                    attacker.setHealth(Math.min(attacker.getMaxHealth(),plugin.getConfig().getDouble("effects.heart.return_after_hit",10)));
                    attacker.playSound(attacker.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
                }
            }
            if(hasEffect(attacker,"ender")&&enderCurses.getOrDefault(victim.getUniqueId(),0)>0){
                double reflected=Math.max(0,event.getDamage());
                final Player attackingPlayer=attacker;
                for(Player cursed:Bukkit.getOnlinePlayers())if(!cursed.equals(victim)&&enderCurses.getOrDefault(cursed.getUniqueId(),0)>0&&!plugin.isTrusted(attackerId,cursed.getUniqueId())){
                    cursed.setMetadata("infuse-isolate-hit",new org.bukkit.metadata.FixedMetadataValue(plugin,true));
                    Bukkit.getScheduler().runTask(plugin,()->{if(cursed.isOnline()&&cursed.isValid()){cursed.damage(reflected,attackingPlayer);cursed.removeMetadata("infuse-isolate-hit",plugin);}});
                }
            }
        }
    }
    private List<Material> consumableCooldownMaterials(){
        return List.of(Material.BREAD,Material.COOKED_BEEF,Material.COOKED_CHICKEN,Material.COOKED_MUTTON,Material.COOKED_PORKCHOP,Material.COOKED_RABBIT,Material.GOLDEN_CARROT,Material.GOLDEN_APPLE,Material.ENCHANTED_GOLDEN_APPLE,Material.COOKIE,Material.MELON_SLICE,Material.PUMPKIN_PIE,Material.BEETROOT_SOUP,Material.MUSHROOM_STEW,Material.RABBIT_STEW,Material.SUSPICIOUS_STEW,Material.BAKED_POTATO,Material.CARROT,Material.POTATO,Material.APPLE,Material.DRIED_KELP,Material.CHORUS_FRUIT,Material.SWEET_BERRIES,Material.GLOW_BERRIES,Material.HONEY_BOTTLE,Material.COD,Material.SALMON,Material.COOKED_COD,Material.COOKED_SALMON,Material.TROPICAL_FISH,Material.PUFFERFISH,Material.BEEF,Material.PORKCHOP,Material.MUTTON,Material.CHICKEN,Material.RABBIT,Material.POISONOUS_POTATO,Material.SPIDER_EYE,Material.ROTTEN_FLESH,Material.EXPERIENCE_BOTTLE);
    }
    private void changeAttributeStack(Player p,Attribute attribute,Map<UUID,Double> stacks,String key,double change,double min,double max){
        var instance=p.getAttribute(attribute);if(instance==null)return;
        double amount=Math.max(min,Math.min(max,stacks.getOrDefault(p.getUniqueId(),0.0)+change));stacks.put(p.getUniqueId(),amount);
        NamespacedKey namespaced=new NamespacedKey(plugin,key);
        for(AttributeModifier modifier:new ArrayList<>(instance.getModifiers()))if(modifier.getKey().equals(namespaced))instance.removeModifier(modifier);
        if(Math.abs(amount)>0.0001)instance.addModifier(new AttributeModifier(namespaced,amount,AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }
    private void lightningChain(Player owner,LivingEntity victim,double damage,int remaining,double radius,Set<UUID> visited){
        if(!victim.isValid()||victim.isDead()||visited.contains(victim.getUniqueId()))return;
        visited.add(victim.getUniqueId());
        victim.getWorld().strikeLightningEffect(victim.getLocation());trueDamage(owner,victim,damage);
        victim.getWorld().playSound(victim.getLocation(),Sound.ENTITY_LIGHTNING_BOLT_THUNDER,1f,1f);
        if(remaining<=0)return;
        LivingEntity next=victim.getNearbyEntities(radius,radius,radius).stream()
            .filter(e->e instanceof LivingEntity&&e!=owner&&e!=victim&&e.isValid()&&!visited.contains(e.getUniqueId()))
            .map(e->(LivingEntity)e).filter(e->isTargetAllowed(owner,e))
            .min(Comparator.comparingDouble(e->e.getLocation().distanceSquared(victim.getLocation()))).orElse(null);
        if(next!=null)Bukkit.getScheduler().runTaskLater(plugin,()->lightningChain(owner,next,damage,remaining-1,radius,visited),2L);
    }
    private void trueDamage(Player owner,LivingEntity victim,double amount){
        if(!victim.isValid()||victim.isDead()||victim.getNoDamageTicks()>0||amount<=0)return;
        double absorption=victim.getAbsorptionAmount();
        if(amount>=victim.getHealth()+absorption){victim.setHealth(0);return;}
        double left=amount;
        if(absorption>0){double used=Math.min(absorption,left);victim.setAbsorptionAmount(Math.max(0,absorption-used));left-=used;}
        if(left>0)victim.setHealth(Math.max(0,victim.getHealth()-left));
        if(victim.isValid()&&!victim.isDead()){
            owner.setMetadata("infuse-isolate-hit",new org.bukkit.metadata.FixedMetadataValue(plugin,true));
            victim.setNoDamageTicks(0);victim.damage(0.01,owner);owner.removeMetadata("infuse-isolate-hit",plugin);
        }
    }
    private void startSpeedDash(Player p){
        Vector dir=p.getLocation().getDirection();Vector velocity=new Vector(dir.getX(),dir.getY()*plugin.getConfig().getDouble("effects.speed.dash_damping",0.6),dir.getZ()).multiply(plugin.getConfig().getDouble("effects.speed.dash_power",2.6));
        p.setVelocity(velocity);
        new org.bukkit.scheduler.BukkitRunnable(){int ticks=0;final int max=plugin.getConfig().getInt("effects.speed.dash_iteration",14);
            @Override public void run(){if(!p.isOnline()||ticks++>=max){cancel();return;}double radius=plugin.getConfig().getDouble("effects.speed.dash_effect_radius",1.5);
                p.getWorld().spawnParticle(Particle.CLOUD,p.getLocation(),8,.4,.3,.4,.02);
                for(Entity ent:p.getNearbyEntities(radius,radius,radius))if(ent instanceof LivingEntity target&&target!=p&&isTargetAllowed(p,target))trueDamage(p,target,plugin.getConfig().getDouble("effects.speed.dash_true_damage",2));
            }
        }.runTaskTimer(plugin,1L,1L);
    }
    private void startFeatherSpark(Player p){
        p.setVelocity(p.getVelocity().setY(Math.max(1.2,p.getVelocity().getY()+1.2)));
        new org.bukkit.scheduler.BukkitRunnable(){int age=0;
            @Override public void run(){if(!p.isOnline()||age++>200){cancel();return;}
                if(age>8&&p.getVelocity().getY()<0&&p.getFallDistance()>=plugin.getConfig().getDouble("effects.feather.fall_trigger_threshold",10)){
                    double fall=p.getFallDistance();double radius=Math.max(plugin.getConfig().getDouble("effects.feather.min_shockwave_radius",3),Math.min(plugin.getConfig().getDouble("effects.feather.max_shockwave_radius",8),fall/4));
                    p.getWorld().spawnParticle(Particle.CLOUD,p.getLocation(),1);p.getWorld().spawnParticle(Particle.CLOUD,p.getLocation(),100,radius/2,.2,radius/2,.2);
                    for(Entity ent:p.getNearbyEntities(radius,radius,radius))if(ent instanceof LivingEntity target&&target!=p&&isTargetAllowed(p,target)){trueDamage(p,target,plugin.getConfig().getDouble("effects.feather.shockwave_true_damage",2));target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,plugin.getConfig().getInt("effects.feather.slow_falling_ticks",100),0,true,false,true));}
                    p.setFallDistance(0);p.setVelocity(p.getVelocity().setY(Math.max(.6,fall/plugin.getConfig().getDouble("effects.feather.bounce_coefficient",8))));cancel();
                }
            }
        }.runTaskTimer(plugin,1L,1L);
    }
    private void startThunderSpark(Player p){
        new org.bukkit.scheduler.BukkitRunnable(){int count=0;final int max=plugin.getConfig().getInt("effects.thunder.strike_time_max",5);
            @Override public void run(){if(!p.isOnline()||count++>=max){cancel();return;}
                LivingEntity target=p.getNearbyEntities(12,8,12).stream().filter(e->e instanceof LivingEntity&&e!=p&&isTargetAllowed(p,(LivingEntity)e)).map(e->(LivingEntity)e).min(Comparator.comparingDouble(e->e.getLocation().distanceSquared(p.getLocation()))).orElse(null);
                if(target!=null){target.getWorld().strikeLightningEffect(target.getLocation());trueDamage(p,target,plugin.getConfig().getDouble("effects.thunder.true_damage",1));}
            }
        }.runTaskTimer(plugin,0L,20L);
    }
    private void startOceanPull(Player p,int iteration){
        int max=plugin.getConfig().getInt("effects.ocean.pull_iteration",50);if(!p.isOnline()||iteration>=max)return;
        double radius=plugin.getConfig().getDouble("effects.ocean.pull_radius",6),strength=plugin.getConfig().getDouble("effects.ocean.pull_strength",0.1);
        for(Player target:p.getWorld().getPlayers())if(!target.equals(p)&&target.getLocation().distanceSquared(p.getLocation())<=radius*radius&&!plugin.isTrusted(p.getUniqueId(),target.getUniqueId()))target.setVelocity(target.getVelocity().add(p.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(strength)));
        Bukkit.getScheduler().runTaskLater(plugin,()->startOceanPull(p,iteration+1),Math.max(1,plugin.getConfig().getInt("effects.ocean.pull_interval_ticks",2)));
    }


}
