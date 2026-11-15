package dev.aam.flight.score;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 计分规则（模组版喵）：
 *  命中        +(50 + 距离/3)          向下取整
 *  击毁        +((空中 ? 150 : 100) + 最大血量/2 + 命中时血量/1.25)
 *  以牙还牙    +600（对方 60 秒内伤害过我）
 */
public final class Scoring {

    public static final String OBJECTIVE = "aam.score";
    private static long revengeTicks() { return 20L * dev.aam.flight.AamConfig.scoreRevengeSeconds; }

    private Scoring() {}

    public static void onHit(ServerPlayer shooter, LivingEntity victim, double distance) {
        if (shooter == null) return;
        int pts = dev.aam.flight.AamConfig.scoreHitBase + (int) Math.floor(distance / (double) dev.aam.flight.AamConfig.scoreHitDistDiv);
        add(shooter, pts);
        ScoreMessages.enqueue(shooter, Component.literal(
                "命中 " + victim.getName().getString() + " +" + pts));
    }

    public static void onKill(ServerPlayer shooter, LivingEntity victim, boolean air, float healthAtHit) {
        if (shooter == null) return;

        int base = air ? dev.aam.flight.AamConfig.scoreKillAirBase : dev.aam.flight.AamConfig.scoreKillGroundBase;
        int pts = base
                + (int) Math.floor(victim.getMaxHealth() / 2.0)
                + (int) Math.floor(healthAtHit / 1.25);

        add(shooter, pts);
        ScoreMessages.enqueue(shooter, Component.literal(
                (air ? "击毁空中单位" : "击毁地面单位")
                        + "[" + victim.getName().getString() + "] +" + pts));

        if (DamageTracker.recentlyDamagedBy(victim, shooter, revengeTicks())) {
            add(shooter, dev.aam.flight.AamConfig.scoreRevenge);
            ScoreMessages.enqueue(shooter, Component.literal("以牙还牙 +600"));
        }
    }

    private static void add(ServerPlayer player, int points) {
        Scores.add(player, points);
    }

    /** 服务端启动时确保计分板存在喵 */
    public static void ensureObjective(net.minecraft.server.MinecraftServer server) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(),
                "scoreboard objectives add " + OBJECTIVE + " dummy");
    }
}
