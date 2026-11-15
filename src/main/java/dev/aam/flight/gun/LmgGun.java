package dev.aam.flight.gun;

import dev.aam.flight.AamConfig;
import dev.aam.flight.ModItems;
import dev.aam.flight.item.ItemStats;
import dev.aam.flight.score.Scores;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 轻机枪（真·物品枪喵）：
 *  - 手持即用：按住右键 15 发/秒，子弹 280 格/秒
 *  - 弹夹 1200 发（存在物品 NBT 里）
 *  - 弹尽 → 3.2 秒换弹，消耗角色分数 1200 分
 *  - 弹药物品在背包里自动兑换成分数（1 个 = 1 分喵）
 *  - 活动栏显示「弹药 / 分数」
 */
public final class LmgGun {

    private static final ResourceKey<Enchantment> LMG_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("lmg", "lmg"));
    private static final ResourceKey<Enchantment> TRACER_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("tracer", "tracer"));

    private static final String TAG_MAG = "aam_mag";

    private static final Map<UUID, Double> TIMER = new HashMap<>();
    private static final Map<UUID, Integer> RELOAD_PENDING = new HashMap<>();
    private static final java.util.Set<UUID> RELOADING = new java.util.HashSet<>();
    private static final Map<UUID, Long> RELOAD_END = new HashMap<>();
    private static final Map<UUID, Long> LAST_SHOT = new HashMap<>();
    private static final Map<UUID, Double> CHARGE = new HashMap<>();
    private static final Map<UUID, Long> NEXT_SHOT = new HashMap<>();
    private static final Map<UUID, Integer> SHOTS = new HashMap<>();

    private LmgGun() {}

    public static int lmgLevel(Level level, ItemStack stack) {
        return enchLevel(level, stack, LMG_KEY);
    }

    public static int tracerLevel(Level level, ItemStack stack) {
        return enchLevel(level, stack, TRACER_KEY);
    }

    private static int enchLevel(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var holder = lookup.get(key);
        if (holder.isEmpty()) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(holder.get(), stack);
    }

    /** 这把物品是不是「枪」喵 */
    public static boolean isGun(Level level, ItemStack stack) {
        return stack.is(ModItems.LMG_GUN) || lmgLevel(level, stack) > 0;
    }

    // ==================== 弹夹存取喵 ====================

    public static int getMag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return ItemStats.effectiveInt(stack, "lmg_mag");
        CompoundTag tag = data.copyTag();
        return tag.contains(TAG_MAG) ? tag.getInt(TAG_MAG).orElse((int) AamConfig.lmgMagazine)
                : (int) AamConfig.lmgMagazine;
    }

    public static void setMag(ItemStack stack, int value) {
        int v = Math.max(0, Math.min(100000, value));
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        tag.putInt(TAG_MAG, v);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ==================== 每 tick 驱动喵 ====================

    public static void tickHeld(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!isGun(player.level(), stack)) {
            RELOAD_PENDING.remove(player.getUUID());
            return;
        }

        ItemStats.ensureLore(stack);

        long now = player.level().getGameTime();
        int mag = getMag(stack);

        // ---- 换弹中：活动栏显示倒计时喵（不再用物品冷却）----
        if (RELOADING.contains(player.getUUID())) {
            long end = RELOAD_END.getOrDefault(player.getUUID(), now);
            if (now < end) {
                if (now % Math.max(1, dev.aam.flight.AamConfig.hudInterval) == 0) {
                    int total = Math.max(1, (int) ItemStats.effective(stack, "lmg_reload"));
                    double done = 1.0 - Math.min(1.0, (double) (end - now) / total);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "§e换弹中 " + bar(done) + " §7" + String.format("%.1f", (end - now) / 20.0) + "s"), true);
                }
                return;
            }
            // 换弹完成 → 装填喵
            RELOADING.remove(player.getUUID());
            RELOAD_END.remove(player.getUUID());
            Integer pending = RELOAD_PENDING.remove(player.getUUID());
            if (pending != null) {
                setMag(stack, pending);
                mag = pending;
                ((ServerLevel) player.level()).playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
        }

        // ---- 弹尽：扣分数 + 开始换弹喵 ----
        if (mag <= 0) {
            if (now % 4 == 0) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§c弹夹空了" + (Scores.get(player) <= 0 ? "（无分数喵）" : "")), true);
            }
            if (Scores.get(player) > 0) {
                int got = Scores.spendUpTo(player, (int) ItemStats.effective(stack, "lmg_cost"));
                RELOAD_PENDING.put(player.getUUID(), got);
                RELOADING.add(player.getUUID());
                RELOAD_END.put(player.getUUID(),
                        now + Math.max(2, (int) ItemStats.effective(stack, "lmg_reload")));
            }
            return;
        }

        // ---- 蓄力条：满 1 条才能发射喵 ----
        int bars = Math.max(1, ItemStats.effectiveInt(stack, "lmg_bars"));
        double rps = Math.max(0.05, ItemStats.effective(stack, "lmg_rps"));
        double fillPerTick = rps / 20.0;                       // 每 tick 充能喵
        double charge = Math.min(bars, CHARGE.getOrDefault(player.getUUID(), bars * 1.0) + fillPerTick);
        double fireInterval = Math.max(1.0, 20.0 / rps);

        // ---- 射击：条满 + 过了连发间隔才能打喵 ----
        if (FireState.isFiring(player)) {
            long next = NEXT_SHOT.getOrDefault(player.getUUID(), 0L);
            if (charge >= 1.0 && now >= next && getMag(stack) > 0) {
                if (shoot(player, stack, Math.max(1, lmgLevel(player.level(), stack)))) {
                    setMag(stack, getMag(stack) - 1);
                    charge -= 1.0;                             // 消耗一条喵
                    LAST_SHOT.put(player.getUUID(), now);
                    // 多连发武器：两发之间 0.2 秒（可调喵）
                    NEXT_SHOT.put(player.getUUID(),
                            now + (bars > 1 ? Math.max(0, (int) ItemStats.effective(stack, "lmg_lockout")) : 0));
                }
            }
        }
        CHARGE.put(player.getUUID(), Math.max(0.0, Math.min(bars, charge)));

        // ---- 活动栏（多条进度条喵）----
        if (now % Math.max(1, dev.aam.flight.AamConfig.hudInterval) == 0) {
            StringBuilder barText = new StringBuilder();
            double c = CHARGE.getOrDefault(player.getUUID(), 0.0);
            for (int i = 0; i < bars; i++) {
                if (i > 0) barText.append(' ');
                barText.append(bar(Math.max(0.0, Math.min(1.0, c - i))));
            }
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§a弹药 " + getMag(stack) + "§7/" + ItemStats.effectiveInt(stack, "lmg_mag")
                            + "   §e分数 " + Scores.get(player)
                            + "   §b" + barText), true);
        }
    }

    /** 按右键不再加信用（改由蓄力条控制喵） */
    public static void onPress(ServerPlayer player) {
        // 空实现：条满才发射喵
    }
    /** 十格进度条喵 */
    private static String bar(double p) {
        int n = (int) Math.round(Math.max(0.0, Math.min(1.0, p)) * 10);
        return "█".repeat(n) + "░".repeat(10 - n);
    }

    /** 是否正在换弹喵（右键拦截用） */
    public static boolean isReloading(ServerPlayer player) {
        return RELOADING.contains(player.getUUID());
    }

    /** 背包里的弹药物品自动兑换成分数（1:1）喵 */
    public static void absorbAmmo(ServerPlayer player) {
        var inv = player.getInventory();
        int converted = 0;
        for (int i = 0; i < inv.getContainerSize() && converted < Math.max(1, (int) dev.aam.flight.AamConfig.ammoRate); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.AMMO)) {
                int n = s.getCount();
                s.setCount(0);
                converted += n;
            }
        }
        if (converted > 0) {
            Scores.add(player, converted);
        }
    }

    public static void reset(ServerPlayer player) {
        TIMER.remove(player.getUUID());
    }

    // ==================== 开火喵 ====================

    private static boolean shoot(ServerPlayer player, ItemStack stack, int lmgLevel) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye = player.getEyePosition().add(0.0, -0.08, 0.0);
        Vec3 dir = player.getLookAngle().normalize();

        // 子弹类型喵（1~5）
        BulletType bulletType = BulletType.of(ItemStats.effectiveInt(stack, "lmg_bullet_type"));

        // 尾迹间隔：类型自带；lmg_tracer > 0 时手动覆盖喵
        int overrideEvery = ItemStats.effectiveInt(stack, "lmg_tracer");
        int tracerEvery = overrideEvery > 0 ? overrideEvery : bulletType.tracerEvery();
        if (alwaysTracer(stack) && tracerEvery == 0) tracerEvery = 1;   // 特殊枪每发都有喵
        int shotNo = SHOTS.merge(player.getUUID(), 1, Integer::sum);
        if (tracerEvery > 0 && (shotNo % tracerEvery) != 0) tracerEvery = 0;

        // 榴弹发射器：打出去的是模组导弹喵
        if (isLauncher(stack)) {
            var missile = new dev.aam.flight.AamMissile(dev.aam.flight.ModEntities.AAM_MISSILE, level);
            missile.setOwner(player);
            missile.setFromStack(stack);
            missile.setPos(eye.x, eye.y, eye.z);
            missile.setDeltaMovement(dir.scale(2.0));
            level.addFreshEntity(missile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.6f);
            return true;
        }

        double spread = ItemStats.effective(stack, "lmg_spread");
        int pellets = Math.max(1, ItemStats.effectiveInt(stack, "lmg_pellets"));

        double speedPerTick = ItemStats.effective(stack, "lmg_bspeed") / 20.0;
        float damage = ItemStats.effectiveFloat(stack, "lmg_dmg");
        double range = ItemStats.effective(stack, "lmg_range");
        int pierce = Math.max(1, ItemStats.effectiveInt(stack, "lmg_pierce"));

        // 单次发射 N 发，每发独立算偏移喵
        for (int i = 0; i < pellets; i++) {
            Vec3 pelletDir = spread > 0.0 ? applySpread(dir, spread, player.getRandom()) : dir;
            BulletManager.spawn(level, player, eye, pelletDir,
                    speedPerTick, damage, range, pierce,
                    tracerEvery, bulletType.tracerParticle(),
                    bulletType.igniteChance(), bulletType.armorPen(),
                    ItemStats.effectiveInt(stack, "lmg_freeze"),
                    ItemStats.effective(stack, "lmg_poison"),
                    ItemStats.effective(stack, "lmg_chain"),
                    ItemStats.effectiveInt(stack, "lmg_chain_n"),
                    ItemStats.effective(stack, "lmg_boom"),
                    ItemStats.effective(stack, "lmg_homing"));
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.7f, 1.25f);
        return true;
    }

        /** 这三把枪每发都带尾迹喵 */
    private static boolean alwaysTracer(ItemStack stack) {
        Identifier model = stack.get(net.minecraft.core.component.DataComponents.ITEM_MODEL);
        if (model == null) return true;
        String path = model.getPath();
        return path.startsWith("sniper_rifle_3d")
                || path.startsWith("shotgun_3d")
                || path.startsWith("grenade_launcher_3d");
    }

    /** 是不是榴弹发射器喵 */
    public static boolean isLauncher(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean("aam_launcher").orElse(false);
    }

    /** 给弹道加随机偏移（±spreadDeg 度喵） */
    private static Vec3 applySpread(Vec3 dir, double spreadDeg, net.minecraft.util.RandomSource random) {
        double yawOff = (random.nextDouble() * 2.0 - 1.0) * spreadDeg;
        double pitchOff = (random.nextDouble() * 2.0 - 1.0) * spreadDeg;

        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = dir.cross(up);
        if (right.lengthSqr() < 1.0E-6) right = new Vec3(1.0, 0.0, 0.0);
        right = right.normalize();
        Vec3 realUp = right.cross(dir).normalize();

        double ty = Math.tan(Math.toRadians(yawOff));
        double tp = Math.tan(Math.toRadians(pitchOff));
        return dir.add(right.scale(ty)).add(realUp.scale(tp)).normalize();
    }
}
