package dev.aam.flight.enchant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.aam.flight.AamMissile;
import dev.aam.flight.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * 附魔效果：aamflight:launch_missile
 *
 * 挂在 minecraft:projectile_spawned 上 —— 射出的箭会被替换成 AAM 导弹实体喵。
 */
public record LaunchMissileEffect(float speedBonus) implements EnchantmentEntityEffect {

    public static final MapCodec<LaunchMissileEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("speed_bonus", 0.25f).forGetter(LaunchMissileEffect::speedBonus)
            ).apply(instance, LaunchMissileEffect::new)
    );

    @Override
    public void apply(ServerLevel level, int enchantLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!(entity instanceof AbstractArrow arrow)) return;
        if (arrow instanceof AamMissile) return;   // 已经是导弹了，不重复替换喵

        AamMissile missile = new AamMissile(ModEntities.AAM_MISSILE, level);
        Entity owner = arrow.getOwner();
        if (owner != null) {
            missile.setOwner(owner);
        }
        missile.setPos(arrow.getX(), arrow.getY(), arrow.getZ());
        missile.setDeltaMovement(arrow.getDeltaMovement().scale(1.0 + speedBonus * enchantLevel));
        level.addFreshEntity(missile);
        arrow.discard();
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
