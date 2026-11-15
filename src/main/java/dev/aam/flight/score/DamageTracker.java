package dev.aam.flight.score;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 记录「谁在最近伤害过我」，用于「以牙还牙」加分喵 */
public final class DamageTracker {

    private static final Map<UUID, Map<UUID, Long>> MAP = new HashMap<>();

    private DamageTracker() {}

    public static void record(LivingEntity victim, DamageSource source) {
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Player p)) return;
        long now = victim.level().getGameTime();
        MAP.computeIfAbsent(victim.getUUID(), k -> new HashMap<>()).put(p.getUUID(), now);
    }

    /** 受害者在 ticks 内是否被该玩家伤害过喵 */
    public static boolean recentlyDamagedBy(LivingEntity victim, Player attacker, long ticks) {
        Map<UUID, Long> m = MAP.get(victim.getUUID());
        if (m == null) return false;
        Long t = m.get(attacker.getUUID());
        if (t == null) return false;
        return victim.level().getGameTime() - t <= ticks;
    }

    public static void forget(LivingEntity victim) {
        MAP.remove(victim.getUUID());
    }
}
