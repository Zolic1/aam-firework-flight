package dev.aam.flight.config;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aam.flight.AamConfig;
import dev.aam.flight.Help;
import dev.aam.flight.gun.BulletType;
import dev.aam.flight.gun.FireState;
import dev.aam.flight.gun.LmgGun;
import dev.aam.flight.item.ItemStats;
import dev.aam.flight.score.Scores;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Map;

/**
 * 指令层喵：/aam 指令树（list / help / save / reload / debug / clearall / &lt;选项&gt; ...）
 * 只负责解析与提示，具体数值存在 {@link ConfigOption}，落盘交给 {@link ConfigStore} 喵。
 */
public final class ConfigCommand {

    private ConfigCommand() {}

    /** 建出 /aam 指令树（权限等级 4 喵） */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("aam")
                .requires(src -> src.permissions().hasPermission(
                        net.minecraft.server.permissions.Permissions.COMMANDS_OWNER));

        root.executes(ConfigCommand::listOptions);
        root.then(Commands.literal("help").executes(ctx -> Help.send(ctx.getSource())));
        root.then(Commands.literal("list").executes(ConfigCommand::listOptions));

        root.then(Commands.literal("save").executes(ctx -> {
            ConfigStore.save();
            ctx.getSource().sendSuccess(() -> Component.literal("[AAM] 全局配置已保存喵"), false);
            return 1;
        }));
        root.then(Commands.literal("reload").executes(ctx -> {
            ConfigStore.load();
            ctx.getSource().sendSuccess(() -> Component.literal("[AAM] 已重新读取全局配置喵"), false);
            return 1;
        }));

        root.then(Commands.literal("debug").executes(ConfigCommand::debug));

        root.then(Commands.literal("clearall")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx,
                                ItemStats::clearAll, "已清空该物品全部覆盖"))));

        for (ConfigOption option : AamConfig.options()) {
            root.then(buildOptionNode(option));
        }
        return root;
    }

    // ==================== 子指令喵 ====================

    private static int listOptions(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() ->
                Component.literal("[AAM] 选项: " + String.join(", ", AamConfig.optionNames())), false);
        return 1;
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("[AAM] 需要玩家执行喵"));
            return 0;
        }
        ItemStack stack = player.getMainHandItem();
        boolean gun = LmgGun.isGun(player.level(), stack);
        src.sendSuccess(() -> Component.literal(
                "[AAM] 手持=" + stack.getItem().toString()
                        + " 是枪=" + gun
                        + " 榴弹=" + LmgGun.isLauncher(stack)
                        + " 弹夹=" + LmgGun.getMag(stack) + "/" + ItemStats.effectiveInt(stack, "lmg_mag")
                        + " 分数=" + Scores.get(player)
                        + " 冷却=" + player.getCooldowns().isOnCooldown(stack)
                        + " 连发中=" + FireState.isFiring(player)), false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildOptionNode(ConfigOption option) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(option.name());

        // /aam <选项> → 查看全局值喵
        node.executes(ctx -> {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "[AAM] " + option.name() + " 全局 = " + option.format(option.get())), false);
            return 1;
        });

        // /aam <选项> <数值> → 设置全局值喵
        node.then(valueArgument(option)
                .executes(ctx -> {
                    option.set(DoubleArgumentType.getDouble(ctx, "value"));
                    ConfigStore.save();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[AAM] " + option.name() + " 全局 = " + option.format(option.get())), true);
                    return 1;
                }));

        node.then(buildBaseNode(option));
        node.then(buildAttrNode(option));

        // /aam <选项> get <目标> → 最终生效值喵
        node.then(Commands.literal("get")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx, stack -> ctx.getSource().sendSuccess(
                                () -> Component.literal("[AAM] 物品 " + option.name() + " = "
                                        + option.format(ItemStats.effective(stack, option.name()))), false), null))));

        // /aam <选项> clear <目标> → 清空覆盖喵
        node.then(Commands.literal("clear")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx,
                                stack -> ItemStats.clearOption(stack, option.name()), "该选项覆盖已清空"))));

        // /aam <选项> reset → 全局恢复默认喵
        node.then(Commands.literal("reset").executes(ctx -> {
            Double def = AamConfig.defaultValue(option.name());
            if (def != null) {
                option.set(def);
                ConfigStore.save();
            }
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "[AAM] " + option.name() + " 全局已恢复默认 = " + option.format(option.get())), true);
            return 1;
        }));

        return node;
    }

    /** base 子指令（物品基础覆盖喵） */
    private static LiteralArgumentBuilder<CommandSourceStack> buildBaseNode(ConfigOption option) {
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal("base");

        base.then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(valueArgument(option).executes(ctx -> forHeldGun(ctx,
                                stack -> ItemStats.setBase(stack, option.name(),
                                        DoubleArgumentType.getDouble(ctx, "value")),
                                "base 设为 " + option.format(DoubleArgumentType.getDouble(ctx, "value")))))));

        base.then(Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(valueArgument(option).executes(ctx -> forHeldGun(ctx,
                                stack -> ItemStats.setBase(stack, option.name(),
                                        ItemStats.getBase(stack, option.name())
                                                + DoubleArgumentType.getDouble(ctx, "value")),
                                "base 叠加 " + option.format(DoubleArgumentType.getDouble(ctx, "value")))))));

        base.then(Commands.literal("get")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx, stack -> {
                            double v = ItemStats.getBase(stack, option.name());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "[AAM] 物品 " + option.name() + " base = " + option.format(v)
                                            + (ItemStats.hasBase(stack, option.name()) ? "" : "（用全局值喵）")), false);
                        }, null))));

        base.then(Commands.literal("clear")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx,
                                stack -> ItemStats.clearBase(stack, option.name()), "已清除 base"))));

        return base;
    }

    /** att 子指令（属性叠加喵） */
    private static LiteralArgumentBuilder<CommandSourceStack> buildAttrNode(ConfigOption option) {
        LiteralArgumentBuilder<CommandSourceStack> att = Commands.literal("att");

        att.then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("id", StringArgumentType.string())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-100000, 100000))
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            double val = DoubleArgumentType.getDouble(ctx, "value");
                                            return forHeldGun(ctx, stack -> {
                                                if (option.exclusive()) {
                                                    ItemStats.clearAttrs(stack, option.name());   // 互斥：先清掉其它 id 喵
                                                }
                                                ItemStats.setAttr(stack, option.name(), id, val);
                                            }, "att set 完成");
                                        })))));

        att.then(Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("id", StringArgumentType.string())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-100000, 100000))
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            double add = DoubleArgumentType.getDouble(ctx, "value");
                                            // 互斥选项：已有别的 id 时拒绝 add 喵
                                            if (option.exclusive()) {
                                                boolean conflict = false;
                                                for (ServerPlayer p : EntityArgument.getPlayers(ctx, "targets")) {
                                                    var attrs = ItemStats.attrs(p.getMainHandItem(), option.name());
                                                    if (attrs.keySet().stream().anyMatch(k -> !k.equals(id))) {
                                                        conflict = true;
                                                        break;
                                                    }
                                                }
                                                if (conflict) {
                                                    ctx.getSource().sendFailure(Component.literal(
                                                            "[AAM] " + option.name()
                                                                    + " 是互斥选项，已有其它 id；请用 att set <目标> <id> <值> 覆盖喵"));
                                                    return 0;
                                                }
                                            }
                                            return forHeldGun(ctx, stack -> {
                                                double cur = ItemStats.attrs(stack, option.name()).getOrDefault(id, 0.0);
                                                ItemStats.setAttr(stack, option.name(), id, cur + add);
                                            }, "att add 完成");
                                        })))));

        att.then(Commands.literal("get")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> forHeldGun(ctx, stack -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    Double v = ItemStats.attrs(stack, option.name()).get(id);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "[AAM] 物品 " + option.name() + " att[" + id + "] = "
                                                    + (v == null ? "（无）" : option.format(v))), false);
                                }, null)))));

        att.then(Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> forHeldGun(ctx,
                                        stack -> ItemStats.removeAttr(stack, option.name(),
                                                StringArgumentType.getString(ctx, "id")),
                                        "att 已移除")))));

        att.then(Commands.literal("list")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> forHeldGun(ctx, stack -> {
                            Map<String, Double> attrs = ItemStats.attrs(stack, option.name());
                            String text = attrs.isEmpty() ? "（无）"
                                    : attrs.entrySet().stream()
                                    .map(e -> e.getKey() + "=" + option.format(e.getValue()))
                                    .reduce((a, b) -> a + ", " + b).orElse("（无）");
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "[AAM] 物品 " + option.name() + " att: " + text), false);
                        }, null))));

        return att;
    }

    // ==================== 工具喵 ====================

    /** 数值参数 + 子弹类型补全提示喵 */
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Double> valueArgument(
            ConfigOption option) {
        return Commands.argument("value", DoubleArgumentType.doubleArg(option.min(), option.max()))
                .suggests(bulletTypeSuggestions(option));
    }

    private static SuggestionProvider<CommandSourceStack> bulletTypeSuggestions(ConfigOption option) {
        return (ctx, builder) -> {
            if ("lmg_bullet_type".equals(option.name())) {
                for (int i = 1; i <= BulletType.count(); i++) {
                    builder.suggest(String.valueOf(i), Component.translatable("aamflight.bullet." + i));
                }
            }
            return builder.buildFuture();
        };
    }

    private interface StackAction {
        void run(ItemStack stack);
    }

    /**
     * 对「选中玩家手上的枪」执行操作喵；
     * 没人的手上有枪 → 返回 0（指令失败喵）。
     */
    private static int forHeldGun(CommandContext<CommandSourceStack> ctx, StackAction action, String message) {
        Collection<? extends ServerPlayer> targets;
        try {
            targets = EntityArgument.getPlayers(ctx, "targets");
        } catch (Exception e) {
            return 0;
        }

        int count = 0;
        for (ServerPlayer p : targets) {
            ItemStack stack = p.getMainHandItem();
            if (!LmgGun.isGun(p.level(), stack)) continue;
            action.run(stack);
            count++;
        }

        if (count == 0) {
            ctx.getSource().sendFailure(Component.literal("[AAM] 目标手上没有枪，执行失败喵"));
            return 0;
        }

        if (message != null) {
            final int total = count;
            ctx.getSource().sendSuccess(() ->
                    Component.literal("[AAM] " + message + "（" + total + " 把枪喵）"), false);
        }
        return count;
    }
}
