package dev.aam.flight;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

/** 原版计分板队伍兼容：同队 / 同盟 → 友方喵 */
public final class Teams {

    private Teams() {}

    public static boolean isFriendly(Player viewer, Entity other) {
        if (other == viewer) return true;
        Team a = viewer.getTeam();
        Team b = other.getTeam();
        if (a == null || b == null) return false;
        return a == b || a.isAlliedTo(b);
    }
}