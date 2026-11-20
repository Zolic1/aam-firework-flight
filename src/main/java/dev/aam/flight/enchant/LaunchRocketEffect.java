package dev.aam.flight.enchant;

import dev.aam.flight.AamMissile;
import dev.aam.flight.ModEntities;
import dev.aam.flight.item.ItemStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 附魔效果：aamflight:launch_rocket 喵
 * 火箭弹 = 没有制导的导弹喵（直线飞行，速度曲线和爆炸一致喵）
 * 支持「火箭齐射数」参数喵：rsalvo 1~16 喵
 */
public record LaunchRocketEffect(float speedBonus) implements EnchantmentEntityEffect {

    public static final MapCodec<LaunchRocketEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("speed_bonus", 0.25f).forGetter(LaunchRocketEffect::speedBonus)
            ).apply(instance, LaunchRocketEffect::new)
    );

    @Override
    public void apply(ServerLevel level, int enchantLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!(entity instanceof AbstractArrow arrow)) return;
        if (arrow instanceof AamMissile) return;

        Entity owner = arrow.getOwner();
        int salvo = Math.max(1, Math.min(16,
                ItemStats.effectiveInt(item.itemStack(), "rsalvo")));

        Vec3 dir = arrow.getDeltaMovement().normalize();
        double speed = arrow.getDeltaMovement().length() * (1.0 + speedBonus * enchantLevel);

        for (int i = 0; i < salvo; i++) {
            AamMissile rocket = new AamMissile(ModEntities.AAM_MISSILE, level);
            rocket.unguided = true;                       // 无制导喵
            if (owner != null) rocket.setOwner(owner);
            rocket.setPos(arrow.getX(), arrow.getY(), arrow.getZ());

            // 齐射时轻微散开喵
            Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
            if (side.lengthSqr() < 1.0E-4) side = new Vec3(1.0, 0.0, 0.0);
            side = side.normalize();
            Vec3 up = side.cross(dir).normalize();
            double spread = salvo <= 1 ? 0.0 : (i - (salvo - 1) / 2.0) * 0.035;
            Vec3 velocity = dir.add(side.scale(spread)).normalize().scale(speed);

            rocket.setDeltaMovement(velocity);
            level.addFreshEntity(rocket);
        }
        arrow.discard();
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
