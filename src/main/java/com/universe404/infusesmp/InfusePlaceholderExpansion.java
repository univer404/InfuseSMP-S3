package com.universe404.infusesmp;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public final class InfusePlaceholderExpansion extends PlaceholderExpansion {
    private final InfuseSMP plugin;
    private final InfuseMechanics mechanics;
    private static final List<String> ICON_ORDER=List.of("earth","emerald","ender","feather","fire","frost","haste","heart","invisibility","ocean","regeneration","speed","strength","thunder");
    public InfusePlaceholderExpansion(InfuseSMP plugin,InfuseMechanics mechanics){this.plugin=plugin;this.mechanics=mechanics;}
    @Override public @NotNull String getIdentifier(){return "infuse";}
    @Override public @NotNull String getAuthor(){return String.join(",",plugin.getDescription().getAuthors());}
    @Override public @NotNull String getVersion(){return plugin.getDescription().getVersion();}
    @Override public boolean persist(){return true;}
    @Override public @Nullable String onPlaceholderRequest(Player player,String params){
        if(player==null)return "";
        String id=params.toLowerCase(Locale.ROOT);UUID uuid=player.getUniqueId();
        if(id.equals("effect_name_primary"))return name(plugin.getEquippedEffect(uuid,"primary"));
        if(id.equals("effect_name_secondary"))return name(plugin.getEquippedEffect(uuid,"secondary"));
        if(id.equals("effect_augmented_primary"))return Boolean.toString(augmented(plugin.getEquippedEffect(uuid,"primary")));
        if(id.equals("effect_augmented_secondary"))return Boolean.toString(augmented(plugin.getEquippedEffect(uuid,"secondary")));
        if(id.equals("effect_icon_primary"))return icon(plugin.getEquippedEffect(uuid,"primary"));
        if(id.equals("effect_icon_secondary"))return icon(plugin.getEquippedEffect(uuid,"secondary"));
        if(id.endsWith("_primary")||id.endsWith("_secondary")){
            String slot=id.endsWith("_primary")?"primary":"secondary";
            String value=plugin.getEquippedEffect(uuid,slot);
            if(value==null)return id.startsWith("effect_spark_ticks")||id.startsWith("effect_cooldown_ticks")?"0":"";
            String effect=value.replace("_augmented","");
            long spark=mechanics.getSparkRemainingMillis(uuid,effect),cooldown=mechanics.getCooldownRemainingMillis(uuid,effect);
            if(id.startsWith("effect_spark_ticks"))return Long.toString((spark+49)/50);
            if(id.startsWith("effect_cooldown_ticks"))return Long.toString((cooldown+49)/50);
            if(id.startsWith("effect_timer_main"))return timer(spark>0?spark:cooldown,false);
            if(id.startsWith("effect_timer_alt"))return timer(spark>0?spark:cooldown,true);
        }
        return null;
    }
    private String name(String value){if(value==null)return "";String v=value.replace("_augmented","");return v.isEmpty()?"":v.substring(0,1).toUpperCase(Locale.ROOT)+v.substring(1);}
    private boolean augmented(String value){return value!=null&&value.endsWith("_augmented");}
    private String timer(long millis,boolean alternate){
        if(millis<=0)return "";
        long ticks=(millis+49)/50,minutes=ticks/1200,seconds=(ticks/20)%60,centiseconds=(ticks%20)*5;
        return alternate?minutes+":"+String.format(Locale.ROOT,"%02d:%02d",seconds,centiseconds):minutes+":"+String.format(Locale.ROOT,"%02d",seconds);
    }
    /**
     * Return a pixel-art glyph from the resource pack's dedicated HUD font.
     * The font tag is important: without it Minecraft renders private-use
     * characters as missing-glyph boxes. Augmented effects use their own icon.
     */
    private String icon(String value){
        if(value==null)return "";
        String effect=value.replace("_augmented","");
        int index=ICON_ORDER.indexOf(effect);
        if(index<0)return "";
        int codepoint=(augmented(value)?0xE010:0xE000)+index;
        return "<font:infusesmp:hud>"+new String(Character.toChars(codepoint))+"</font>";
    }
}
