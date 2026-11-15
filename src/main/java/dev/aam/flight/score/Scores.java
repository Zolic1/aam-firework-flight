package dev.aam.flight.score;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 模组侧分数（同时也是弹药池喵）：
 *  - 击杀/命中加分 → Scores.add
 *  - 轻机枪换弹 → Scores.spend
 *  - 弹药物品 → Scores.add（1:1 兑换喵）
 * 同步写回 scoreboard 的 aam.score，方便其它系统读取喵。
 */
public final class Scores {

    public static final String OBJECTIVE = "aam.score";

    private static final Map<UUID, Integer> MAP = new HashMap<>();

    private Scores() {}

    public static int get(ServerPlayer player) {
        return MAP.getOrDefault(player.getUUID(), 0);
    }

    public static void add(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return;
        MAP.merge(player.getUUID(), amount, Integer::sum);
        sync(player);
    }

    /** 花掉最多 amount 分，返回实际花掉的数目喵 */
    public static int spendUpTo(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return 0;
        int cur = get(player);
        int used = Math.min(cur, amount);
        if (used <= 0) return 0;
        MAP.put(player.getUUID(), cur - used);
        sync(player);
        return used;
    }

    public static void clear(ServerPlayer player) {
        MAP.remove(player.getUUID());
        sync(player);
    }

    private static void sync(ServerPlayer player) {
        var server = player.level().getServer();
        if (server == null) return;
        server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput(),
                "scoreboard players set " + player.getScoreboardName() + " " + OBJECTIVE + " " + get(player));
    }
}
