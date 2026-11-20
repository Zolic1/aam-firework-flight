package dev.aam.flight.bench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/** 注册调参工作台喵 */
public final class ModBlocks {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("aamflight", "tuning_bench");

    public static final Block TUNING_BENCH = new TuningBenchBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5f)
            .sound(SoundType.WOOD));

    public static final ResourceKey<Item> BENCH_ITEM_KEY = ResourceKey.create(Registries.ITEM, ID);
    public static final Item TUNING_BENCH_ITEM = new BlockItem(TUNING_BENCH,
            new Item.Properties().setId(BENCH_ITEM_KEY));

    public static BlockEntityType<TuningBenchBlockEntity> TUNING_BE;
    public static MenuType<TuningMenu> TUNING_MENU;

    private ModBlocks() {}

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ID, TUNING_BENCH);
        Registry.register(BuiltInRegistries.ITEM, ID, TUNING_BENCH_ITEM);
        TUNING_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID,
                FabricBlockEntityTypeBuilder.create(TuningBenchBlockEntity::new, TUNING_BENCH).build());
        TUNING_MENU = Registry.register(BuiltInRegistries.MENU, ID,
                new MenuType<>(TuningMenu::new, FeatureFlags.VANILLA_SET));
        org.slf4j.LoggerFactory.getLogger("AAM").info("[AAM] tuning bench registered");
    }
}
