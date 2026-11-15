package dev.aam.flight;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.List;

/** 干扰弹：掷出后让附近导弹有概率脱锁喵 */
public final class Flares {

    /** 干扰作用半径（格喵） */
    public static final double RADIUS = 75.0;
    /** 脱锁后多久不再重新锁定（tick 喵） */
    public static final int BLOCK_TICKS = 60;

    private Flares() {}

    public static void deploy(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // 视觉：一团热诱弹烟雾 + 火焰喵
        for (int i = 0; i < 18; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 3.0;
            double oy = (level.random.nextDouble() - 0.5) * 2.0;
            double oz = (level.random.nextDouble() - 0.5) * 3.0;
            level.sendParticles(ParticleTypes.FLAME,
                    player.getX() + ox, player.getY() + 1.0 + oy, player.getZ() + oz,
                    1, 0.0, 0.02, 0.0, 0.01);
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX() + ox, player.getY() + 1.0 + oy, player.getZ() + oz,
                    1, 0.0, 0.01, 0.0, 0.01);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2f, 0.7f);

        // 让附近导弹判定脱锁喵
        List<AamMissile> missiles = level.getEntitiesOfClass(AamMissile.class,
                player.getBoundingBox().inflate(RADIUS),
                m -> m.isAlive() && !isOwn(player, m));

        for (AamMissile m : missiles) {
            double resist = m.getFlareResist();
            if (level.random.nextDouble() < resist) {
                // 抗住了喵：导弹闪一下火花
                level.sendParticles(ParticleTypes.CRIT,
                        m.getX(), m.getY(), m.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            } else {
                m.breakLockByFlare(BLOCK_TICKS);
            }
        }
    }

    private static boolean isOwn(ServerPlayer player, AamMissile m) {
        Entity owner = m.getOwner();
        return owner != null && owner.getUUID().equals(player.getUUID());
    }
}
