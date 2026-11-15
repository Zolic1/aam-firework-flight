package dev.aam.flight.score;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 活动栏消息队列喵：
 *  - 所有加分提示先进队列
 *  - 每 8 tick（0.4 秒）弹出一条，显示在活动栏（不是聊天栏喵）
 */
public final class ScoreMessages {

    private static int interval() { return Math.max(1, dev.aam.flight.AamConfig.scoreInterval); }   // 默认 8 tick 喵

    private static final Map<UUID, Deque<Component>> QUEUE = new HashMap<>();
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();

    private ScoreMessages() {}

    public static void enqueue(ServerPlayer player, Component message) {
        QUEUE.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>()).addLast(message);
    }

    /** 每 tick 调用喵 */
    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        Deque<Component> queue = QUEUE.get(id);
        if (queue == null || queue.isEmpty()) {
            COOLDOWN.remove(id);
            return;
        }

        int cd = COOLDOWN.getOrDefault(id, 0);
        if (cd > 0) {
            COOLDOWN.put(id, cd - 1);
            return;
        }

        Component next = queue.pollFirst();
        if (next != null) {
            player.displayClientMessage(next, true);   // true = 活动栏喵
        }
        COOLDOWN.put(id, interval());
    }

    public static void clear(ServerPlayer player) {
        QUEUE.remove(player.getUUID());
        COOLDOWN.remove(player.getUUID());
    }
}
