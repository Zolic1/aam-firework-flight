package dev.aam.flight.enchant;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** 注册自定义附魔效果类型 aamflight:launch_missile */
public final class ModEnchantments {

    public static final Identifier LAUNCH_MISSILE_ID =
            Identifier.fromNamespaceAndPath("aamflight", "launch_missile");
    public static final Identifier LAUNCH_ROCKET_ID =
            Identifier.fromNamespaceAndPath("aamflight", "launch_rocket");

    private ModEnchantments() {}

    public static void init() {
        Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                LAUNCH_MISSILE_ID,
                LaunchMissileEffect.CODEC);
        Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                LAUNCH_ROCKET_ID,
                LaunchRocketEffect.CODEC);
    }
}
