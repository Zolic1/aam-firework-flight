package dev.aam.flight;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * 把游戏规则镜像到记分板，供数据包 function 判断喵
 *  - aam_rule 的 #mob_armor = 1/0（对应 aamflightMobArmor 喵）
 */
public final class RuleMirror {

    private static int tickCounter = 0;

    private RuleMirror() {}

    public static void tick(MinecraftServer server) {
        if (++tickCounter % Math.max(1, AamConfig.ruleInterval) != 0) return;

        ServerLevel level = server.overworld();
        if (level == null) return;

        // 目标存在与否都执行一次（已存在会失败但无害喵）
        level.getServer().getCommands().performPrefixedCommand(
                level.getServer().createCommandSourceStack().withSuppressedOutput(),
                "scoreboard objectives add aam_rule dummy");

        boolean mobArmor = level.getGameRules().get(ModGameRules.MOB_ARMOR);
        level.getServer().getCommands().performPrefixedCommand(
                level.getServer().createCommandSourceStack().withSuppressedOutput(),
                "scoreboard players set #mob_armor aam_rule " + (mobArmor ? 1 : 0));
    }
}
