package dev.aam.flight;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 服务端锁定管理喵：
 *
 *  - 未锁定：准星锥体（±8°）内最近的生物，需连续保持 60 tick（3 秒）才锁定
 *  - 已锁定：不会因为转头而立即丢失
 *      · 目标在视野内（±25° 锥体）→ 保持
 *      · 目标离开视野 → 开始计时，满 8 秒（160 tick）才丢失
 *      · 目标死亡 / 消失 → 立即丢失
 *  - 玩家按键（默认 X）→ 手动丢失
 */
public final class LockManager {

    public static final String TAG = "aam.crosshair";

    // 锁定所需 tick 来自 AamConfig.lockHold 喵
    // 脱锁时间来自 AamConfig.lockOutOfView 喵
    private static double viewCos() { return Math.cos(Math.toRadians(AamConfig.lockViewCone)); }

    private static final Map<UUID, UUID> CANDIDATE = new HashMap<>();
    private static final Map<UUID, Integer> HOLD = new HashMap<>();
    private static final Map<UUID, UUID> LOCKED = new HashMap<>();
    private static final Map<UUID, Integer> OUT_OF_VIEW = new HashMap<>();
    private static final Map<UUID, Boolean> IN_VIEW = new HashMap<>();

    /** 锁定快照（用于击毁后残留 0.7 秒喵） */
    public record Snapshot(String name, double dx, double dy, double dz,
                           float width, float height, float dist, float speed, int ticksLeft) {}

    private static final Map<UUID, Snapshot> LAST = new HashMap<>();
    private static final java.util.Set<UUID> MANUAL = new java.util.HashSet<>();   // 手动锁定（X 键喵）
    private static final Map<UUID, Snapshot> GHOST = new HashMap<>();
    /** 0.7 秒 = 14 tick 喵 */
    private static final int GHOST_TICKS = 14;
    private LockManager() {}

    /** 每 tick 调用，返回当前锁定的目标（可能不在视野内喵） */
    public static Entity update(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        UUID pid = player.getUUID();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // ---- 已锁定：维持判定 ----
        UUID lockedId = LOCKED.get(pid);
        if (lockedId != null) {
            Entity e = level.getEntityInAnyDimension(lockedId);
            boolean alive = (e instanceof LivingEntity le && le.isAlive() && !le.isSpectator())
                    || (e instanceof AamMissile m && m.isAlive());
            if (!alive) {
                // 目标被击毁 → 留 0.7 秒残影喵
                Snapshot snap = LAST.get(pid);
                if (snap != null) {
                    GHOST.put(pid, new Snapshot(snap.name(), snap.dx(), snap.dy(), snap.dz(),
                            snap.width(), snap.height(), snap.dist(), snap.speed(), GHOST_TICKS));
                }
                doRelease(level, pid);
            } else {
                boolean inView = dot(look, eye, e) >= viewCos()
                        && LockOn.canSee(level, eye, e, player);   // 墙后不算在视野内喵
                IN_VIEW.put(pid, inView);

                if (inView) {
                    OUT_OF_VIEW.remove(pid);
                } else {
                    int out = OUT_OF_VIEW.getOrDefault(pid, 0) + 1;
                    OUT_OF_VIEW.put(pid, out);
                    if (out >= AamConfig.lockOutOfView) {
                        GHOST.remove(pid);      // 视野外超时 → 立即消失，不留残影喵
                        LAST.remove(pid);
                        doRelease(level, pid);
                        return null;
                    }
                }

                // 刷新快照（击毁后画残影用喵）
                Vec3 center = e.getBoundingBox().getCenter();
                Vec3 rel = center.subtract(eye);
                double len = Math.max(0.05, rel.length());
                LAST.put(pid, new Snapshot(e.getName().getString(), rel.x, rel.y, rel.z,
                        e.getBbWidth(), e.getBbHeight(), (float) len,
                        (float) (e.getDeltaMovement().length() * 20.0), GHOST_TICKS));

                // 保持锁定期间刷新标签（防止被其它逻辑清掉喵）
                if (!e.getTags().contains(TAG)) {
                    e.addTag(TAG);
                }
                return e;
            }
        }

        // ---- 未锁定：积累候选（自动锁定跳过友方喵）----
        Entity cand = LockOn.find(level, player, AamConfig.lockRange,
                e -> !Teams.isFriendly(player, e));
        if (cand == null) {
            // 没有生物 → 看看准星上有没有敌方导弹喵
            cand = LockOn.findMissile(level, player, AamConfig.lockRange,
                    m -> m.getOwner() == null || !(m.getOwner() instanceof net.minecraft.world.entity.player.Player p) || !Teams.isFriendly(player, p));
        }
        UUID candId = (cand == null) ? null : cand.getUUID();
        UUID prevCand = CANDIDATE.get(pid);

        int hold;
        if (candId == null) {
            CANDIDATE.remove(pid);
            HOLD.remove(pid);
            IN_VIEW.remove(pid);
            return null;
        } else if (candId.equals(prevCand)) {
            hold = HOLD.getOrDefault(pid, 0) + 1;
            HOLD.put(pid, hold);
        } else {
            CANDIDATE.put(pid, candId);
            hold = 1;
            HOLD.put(pid, hold);
        }

        if (hold >= AamConfig.lockHold) {
            cand.addTag(TAG);
            LOCKED.put(pid, candId);
            IN_VIEW.put(pid, true);
            OUT_OF_VIEW.remove(pid);
            return cand;
        }

        return null;
    }

    /** 这个目标是不是我手动锁的喵 */
    public static boolean isManualLock(ServerPlayer player, Entity target) {
        if (target == null) return false;
        UUID pid = player.getUUID();
        return MANUAL.contains(pid) && target.getUUID().equals(LOCKED.get(pid));
    }
    /** 击毁残影：还有就返回快照（每调用一次消耗 1 tick 喵） */
    public static Snapshot ghost(ServerPlayer player) {
        UUID pid = player.getUUID();
        Snapshot g = GHOST.get(pid);
        if (g == null) return null;
        if (g.ticksLeft() <= 1) {
            GHOST.remove(pid);
            return null;
        }
        Snapshot next = new Snapshot(g.name(), g.dx(), g.dy(), g.dz(), g.width(), g.height(),
                g.dist(), g.speed(), g.ticksLeft() - 1);
        GHOST.put(pid, next);
        return next;
    }

    /** 手动取消锁定：立即清掉残影喵 */
    public static void clearGhost(ServerPlayer player) {
        GHOST.remove(player.getUUID());
    }
    /** 该玩家当前锁定的实体（不限距离，导弹可以直接用喵） */
    public static Entity lockedTarget(ServerPlayer player) {
        UUID id = LOCKED.get(player.getUUID());
        if (id == null) return null;
        return ((ServerLevel) player.level()).getEntityInAnyDimension(id);
    }
    /** 锁定进度 0~1（未锁定时给 HUD 显示进度用） */
    public static float progress(ServerPlayer player) {
        UUID pid = player.getUUID();
        if (LOCKED.containsKey(pid)) return 1.0f;
        int hold = HOLD.getOrDefault(pid, 0);
        return Math.max(0.0f, Math.min(1.0f, hold / (float) AamConfig.lockHold));
    }

    /** 目标当前是否在视野内（HUD 决定画框还是画边缘箭头喵） */
    public static boolean isInView(ServerPlayer player) {
        return IN_VIEW.getOrDefault(player.getUUID(), true);
    }

    /** 手动释放（按键喵），并加一点冷却避免立刻重新锁上 */
    public static void release(ServerPlayer player) {
        GHOST.remove(player.getUUID());   // 手动取消 → 立即清除喵
        LAST.remove(player.getUUID());
        ServerLevel level = (ServerLevel) player.level();
        UUID pid = player.getUUID();
        doRelease(level, pid);
        HOLD.put(pid, -40);        // 手动解除后要重新积累 100 tick 才可能再锁喵
        CANDIDATE.remove(pid);
    }

    /**
     * 按键切换喵：
     *  - 已锁定 → 取消锁定
     *  - 未锁定 → 立刻锁定「离视线最近」的目标（不用等 3 秒喵）
     */
    public static void toggle(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        UUID pid = player.getUUID();

        // 准星当前指着的目标（手动锁定允许友军喵）
        Entity cand = LockOn.find(level, player, AamConfig.lockRange);
        if (cand == null) {
            // 准星上没有生物 → 先看准星上有没有导弹喵
            cand = LockOn.findMissile(level, player, AamConfig.lockRange, m -> true);
        }
        if (cand == null) {
            // 还没锁到 → 自动锁「身后追我的导弹」喵
            cand = LockOn.findMissileBehind(level, player, AamConfig.lockRange);
        }

        if (cand == null) {
            // 准星没目标 → 取消锁定喵
            release(player);
            return;
        }

        UUID oldId = LOCKED.get(pid);
        if (oldId != null && !oldId.equals(cand.getUUID())) {
            untag(level, oldId);   // 摘掉旧目标的标签，切到新目标喵
        }

        cand.addTag(TAG);
        LOCKED.put(pid, cand.getUUID());
        MANUAL.add(pid);   // 这是手动锁定喵
        CANDIDATE.put(pid, cand.getUUID());
        HOLD.put(pid, AamConfig.lockHold);
        IN_VIEW.put(pid, true);
        OUT_OF_VIEW.remove(pid);
    }
    public static void clear(ServerPlayer player) {
        doRelease((ServerLevel) player.level(), player.getUUID());
        CANDIDATE.remove(player.getUUID());
        HOLD.remove(player.getUUID());
    }

    /** 摘掉指定实体的锁定标签喵 */
    private static void untag(ServerLevel level, UUID entityId) {
        if (entityId == null) return;
        Entity e = level.getEntityInAnyDimension(entityId);
        if (e != null) e.removeTag(TAG);
    }
    private static void doRelease(ServerLevel level, UUID pid) {
        MANUAL.remove(pid);
        UUID id = LOCKED.remove(pid);
        OUT_OF_VIEW.remove(pid);
        IN_VIEW.remove(pid);
        if (id == null) return;
        Entity e = level.getEntityInAnyDimension(id);
        if (e != null) e.removeTag(TAG);
    }

    private static double dot(Vec3 look, Vec3 eye, Entity target) {
        Vec3 d = target.getBoundingBox().getCenter().subtract(eye);
        double len = d.length();
        if (len < 1.0E-4) return 1.0;
        return d.dot(look) / len;
    }
}
