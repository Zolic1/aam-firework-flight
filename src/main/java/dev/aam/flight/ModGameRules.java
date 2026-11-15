package dev.aam.flight;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/**
 * 模组自定义游戏规则喵
 * ⚠️ 标识符只能用小写 + 下划线（大写会直接崩客户端喵😿）
 * 指令名 = 标识符，所以用法是：
 *   /gamerule aamflight:destroy_blocks true
 *   /gamerule aamflight:mob_armor true
 */
public final class ModGameRules {

    /** 模组爆炸是否破坏方块喵（true = 破坏，false = 只伤害不破坏喵） */
    public static final GameRule<Boolean> DESTROY_BLOCKS = GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Identifier.fromNamespaceAndPath("aamflight", "destroy_blocks"));

    /** 是否让数据包给生物加护甲喵（true = 生效，false = 不加喵） */
    public static final GameRule<Boolean> MOB_ARMOR = GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Identifier.fromNamespaceAndPath("aamflight", "mob_armor"));

    private ModGameRules() {}

    public static void init() {
        // 触发类加载即可喵
    }
}
