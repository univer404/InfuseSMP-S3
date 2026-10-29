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
    private static final Map<String,Integer> IDS=Map.ofEntries(Map.entry("emerald",1),Map.entry("feather",2),Map.entry("fire",3),Map.entry("frost",4),Map.entry("thunder",5),Map.entry("haste",6),Map.entry("heart",7),Map.entry("invisibility",8),Map.entry("ocean",9),Map.entry("regeneration",10),Map.entry("speed",11),Map.entry("strength",12),Map.entry("ender",13));
    private static final Map<String,String> COLORS=Map.ofEntries(
      Map.entry("emerald","#0DFC00"),Map.entry("feather","#BEA3CA"),Map.entry("fire","#FC7803"),Map.entry("frost","#7AE3FC"),Map.entry("thunder","#FCED00"),Map.entry("haste","#B96C00"),Map.entry("heart","#FC0046"),Map.entry("invisibility","#7E00FC"),Map.entry("ocean","#005AFC"),Map.entry("regeneration","#FC00DD"),Map.entry("speed","#D1A44B"),Map.entry("strength","#9E2020"),Map.entry("ender","#831177"));
    private static final Map<String,String> NAMES=Map.ofEntries(
      Map.entry("emerald","ᴇᴍᴇʀᴀʟᴅ"),Map.entry("feather","ғᴇᴀᴛʜᴇʀ"),Map.entry("fire","ғɪʀᴇ"),Map.entry("frost","ғʀᴏsᴛ"),Map.entry("thunder","ᴛʜᴜɴᴅᴇʀ"),Map.entry("haste","ʜᴀsᴛᴇ"),Map.entry("heart","ʜᴇᴀʀᴛ"),Map.entry("invisibility","ɪɴᴠɪsɪʙɪʟɪᴛʏ"),Map.entry("ocean","ᴏᴄᴇᴀɴ"),Map.entry("regeneration","ʀᴇɢᴇɴᴇʀᴀᴛɪᴏɴ"),Map.entry("speed","sᴘᴇᴇᴅ"),Map.entry("strength","sᴛʀᴇɴɢᴛʜ"),Map.entry("ender","ᴇɴᴅᴇʀ"));
    private static final Map<String,List<String>> LORE = Map.ofEntries(
        Map.entry("emerald", List.of(
            "<#09B000>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:",
            "<#0DFC00>💲 <#6D6D6D>ʟᴏᴏᴛɪɴɢ 5",
            "<#0DFC00>💲 <#6D6D6D>ᴘᴇʀᴍᴀɴᴇɴᴛ ʜᴇʀᴏ ᴏғ ᴛʜᴇ ᴠɪʟʟᴀɢᴇ",
            "<#0DFC00>💲 <#6D6D6D>2.0x xᴘ ɢᴀɪɴ",
            "<#0DFC00>💲 <#6D6D6D>ᴄᴏɴsᴜᴍᴀʙʟᴇs ʜᴀᴠᴇ ᴀ 50% ᴄʜᴀɴᴄᴇ ᴏғ ɴᴏᴛ ʙᴇɪɴɢ ᴄᴏɴsᴜᴍᴇᴅ",
            "<#0DFC00>💲 <#6D6D6D>ʀᴇᴄᴇɪᴠɪɴɢ 10 ʜɪᴛs ʟᴏᴄᴋs ᴛʜᴇ ᴀᴛᴛᴀᴄᴋᴇʀ's ғᴏᴏᴅ ᴀɴᴅ xᴘ",
            "<#09B000>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#0DFC00>💲 <#6D6D6D>ʜᴇʀᴏ ᴏғ ᴛʜᴇ ᴠɪʟʟᴀɢᴇ 5",
            "<#0DFC00>💲 <#6D6D6D>4.0x xᴘ ɢᴀɪɴ",
            "<#0DFC00>💲 <#6D6D6D>ᴄᴏɴsᴜᴍᴀʙʟᴇs ʜᴀᴠᴇ ᴀ 75% ᴄʜᴀɴᴄᴇ ɴᴏᴛ ᴛᴏ ʙᴇ ᴄᴏɴsᴜᴍᴇᴅ",
            "<#0DFC00>💲 <#6D6D6D>sʜᴀʀᴇ xᴘ ᴡɪᴛʜ ɴᴇᴀʀʙʏ ᴛᴇᴀᴍᴍᴀᴛᴇs ʙʏ ʜɪᴛᴛɪɴɢ ᴛʜᴇᴍ"
        )),
        Map.entry("feather", List.of(
            "<#84728C>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#BEA3CA>☁ <#6D6D6D>ɴᴏ ғᴀʟʟ ᴅᴀᴍᴀɢᴇ",
            "<#BEA3CA>☁ <#6D6D6D>ɪᴍᴍᴜɴᴇ ᴛᴏ ᴋɪɴᴇᴛɪᴄ ᴇɴᴇʀɢʏ",
            "<#BEA3CA>☁ <#6D6D6D>ᴡɪɴᴅʙᴜʀsᴛ ᴏɴ ᴀʟʟ ᴀᴛᴛᴀᴄᴋs",
            "<#BEA3CA>☁ <#6D6D6D>ғᴀʟʟɪɴɢ ғʀᴏᴍ ʜɪɢʜᴇʀ ᴅᴇᴀʟs ᴍᴏʀᴇ ᴅᴀᴍᴀɢᴇ",
            "<#BEA3CA>☁ <#6D6D6D>ᴡɪɴᴅ ᴄʜᴀʀɢᴇs ʜᴀᴠᴇ 2.0x ᴠᴇʟᴏᴄɪᴛʏ ᴀɴᴅ 0.5x ᴜsᴇ sᴘᴇᴇᴅ",
            "<#BEA3CA>☁ <#6D6D6D>ʀᴇᴄᴇɪᴠɪɴɢ 10 ʜɪᴛs sᴘᴀᴡɴs ᴀ ᴡɪɴᴅ ᴄʜᴀʀɢᴇ (ᴄʀᴏᴜᴄʜ ᴛᴏ ᴜsᴇ)",
            "<#84728C>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#BEA3CA>☁ <#6D6D6D>ʟᴀᴜɴᴄʜᴇs ʏᴏᴜ ᴜᴘᴡᴀʀᴅ",
            "<#BEA3CA>☁ <#6D6D6D>ғᴀʟʟɪɴɢ ғʀᴏᴍ ʜɪɢʜᴇʀ ᴅᴇᴀʟs 2.0 ʜᴇᴀʀᴛs ᴏɴ ɪᴍᴘᴀᴄᴛ",
            "<#BEA3CA>☁ <#6D6D6D>ɪᴍᴘᴀᴄᴛ ʀᴀᴅɪᴜs ᴀɴᴅ ʙᴏᴜɴᴄᴇ ʜᴇɪɢʜᴛ sᴄᴀʟᴇ ᴡɪᴛʜ ᴅɪsᴛᴀɴᴄᴇ ғᴀʟʟᴇɴ",
            "<#BEA3CA>☁ <#6D6D6D>ᴇɴᴇᴍɪᴇs ʜɪᴛ ʙʏ ᴛʜᴇ sʜᴏᴄᴋᴡᴀᴠᴇ ɢᴇᴛ sʟᴏᴡ ғᴀʟʟɪɴɢ",
            "<#BEA3CA>☁ <#6D6D6D>ᴄʀᴏᴜᴄʜ ᴛᴏ ᴄᴀɴᴄᴇʟ ᴛʜᴇ ʙᴏᴜɴᴄᴇ"
        )),
        Map.entry("fire", List.of(
            "<#B05302>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#FC7803>🔥 <#6D6D6D>ᴘᴇʀᴍᴀɴᴇɴᴛ ғɪʀᴇ ʀᴇsɪsᴛᴀɴᴄᴇ",
            "<#FC7803>🔥 <#6D6D6D>ᴘʀᴏᴘᴇʟ ʏᴏᴜʀsᴇʟғ ᴛʜʀᴏᴜɢʜ ʟᴀᴠᴀ",
            "<#FC7803>🔥 <#6D6D6D>ʟᴀᴠᴀ ʙʀᴇᴀᴋs ʏᴏᴜʀ ғᴀʟʟ",
            "<#FC7803>🔥 <#6D6D6D>sɴᴏᴡʙᴀʟʟs ᴛᴜʀɴ ɪɴᴛᴏ ғɪʀᴇʙᴀʟʟs",
            "<#FC7803>🔥 <#6D6D6D>5ᴛʜ ʜɪᴛ sᴇᴛs ᴇɴᴇᴍɪᴇs ᴏɴ ғɪʀᴇ",
            "<#FC7803>🔥 <#6D6D6D>ғʟᴀᴍᴇ ᴏɴ ʀᴀɴɢᴇᴅ ᴀᴛᴛᴀᴄᴋs",
            "<#FC7803>🔥 <#6D6D6D>ғɪʀᴇ sᴘʀᴇᴀᴅs ᴛᴏ ɴᴇᴀʀʙʏ ᴇɴᴇᴍɪᴇs ᴛʜᴀᴛ ᴛᴏᴜᴄʜ",
            "<#FC7803>🔥 <#6D6D6D>ᴇxᴛɪɴɢᴜɪsʜ ɴᴇᴀʀʙʏ ᴡᴀᴛᴇʀ ᴡʜᴇɴ ʙᴜʀɴɪɴɢ",
            "<#B05302>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#FC7803>🔥 <#6D6D6D>ɪɢɴɪᴛᴇs ɴᴇᴀʀʙʏ ᴘʟᴀʏᴇʀs ᴀɴᴅ ʟᴀᴜɴᴄʜᴇs ᴛʜᴇᴍ ᴜᴘᴡᴀʀᴅ",
            "<#FC7803>🔥 <#6D6D6D>ᴇxᴛɪɴɢᴜɪsʜᴇs ᴡᴀᴛᴇʀ ᴀɴᴅ sᴇᴛs sᴏᴍᴇ ɴᴇᴀʀʙʏ ᴀɪʀ ʙʟᴏᴄᴋs ᴏɴ ғɪʀᴇ"
        )),
        Map.entry("frost", List.of(
            "<#559FB0>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#7AE3FC>❄ <#6D6D6D>10ᴛʜ ʜɪᴛ ғʀᴇᴇᴢᴇs ᴇɴᴇᴍɪᴇs ᴀɴᴅ ᴘᴜᴛs ᴡɪɴᴅ ᴄʜᴀʀɢᴇs ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ",
            "<#7AE3FC>❄ <#6D6D6D>ғʀᴏsᴛᴇᴅ ᴇɴᴇᴍɪᴇs ᴄʀᴇᴀᴛᴇ ɪᴄᴇ ᴛʀᴀɪʟs ᴀɴᴅ ɢᴇᴛ ᴍɪɴɪɴɢ ғᴀᴛɪɢᴜᴇ",
            "<#7AE3FC>❄ <#6D6D6D>1.5x sᴘᴇᴇᴅ ᴀɴᴅ 30% ᴋʙ ʀᴇsɪsᴛᴀɴᴄᴇ ᴏɴ ɪᴄᴇ/sɴᴏᴡ",
            "<#7AE3FC>❄ <#6D6D6D>ᴄᴏᴏʟs ᴅᴏᴡɴ ᴀɴᴅ sᴏʟɪᴅɪғɪᴇs ʟᴀᴠᴀ",
            "<#7AE3FC>❄ <#6D6D6D>ғʀᴏsᴛ ᴏɴ ʀᴀɴɢᴇᴅ ᴀᴛᴛᴀᴄᴋs",
            "<#559FB0>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#7AE3FC>❄ <#6D6D6D>ᴀᴘᴘʟɪᴇs ғʀᴏsᴛ ᴀɴᴅ ᴍɪɴɪɴɢ ғᴀᴛɪɢᴜᴇ 2 ᴏɴ ʜɪᴛ",
            "<#7AE3FC>❄ <#6D6D6D>2.0x sᴘᴇᴇᴅ ᴏɴ ɪᴄᴇ/sɴᴏᴡ"
        )),
        Map.entry("thunder", List.of(
            "<#B0A600>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#FCED00>⚡ <#6D6D6D>sᴍɪᴛᴇs ᴘʟᴀʏᴇʀs ᴇᴠᴇʀʏ 10ᴛʜ ʜɪᴛ (5ᴛʜ ɪғ ʀᴀɪɴɪɴɢ)",
            "<#FCED00>⚡ <#6D6D6D>ᴛʜᴜɴᴅᴇʀ ᴄʜᴀɪɴs ᴛᴏ ɴᴇᴀʀʙʏ ᴇɴᴇᴍɪᴇs ᴜᴘ ᴛᴏ 10 ᴛɪᴍᴇs",
            "<#FCED00>⚡ <#6D6D6D>ᴛʀɪᴅᴇɴᴛs sᴍɪᴛᴇ ᴇɴᴇᴍɪᴇs ᴡʜᴇɴ ᴛʜʀᴏᴡɴ",
            "<#B0A600>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#FCED00>⚡ <#6D6D6D>sᴍɪᴛᴇs ɴᴇᴀʀʙʏ ᴇɴᴇᴍɪᴇs ᴜᴘ ᴛᴏ 5 ᴛɪᴍᴇs",
            "<#FCED00>⚡ <#6D6D6D>ʀᴀᴅɪᴜs ɪɴᴄʀᴇᴀsᴇs ᴡɪᴛʜ ᴍᴏʀᴇ ᴇɴᴇᴍɪᴇs"
        )),
        Map.entry("haste", List.of(
            "<#804B00>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#B96C00>⛏ <#6D6D6D>ʙʀᴇᴀᴋ ʙʟᴏᴄᴋs ғᴀsᴛᴇʀ",
            "<#B96C00>⛏ <#6D6D6D>ᴛᴏᴏʟs ʜᴀᴠᴇ ғᴏʀᴛᴜɴᴇ 5 ᴀɴᴅ ᴜɴʙʀᴇᴀᴋɪɴɢ 10",
            "<#B96C00>⛏ <#6D6D6D>ᴇɴᴇᴍɪᴇs ʜᴀᴠᴇ ʀᴇᴅᴜᴄᴇᴅ ɪ-ғʀᴀᴍᴇs",
            "<#B96C00>⛏ <#6D6D6D>ᴇᴀᴄʜ ʜɪᴛ ɪɴᴄʀᴇᴀsᴇs ᴀᴛᴛᴀᴄᴋ sᴘᴇᴇᴅ",
            "<#B96C00>⛏ <#6D6D6D>ᴀᴛᴛᴀᴄᴋ sᴘᴇᴇᴅ ᴅᴇᴄᴀʏs ᴇᴠᴇʀʏ 1 s",
            "<#804B00>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#B96C00>⛏ <#6D6D6D>ɪɴsᴛᴀ ᴍɪɴᴇ ᴀʟʟ ʙʟᴏᴄᴋs"
        )),
        Map.entry("heart", List.of(
            "<#B00030>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#FC0046>❤ <#6D6D6D>+5 ʜᴇᴀʀᴛs",
            "<#FC0046>❤ <#6D6D6D>ғᴏᴏᴅ ɪᴛᴇᴍs ɢɪᴠᴇ 2 ᴀʙsᴏʀᴘᴛɪᴏɴ ʜᴇᴀʀᴛs",
            "<#FC0046>❤ <#6D6D6D>ᴇɴᴄʜᴀɴᴛᴇᴅ ɢᴏʟᴅᴇɴ ᴀᴘᴘʟᴇs ɢɪᴠᴇ 10 ᴀʙsᴏʀᴘᴛɪᴏɴ ʜᴇᴀʀᴛs",
            "<#FC0046>❤ <#6D6D6D>ʜɪᴛ ᴛᴇᴀᴍᴍᴀᴛᴇs ᴛᴏ ɢɪᴠᴇ ᴛʜᴇᴍ ʏᴏᴜʀ ᴇxᴛʀᴀ ʜᴘ ᴀs ᴀʙsᴏʀᴘᴛɪᴏɴ",
            "<#B00030>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#FC0046>❤ <#6D6D6D>+10 ʜᴇᴀʀᴛs",
            "<#FC0046>❤ <#6D6D6D>ʜᴇᴀʟs ʏᴏᴜ ʙʏ 20% ᴏғ ᴅᴀᴍᴀɢᴇ ᴅᴇᴀʟᴛ ɪɴ ᴛʜᴇ ᴘᴀsᴛ 30 s"
        )),
        Map.entry("invisibility", List.of(
            "<#5800B0>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#7E00FC>👁 <#6D6D6D>ɪɴᴠɪsɪʙɪʟɪᴛʏ",
            "<#7E00FC>👁 <#6D6D6D>ɴᴏ ᴇɴᴛɪᴛʏ ᴄᴏʟʟɪsɪᴏɴ",
            "<#7E00FC>👁 <#6D6D6D>ᴍᴏʙs ᴅᴏɴ'ᴛ ᴛᴀʀɢᴇᴛ ʏᴏᴜ",
            "<#7E00FC>👁 <#6D6D6D>ɪᴍᴍᴜɴᴇ ᴛᴏ ᴡɪᴛʜᴇʀ ᴀɴᴅ ᴅᴀʀᴋɴᴇss",
            "<#7E00FC>👁 <#6D6D6D>ᴏʙsᴄᴜʀᴇs ᴋɪʟʟ ᴄʀᴇᴅɪᴛ",
            "<#7E00FC>👁 <#6D6D6D>ʙʟɪɴᴅɴᴇss ᴏɴ ʀᴀɴɢᴇᴅ ᴀᴛᴛᴀᴄᴋs",
            "<#7E00FC>👁 <#6D6D6D>10ᴛʜ ʜɪᴛ ɢɪᴠᴇs ʙʟɪɴᴅɴᴇss",
            "<#5800B0>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#7E00FC>👁 <#6D6D6D>ᴄʀᴇᴀᴛᴇs ᴀ sᴍᴏᴋᴇ sᴄʀᴇᴇɴ",
            "<#7E00FC>👁 <#6D6D6D>ɢɪᴠᴇs ᴛʀᴜᴇ ɪɴᴠɪs ᴛᴏ ʏᴏᴜ ᴀɴᴅ ᴛʀᴜsᴛᴇᴅ ᴛᴇᴀᴍᴍᴀᴛᴇs",
            "<#7E00FC>👁 <#6D6D6D>sᴍᴏᴋᴇ ʀᴀᴅɪᴜs ɪɴᴄʀᴇᴀsᴇs ᴡɪᴛʜ ᴍᴏʀᴇ ᴛᴇᴀᴍᴍᴀᴛᴇs"
        )),
        Map.entry("ocean", List.of(
            "<#003EB0>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#005AFC>🌊 <#6D6D6D>ᴅᴏʟᴘʜɪɴ's ɢʀᴀᴄᴇ ᴀɴᴅ ᴡᴀᴛᴇʀ ʙʀᴇᴀᴛʜɪɴɢ",
            "<#005AFC>🌊 <#6D6D6D>ᴅʀᴏᴡɴ ɴᴇᴀʀʙʏ ᴇɴᴇᴍɪᴇs",
            "<#005AFC>🌊 <#6D6D6D>sᴛᴀɴᴅɪɴɢ ɪɴ ʀᴀɪɴ ɪɴᴄʀᴇᴀsᴇs ᴅʀᴏᴡɴɪɴɢ ᴅᴀᴍᴀɢᴇ",
            "<#005AFC>🌊 <#6D6D6D>ɴᴇᴀʀʙʏ ᴛᴇᴀᴍᴍᴀᴛᴇs ɢᴇᴛ ᴡᴀᴛᴇʀ ʙʀᴇᴀᴛʜɪɴɢ",
            "<#005AFC>🌊 <#6D6D6D>ᴛʀɪᴅᴇɴᴛs ʜᴀᴠᴇ ʟᴏʏᴀʟᴛʏ 5 ᴀɴᴅ ᴘᴜʟʟ ᴇɴᴇᴍɪᴇs",
            "<#003EB0>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#005AFC>🌊 <#6D6D6D>ᴄʀᴇᴀᴛᴇs ᴀ ᴡʜɪʀʟᴘᴏᴏʟ ᴛʜᴀᴛ ᴘᴜʟʟs ɪɴ ɴᴇᴀʀʙʏ ᴇɴᴇᴍɪᴇs"
        )),
        Map.entry("regeneration", List.of(
            "<#B0009A>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#FC00DD>+ <#6D6D6D>ɴᴏ ʜᴜɴɢᴇʀ ʟᴏss",
            "<#FC00DD>+ <#6D6D6D>ᴘᴇʀᴍᴀɴᴇɴᴛ ʀᴇɢᴇɴᴇʀᴀᴛɪᴏɴ",
            "<#FC00DD>+ <#6D6D6D>ᴇᴠᴇʀʏ ʜɪᴛ ɢɪᴠᴇs ʀᴇɢᴇɴᴇʀᴀᴛɪᴏɴ 2 ғᴏʀ 3 s",
            "<#FC00DD>+ <#6D6D6D>ᴀʟʟ ғᴏᴏᴅ ɢɪᴠᴇs +3 sᴀᴛᴜʀᴀᴛɪᴏɴ ʙᴀʀs",
            "<#FC00DD>+ <#6D6D6D>ᴇᴀᴛ ᴀɴʏᴛɪᴍᴇ",
            "<#FC00DD>+ <#6D6D6D>10ᴛʜ ʜɪᴛ ᴛᴀᴋᴇs ᴀᴡᴀʏ 1 ʜᴜɴɢᴇʀ ʙᴀʀ ғʀᴏᴍ ᴛᴀʀɢᴇᴛ",
            "<#B0009A>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#FC00DD>+ <#6D6D6D>ᴅᴀᴍᴀɢᴇ ᴅᴇᴀʟᴛ ʜᴇᴀʟs ʏᴏᴜ ᴀɴᴅ ᴛᴇᴀᴍᴍᴀᴛᴇs"
        )),
        Map.entry("speed", List.of(
            "<#b5883a>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#d1a44b>⏩ <#6D6D6D>+0.2x ᴍᴏᴠᴇᴍᴇɴᴛ sᴘᴇᴇᴅ",
            "<#d1a44b>⏩ <#6D6D6D>ᴇᴀᴄʜ ʜɪᴛ ɪɴᴄʀᴇᴀsᴇs sᴘᴇᴇᴅ",
            "<#d1a44b>⏩ <#6D6D6D>ɢᴇᴛᴛɪɴɢ ʜɪᴛ ʟᴏᴡᴇʀs sᴘᴇᴇᴅ",
            "<#d1a44b>⏩ <#6D6D6D>ɪᴍᴘʀᴏᴠᴇᴅ ᴘʀᴏᴊᴇᴄᴛɪʟᴇ sᴘᴇᴇᴅ, ᴀᴄᴄᴜʀᴀᴄʏ ᴀɴᴅ ʀᴀɴɢᴇ",
            "<#b5883a>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#d1a44b>⏩ <#6D6D6D>ᴅᴀsʜ ғᴏʀᴡᴀʀᴅ ᴅᴇᴀʟɪɴɢ ᴅᴀᴍᴀɢᴇ"
        )),
        Map.entry("strength", List.of(
            "<#6F1616>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#9E2020>🛡 <#6D6D6D>sʜɪᴇʟᴅ ᴅɪsᴀʙʟᴇs ᴀʀᴇ 2x ʟᴏɴɢᴇʀ",
            "<#9E2020>🛡 <#6D6D6D>ᴅᴀᴍᴀɢᴇs sʜɪᴇʟᴅs ʙʏ 20 ᴡʜᴇɴ ᴅɪsᴀʙʟɪɴɢ",
            "<#9E2020>🛡 <#6D6D6D>0.5x ᴅᴀᴍᴀɢᴇ ᴘᴇɴᴇᴛʀᴀᴛᴇs ᴡʜᴇɴ ᴅɪsᴀʙʟɪɴɢ sʜɪᴇʟᴅs",
            "<#9E2020>🛡 <#6D6D6D>2x ᴅᴀᴍᴀɢᴇ ᴛᴏ ᴀʟʟ ᴍᴏʙs",
            "<#9E2020>🛡 <#6D6D6D>+0.3 ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ ғᴏʀ ᴇᴀᴄʜ 1 ʜᴘ ʟᴏsᴛ",
            "<#9E2020>🛡 <#6D6D6D>ᴘɪᴇʀᴄɪɴɢ ᴏɴ ʀᴀɴɢᴇᴅ ᴀᴛᴛᴀᴄᴋs",
            "<#6F1616>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#9E2020>🛡 <#6D6D6D>ᴀʟʟ ᴀᴛᴛᴀᴄᴋs ᴀʀᴇ ᴄʀɪᴛɪᴄᴀʟ"
        )),
        Map.entry("ender", List.of(
            "<#831177>ᴘᴀꜱꜱɪᴠᴇ ᴇғғᴇᴄᴛs:",
            "<#541351>⭐ <#6D6D6D>ᴀʟʟ ɴᴇᴀʀʙʏ ᴜɴᴛʀᴜsᴛᴇᴅ ᴘʟᴀʏᴇʀs ʜᴀᴠᴇ ɢʟᴏᴡɪɴɢ",
            "<#541351>⭐ <#6D6D6D>ᴜsᴇ ᴅʀᴀɢᴏɴ's ʙʀᴇᴀᴛʜ ᴛᴏ sʜᴏᴏᴛ ᴘᴏᴡᴇʀғᴜʟ ғɪʀᴇʙᴀʟʟs ᴛʜᴀᴛ ᴄᴜʀsᴇ ᴘʟᴀʏᴇʀs",
            "<#541351>⭐ <#6D6D6D>ᴄᴜʀsᴇ ᴜɴᴛʀᴜsᴛᴇᴅ ᴘʟᴀʏᴇʀs ᴏɴ ʜɪᴛ ᴛᴏ sʜᴀʀᴇ ᴅᴀᴍᴀɢᴇ",
            "<#831177>ꜱᴘᴀʀᴋ ᴇғғᴇᴄᴛs:",
            "<#541351>⭐ <#6D6D6D>ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ᴛʜᴇ ᴄᴜʀsᴏʀ ᴡɪᴛʜɪɴ 15 ʙʟᴏᴄᴋs",
            "<#541351>⭐ <#6D6D6D>ɪɴsᴛᴀɴᴛʟʏ ᴋɪʟʟs ɴᴇᴀʀʙʏ ᴍᴏʙs ᴀɴᴅ ᴄᴜʀsᴇs ᴘʟᴀʏᴇʀs",
            "<#541351>⭐ <#6D6D6D>ᴄᴜʀsɪɴɢ ᴀ ᴘʟᴀʏᴇʀ ʀᴇsᴇᴛs sᴘᴀʀᴋ ᴄᴏᴏʟᴅᴏᴡɴ"
        ))
    );
    public InfuseItems(JavaPlugin plugin){this.plugin=plugin;effectKey=new NamespacedKey(plugin,"infuse_effect");augmentedKey=new NamespacedKey(plugin,"infuse_augmented");}
    public ItemStack create(String effect,boolean augmented,boolean recipeInfo){
        effect=effect.toLowerCase(Locale.ROOT);
        if(!IDS.containsKey(effect))return null;
        Material configured=Material.POTION;
        if(configured==null||configured!=Material.POTION){if(configured!=null)plugin.getLogger().warning("items.material must be POTION for Infuse resource-pack models; using POTION.");configured=Material.POTION;}
        ItemStack item=new ItemStack(configured);PotionMeta meta=(PotionMeta)item.getItemMeta();
        String color=COLORS.get(effect);
        meta.setColor(Color.fromRGB(Integer.parseInt(color.substring(1,3),16),Integer.parseInt(color.substring(3,5),16),Integer.parseInt(color.substring(5,7),16)));
        int modelBase=414001;
        int augmentedOffset=100;
        meta.setCustomModelData(modelBase+IDS.get(effect)-1+(augmented?augmentedOffset:0));
        String display=NAMES.get(effect);
        meta.displayName(mm.deserialize("<"+color+">"+display+" ᴇғғᴇᴄᴛ").decoration(TextDecoration.ITALIC,false));
        List<net.kyori.adventure.text.Component> lore=new ArrayList<>();
        lore.add(mm.deserialize("<dark_gray>ɪɴғᴜsᴇ s3").decoration(TextDecoration.ITALIC,false));
        for(String line:LORE.getOrDefault(effect,List.of()))lore.add(mm.deserialize(line).decoration(TextDecoration.ITALIC,false));
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
