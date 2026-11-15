package dev.aam.flight;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.aam.flight.enchant.ModEnchantments;
import dev.aam.flight.net.FirePayload;
import dev.aam.flight.net.ReleaseLockPayload;
import dev.aam.flight.net.RadarPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import dev.aam.flight.gun.BulletManager;
import dev.aam.flight.gun.FireState;
import dev.aam.flight.gun.LmgGun;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import dev.aam.flight.score.DamageTracker;
import dev.aam.flight.score.Scoring;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class AamFlightMod implements ModInitializer {

    /** 全局目标速度（格/tick）== 1.5 约等于 30 格/s，与满级烟花相同 */
    public static double targetSpeed = 1.5;   // 兼容旧代码，实际用 AamConfig.targetSpeed 喵

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModItems.init();
        ModGameRules.init();   // 注册自定义游戏规则喵

        // SSUDC 做成可选内置数据包（建世界时能在数据包列表里勾选喵）
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("aam-flight", "ssudc"),
                net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("aam-firework-flight").orElseThrow(),
                Component.literal("SSUDC 数据包"),
                net.fabricmc.fabric.api.resource.ResourcePackActivationType.NORMAL);
        dev.aam.flight.config.ConfigStore.load();   // 读取 config/aam-flight.properties 喵
        ModEnchantments.init();
        ServerLifecycleEvents.SERVER_STARTED.register(Scoring::ensureObjective);

        // 轻机枪：右键 → 真子弹射线判定喵
        // 干扰弹：右键投撒喵
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack flareStack = player.getItemInHand(hand);
            if (!flareStack.is(ModItems.FLARE)) return InteractionResult.PASS;
            if (world.isClientSide()) return InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer sp) {
                Flares.deploy(sp);
                flareStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!LmgGun.isGun(world, stack)) return InteractionResult.PASS;

            // 冷却中（射击间隔 / 换弹）→ 直接失败，不放动画不摆手喵
            if (player instanceof ServerPlayer spReload && LmgGun.isReloading(spReload)) {
                return InteractionResult.FAIL;   // 换弹中不能开枪喵
            }

            // 拦截原版弩的蓄力/射击，连发由 tick 里处理喵
            if (world.isClientSide()) return InteractionResult.CONSUME;
            if (player instanceof ServerPlayer sp) {
                FireState.set(sp, true);
            }
            return InteractionResult.CONSUME;
        });
        // 进服自动解锁本模组配方喵
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> RecipeUnlock.unlock(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> dev.aam.flight.config.ConfigStore.save());
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(RuleMirror::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamage, damageTaken, blocked) -> DamageTracker.record(entity, source));
        PayloadTypeRegistry.playC2S().register(ReleaseLockPayload.TYPE, ReleaseLockPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ReleaseLockPayload.TYPE,
                (payload, ctx) -> LockManager.toggle(ctx.player()));

        // 轻机枪：按住右键喵
        PayloadTypeRegistry.playC2S().register(FirePayload.TYPE, FirePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FirePayload.TYPE,
                (payload, ctx) -> FireState.set(ctx.player(), payload.pressed()));

        // 子弹推进喵
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> BulletManager.tickAll());
        PayloadTypeRegistry.playS2C().register(RadarPayload.TYPE, RadarPayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // 统一配置指令喵
            dispatcher.register(dev.aam.flight.config.ConfigCommand.build());

            // 旧指令保留（等价于 /aam speed）喵
            dispatcher.register(Commands.literal("aamspeed")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(() ->
                                Component.literal("[AAM] 当前推进目标速度: " + AamConfig.targetSpeed + " 格/tick"), false);
                        return 1;
                    })
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 8.0))
                            .executes(ctx -> {
                                AamConfig.targetSpeed = DoubleArgumentType.getDouble(ctx, "value");
                                final double v = AamConfig.targetSpeed;
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("[AAM] 推进目标速度已设为 " + v + " 格/tick（约 " + Math.round(v * 20) + " 格/s）"), true);
                                return 1;
                            })));

            // 发射一枚模组版 AAM 导弹（吃当前视线锁定目标喵）
            dispatcher.register(Commands.literal("aamlaunch")
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        ServerLevel level = (ServerLevel) p.level();

                        AamMissile m = new AamMissile(ModEntities.AAM_MISSILE, level);
                        Vec3 look = p.getLookAngle();
                        m.setOwner(p);
                        m.setFromStack(p.getMainHandItem());
                        m.setPos(p.getX(), p.getEyeY() - 0.15, p.getZ());
                        m.setDeltaMovement(look.scale(2.0));
                        level.addFreshEntity(m);

                        ctx.getSource().sendSuccess(() ->
                                Component.literal("[AAM] 已发射导弹喵（追踪视线锁定目标）"), true);
                        return 1;
                    }));
        });
    }
}
