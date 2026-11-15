package dev.aam.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

/**
 * 视线锥形索敌（参数来自 AamConfig 喵）：
 *  - 准星锥角内、最接近准星的目标
 *  - 必须有视线（实心方块会挡住；玻璃/水不挡）
 */
public final class LockOn {

    private LockOn() {}

    public static double range() {
        return AamConfig.lockRange;
    }

    private static double coneCos() {
        return Math.cos(Math.toRadians(AamConfig.lockCone));
    }

    public static LivingEntity find(Level level, Entity viewer, double range) {
        return find(level, viewer, range, e -> true);
    }

    public static LivingEntity find(Level level, Entity viewer, double range, Predicate<LivingEntity> extra) {
        Vec3 eye = viewer.getEyePosition();
        Vec3 look = viewer.getLookAngle();

        AABB box = viewer.getBoundingBox().inflate(range);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != viewer && e.isAlive() && !e.isSpectator() && !e.isRemoved() && extra.test(e));

        LivingEntity best = null;
        double bestCos = coneCos();

        for (LivingEntity e : candidates) {
            Vec3 d = e.getBoundingBox().getCenter().subtract(eye);
            double len = d.length();
            if (len < 0.05 || len > range) continue;

            double cos = d.dot(look) / len;
            if (cos <= bestCos) continue;                 // 不够靠近准星喵
            if (!canSee(level, eye, e, viewer)) continue;  // 被挡住喵

            bestCos = cos;
            best = e;
        }
        return best;
    }

    /** 准星上最近的敌方导弹喵（用于锁定拦截喵） */
    public static AamMissile findMissile(Level level, Entity viewer, double range, Predicate<AamMissile> extra) {
        Vec3 eye = viewer.getEyePosition();
        Vec3 look = viewer.getLookAngle();

        AABB box = viewer.getBoundingBox().inflate(range);
        List<AamMissile> candidates = level.getEntitiesOfClass(AamMissile.class, box,
                m -> m != viewer && m.isAlive() && !m.isRemoved() && extra.test(m));

        AamMissile best = null;
        double bestCos = coneCos();
        for (AamMissile m : candidates) {
            Vec3 d = m.getBoundingBox().getCenter().subtract(eye);
            double len = d.length();
            if (len < 0.05 || len > range) continue;
            double cos = d.dot(look) / len;
            if (cos <= bestCos) continue;
            if (!canSee(level, eye, m, viewer)) continue;
            bestCos = cos;
            best = m;
        }
        return best;
    }
    /** 找一个「在身后」的导弹（优先正锁着你的喵） */
    public static AamMissile findMissileBehind(Level level, Entity viewer, double range) {
        Vec3 eye = viewer.getEyePosition();
        Vec3 look = viewer.getLookAngle();

        AABB box = viewer.getBoundingBox().inflate(range);
        List<AamMissile> candidates = level.getEntitiesOfClass(AamMissile.class, box,
                m -> m != viewer && m.isAlive() && !m.isRemoved() && !isOwn(viewer, m) && !m.isStealth());   // 背后锁不了隐蔽弹喵

        AamMissile best = null;
        double bestScore = Double.MAX_VALUE;
        for (AamMissile m : candidates) {
            Vec3 d = m.getBoundingBox().getCenter().subtract(eye);
            double len = d.length();
            if (len < 0.05 || len > range) continue;
            double cos = d.dot(look) / len;
            if (cos > -0.1) continue;                        // 必须在我身后喵
            // 正在锁着我的优先级最高喵
            double score = (viewer instanceof ServerPlayer sp && m.isTargeting(sp)) ? -1000.0 : 0.0;
            score += len;
            if (score < bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }

    private static boolean isOwn(Entity viewer, AamMissile m) {
        return m.getOwner() != null && m.getOwner().getUUID().equals(viewer.getUUID());
    }
    /** 从眼睛到目标中心是否有视线喵 */
    public static boolean canSee(Level level, Vec3 eye, Entity target, Entity viewer) {
        Vec3 center = target.getBoundingBox().getCenter();
        BlockHitResult hit = level.clip(new ClipContext(
                eye, center,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                viewer));

        if (hit.getType() != HitResult.Type.BLOCK) return true;

        BlockState state = level.getBlockState(hit.getBlockPos());
        return !state.canOcclude();     // 玻璃之类允许透视喵
    }
}
