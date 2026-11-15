package dev.aam.flight.gun;

/** 子弹类型喵：每种都有自己的尾迹、燃烧、破甲属性 */
public record BulletType(String name, int tracerEvery, int tracerParticle,
                         double igniteChance, double armorPen) {

    public static final int PARTICLE_NONE = 0;
    public static final int PARTICLE_SMOKE = 1;
    public static final int PARTICLE_END_ROD = 2;

    /** 索引 0 不用，1~5 对应游戏内选项喵 */
    public static final BulletType[] TYPES = {
            null,
            // 1. 追踪[无尾迹]：无尾迹、22.5% 破甲喵
            new BulletType("追踪[无尾迹]", 0, PARTICLE_NONE, 0.0, 0.225),
            // 2. 曳光燃烧弹[烟雾]：每 2 发尾迹、4.5% 燃烧、9.5% 破甲喵
            new BulletType("曳光燃烧弹[烟雾]", 2, PARTICLE_SMOKE, 0.045, 0.095),
            // 3. 通用弹药[烟雾]：每 6 发尾迹、0.5% 燃烧、16.5% 破甲喵
            new BulletType("通用弹药[烟雾]", 6, PARTICLE_SMOKE, 0.005, 0.165),
            // 4. 曳光燃烧弹[末地烛]：每 3 发尾迹、2.5% 燃烧、10.5% 破甲喵
            new BulletType("曳光燃烧弹[末地烛]", 3, PARTICLE_END_ROD, 0.025, 0.105),
            // 5. 破甲弹药[烟雾]：每 8 发尾迹、1.5% 燃烧、30% 破甲喵
            new BulletType("破甲弹药[烟雾]", 8, PARTICLE_SMOKE, 0.015, 0.30),
    };

    public static BulletType of(int index) {
        if (index < 1 || index >= TYPES.length) return TYPES[3];
        return TYPES[index];
    }

    public static int count() {
        return TYPES.length - 1;
    }
}
