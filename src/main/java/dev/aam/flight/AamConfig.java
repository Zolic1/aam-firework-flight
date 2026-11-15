package dev.aam.flight;

import dev.aam.flight.config.ConfigOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * 注册表层喵：所有可调参数的「当前值 + 定义（范围 / 默认值 / 是否互斥）」。
 *
 * 生效顺序：物品 base 覆盖 → 全局值 → 加上物品 att 叠加（见 ItemStats 喵）
 * 指令在 {@code config.ConfigCommand}，落盘在 {@code config.ConfigStore} 喵。
 */
public final class AamConfig {

    private AamConfig() {}

    // ---- 推进 ----
    public static double targetSpeed = 1.5;
    public static double thrusterAccel = 0.25;

    // ---- 导弹 ----
    public static double missileMaxSpeed = 9.0;   // 180 格/秒喵
    public static double missileStartSpeed = 1.0;
    public static double missileAccelTicks = 49;
    public static double missileSeekDelay = 12;
    public static double missileTurnBlend = 0.45;
    public static int missileRetrack = 2;   // 重追踪次数喵
    public static double missileFlareResist = 0.35;   // 导弹抗干扰 0~1 喵
    public static int missileStealth = 0;             // 隐蔽 0/1 喵
    public static double missileLead = 0.85;
    public static double missilePower = 7.0;
    public static double missileFuse = 2.2;
    public static int missileLife = 1500;
    public static double missileSeekRange = 256.0;

    // ---- 轻机枪喵 ----
    public static double lmgDamage = 7.0;
    public static double lmgRange = 96.0;
    public static int lmgPierce = 2;
    public static int lmgCooldownTicks = 4;
    public static double lmgRoundsPerSecond = 15.0;
    public static double lmgBulletSpeed = 280.0;
    public static double lmgMagazine = 1200.0;
    public static double lmgReloadTicks = 64.0;
    public static double lmgReloadCost = 1200.0;
    public static double lmgSpread = 0.8;
    public static int lmgPellets = 1;
    public static int lmgBars = 1;                 // 蓄力条数量喵
    public static double lmgKnockback = 0.03;      // 子弹击退强度喵（很小喵）
    public static int lmgBulletType = 3;   // 1~5 子弹类型喵
    public static int lmgFreeze = 0;        // 冻结 tick 喵
    public static double lmgPoison = 0.0;   // 毒素秒数喵
    public static double lmgChain = 0.0;    // 链电半径喵
    public static int lmgChainN = 0;        // 链电个数喵
    public static double lmgBoom = 0.0;     // 弹体小爆炸当量喵
    public static double lmgHoming = 0.0;   // 追踪转向系数喵
    public static int lmgTracerEvery = 3;          // 普通枪每 N 发一条尾迹喵
    public static int lmgLockout = 4;              // 多连发间隔 tick 喵

    // ---- 锁定 ----
    public static double lockRange = 256.0;
    public static double lockCone = 8.0;
    public static double lockViewCone = 55.0;
    public static int lockHold = 60;
    public static int lockOutOfView = 160;

    // ---- 雷达 ----
    public static double radarRange = 512.0;
    public static int radarBlips = 24;
    public static int radarInterval = 1;    // 雷达发包间隔 tick 喵
    public static int hudInterval = 2;      // 活动栏刷新间隔 tick 喵
    public static int scoreInterval = 8;    // 得分消息间隔 tick 喵
    public static int ammoRate = 64;        // 每 tick 最多兑换的弹药数喵
    public static int ruleInterval = 20;    // 规则→记分板同步间隔 tick 喵

    // ---- 计分 ----
    public static int scoreHitBase = 50;
    public static int scoreHitDistDiv = 3;
    public static int scoreKillAirBase = 150;
    public static int scoreKillGroundBase = 100;
    public static int scoreRevenge = 600;
    public static int scoreRevengeSeconds = 60;

    // ==================== 注册表喵 ====================

    private static final List<ConfigOption> OPTIONS = new ArrayList<>();
    private static final Map<String, ConfigOption> BY_NAME = new LinkedHashMap<>();
    private static final Map<String, Double> DEFAULTS = new HashMap<>();

    private static void opt(String name, DoubleSupplier get, DoubleConsumer set,
                            double min, double max, boolean integer) {
        opt(name, get, set, min, max, integer, false);
    }

    private static void opt(String name, DoubleSupplier get, DoubleConsumer set,
                            double min, double max, boolean integer, boolean exclusive) {
        ConfigOption option = new ConfigOption(name, get, set, min, max, integer, exclusive);
        OPTIONS.add(option);
        BY_NAME.put(name, option);
    }

    static {
        opt("speed", () -> targetSpeed, v -> targetSpeed = v, 0.1, 20.0, false);
        opt("thruster_accel", () -> thrusterAccel, v -> thrusterAccel = v, 0.01, 2.0, false);

        opt("mspeed", () -> missileMaxSpeed, v -> missileMaxSpeed = v, 0.2, 10.0, false);
        opt("mstart", () -> missileStartSpeed, v -> missileStartSpeed = v, 0.1, 10.0, false);
        opt("maccelt", () -> missileAccelTicks, v -> missileAccelTicks = v, 1, 400, true);
        opt("mseekdelay", () -> missileSeekDelay, v -> missileSeekDelay = v, 0, 400, true);
        opt("mturn", () -> missileTurnBlend, v -> missileTurnBlend = v, 0.02, 1.0, false);
        opt("mretrack", () -> missileRetrack, v -> missileRetrack = (int) v, 0, 8, true);
        opt("mflare", () -> missileFlareResist, v -> missileFlareResist = v, 0.0, 1.0, false);
        opt("mstealth", () -> missileStealth, v -> missileStealth = (int) v, 0, 1, true);
        opt("mlead", () -> missileLead, v -> missileLead = v, 0.0, 3.0, false);
        opt("mpower", () -> missilePower, v -> missilePower = v, 0.0, 30.0, false);
        opt("mfuse", () -> missileFuse, v -> missileFuse = v, 0.2, 10.0, false);
        opt("mlife", () -> missileLife, v -> missileLife = (int) v, 20, 20000, true);
        opt("mseek", () -> missileSeekRange, v -> missileSeekRange = v, 16.0, 1024.0, false);

        opt("lmg_dmg", () -> lmgDamage, v -> lmgDamage = v, 0.5, 100.0, false);
        opt("lmg_range", () -> lmgRange, v -> lmgRange = v, 0.0, 2048.0, false);
        opt("lmg_pierce", () -> lmgPierce, v -> lmgPierce = (int) v, 1, 16, true);
        opt("lmg_rate", () -> lmgCooldownTicks, v -> lmgCooldownTicks = (int) v, 1, 40, true);
        opt("lmg_rps", () -> lmgRoundsPerSecond, v -> lmgRoundsPerSecond = v, 1.0, 40.0, false);
        opt("lmg_bspeed", () -> lmgBulletSpeed, v -> lmgBulletSpeed = v, 10.0, 2000.0, false);
        opt("lmg_mag", () -> lmgMagazine, v -> lmgMagazine = v, 1, 100000, true);
        opt("lmg_reload", () -> lmgReloadTicks, v -> lmgReloadTicks = v, 1, 400, true);
        opt("lmg_cost", () -> lmgReloadCost, v -> lmgReloadCost = v, 0, 100000, true);
        opt("lmg_spread", () -> lmgSpread, v -> lmgSpread = v, 0.0, 45.0, false);
        opt("lmg_pellets", () -> lmgPellets, v -> lmgPellets = (int) v, 1, 64, true);
        opt("lmg_bars", () -> lmgBars, v -> lmgBars = (int) v, 1, 4, true);
        opt("lmg_kb", () -> lmgKnockback, v -> lmgKnockback = v, 0.0, 2.0, false);
        opt("lmg_tracer", () -> lmgTracerEvery, v -> lmgTracerEvery = (int) v, 1, 20, true);
        opt("lmg_bullet_type", () -> lmgBulletType, v -> lmgBulletType = (int) v, 1, 5, true, true);   // 互斥喵
        opt("lmg_freeze", () -> lmgFreeze, v -> lmgFreeze = (int) v, 0, 400, true);
        opt("lmg_poison", () -> lmgPoison, v -> lmgPoison = v, 0.0, 30.0, false);
        opt("lmg_chain", () -> lmgChain, v -> lmgChain = v, 0.0, 32.0, false);
        opt("lmg_chain_n", () -> lmgChainN, v -> lmgChainN = (int) v, 0, 16, true);
        opt("lmg_boom", () -> lmgBoom, v -> lmgBoom = v, 0.0, 16.0, false);
        opt("lmg_homing", () -> lmgHoming, v -> lmgHoming = v, 0.0, 1.0, false);
        opt("lmg_lockout", () -> lmgLockout, v -> lmgLockout = (int) v, 0, 40, true);

        opt("lockrange", () -> lockRange, v -> lockRange = v, 8.0, 1024.0, false);
        opt("cone", () -> lockCone, v -> lockCone = v, 1.0, 60.0, false);
        opt("viewcone", () -> lockViewCone, v -> lockViewCone = v, 5.0, 120.0, false);
        opt("hold", () -> lockHold, v -> lockHold = (int) v, 0, 600, true);
        opt("outview", () -> lockOutOfView, v -> lockOutOfView = (int) v, 0, 1200, true);

        opt("radarrange", () -> radarRange, v -> radarRange = v, 16.0, 2048.0, false);
        opt("radarblips", () -> radarBlips, v -> radarBlips = (int) v, 1, 128, true);
        opt("radar_interval", () -> radarInterval, v -> radarInterval = (int) v, 1, 40, true);
        opt("hud_interval", () -> hudInterval, v -> hudInterval = (int) v, 1, 40, true);
        opt("score_interval", () -> scoreInterval, v -> scoreInterval = (int) v, 1, 80, true);
        opt("ammo_rate", () -> ammoRate, v -> ammoRate = (int) v, 1, 4096, true);
        opt("rule_interval", () -> ruleInterval, v -> ruleInterval = (int) v, 1, 400, true);

        opt("sc_hit", () -> scoreHitBase, v -> scoreHitBase = (int) v, 0, 10000, true);
        opt("sc_hitdiv", () -> scoreHitDistDiv, v -> scoreHitDistDiv = (int) v, 1, 100, true);
        opt("sc_air", () -> scoreKillAirBase, v -> scoreKillAirBase = (int) v, 0, 10000, true);
        opt("sc_ground", () -> scoreKillGroundBase, v -> scoreKillGroundBase = (int) v, 0, 10000, true);
        opt("sc_revenge", () -> scoreRevenge, v -> scoreRevenge = (int) v, 0, 100000, true);
        opt("sc_revsec", () -> scoreRevengeSeconds, v -> scoreRevengeSeconds = (int) v, 0, 3600, true);

        for (ConfigOption option : OPTIONS) {
            DEFAULTS.put(option.name(), option.get());
        }
    }

    // ==================== 访问器喵 ====================

    /** 全部选项（顺序即 /aam list 的顺序喵） */
    public static List<ConfigOption> options() {
        return OPTIONS;
    }

    /** 选项名列表喵 */
    public static List<String> optionNames() {
        return List.copyOf(BY_NAME.keySet());
    }

    public static ConfigOption option(String name) {
        return BY_NAME.get(name);
    }

    /** 全局值（找不到就 0 喵） */
    public static double global(String option) {
        ConfigOption o = BY_NAME.get(option);
        return o == null ? 0.0 : o.get();
    }

    /** 默认值（reset 用喵） */
    public static Double defaultValue(String option) {
        return DEFAULTS.get(option);
    }
}
