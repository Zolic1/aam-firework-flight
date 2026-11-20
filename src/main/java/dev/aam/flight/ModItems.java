package dev.aam.flight;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import dev.aam.flight.item.ItemStats;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

/** 自定义创造物品栏标签页：RWR 头盔 / 推进装置 / 带附魔的弩喵 */
public final class ModItems {

    private ModItems() {}

    private static final ResourceKey<Enchantment> AAM_MISSILE_KEY =
            ResourceKey.create(Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath("aamflight", "aam_missile"));

    public static final ResourceKey<net.minecraft.world.item.Item> GUN_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aamflight", "lmg_gun"));
    public static final ResourceKey<net.minecraft.world.item.Item> AMMO_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aamflight", "ammo"));

    public static final net.minecraft.world.item.Item LMG_GUN = Registry.register(
            BuiltInRegistries.ITEM, GUN_KEY,
            new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()
                    .setId(GUN_KEY)
                    .stacksTo(1)
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("aamflight", "lmg_gun"))
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.lmg_gun").withStyle(ChatFormatting.RED))
                    .component(DataComponents.RARITY, net.minecraft.world.item.Rarity.EPIC)));

    public static final net.minecraft.world.item.Item AMMO = Registry.register(
            BuiltInRegistries.ITEM, AMMO_KEY,
            new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()
                    .setId(AMMO_KEY)
                    .stacksTo(64)
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("aamflight", "ammo"))
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.ammo").withStyle(ChatFormatting.GRAY))));

    /** 干扰弹喵 */
    public static final ResourceKey<net.minecraft.world.item.Item> FLARE_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aamflight", "flare"));
    public static final net.minecraft.world.item.Item FLARE = Registry.register(
            BuiltInRegistries.ITEM, FLARE_KEY,
            new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()
                    .setId(FLARE_KEY)
                    .stacksTo(16)
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.flare").withStyle(ChatFormatting.GOLD))
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("aamflight", "flare"))));
    /** 导弹实体用的隐藏物品（只用来渲染 3D 模型喵） */
    public static final ResourceKey<net.minecraft.world.item.Item> MISSILE_MODEL_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aamflight", "missile_display"));
    public static final net.minecraft.world.item.Item MISSILE_DISPLAY = Registry.register(
            BuiltInRegistries.ITEM, MISSILE_MODEL_KEY,
            new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()
                    .setId(MISSILE_MODEL_KEY)
                    .stacksTo(1)
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("aamflight", "missile_3d"))
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.missile_display").withStyle(ChatFormatting.RED))));
    public static final CreativeModeTab TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("aamflight", "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.aamflight.main"))
                    .icon(() -> new ItemStack(Items.CROSSBOW))
                    .displayItems(ModItems::fillItems)
                    .build()
    );

    public static void init() {
        // 触发类加载即可喵
    }

    private static void fillItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output out) {
        out.accept(dev.aam.flight.bench.ModBlocks.TUNING_BENCH_ITEM);
        out.accept(rwrHelmet());
        out.accept(thruster());
        out.accept(new ItemStack(LMG_GUN));
        out.accept(sniperRifle());
        out.accept(marksmanRifle());
        out.accept(assaultRifle());
        out.accept(smg());
        out.accept(shotgun());
        out.accept(gatling());
        out.accept(grenadeLauncher());
        // —— 科幻枪械 8 把喵 ——
        out.accept(pulseRifle());
        out.accept(plasmaRifle());
        out.accept(railgun());
        out.accept(arcSmg());
        out.accept(phaseShotgun());
        out.accept(nanoMg());
        out.accept(plasmaCannon());
        out.accept(antimatterSniper());
        // —— 机制枪械 8 把喵 ——
        out.accept(laserRifle());
        out.accept(cryoRifle());
        out.accept(venomSprayer());
        out.accept(teslaGun());
        out.accept(explosiveMg());
        out.accept(smartPistol());
        out.accept(kineticCannon());
        out.accept(laserSniper());
        out.accept(new ItemStack(FLARE));
        out.accept(new ItemStack(AMMO));
        out.accept(aamCrossbow(params.holders()));
    }

    /** 激光步枪：瞬间命中（hitscan），一条光束穿 5 个喵 */
    public static ItemStack laserRifle() {
        return gunWithModel("aamflight.gun.laser_rifle", ChatFormatting.AQUA, "aamflight:laser_rifle_3d",
                "lmg_dmg", 18, "lmg_range", 128, "lmg_spread", 0.0, "lmg_rps", 8.0,
                "lmg_mag", 30, "lmg_reload", 40, "lmg_pierce", 5, "lmg_bspeed", 1200,
                "lmg_pellets", 1, "lmg_bullet_type", 3);
    }

    /** 霜冻枪：命中附加冻结 + 缓慢喵 */
    public static ItemStack cryoRifle() {
        return gunWithModel("aamflight.gun.cryo_rifle", ChatFormatting.BLUE, "aamflight:cryo_rifle_3d",
                "lmg_dmg", 6, "lmg_range", 96, "lmg_spread", 1.5, "lmg_rps", 5.0,
                "lmg_mag", 20, "lmg_reload", 50, "lmg_pierce", 2, "lmg_bspeed", 600,
                "lmg_pellets", 1, "lmg_bullet_type", 2, "lmg_freeze", 140);
    }

    /** 毒液喷射器：6 弹丸 + 毒素喵 */
    public static ItemStack venomSprayer() {
        return gunWithModel("aamflight.gun.venom_sprayer", ChatFormatting.GREEN, "aamflight:venom_sprayer_3d",
                "lmg_dmg", 3, "lmg_range", 56, "lmg_spread", 9.0, "lmg_rps", 6.0,
                "lmg_mag", 40, "lmg_reload", 45, "lmg_pierce", 1, "lmg_bspeed", 420,
                "lmg_pellets", 6, "lmg_bullet_type", 2, "lmg_poison", 4.0);
    }

    /** 链式特斯拉枪：命中后电击周围 3 个敌人喵 */
    public static ItemStack teslaGun() {
        return gunWithModel("aamflight.gun.tesla_gun", ChatFormatting.LIGHT_PURPLE, "aamflight:tesla_gun_3d",
                "lmg_dmg", 8, "lmg_range", 160, "lmg_spread", 0.6, "lmg_rps", 8.0,
                "lmg_mag", 24, "lmg_reload", 50, "lmg_pierce", 2, "lmg_bspeed", 800,
                "lmg_pellets", 1, "lmg_bullet_type", 4, "lmg_chain", 6.0, "lmg_chain_n", 3);
    }

    /** 爆裂机枪：每发命中都有小范围爆裂喵 */
    public static ItemStack explosiveMg() {
        return gunWithModel("aamflight.gun.explosive_mg", ChatFormatting.GOLD, "aamflight:explosive_mg_3d",
                "lmg_dmg", 5, "lmg_range", 140, "lmg_spread", 2.2, "lmg_rps", 12.0,
                "lmg_mag", 60, "lmg_reload", 60, "lmg_pierce", 1, "lmg_bspeed", 700,
                "lmg_pellets", 1, "lmg_bullet_type", 3, "lmg_boom", 2.0);
    }

    /** 智能追踪手枪：子弹自动追踪喵 */
    public static ItemStack smartPistol() {
        return gunWithModel("aamflight.gun.smart_pistol", ChatFormatting.WHITE, "aamflight:smart_pistol_3d",
                "lmg_dmg", 10, "lmg_range", 96, "lmg_spread", 0.3, "lmg_rps", 4.0,
                "lmg_mag", 12, "lmg_reload", 40, "lmg_pierce", 1, "lmg_bspeed", 560,
                "lmg_pellets", 1, "lmg_bullet_type", 1, "lmg_homing", 0.35);
    }

    /** 冲压炮：超高击退喵 */
    public static ItemStack kineticCannon() {
        return gunWithModel("aamflight.gun.kinetic_cannon", ChatFormatting.GOLD, "aamflight:kinetic_cannon_3d",
                "lmg_dmg", 40, "lmg_range", 120, "lmg_spread", 0.5, "lmg_rps", 0.8,
                "lmg_mag", 4, "lmg_reload", 55, "lmg_pierce", 3, "lmg_bspeed", 880,
                "lmg_pellets", 1, "lmg_bullet_type", 5, "lmg_kb", 0.6);
    }

    /** 激光狙击炮：瞬间命中 + 穿 10 个喵 */
    public static ItemStack laserSniper() {
        return gunWithModel("aamflight.gun.laser_sniper", ChatFormatting.LIGHT_PURPLE, "aamflight:laser_sniper_3d",
                "lmg_dmg", 90, "lmg_range", 512, "lmg_spread", 0.0, "lmg_rps", 0.4,
                "lmg_mag", 2, "lmg_reload", 70, "lmg_pierce", 10, "lmg_bspeed", 1500,
                "lmg_pellets", 1, "lmg_bullet_type", 5);
    }
    // ==================== 科幻枪械 8 把喵 ====================

    /** 脉冲步枪：高射速均衡型喵 */
    public static ItemStack pulseRifle() {
        return gunWithModel("aamflight.gun.pulse_rifle", ChatFormatting.AQUA, "aamflight:pulse_rifle_3d",
                "lmg_dmg", 9, "lmg_range", 200, "lmg_spread", 1.2, "lmg_rps", 12.0,
                "lmg_mag", 40, "lmg_reload", 44, "lmg_pierce", 2, "lmg_bspeed", 700,
                "lmg_pellets", 1, "lmg_bullet_type", 3);
    }

    /** 等离子步枪：高伤害慢射喵 */
    public static ItemStack plasmaRifle() {
        return gunWithModel("aamflight.gun.plasma_rifle", ChatFormatting.LIGHT_PURPLE, "aamflight:plasma_rifle_3d",
                "lmg_dmg", 16, "lmg_range", 260, "lmg_spread", 1.0, "lmg_rps", 6.0,
                "lmg_mag", 24, "lmg_reload", 52, "lmg_pierce", 2, "lmg_bspeed", 620,
                "lmg_pellets", 1, "lmg_bullet_type", 4);
    }

    /** 磁轨炮：一发入魂，极限穿透喵 */
    public static ItemStack railgun() {
        return gunWithModel("aamflight.gun.railgun", ChatFormatting.DARK_AQUA, "aamflight:railgun_3d",
                "lmg_dmg", 70, "lmg_range", 640, "lmg_spread", 0.02, "lmg_rps", 0.5,
                "lmg_mag", 3, "lmg_reload", 70, "lmg_pierce", 6, "lmg_bspeed", 1800,
                "lmg_pellets", 1, "lmg_bullet_type", 5);
    }

    /** 电弧冲锋枪：超高射速弹幕喵 */
    public static ItemStack arcSmg() {
        return gunWithModel("aamflight.gun.arc_smg", ChatFormatting.YELLOW, "aamflight:arc_smg_3d",
                "lmg_dmg", 4, "lmg_range", 80, "lmg_spread", 3.2, "lmg_rps", 26.0,
                "lmg_mag", 60, "lmg_reload", 38, "lmg_pierce", 1, "lmg_bspeed", 520,
                "lmg_pellets", 1, "lmg_bullet_type", 2);
    }

    /** 相位霰弹枪：12 弹丸 + 3 条蓄力喵 */
    public static ItemStack phaseShotgun() {
        return gunWithModel("aamflight.gun.phase_shotgun", ChatFormatting.LIGHT_PURPLE, "aamflight:phase_shotgun_3d",
                "lmg_dmg", 5, "lmg_range", 28, "lmg_spread", 7.5, "lmg_rps", 0.8,
                "lmg_mag", 8, "lmg_reload", 60, "lmg_pierce", 1, "lmg_bspeed", 420,
                "lmg_pellets", 12, "lmg_bars", 3, "lmg_bullet_type", 2);
    }

    /** 纳米机枪：大弹匣持续压制喵 */
    public static ItemStack nanoMg() {
        return gunWithModel("aamflight.gun.nano_mg", ChatFormatting.GREEN, "aamflight:nano_mg_3d",
                "lmg_dmg", 6, "lmg_range", 120, "lmg_spread", 2.4, "lmg_rps", 18.0,
                "lmg_mag", 150, "lmg_reload", 90, "lmg_pierce", 2, "lmg_bspeed", 640,
                "lmg_pellets", 1, "lmg_bullet_type", 3);
    }

    /** 等离子炮：发射追踪导弹喵 */
    public static ItemStack plasmaCannon() {
        return gunWithModel("aamflight.gun.plasma_cannon", ChatFormatting.DARK_PURPLE, "aamflight:plasma_cannon_3d",
                "lmg_dmg", 0, "lmg_range", 320, "lmg_spread", 0.5, "lmg_rps", 0.35,
                "lmg_mag", 4, "lmg_reload", 70, "lmg_pierce", 1, "lmg_bspeed", 200,
                "lmg_pellets", 1, "lmg_bars", 2, "aam_launcher", 1,
                "mpower", 11, "mspeed", 3.4, "mstart", 1.4, "maccelt", 34,
                "mseekdelay", 8, "mturn", 0.55, "mlead", 0.85, "mfuse", 3.2,
                "mlife", 600, "mseek", 320);
    }

    /** 反物质狙击炮：双条蓄力，一枪穿一排喵 */
    public static ItemStack antimatterSniper() {
        return gunWithModel("aamflight.gun.antimatter_sniper", ChatFormatting.DARK_PURPLE, "aamflight:antimatter_sniper_3d",
                "lmg_dmg", 120, "lmg_range", 768, "lmg_spread", 0.0, "lmg_rps", 0.25,
                "lmg_mag", 1, "lmg_reload", 80, "lmg_pierce", 8, "lmg_bspeed", 2200,
                "lmg_pellets", 1, "lmg_bars", 2, "lmg_bullet_type", 5);
    }

    // ==================== 预设枪械喵 ====================

    /** 带独立贴图的预设枪喵 */
    private static ItemStack gunWithModel(String name, ChatFormatting color, String modelId, Object... pairs) {
        ItemStack stack = gun(name, color, pairs);
        Identifier model = Identifier.parse(modelId);
        stack.set(DataComponents.ITEM_MODEL, model);
        return stack;
    }
    private static ItemStack gun(String name, ChatFormatting color, Object... pairs) {
        ItemStack stack = new ItemStack(LMG_GUN);
        stack.set(DataComponents.ITEM_NAME, Component.translatable(name)
                .withStyle(s -> s.withColor(color).withItalic(false)));
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            ItemStats.setBase(stack, (String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return stack;
    }

    /** 狙击枪：一枪一个，极精准、低射速喵 */
    public static ItemStack sniperRifle() {
        return gunWithModel("aamflight.gun.sniper_rifle", ChatFormatting.AQUA, "aamflight:sniper_rifle_3d",
                "lmg_dmg", 45, "lmg_range", 512, "lmg_spread", 0.05, "lmg_rps", 1.2,
                "lmg_mag", 5, "lmg_reload", 50, "lmg_pierce", 4, "lmg_bspeed", 1400,
                "lmg_pellets", 1);
    }

    /** 射手步枪：中远距离半自动喵 */
    public static ItemStack marksmanRifle() {
        return gunWithModel("aamflight.gun.marksman_rifle", ChatFormatting.GOLD, "aamflight:marksman_rifle_3d",
                "lmg_dmg", 18, "lmg_range", 320, "lmg_spread", 0.4, "lmg_rps", 4.0,
                "lmg_mag", 20, "lmg_reload", 48, "lmg_pierce", 2, "lmg_bspeed", 900,
                "lmg_pellets", 1);
    }

    /** 突击步枪：折中通用喵 */
    public static ItemStack assaultRifle() {
        return gunWithModel("aamflight.gun.assault_rifle", ChatFormatting.YELLOW, "aamflight:assault_rifle_3d",
                "lmg_dmg", 8, "lmg_range", 160, "lmg_spread", 1.6, "lmg_rps", 10.0,
                "lmg_mag", 30, "lmg_reload", 50, "lmg_pierce", 2, "lmg_bspeed", 620,
                "lmg_pellets", 1);
    }

    /** 冲锋枪：高射速、近距、散布大喵 */
    public static ItemStack smg() {
        return gunWithModel("aamflight.gun.smg", ChatFormatting.WHITE, "aamflight:smg_3d",
                "lmg_dmg", 5, "lmg_range", 64, "lmg_spread", 2.8, "lmg_rps", 20.0,
                "lmg_mag", 45, "lmg_reload", 40, "lmg_pierce", 1, "lmg_bspeed", 480,
                "lmg_pellets", 1);
    }

    /** 霰弹枪：多弹丸、近距离大伤害喵 */
    public static ItemStack shotgun() {
        return gunWithModel("aamflight.gun.shotgun", ChatFormatting.RED, "aamflight:shotgun_3d",
                "lmg_dmg", 4, "lmg_range", 24, "lmg_spread", 7.0, "lmg_rps", 0.7,
                "lmg_mag", 8, "lmg_reload", 60, "lmg_pierce", 1, "lmg_bspeed", 400,
                "lmg_pellets", 10, "lmg_bars", 2);
    }

    /** 加特林：超高射速 + 大散布喵 */
    public static ItemStack gatling() {
        return gunWithModel("aamflight.gun.gatling", ChatFormatting.DARK_RED, "aamflight:gatling_3d",
                "lmg_dmg", 5, "lmg_range", 96, "lmg_spread", 9.0, "lmg_rps", 35.0,
                "lmg_mag", 300, "lmg_reload", 100, "lmg_pierce", 1, "lmg_bspeed", 700,
                "lmg_pellets", 1);
    }

    /** 榴弹发射器：发射模组导弹喵 */
    public static ItemStack grenadeLauncher() {
        ItemStack stack = gunWithModel("aamflight.gun.grenade_launcher", ChatFormatting.DARK_GREEN, "aamflight:grenade_launcher_3d",
                "lmg_dmg", 0, "lmg_range", 256, "lmg_spread", 0.6, "lmg_rps", 0.45,
                "lmg_mag", 6, "lmg_reload", 60, "lmg_pierce", 1, "lmg_bspeed", 200,
                "lmg_pellets", 1,
                "mpower", 8, "mspeed", 3.0, "mstart", 1.2, "maccelt", 40,
                "mseekdelay", 10, "mturn", 0.5, "mlead", 0.8, "mfuse", 2.6,
                "mlife", 500, "mseek", 256, "lmg_bars", 2);
        net.minecraft.world.item.component.CustomData data =
                stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        net.minecraft.nbt.CompoundTag tag = data == null ? new net.minecraft.nbt.CompoundTag() : data.copyTag();
        tag.putBoolean("aam_launcher", true);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
        return stack;
    }

    /** RWR 头盔：护甲 4 / 韧性 3 / 耐久 3000 / 绿色雷达喵 */
    public static ItemStack rwrHelmet() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("aam_rwr", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.rwr_helmet").withStyle(ChatFormatting.GREEN));
        stack.set(DataComponents.MAX_DAMAGE, 3000);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(List.of(
                entry(Attributes.ARMOR, 4.0, "rwr_armor"),
                entry(Attributes.ARMOR_TOUGHNESS, 3.0, "rwr_toughness")
        )));
        return stack;
    }

    /** 推进装置：护甲 4 / 韧性 4 / 耐久 12000 / 煤炭修理喵 */
    public static ItemStack thruster() {
        ItemStack stack = new ItemStack(Items.NETHERITE_BOOTS);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("aam_thruster", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.thruster").withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.MAX_DAMAGE, 12000);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(List.of(
                entry(Attributes.ARMOR, 4.0, "thruster_armor"),
                entry(Attributes.ARMOR_TOUGHNESS, 4.0, "thruster_toughness")
        )));
        stack.set(DataComponents.REPAIRABLE, new net.minecraft.world.item.enchantment.Repairable(
                net.minecraft.core.HolderSet.direct(Items.COAL.builtInRegistryHolder())));
        return stack;
    }

    /** 带「空对空导弹」附魔的弩喵 */
    public static ItemStack aamCrossbow(HolderLookup.Provider holders) {
        ItemStack stack = new ItemStack(Items.CROSSBOW);
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.aamflight.aam_crossbow").withStyle(ChatFormatting.AQUA));
        try {
            HolderLookup.RegistryLookup<Enchantment> lookup = holders.lookupOrThrow(Registries.ENCHANTMENT);
            Holder<Enchantment> ench = lookup.getOrThrow(AAM_MISSILE_KEY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(ench, 3);
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        } catch (Exception ignored) {
            // 附魔没注册上就算了，至少给个弩喵
        }
        return stack;
    }

    private static ItemAttributeModifiers.Entry entry(Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                                                      double amount, String id) {
        return new ItemAttributeModifiers.Entry(
                attr,
                new AttributeModifier(Identifier.fromNamespaceAndPath("aamflight", id), amount,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR
        );
    }
}
