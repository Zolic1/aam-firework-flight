package dev.aam.flight.item;

import dev.aam.flight.AamConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品属性（存在弩的 custom_data 里喵）：
 *   b_&lt;option&gt;            = 基础值覆盖
 *   aam_att/&lt;option&gt;/&lt;id&gt; = 属性叠加
 *
 * 最终值 = (有 b_ 覆盖 ? 覆盖值 : 全局值) + Σ att
 */
public final class ItemStats {

    public static final String ATTR_ROOT = "aam_att";

    private ItemStats() {}

    private static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void write(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        rebuildLore(stack);
    }

    // ==================== base ====================

    public static boolean hasBase(ItemStack stack, String option) {
        return tag(stack).contains("b_" + option);
    }

    public static double getBase(ItemStack stack, String option) {
        CompoundTag t = tag(stack);
        String key = "b_" + option;
        return t.contains(key) ? num(t, key) : AamConfig.global(option);
    }

    public static void setBase(ItemStack stack, String option, double value) {
        CompoundTag t = tag(stack);
        t.putDouble("b_" + option, value);
        write(stack, t);
    }

    public static void clearBase(ItemStack stack, String option) {
        CompoundTag t = tag(stack);
        t.remove("b_" + option);
        write(stack, t);
    }

    // ==================== att ====================

    public static Map<String, Double> attrs(ItemStack stack, String option) {
        Map<String, Double> out = new LinkedHashMap<>();
        CompoundTag t = tag(stack);
        if (!t.contains(ATTR_ROOT)) return out;
        CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
        if (!root.contains(option)) return out;
        CompoundTag opt = root.getCompound(option).orElse(new CompoundTag());
        for (String key : opt.keySet()) {
            out.put(key, num(opt, key));
        }
        return out;
    }

    public static boolean hasAttr(ItemStack stack, String option, String id) {
        return attrs(stack, option).containsKey(id);
    }

    public static void setAttr(ItemStack stack, String option, String id, double value) {
        CompoundTag t = tag(stack);
        CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
        CompoundTag opt = root.getCompound(option).orElse(new CompoundTag());
        opt.putDouble(id, value);
        root.put(option, opt);
        t.put(ATTR_ROOT, root);
        write(stack, t);
    }

    public static void removeAttr(ItemStack stack, String option, String id) {
        CompoundTag t = tag(stack);
        CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
        CompoundTag opt = root.getCompound(option).orElse(new CompoundTag());
        opt.remove(id);
        root.put(option, opt);
        t.put(ATTR_ROOT, root);
        write(stack, t);
    }

    /** 清掉某选项的全部 att（互斥选项用喵） */
    public static void clearAttrs(ItemStack stack, String option) {
        CompoundTag t = tag(stack);
        CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
        root.remove(option);
        t.put(ATTR_ROOT, root);
        write(stack, t);
    }
    public static void clearOption(ItemStack stack, String option) {
        CompoundTag t = tag(stack);
        t.remove("b_" + option);
        CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
        root.remove(option);
        t.put(ATTR_ROOT, root);
        write(stack, t);
    }

    public static void clearAll(ItemStack stack) {
        CompoundTag t = tag(stack);
        List<String> keys = new ArrayList<>(t.keySet());
        for (String k : keys) {
            if (k.startsWith("b_")) t.remove(k);
        }
        t.remove(ATTR_ROOT);
        write(stack, t);
    }

    // ==================== 最终值喵 ====================

    public static double effective(ItemStack stack, String option) {
        double base = getBase(stack, option);
        for (double v : attrs(stack, option).values()) {
            base += v;
        }
        return base;
    }

    public static int effectiveInt(ItemStack stack, String option) {
        return (int) Math.round(effective(stack, option));
    }

    public static float effectiveFloat(ItemStack stack, String option) {
        return (float) effective(stack, option);
    }

    // ==================== Lore 喵 ====================

    private static final Map<String, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put("lmg_dmg", "伤害");
        NAMES.put("lmg_range", "射击距离");
        NAMES.put("lmg_pierce", "穿透");
        NAMES.put("lmg_rps", "射速");
        NAMES.put("lmg_bspeed", "子弹速度");
        NAMES.put("lmg_mag", "弹夹容量");
        NAMES.put("lmg_reload", "换弹时间");
        NAMES.put("lmg_cost", "换弹消耗");
        NAMES.put("lmg_spread", "弹道偏移");
        NAMES.put("lmg_pellets", "弹丸数");
        NAMES.put("lmg_rate", "冷却");
        NAMES.put("msalvo", "导弹齐射数");
        NAMES.put("rsalvo", "火箭齐射数");
        NAMES.put("speed", "推进速度");
        NAMES.put("thruster_accel", "推进加速");
        NAMES.put("mpower", "爆炸当量");
        NAMES.put("mstart", "出膛速度");
        NAMES.put("maccelt", "加速时间");
        NAMES.put("mseekdelay", "索敌延迟");
        NAMES.put("mturn", "转向灵活");
        NAMES.put("mlead", "提前量");
        NAMES.put("mfuse", "引信半径");
        NAMES.put("mlife", "导弹寿命");
        NAMES.put("mseek", "索敌距离");
        NAMES.put("mspeed", "导弹速度");
        NAMES.put("lockrange", "锁定距离");
        NAMES.put("cone", "锁定锥角");
        NAMES.put("hold", "锁定时间");
        NAMES.put("outview", "脱锁时间");
        NAMES.put("radarrange", "雷达距离");
        NAMES.put("sc_hit", "命中得分");
    }

    public static String displayName(String option) {
        return NAMES.getOrDefault(option, option);
    }

    /** 重建 lore：无斜体、带颜色、格式 ===属性=== / [ID] ±xxx 属性名 喵 */
    public static void rebuildLore(ItemStack stack) {
        CompoundTag t = tag(stack);

        List<String> options = new ArrayList<>();
        for (String key : t.keySet()) {
            if (key.startsWith("b_")) options.add(key.substring(2));
        }
        if (t.contains(ATTR_ROOT)) {
            CompoundTag root = t.getCompound(ATTR_ROOT).orElse(new CompoundTag());
            for (String key : root.keySet()) {
                if (!options.contains(key)) options.add(key);
            }
        }

        if (options.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(List.of()));
            return;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(plain("===属性===", ChatFormatting.DARK_GRAY));

        for (String option : options) {
            if (hasBaseDirect(t, option)) {
                double baseVal = num(t, "b_" + option);
                lines.add(line("基础", baseVal, displayName(option)));
            }
            for (Map.Entry<String, Double> e : attrs(stack, option).entrySet()) {
                lines.add(line(e.getKey(), e.getValue(), displayName(option)));
            }
        }

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    /** 数值读取容错：double / int 都能读喵 */
    private static double num(CompoundTag t, String key) {
        var d = t.getDouble(key);
        if (d.isPresent()) return d.get();
        var i = t.getInt(key);
        if (i.isPresent()) return i.get();
        return 0.0;
    }

    /** 手持时若标记了 lore 待刷新 → 重建喵 */
    public static void ensureLore(ItemStack stack) {
        CompoundTag t = tag(stack);
        if (!t.contains("aam_lore_stale")) return;
        t.remove("aam_lore_stale");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        rebuildLore(stack);
    }

    private static boolean hasBaseDirect(CompoundTag t, String option) {
        return t.contains("b_" + option);
    }

    /** [ID] ±xxx 属性名 —— 全部强制非斜体喵 */
    private static Component line(String id, double value, String name) {
        String val = (value >= 0 ? "+" : "") + trim(value);
        ChatFormatting color = value >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

        Component head = plain("[" + id + "] ", ChatFormatting.DARK_GRAY);
        Component body = plain(val, color);
        Component tail = plain(" " + name, ChatFormatting.GRAY);
        return head.copy().append(body).append(tail);
    }

    /** 无斜体的文本喵 */
    private static Component plain(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(s -> s.withColor(color).withItalic(false));
    }

    private static String trim(double v) {
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.valueOf(Math.round(v * 100.0) / 100.0);
    }
}
