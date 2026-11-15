package dev.aam.flight;

import dev.aam.flight.score.Scoring;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AAM 空对空导弹（模组实体版喵）
 *
 *  - 无重力、持续加速
 *  - 制导：射手锁定目标优先（不限距离）→ 附近锁定标签 → 附近玩家；带提前量 + 转向平滑
 *  - 碰撞：自己算射线（原版箭高速会插地穿模喵）
 *  - 爆炸：7.0 TNT 当量 + 大量火焰粒子喵
 */
public class AamMissile extends AbstractArrow implements net.minecraft.world.entity.projectile.ItemSupplier {



    // ---- 可由「发射物品属性」覆盖的参数喵 ----
    private double pMaxSpeed = AamConfig.missileMaxSpeed;
    private double pStartSpeed = AamConfig.missileStartSpeed;
    private double pAccelTicks = AamConfig.missileAccelTicks;
    private double pSeekDelay = AamConfig.missileSeekDelay;
    private double pTurnBlend = AamConfig.missileTurnBlend;
    private double pLead = AamConfig.missileLead;
    private double pPower = AamConfig.missilePower;
    private double pFuse = AamConfig.missileFuse;
    private int pLife = AamConfig.missileLife;
    private double pSeekRange = AamConfig.missileSeekRange;
    private double pFlareResist = AamConfig.missileFlareResist;   // 抗干扰 0~1 喵
    private boolean pStealth = false;                            // 隐蔽：RWR 不告警喵

    /** 从发射物品读取导弹参数（没有就沿用全局喵） */
    public void setFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        pMaxSpeed = dev.aam.flight.item.ItemStats.effective(stack, "mspeed");
        pStartSpeed = dev.aam.flight.item.ItemStats.effective(stack, "mstart");
        pAccelTicks = dev.aam.flight.item.ItemStats.effective(stack, "maccelt");
        pSeekDelay = dev.aam.flight.item.ItemStats.effective(stack, "mseekdelay");
        pTurnBlend = dev.aam.flight.item.ItemStats.effective(stack, "mturn");
        pLead = dev.aam.flight.item.ItemStats.effective(stack, "mlead");
        pPower = dev.aam.flight.item.ItemStats.effective(stack, "mpower");
        pFuse = dev.aam.flight.item.ItemStats.effective(stack, "mfuse");
        pLife = (int) dev.aam.flight.item.ItemStats.effective(stack, "mlife");
        pSeekRange = dev.aam.flight.item.ItemStats.effective(stack, "mseek");
        pFlareResist = dev.aam.flight.item.ItemStats.effective(stack, "mflare");
        pStealth = dev.aam.flight.item.ItemStats.effective(stack, "mstealth") > 0.5;
        retracks = (int) dev.aam.flight.item.ItemStats.effective(stack, "mretrack");
    }

    // ---- 重追踪参数喵 ----
    private static final double LOST_TURN_SCALE = 0.5;   // 越过目标后转向降到 50% 喵
    private static final int RETRACK_DELAY = 160;        // 8 秒后开始恢复喵
    private static final int RETRACK_RECOVER = 100;      // 5 秒线性提回来喵
    private int retracks = AamConfig.missileRetrack;     // 剩余重追踪次数喵
    private double turnScale = 1.0;
    private long turnRecoverAt = 0;
    private double lastDist = -1.0;
    private boolean hadTarget = false;
    private boolean approaching = false;
    private long nextRetrackTick = 0;

    /** 导弹血量：可被子弹/爆炸击毁喵 */
    public static final float MISSILE_HP = 12.0f;
    private float hp = MISSILE_HP;
    private boolean exploding = false;   // 防止连锁爆炸递归喵
    private boolean targetIsMissile = false;   // 我的目标是不是导弹喵
    private AamMissile targetMissileRef = null;   // 我锁定的那枚导弹喵

    /** 这枚导弹是不是正锁着某个实体喵 */
    public boolean isTargeting(Entity e) {
        return e != null && targetId != null && targetId.equals(e.getUUID());
    }

    /** 剩余寿命（tick 喵） */
    public int getRemainingTicks() {
        return Math.max(0, pLife - this.tickCount);
    }

    /** 机动中剩余的时间（秒喵） */
    public float getManeuverSeconds() {
        return getRemainingTicks() / 20.0f;
    }

    /** 距离它当前目标还有多远（没有目标就返回 -1 喵） */
    public double getTargetDistance() {
        if (targetMissileRef != null && targetMissileRef.isAlive()) {
            return this.distanceTo(targetMissileRef);
        }
        return -1.0;
    }

    /** 当前速度（格/秒喵） */
    public double getSpeedPerSecond() {
        return this.getDeltaMovement().length() * 20.0;
    }
    /** 抗干扰能力 0~1 喵 */
    public double getFlareResist() {
        return Math.max(0.0, Math.min(1.0, pFlareResist));
    }

    /** 是不是隐蔽弹（RWR 不告警喵） */
    public boolean isStealth() {
        return pStealth;
    }

    /** 被干扰：丢掉锁定，并短暂不再重新锁定喵 */
    public void breakLockByFlare(int ticks) {
        targetId = null;
        targetMissileRef = null;
        targetIsMissile = false;
        hadTarget = false;
        lastDist = -1.0;
        flareBlockTicks = Math.max(flareBlockTicks, ticks);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private int flareBlockTicks = 0;
    private int hitCooldown = 0;    // 穿过目标后的短暂无判定喵
    public float getMissileHp() {
        return Math.max(0.0f, hp);
    }

    /** 被打中：掉血，归零就解体喵 */
    @Override
    public boolean hurtServer(ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (serverLevel.isClientSide() || exploding) return false;
        Entity src = source.getEntity();
        // 不误伤自己射的弹喵
        if (src != null && getOwner() != null && src.getUUID().equals(getOwner().getUUID())) return false;
        hp -= amount;
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                getX(), getY(), getZ(), 6, 0.15, 0.15, 0.15, 0.05);
        if (hp <= 0.0f) {
            hp = 0.0f;
            explodeAndDie();
        }
        return true;
    }

    private UUID targetId;

    public AamMissile(EntityType<? extends AamMissile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setBaseDamage(8.0);
        this.pickup = Pickup.DISALLOWED;
    }

    /** 区块卸载时不保存 → 导弹直接消失喵 */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.MISSILE_DISPLAY);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            super.tick();
            return;
        }

        if (this.tickCount > pLife) {
            explodeAndDie();
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();

        // ---- 制导 ----
        // 发射 0.6 秒（可调）后才开始锁定喵
        if (flareBlockTicks > 0) flareBlockTicks--;
        Entity target = (this.tickCount >= (int) pSeekDelay && flareBlockTicks <= 0) ? resolveTarget() : null;

        // ---- 重追踪：必须「先接近再远离」才算一次越过喵 ----
        double targetDist = -1.0;
        if (target != null) {
            double dist = this.distanceTo(target);
            targetDist = dist;
            boolean closing = hadTarget && dist < lastDist - 0.15;      // 正在接近喵
            boolean receding = hadTarget && dist > lastDist + 0.5;      // 正在远离喵
            if (closing) {
                approaching = true;                                     // 重新武装判定喵
            }
            if (approaching && receding && retracks > 0
                    && this.tickCount >= nextRetrackTick
                    && lastDist < 60.0) {
                retracks--;
                approaching = false;                                    // 防止连续触发喵
                turnScale = LOST_TURN_SCALE;
                turnRecoverAt = this.tickCount + RETRACK_DELAY;         // 8 秒后才恢复喵
                nextRetrackTick = this.tickCount + RETRACK_DELAY + RETRACK_RECOVER;  // 13 秒内不再扣喵
            }
            lastDist = dist;
            hadTarget = true;
        } else {
            hadTarget = false;
            approaching = false;
        }

        // 转向速度恢复：8 秒后用 5 秒线性提回来喵
        if (this.tickCount >= turnRecoverAt && turnScale < 1.0) {
            double t = Math.min(1.0, (this.tickCount - turnRecoverAt) / (double) RETRACK_RECOVER);
            turnScale = LOST_TURN_SCALE + (1.0 - LOST_TURN_SCALE) * t;
        }

        // 重追踪次数用完 → 不再寻的喵
        if (retracks <= 0) {
            target = null;
        }

        Vec3 desired;
        if (target != null) {
            // 打导弹时不打提前量（导弹会自己转，提前量反而打偏喵）
            Vec3 aim = (target instanceof AamMissile) ? target.position().add(0.0, target.getBbHeight() * 0.5, 0.0)
                    : leadPoint(target, speed);
            Vec3 to = aim.subtract(this.position());
            desired = to.lengthSqr() > 1.0E-6 ? to.normalize() : (speed > 0.05 ? motion.normalize() : this.getLookAngle());
        } else if (speed > 0.05) {
            desired = motion.normalize();
        } else {
            desired = this.getLookAngle();
        }

        // ---- 转向平滑（受重追踪惩罚影响喵）----
        // 接近目标时转向加成（越近越灵活喵）
        double approachBoost = (targetDist > 0 && targetDist < 40.0)
                ? (1.0 + 0.6 * (1.0 - targetDist / 40.0))
                : 1.0;
        double effectiveTurn = Math.min(1.0, pTurnBlend * turnScale * approachBoost
                * ((target instanceof AamMissile) ? 1.6 : 1.0));   // 追导弹更灵活喵
        Vec3 dir = desired;
        if (speed > 0.05 && desired.lengthSqr() > 1.0E-6) {
            Vec3 cur = motion.normalize();
            Vec3 blended = cur.scale(1.0 - effectiveTurn).add(desired.scale(effectiveTurn));
            if (blended.lengthSqr() > 1.0E-6) {
                dir = blended.normalize();
            }
        }

        // 三段加速：0~2.45s → 80/s，2.45~7.45s → 120/s，7.45~15.45s → 最高速喵
        double newSpeed;
        int tt = this.tickCount;
        if (tt <= 49) {
            newSpeed = pStartSpeed + (4.0 - pStartSpeed) * (tt / 49.0);
        } else if (tt <= 149) {
            newSpeed = 4.0 + (6.0 - 4.0) * ((tt - 49) / 100.0);
        } else {
            newSpeed = 6.0 + (pMaxSpeed - 6.0) * Math.min(1.0, (tt - 149) / 160.0);
        }
        Vec3 step = dir.scale(newSpeed);

        // ---- 方块碰撞：自己扫射线喵 ----
        Vec3 from = this.position();
        Vec3 to = from.add(step);
        BlockHitResult blockHit = serverLevel.clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            explodeAndDie();
            return;
        }

        if (hitCooldown > 0) hitCooldown--;

        // ---- 近炸引信：90% 概率引爆，未引爆则直接穿过喵 ----
        if (hitCooldown <= 0 && target != null && target.isAlive()) {
            double fuse = Math.max(0.6, pFuse);
            if (this.distanceTo(target) <= fuse) {
                if (serverLevel.random.nextFloat() < 0.9f) {
                    explodeAndDie();
                } else {
                    passThroughTarget();
                }
                return;
            }
        }

        // ---- 命中判定：只对「锁定目标」生效，且必须真的擦到喵 ----
        if (target != null && target.isAlive()) {
            // 用本 tick 的移动线段和目标包围盒做精确相交（不再用大 fuse 膨胀喵）
            double pad = (target instanceof AamMissile) ? 0.8 : 0.25;   // 导弹目标小又快要放宽喵
            var hitClip = target.getBoundingBox().inflate(pad).clip(from, to);
            if (hitClip.isPresent() || this.getBoundingBox().intersects(target.getBoundingBox())
                    || this.distanceTo(target) < 1.6) {
                if (serverLevel.random.nextFloat() < 0.9f) {
                    explodeAndDie();
                } else {
                    passThroughTarget();
                }
                return;
            }
        }

        // ---- 移动 ----
        // ---- 尾迹：火焰粒子喵 ----
        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                4, 0.06, 0.06, 0.06, 0.01);
        serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(),
                1, 0.02, 0.02, 0.02, 0.005);

        this.setDeltaMovement(step);
        this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);

        double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.setYRot((float) (Math.atan2(dir.x, dir.z) * 180.0 / Math.PI) - 90.0f);
        this.setXRot((float) (-(Math.atan2(dir.y, horiz) * 180.0 / Math.PI)));
        this.setOldPosAndRot();

        this.tickCount++;   // 自己维护寿命喵
    }

    /** 提前量：预测目标未来位置喵 */
    private Vec3 leadPoint(Entity target, double speed) {
        Vec3 tpos = target.getBoundingBox().getCenter();
        double dist = tpos.distanceTo(this.position());
        double flightTicks = dist / Math.max(1.0, speed);
        return tpos.add(target.getDeltaMovement().scale(flightTicks * pLead));
    }

    /** 是不是自己人射的导弹喵 */
    private static boolean m_own(AamMissile m, Entity owner) {
        return owner != null && m.getOwner() != null && m.getOwner().getUUID().equals(owner.getUUID());
    }

    /** 未引爆：直接穿过目标，并按「越过未命中」结算喵 */
    private void passThroughTarget() {
        hitCooldown = 8;                 // 8 tick 内不再判定，避免连续抽奖喵
        if (retracks > 0) {
            retracks--;                  // 按越过未命中算：消耗一次重追踪喵
            turnScale = LOST_TURN_SCALE;
            turnRecoverAt = this.tickCount + RETRACK_DELAY;
            nextRetrackTick = this.tickCount + RETRACK_DELAY + RETRACK_RECOVER;
        }
        approaching = false;
        hadTarget = false;
        lastDist = -1.0;
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.1);
        }
    }
    private Entity resolveTarget() {
        if (!(level() instanceof ServerLevel serverLevel)) return null;

        if (targetId != null) {
            Entity e = serverLevel.getEntityInAnyDimension(targetId);
            if (e instanceof LivingEntity le && le.isAlive() && !le.isSpectator()) {
                return le;
            }
            targetId = null;
        }

        Entity owner = this.getOwner();

        // ① 射手当前锁定的目标（不限距离喵）
        if (owner instanceof ServerPlayer sp) {
            Entity locked = LockManager.lockedTarget(sp);
            if (locked instanceof AamMissile m && m.isAlive()) {
                targetId = m.getUUID();
                targetIsMissile = true;
                targetMissileRef = m;
                return m;                       // 拦截导弹喵
            }
            if (locked instanceof LivingEntity le && le.isAlive() && !le.isSpectator()) {
                targetId = le.getUUID();
                return le;
            }
        }

        // ② 测试弹专用：只锁最近的玩家喵
        if (getTags().contains("aam_lock_player")) {
            net.minecraft.world.entity.player.Player target = serverLevel.getEntitiesOfClass(
                            net.minecraft.world.entity.player.Player.class,
                            this.getBoundingBox().inflate(Math.max(16.0, pSeekRange)),
                            e -> e != owner && e.isAlive() && !e.isSpectator())
                    .stream()
                    .min(java.util.Comparator.comparingDouble(this::distanceTo))
                    .orElse(null);
            if (target != null) {
                targetId = target.getUUID();
                return target;
            }
            return null;
        }

        // ③ 召唤出来的导弹（没有射手喵）→ 自动锁定最近的敌对生物喵
        if (!(owner instanceof ServerPlayer)) {
            LivingEntity near = serverLevel.getEntitiesOfClass(LivingEntity.class,
                            this.getBoundingBox().inflate(Math.max(16.0, pSeekRange)),
                            e -> e != (Object) this && e != owner && e.isAlive() && !e.isSpectator())
                    .stream()
                    .min(java.util.Comparator.comparingDouble(this::distanceTo))
                    .orElse(null);
            if (near != null) {
                targetId = near.getUUID();
                return near;
            }
            return null;
        }

        // ③ 射手没锁定时也找最近的敌人（射程内喵）
        LivingEntity near = serverLevel.getEntitiesOfClass(LivingEntity.class,
                        this.getBoundingBox().inflate(Math.max(16.0, pSeekRange)),
                        e -> e != owner && e.isAlive() && !e.isSpectator()
                                && !(owner instanceof Player op && Teams.isFriendly(op, e)))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceTo))
                .orElse(null);
        if (near != null) {
            targetId = near.getUUID();
            return near;
        }

        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide()) {
            if (getTags().contains("aam_no_explode")) { this.discard(); return; }   // 测试弹喵
            explodeAndDie();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide()) {
            if (getTags().contains("aam_no_explode")) { this.discard(); return; }   // 测试弹喵
            explodeAndDie();
        }
    }

    /** 7 TNT 当量爆炸 + 大量火焰粒子 + 计分喵 */
    private void explodeAndDie() {
        if (exploding) return;   // 已经在爆炸了，直接返回喵
        exploding = true;
        if (!(level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }
        Vec3 p = this.position();
        Entity ownerEntity = this.getOwner();
        ServerPlayer shooter = (ownerEntity instanceof ServerPlayer sp) ? sp : null;

        double radius = (float) pPower * 2.2;   // 伤害半径（空爆范围加大喵）

        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e != ownerEntity && e.isAlive() && !e.isSpectator());

        Map<UUID, Float> hpBefore = new HashMap<>();
        Map<UUID, Boolean> airBefore = new HashMap<>();
        for (LivingEntity v : victims) {
            hpBefore.put(v.getUUID(), v.getHealth());
            airBefore.put(v.getUUID(), !v.onGround());
        }

        // 火焰 + 浓烟（原来那种多火焰喵）
        for (int i = 0; i < 60; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 4.0;
            double oy = (this.random.nextDouble() - 0.5) * 4.0;
            double oz = (this.random.nextDouble() - 0.5) * 4.0;
            serverLevel.sendParticles(ParticleTypes.FLAME, p.x + ox, p.y + oy, p.z + oz,
                    1, 0.0, 0.05, 0.0, 0.02);
        }
        for (int i = 0; i < 24; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 5.0;
            double oy = (this.random.nextDouble() - 0.5) * 5.0;
            double oz = (this.random.nextDouble() - 0.5) * 5.0;
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, p.x + ox, p.y + oy, p.z + oz,
                    1, 0.0, 0.02, 0.0, 0.02);
        }

        boolean destroyBlocks = serverLevel.getGameRules().get(ModGameRules.DESTROY_BLOCKS);

        // 自制爆炸：伤害 + 粒子，但击退极小喵（原版爆炸的击退太夸张喵）
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);

        float maxDamage = (float) (pPower * 7.5);   // power 8 -> 中心 60、半径一半 40 喵
        for (LivingEntity v : victims) {
            double d = v.position().distanceTo(p);
            double falloff = Math.max(0.0, 1.0 - d / radius);
            float dmg = (float) (maxDamage * (0.35 + 0.65 * falloff));   // 线性衰减喵
            if (dmg <= 0.1f) continue;

            v.invulnerableTime = 0;
            v.hurt(serverLevel.damageSources().explosion(this, ownerEntity), dmg);

            // 破甲弹头：追加魔法伤害（无视护甲，满护甲玩家也能打到 26 颗心喵）
            float ap = (float) (dmg * 1.4);
            if (ap > 0.1f) {
                v.invulnerableTime = 0;
                v.hurt(serverLevel.damageSources().magic(), ap);
            }

            // 只给一点点推力（0.06 = 很轻喵）
            Vec3 dir = v.position().add(0.0, 0.5, 0.0).subtract(p);
            if (dir.lengthSqr() > 1.0E-4) {
                dir = dir.normalize();
                v.push(dir.x * 0.06, 0.02, dir.z * 0.06);
            }
        }

        // 直接命中导弹目标 → 摧毁它（拦截成功喵）
        if (targetMissileRef != null && targetMissileRef.isAlive() && !targetMissileRef.exploding) {
            targetMissileRef.hurtServer(serverLevel, serverLevel.damageSources().generic(), 40.0f);
        }

        // 爆炸连锁：只炸「不是在拦截导弹」的敌方导弹喵
        for (AamMissile other : serverLevel.getEntitiesOfClass(AamMissile.class,
                new net.minecraft.world.phys.AABB(p, p).inflate(radius),
                m -> m != this && m.isAlive() && !m.exploding && !m.targetIsMissile)) {
            if (m_own(other, ownerEntity)) continue;
            other.hurtServer(serverLevel, serverLevel.damageSources().generic(),
                    (float) (pPower * 6.0));
        }

        // 需要破坏方块时，自己挖一个球（不走原版爆炸，避免击退喵）
        if (Boolean.TRUE.equals(destroyBlocks)) {
            int r = (int) Math.max(1, Math.round(pPower * 0.65));   // 弹坑（缩小喵）
            BlockPos center = BlockPos.containing(p);
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dy * dy + dz * dz > r * r) continue;
                        BlockPos bp = center.offset(dx, dy, dz);
                        var state = serverLevel.getBlockState(bp);
                        if (state.isAir()) continue;
                        if (state.getBlock() == net.minecraft.world.level.block.Blocks.BEDROCK) continue;
                        if (state.getBlock() == net.minecraft.world.level.block.Blocks.OBSIDIAN) continue;
                        serverLevel.destroyBlock(bp, false);
                    }
                }
            }
        }

        serverLevel.playSound(null, p.x, p.y, p.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.8f);

        for (LivingEntity v : victims) {
            Float before = hpBefore.get(v.getUUID());
            if (before == null) continue;
            double dist = (shooter != null) ? shooter.distanceTo(v) : 0.0;
            Scoring.onHit(shooter, v, dist);
            if (!v.isAlive()) {
                Scoring.onKill(shooter, v, Boolean.TRUE.equals(airBefore.get(v.getUUID())), before);
            }
        }

        this.discard();
    }

    // ==================== 客户端不做插值/预测喵 ====================

    @Override
    protected void lerpPositionAndRotationStep(int steps, double x, double y, double z, double yRot, double xRot) {
        this.setPos(x, y, z);
        this.setYRot((float) yRot);
        this.setXRot((float) xRot);
        this.setOldPosAndRot();
    }

    @Override
    public void lerpMotion(Vec3 motion) {
        this.setDeltaMovement(motion);
    }
}