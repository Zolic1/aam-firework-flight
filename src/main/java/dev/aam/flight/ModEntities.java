package dev.aam.flight;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** 模组实体注册 */
public final class ModEntities {

    public static final ResourceKey<EntityType<?>> MISSILE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("aamflight", "aam_missile"));

    public static final EntityType<AamMissile> AAM_MISSILE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            MISSILE_KEY,
            EntityType.Builder.<AamMissile>of(AamMissile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.13f)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build(MISSILE_KEY)
    );

    private ModEntities() {}

    public static void init() {
        // 触发类加载即可
    }
}
