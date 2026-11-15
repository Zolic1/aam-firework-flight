package dev.aam.flight.gun;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 轻量子弹（不是实体喵）
 *  普通弹：逐 tick 推进 + 射线检测喵
 *  激光弹：发射瞬间即完成命中判定（hitscan），画一条光束喵
 *  附带：冻结 / 毒素 / 链电 / 小爆炸 / 追踪 喵
 */
public final class BulletManager {

    public static final class Bullet {
        final ServerLevel level;
        final UUID ownerId;
        final Entity owner;
        Vec3 pos;
        Vec3 dir;                       // 追踪弹会改方向喵
        final double speedPerTick;
        final float damage;
        final double range;
        final int tracerEvery;
        final int tracerParticle;
        final double igniteChance;
        final double armorPen;
        final int freezeTicks;
        final double poisonSec;
        final double chainRadius;
        final int chainCount;
        final double boomPower;
        final double homingTurn;
        int pierceLeft;
        double traveled;

        Bullet(ServerLevel level, Entity owner, Vec3 pos, Vec3 dir, double speedPerTick,
               float damage, double range, int pierce,
               int tracerEvery, int tracerParticle, double igniteChance, double armorPen,
               int freezeTicks, double poisonSec, double chainRadius, int chainCount,
               double boomPower, double homingTurn) {
            this.level = level;
            this.owner = owner;
            this.ownerId = owner.getUUID();
            this.pos = pos;
            this.dir = dir;
            this.speedPerTick = speedPerTick;
            this.damage = damage;
            this.range = range;
            this.pierceLeft = pierce;
            this.tracerEvery = tracerEvery;
            this.tracerParticle = tracerParticle;
            this.igniteChance = igniteChance;
            this.armorPen = armorPen;
            this.freezeTicks = freezeTicks;
            this.poisonSec = poisonSec;
            this.chainRadius = chainRadius;
            this.chainCount = chainCount;
            this.boomPower = boomPower;
            this.homingTurn = homingTurn;
            this.traveled = 0;
        }
    }

    private static final List<Bullet> BULLETS = new ArrayList<>();

    private BulletManager() {}

    public static void spawn(ServerLevel level, Entity owner, Vec3 pos, Vec3 dir,
                             double speedPerTick, float damage, double range, int pierce,
                             int tracerEvery, int tracerParticle, double igniteChance, double armorPen,
                             int freezeTicks, double poisonSec, double chainRadius, int chainCount,
                             double boomPower, double homingTurn) {
        Bullet b = new Bullet(level, owner, pos, dir.normalize(), speedPerTick, damage, range, pierce,
                tracerEvery, tracerParticle, igniteChance, armorPen,
                freezeTicks, poisonSec, chainRadius, chainCount, boomPower, homingTurn);
        if (speedPerTick >= 900) {          // 激光：瞬间命中喵
            hitscan(b);
            return;
        }
        BULLETS.add(b);
    }

    /** 激光：一条线打到底，穿透范围内全部目标喵 */
    private static void hitscan(Bullet b) {
        Vec3 from = b.pos;
        Vec3 to = from.add(b.dir.scale(b.range));

        var blockHit = b.level.clip(new net.minecraft.world.level.ClipContext(from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, b.owner));
        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            to = blockHit.getLocation();
        }

        // 光束喵
        int steps = (int) Math.max(4, from.distanceTo(to) * 3);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            b.level.sendParticles(ParticleTypes.END_ROD,
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t,
                    from.z + (to.z - from.z) * t,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        AABB box = new AABB(from, to).inflate(0.6);
        List<LivingEntity> hits = b.level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.getUUID() != b.ownerId && e.isAlive() && !e.isSpectator());
        hits.sort(Comparator.comparingDouble(e -> e.distanceToSqr(from)));

        int left = b.pierceLeft;
        for (LivingEntity target : hits) {
            if (left-- <= 0) break;
            if (target.getBoundingBox().inflate(0.4).clip(from, to).isEmpty()) continue;
            applyHit(b, target, from);
        }
    }

    public static void tickAll() {
        if (BULLETS.isEmpty()) return;
        BULLETS.removeIf(b -> !tick(b));
    }

    /** @return false = 移除这颗子弹喵 */
    private static boolean tick(Bullet b) {
        if (b.level.isClientSide()) return false;

        // 追踪：朝最近目标转向喵
        if (b.homingTurn > 0.0) {
            LivingEntity near = b.level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(b.pos, b.pos).inflate(24.0),
                            e -> e.getUUID() != b.ownerId && e.isAlive() && !e.isSpectator())
                    .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(b.pos))).orElse(null);
            if (near != null) {
                Vec3 want = near.getEyePosition().subtract(b.pos).normalize();
                b.dir = b.dir.scale(1.0 - b.homingTurn).add(want.scale(b.homingTurn)).normalize();
            }
        }

        Vec3 from = b.pos;
        Vec3 to = from.add(b.dir.scale(b.speedPerTick));

        var blockHit = b.level.clip(new net.minecraft.world.level.ClipContext(from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, b.owner));
        boolean hitBlock = blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK;
        if (hitBlock) {
            to = blockHit.getLocation();
        }

        AABB box = new AABB(from, to).inflate(1.0);
        List<LivingEntity> hits = b.level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.getUUID() != b.ownerId && e.isAlive() && !e.isSpectator());
        hits.sort(Comparator.comparingDouble(e -> e.distanceToSqr(from)));

        for (LivingEntity target : hits) {
            if (b.pierceLeft <= 0) break;
            if (target.getBoundingBox().inflate(0.15).clip(from, to).isEmpty()) continue;
            applyHit(b, target, from);
            b.pierceLeft--;
        }

        // 也能打爆在飞的导弹喵
        for (dev.aam.flight.AamMissile m : b.level.getEntitiesOfClass(dev.aam.flight.AamMissile.class, box,
                m -> m.isAlive() && (m.getOwner() == null || !m.getOwner().getUUID().equals(b.ownerId)))) {
            if (m.getBoundingBox().inflate(0.25).clip(from, to).isEmpty()) continue;
            m.hurt(b.level.damageSources().generic(), Math.max(3.0f, b.damage));
        }

        if (b.tracerEvery > 0 && b.tracerParticle != BulletType.PARTICLE_NONE) {
            var particle = (b.tracerParticle == BulletType.PARTICLE_END_ROD)
                    ? ParticleTypes.END_ROD
                    : ParticleTypes.WHITE_ASH;
            int steps = (int) Math.max(2, from.distanceTo(to) * 2);
            for (int i = 1; i <= steps; i++) {
                double t = (double) i / steps;
                b.level.sendParticles(particle,
                        from.x + (to.x - from.x) * t,
                        from.y + (to.y - from.y) * t,
                        from.z + (to.z - from.z) * t,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        if (hitBlock) {
            b.level.sendParticles(ParticleTypes.CRIT, to.x, to.y, to.z, 5, 0.1, 0.1, 0.1, 0.04);
            b.level.sendParticles(ParticleTypes.SMOKE, to.x, to.y, to.z, 2, 0.05, 0.05, 0.05, 0.01);
            if (b.boomPower > 0.0) boom(b, to);
            return false;
        }

        b.traveled += from.distanceTo(to);
        b.pos = to;
        return b.traveled < b.range && b.pierceLeft > 0;
    }

    /** 伤害 + 各种附加效果喵 */
    private static void applyHit(Bullet b, LivingEntity target, Vec3 from) {
        target.invulnerableTime = 0;

        if (b.owner instanceof ServerPlayer sp) {
            target.hurt(b.level.damageSources().playerAttack(sp), b.damage);
        } else {
            target.hurt(b.level.damageSources().generic(), b.damage);
        }

        if (b.armorPen > 0.0) {
            double extra = b.damage * b.armorPen * (target.getArmorValue() / 20.0);
            if (extra > 0.05) {
                target.invulnerableTime = 0;
                target.hurt(b.level.damageSources().magic(), (float) extra);
            }
        }

        if (b.igniteChance > 0.0 && b.level.random.nextDouble() < b.igniteChance) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 60));
        }

        // 冻结喵
        if (b.freezeTicks > 0) {
            target.setTicksFrozen(target.getTicksFrozen() + b.freezeTicks);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, b.freezeTicks, 2));
            b.level.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    12, 0.2, 0.2, 0.2, 0.02);
        }

        // 毒素喵
        if (b.poisonSec > 0.0) {
            int ticks = (int) (b.poisonSec * 20);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, 1));
            b.level.sendParticles(ParticleTypes.ITEM_SLIME,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    6, 0.15, 0.15, 0.15, 0.02);
        }

        // 链电喵
        if (b.chainRadius > 0.0 && b.chainCount > 0) {
            List<LivingEntity> near = b.level.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(b.chainRadius),
                    e -> e != target && e.getUUID() != b.ownerId && e.isAlive() && !e.isSpectator());
            int n = 0;
            for (LivingEntity e : near) {
                if (n++ >= b.chainCount) break;
                e.invulnerableTime = 0;
                e.hurt(b.level.damageSources().magic(), (float) (b.damage * 0.6));
                b.level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(),
                        8, 0.2, 0.2, 0.2, 0.05);
            }
        }

        // 小爆炸喵
        if (b.boomPower > 0.0) boom(b, target.position().add(0.0, target.getBbHeight() * 0.5, 0.0));

        double kb = dev.aam.flight.AamConfig.lmgKnockback;
        target.push(b.dir.x * kb, 0.0, b.dir.z * kb);

        b.level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                6, 0.12, 0.12, 0.12, 0.05);
    }

    /** 子弹自带的小范围爆裂（不破坏方块喵） */
    private static void boom(Bullet b, Vec3 at) {
        double r = b.boomPower * 2.0;
        List<LivingEntity> victims = b.level.getEntitiesOfClass(LivingEntity.class,
                new AABB(at, at).inflate(r),
                e -> e.getUUID() != b.ownerId && e.isAlive() && !e.isSpectator());
        for (LivingEntity v : victims) {
            double d = v.position().distanceTo(at);
            double falloff = Math.max(0.0, 1.0 - d / r);
            float dmg = (float) (b.boomPower * 4.0 * (0.35 + 0.65 * falloff));
            if (dmg <= 0.1f) continue;
            v.invulnerableTime = 0;
            if (b.owner instanceof ServerPlayer sp) {
                v.hurt(b.level.damageSources().explosion(b.owner, sp), dmg);
            } else {
                v.hurt(b.level.damageSources().explosion(b.owner, b.owner), dmg);
            }
        }
        b.level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }
}
