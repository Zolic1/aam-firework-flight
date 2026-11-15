package dev.aam.flight.net;

import dev.aam.flight.LockManager;
import dev.aam.flight.AamConfig;
import dev.aam.flight.RwrUtil;
import dev.aam.flight.Teams;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 服务端雷达：扫描玩家周围目标 + 锁定管理，每 tick 用数据包下发给客户端。
 *
 * 性能：只在戴 RWR 头盔时扫描；最多 AamConfig.radarBlips 个光点；每 tick 一次（用户要求刷新快喵）。
 */
public final class RadarSender {




    private RadarSender() {}

    /** 每人的发包计时器（多玩家互不影响喵） */
    private static final java.util.Map<java.util.UUID, Integer> SEND_TICK = new java.util.HashMap<>();

    public static void tick(ServerPlayer player) {
        int interval = Math.max(1, AamConfig.radarInterval);
        if (interval > 1) {
            int n = SEND_TICK.getOrDefault(player.getUUID(), 0);
            SEND_TICK.put(player.getUUID(), (n + 1) % interval);
            if (n != 0) return;      // 只在计数到 0 的那一 tick 发包喵
        }
        // 锁定每 tick 都要更新（3 秒持续判定喵）
        Entity lock = LockManager.update(player);
        float progress = LockManager.progress(player);

        if (!RwrUtil.hasHelmet(player)) return;

        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye = player.getEyePosition();
        float yaw = player.getYRot();

        AABB box = player.getBoundingBox().inflate(AamConfig.radarRange);
        List<Entity> found = level.getEntities(player, box, RadarSender::interesting);
        if (found.size() > AamConfig.radarBlips) {
            List<Entity> sorted = new ArrayList<>(found);
            sorted.sort(Comparator.comparingDouble(e -> e.distanceToSqr(eye)));
            found = sorted.subList(0, AamConfig.radarBlips);
        }

        List<RadarPayload.Blip> blips = new ArrayList<>(found.size());
        boolean threat = false;
        boolean lockedOnMe = false;

        for (Entity e : found) {
            Vec3 d = e.getBoundingBox().getCenter().subtract(eye);
            double len = d.length();
            if (len < 0.05 || len > AamConfig.radarRange) continue;

            double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
            double bearing = wrap(Math.toDegrees(Math.atan2(-d.x, d.z)) - yaw);
            double elev = Math.toDegrees(Math.atan2(d.y, Math.max(0.001, horiz)));

            byte type;
            if (e instanceof Player) {
                type = RadarPayload.TYPE_PLAYER;
            } else if (e instanceof AbstractArrow arrow) {
                type = RadarPayload.TYPE_MISSILE;
                // 只要是非我方导弹就告警（不管它是不是朝我飞喵）
                boolean friendly = false;
                Entity owner = arrow.getOwner();
                if (owner instanceof Player ownerPlayer) {
                    friendly = Teams.isFriendly(player, ownerPlayer);
                }
                // 隐蔽弹：只有「视野内 + 手动锁定」才暴露喵
                if (arrow instanceof dev.aam.flight.AamMissile sm && sm.isStealth()) {
                    boolean manual = dev.aam.flight.LockManager.isManualLock(player, sm);
                    boolean inFront = d.dot(player.getLookAngle()) > 0.0;   // 在视野前方喵
                    if (!(manual && inFront)) {
                        continue;      // 完全隐藏（没有光点也不告警喵）
                    }
                }

                if (!friendly) {
                    threat = true;
                    // 这枚导弹是不是正锁着我喵
                    if (arrow instanceof dev.aam.flight.AamMissile m && m.isTargeting(player)) {
                        lockedOnMe = true;
                    }
                }
            } else {
                continue;
            }

            // 玩家的相对偏移 + 敌我标记（HUD 画点用喵）
            Vec3 rel = e.getBoundingBox().getCenter().subtract(eye);
            boolean friendlyBlip = e instanceof net.minecraft.world.entity.player.Player pp && Teams.isFriendly(player, pp);
            blips.add(new RadarPayload.Blip((float) bearing, (float) elev, (float) len, type,
                    (float) rel.x, (float) rel.y, (float) rel.z, friendlyBlip));
        }

        // 目标被击毁 → 用 0.7 秒残影撑着喵
        dev.aam.flight.LockManager.Snapshot ghost = null;
        if (lock == null) ghost = dev.aam.flight.LockManager.ghost(player);
        boolean lockDestroyed = ghost != null;

        boolean locked = lock != null || ghost != null;
        boolean lockFriendly = locked && lock instanceof LivingEntity leFriend && Teams.isFriendly(player, leFriend);
        String lockName = "";
        float lockHp = 0, lockMaxHp = 1, lockDist = 0, lockBearing = 0, lockElev = 0;
        float lockW = 0.6f, lockH = 1.8f, lockSpeed = 0;
        float lockDx = 0, lockDy = 0, lockDz = 0;
        boolean lockIsMissile = false;
        float lockManeuver = 0, lockEta = -1;

        if (lock != null) {
            Vec3 d = lock.getBoundingBox().getCenter().subtract(eye);
            double len = Math.max(0.05, d.length());
            double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
            lockName = lock.getName().getString();
            if (lock instanceof dev.aam.flight.AamMissile m) {
                // 锁定导弹：显示耐久 / 机动时间 / 预计到达喵
                lockName = "导弹";
                lockIsMissile = true;
                lockHp = m.getMissileHp();
                lockMaxHp = dev.aam.flight.AamMissile.MISSILE_HP;
                lockManeuver = m.getManeuverSeconds();
                // 命中预警：只有「还有机动时间」且「正在接近我」才算喵
                Vec3 rel = player.getEyePosition().subtract(m.position());
                double distToMe = rel.length();
                double closing = m.getDeltaMovement().scale(20.0)
                        .dot(rel.scale(1.0 / Math.max(0.001, distToMe)));
                if (lockManeuver > 0.2f && closing > 0.5 && distToMe > 0.5) {
                    lockEta = (float) (distToMe / closing);
                } else {
                    lockEta = -1.0f;      // 没机动时间 / 在远离 → 显示 -- 喵
                }
            } else if (lock instanceof LivingEntity le) {
                lockHp = le.getHealth();
                lockMaxHp = Math.max(1.0f, le.getMaxHealth());
            }
            lockDist = (float) len;
            lockBearing = (float) wrap(Math.toDegrees(Math.atan2(-d.x, d.z)) - yaw);
            lockElev = (float) Math.toDegrees(Math.atan2(d.y, Math.max(0.001, horiz)));
            lockDx = (float) d.x;
            lockDy = (float) d.y;
            lockDz = (float) d.z;
            lockW = lock.getBbWidth();
            lockH = lock.getBbHeight();
            lockSpeed = (float) (lock.getDeltaMovement().length() * 20.0);
        } else if (ghost != null) {
            lockName = ghost.name();
            lockHp = 0;
            lockMaxHp = 1;          // 血条显示空的喵
            lockDist = ghost.dist();
            lockDx = (float) ghost.dx();
            lockDy = (float) ghost.dy();
            lockDz = (float) ghost.dz();
            lockW = ghost.width();
            lockH = ghost.height();
            lockSpeed = ghost.speed();
        }

        ServerPlayNetworking.send(player, new RadarPayload(
                blips, threat, locked, lockName, lockHp, lockMaxHp, lockDist,
                lockBearing, lockElev, lockDx, lockDy, lockDz, lockW, lockH, lockSpeed, progress, LockManager.isInView(player), lockFriendly, lockedOnMe, lockIsMissile, lockManeuver, lockEta, lockDestroyed));
    }

    /** 只上报玩家与导弹（不显示普通生物喵） */
    private static boolean interesting(Entity e) {
        if (!e.isAlive() || e.isSpectator()) return false;
        if (e instanceof Player) return true;
        // 只上报「正在飞」的弹射物（插在地上的箭不算喵）
        if (e instanceof AbstractArrow arrow) {
            return arrow.getDeltaMovement().lengthSqr() > 0.05;
        }
        return false;
    }

    private static double wrap(double deg) {
        while (deg > 180.0) deg -= 360.0;
        while (deg < -180.0) deg += 360.0;
        return deg;
    }
}
