package dev.aam.flight.gun;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** 记录「正在按住右键」的玩家喵 */
public final class FireState {

    private static final Set<UUID> FIRING = new HashSet<>();

    private FireState() {}

    public static void set(ServerPlayer player, boolean pressed) {
        if (pressed) {
            FIRING.add(player.getUUID());
            LmgGun.onPress(player);   // 点一下就打一发喵
        } else {
            FIRING.remove(player.getUUID());
            LmgGun.reset(player);
        }
    }

    public static boolean isFiring(ServerPlayer player) {
        return FIRING.contains(player.getUUID());
    }

    public static void clear(ServerPlayer player) {
        FIRING.remove(player.getUUID());
        LmgGun.reset(player);
    }
}