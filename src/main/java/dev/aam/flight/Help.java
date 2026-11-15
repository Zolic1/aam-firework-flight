package dev.aam.flight;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

/** /aam help 的帮助文本喵（全部走语言文件，支持中英双语；子弹类型可点击喵） */
public final class Help {

    private Help() {}

    /** 空字符串 = 空行；其余都是语言键喵 */
    private static final String[] LINES = {
        "aamflight.help.title",
        "aamflight.help.subtitle",
        "",
        "aamflight.help.section.commands",
        "aamflight.help.cmd_help",
        "aamflight.help.cmd_list",
        "aamflight.help.cmd_get",
        "aamflight.help.cmd_set",
        "aamflight.help.cmd_reset",
        "aamflight.help.cmd_base_set",
        "aamflight.help.cmd_base_add",
        "aamflight.help.cmd_base_get",
        "aamflight.help.cmd_base_clear",
        "aamflight.help.cmd_att_set",
        "aamflight.help.cmd_att_add",
        "aamflight.help.cmd_att_get",
        "aamflight.help.cmd_att_remove",
        "aamflight.help.cmd_att_list",
        "aamflight.help.cmd_item_get",
        "aamflight.help.cmd_item_clear",
        "aamflight.help.cmd_clearall",
        "aamflight.help.cmd_debug",
        "aamflight.help.cmd_save",
        "aamflight.help.note_perm",
        "",
        "aamflight.help.section.params",
        "aamflight.help.param_1",
        "aamflight.help.param_2",
        "aamflight.help.param_3",
        "aamflight.help.param_4",
        "",
        "aamflight.help.section.missile",
        "aamflight.help.missile_1",
        "aamflight.help.missile_2",
        "aamflight.help.missile_3",
        "",
        "aamflight.help.section.bullets",
        "aamflight.bullet.1",
        "aamflight.bullet.2",
        "aamflight.bullet.3",
        "aamflight.bullet.4",
        "aamflight.bullet.5",
        "aamflight.help.bullet_note",
        "",
        "aamflight.help.section.recipes",
        "aamflight.help.recipe.sniper_rifle",
        "aamflight.help.recipe.marksman_rifle",
        "aamflight.help.recipe.assault_rifle",
        "aamflight.help.recipe.smg",
        "aamflight.help.recipe.shotgun",
        "aamflight.help.recipe.gatling",
        "aamflight.help.recipe.grenade_launcher",
        "aamflight.help.recipe.pulse_rifle",
        "aamflight.help.recipe.plasma_rifle",
        "aamflight.help.recipe.railgun",
        "aamflight.help.recipe.arc_smg",
        "aamflight.help.recipe.phase_shotgun",
        "aamflight.help.recipe.nano_mg",
        "aamflight.help.recipe.plasma_cannon",
        "aamflight.help.recipe.antimatter_sniper",
        "aamflight.help.recipe.laser_rifle",
        "aamflight.help.recipe.cryo_rifle",
        "aamflight.help.recipe.venom_sprayer",
        "aamflight.help.recipe.tesla_gun",
        "aamflight.help.recipe.explosive_mg",
        "aamflight.help.recipe.smart_pistol",
        "aamflight.help.recipe.kinetic_cannon",
        "aamflight.help.recipe.laser_sniper",
        "",
        "aamflight.help.section.others",
        "aamflight.help.other_1",
        "aamflight.help.other_2",
        "aamflight.help.other_3",
        "",
        "aamflight.help.section.rules",
        "aamflight.help.rule_1",
        "aamflight.help.rule_2",
        "",
        "aamflight.help.section.tips",
        "aamflight.help.tip_1",
        "aamflight.help.tip_2",
        "aamflight.help.tip_3",
    };

    public static int send(CommandSourceStack src) {
        for (String key : LINES) {
            if (key.isEmpty()) {
                src.sendSuccess(() -> Component.literal(""), false);
                continue;
            }
            if (key.startsWith("aamflight.bullet.")) {
                final int idx = Integer.parseInt(key.substring("aamflight.bullet.".length()));
                src.sendSuccess(() -> Component.literal("\u25b6 ")
                        .append(Component.translatable(key))
                        .withStyle(s -> s
                                .withColor(net.minecraft.ChatFormatting.AQUA)
                                .withClickEvent(new ClickEvent.RunCommand("/aam lmg_bullet_type " + idx))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.translatable("aamflight.help.click_to_apply")))), false);
                continue;
            }
            boolean title = key.endsWith(".title");
            boolean section = key.contains(".section.");
            src.sendSuccess(() -> Component.translatable(key).withStyle(s -> s
                    .withColor(title ? net.minecraft.ChatFormatting.AQUA
                            : section ? net.minecraft.ChatFormatting.YELLOW
                            : net.minecraft.ChatFormatting.GRAY)), false);
        }
        return 1;
    }
}
