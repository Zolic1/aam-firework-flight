import json, os, sys

RES = sys.argv[1]
JAVA = sys.argv[2]
lang_dir = os.path.join(RES, 'assets', 'aamflight', 'lang')

# ========== 已有基础键（物品 / 创造标签 / 附魔 / 游戏规则 / 子弹名） ==========
BASE_ZH = {
    "item.aamflight.lmg_gun": "原型枪",
    "item.aamflight.ammo": "轻机枪弹药",
    "item.aamflight.flare": "干扰弹",
    "item.aamflight.missile_display": "导弹",
    "itemGroup.aamflight.main": "AAM 空对空导弹",
    "enchantment.aamflight.aam_missile": "空对空导弹",
    "enchantment.aamflight.aam_missile.desc": "射出的弹丸会变成追踪导弹喵",
    "gamerule.aamflight.destroyBlocks": "模组爆炸破坏方块",
    "gamerule.aamflight.destroyBlocks.description": "导弹与榴弹爆炸是否破坏地形喵",
    "gamerule.aamflight.mobArmor": "生物护甲加成",
    "gamerule.aamflight.mobArmor.description": "是否让数据包为生物设置 20 点护甲喵",
    "aamflight.bullet.1": "追踪[无尾迹]",
    "aamflight.bullet.2": "曳光燃烧弹[烟雾]",
    "aamflight.bullet.3": "通用弹药[烟雾]",
    "aamflight.bullet.4": "曳光燃烧弹[末地烛]",
    "aamflight.bullet.5": "破甲弹药[烟雾]",
}
BASE_EN = {
    "item.aamflight.lmg_gun": "Prototype Gun",
    "item.aamflight.ammo": "LMG Ammunition",
    "item.aamflight.flare": "Countermeasure Flare",
    "item.aamflight.missile_display": "Missile",
    "itemGroup.aamflight.main": "AAM Air-to-Air Missiles",
    "enchantment.aamflight.aam_missile": "Air-to-Air Missile",
    "enchantment.aamflight.aam_missile.desc": "Fired projectiles become homing missiles",
    "gamerule.aamflight.destroyBlocks": "Mod explosions destroy blocks",
    "gamerule.aamflight.destroyBlocks.description": "Whether missile and grenade explosions destroy terrain",
    "gamerule.aamflight.mobArmor": "Mob armor bonus",
    "gamerule.aamflight.mobArmor.description": "Whether the bundled datapack gives mobs 20 armor points",
    "aamflight.bullet.1": "Tracking [No Tracer]",
    "aamflight.bullet.2": "Tracer Incendiary [Smoke]",
    "aamflight.bullet.3": "Standard Ammo [Smoke]",
    "aamflight.bullet.4": "Tracer Incendiary [End Rod]",
    "aamflight.bullet.5": "Armor-Piercing [Smoke]",
}

GUNS = [
    ('sniper_rifle', '狙击枪', 'Sniper Rifle'),
    ('marksman_rifle', '射手步枪', 'Marksman Rifle'),
    ('assault_rifle', '突击步枪', 'Assault Rifle'),
    ('smg', '冲锋枪', 'SMG'),
    ('shotgun', '霰弹枪', 'Shotgun'),
    ('gatling', '加特林', 'Gatling Gun'),
    ('grenade_launcher', '榴弹发射器', 'Grenade Launcher'),
    ('pulse_rifle', '脉冲步枪', 'Pulse Rifle'),
    ('plasma_rifle', '等离子步枪', 'Plasma Rifle'),
    ('railgun', '磁轨炮', 'Railgun'),
    ('arc_smg', '电弧冲锋枪', 'Arc SMG'),
    ('phase_shotgun', '相位霰弹枪', 'Phase Shotgun'),
    ('nano_mg', '纳米机枪', 'Nano Machine Gun'),
    ('plasma_cannon', '等离子炮', 'Plasma Cannon'),
    ('antimatter_sniper', '反物质狙击炮', 'Antimatter Sniper Cannon'),
    ('laser_rifle', '激光步枪', 'Laser Rifle'),
    ('cryo_rifle', '霜冻枪', 'Cryo Rifle'),
    ('venom_sprayer', '毒液喷射器', 'Venom Sprayer'),
    ('tesla_gun', '链式特斯拉枪', 'Tesla Chain Gun'),
    ('explosive_mg', '爆裂机枪', 'Explosive MG'),
    ('smart_pistol', '智能追踪手枪', 'Smart Pistol'),
    ('kinetic_cannon', '冲压炮', 'Kinetic Cannon'),
    ('laser_sniper', '激光狙击炮', 'Laser Sniper Cannon'),
]

# ========== Help 行（顺序 = 显示顺序喵） ==========
# (key, 中文, English) —— key 以 section. 开头的是黄色小标题喵
HELP = [
    ("aamflight.help.title", "===== AAM 空对空导弹模组 =====", "===== AAM Air-to-Air Missile Mod ====="),
    ("aamflight.help.subtitle", "真·子弹枪械 + 追踪导弹 + 视线锁定 + 战雷式 RWR 雷达喵",
     "Real-bullet guns, homing missiles, line-of-sight lock-on and a War Thunder style RWR"),
    ("", "", ""),

    ("aamflight.help.section.commands", "--- 指令用法 ---", "--- Commands ---"),
    ("aamflight.help.cmd_help", "/aam help  —— 显示本帮助喵", "/aam help  — show this help"),
    ("aamflight.help.cmd_list", "/aam list  —— 列出全部选项名喵", "/aam list  — list all option names"),
    ("aamflight.help.cmd_get", "/aam <选项>  —— 查看全局值喵", "/aam <option>  — show the global value"),
    ("aamflight.help.cmd_set", "/aam <选项> <数值>  —— 设置全局值（影响所有枪喵）",
     "/aam <option> <value>  — set the global value (affects all guns)"),
    ("aamflight.help.cmd_reset", "/aam <选项> reset  —— 全局恢复默认喵", "/aam <option> reset  — restore the global default"),
    ("aamflight.help.cmd_base_set", "/aam <选项> base set <目标> <值>  —— 改手上的枪：基础值喵",
     "/aam <option> base set <targets> <value>  — set the held gun's base value"),
    ("aamflight.help.cmd_base_add", "/aam <option> base add <目标> <值>  —— 基础值叠加喵",
     "/aam <option> base add <targets> <value>  — add to the base value"),
    ("aamflight.help.cmd_base_get", "/aam <选项> base get <目标>  —— 查看基础值喵",
     "/aam <option> base get <targets>  — show the base value"),
    ("aamflight.help.cmd_base_clear", "/aam <选项> base clear <目标>  —— 清除基础值喵",
     "/aam <option> base clear <targets>  — clear the base value"),
    ("aamflight.help.cmd_att_set", "/aam <选项> att set <目标> <id> <值>  —— 设置属性喵",
     "/aam <option> att set <targets> <id> <value>  — set an attribute"),
    ("aamflight.help.cmd_att_add", "/aam <选项> att add <目标> <id> <值>  —— 叠加属性喵",
     "/aam <option> att add <targets> <id> <value>  — add to an attribute"),
    ("aamflight.help.cmd_att_get", "/aam <选项> att get <目标> <id>  —— 查看属性喵",
     "/aam <option> att get <targets> <id>  — show an attribute"),
    ("aamflight.help.cmd_att_remove", "/aam <选项> att remove <目标> <id>  —— 移除属性喵",
     "/aam <option> att remove <targets> <id>  — remove an attribute"),
    ("aamflight.help.cmd_att_list", "/aam <选项> att list <目标>  —— 列出属性喵",
     "/aam <option> att list <targets>  — list attributes"),
    ("aamflight.help.cmd_item_get", "/aam <选项> get <目标>  —— 最终生效值喵",
     "/aam <option> get <targets>  — final effective value"),
    ("aamflight.help.cmd_item_clear", "/aam <选项> clear <目标>  —— 清空该选项覆盖喵",
     "/aam <option> clear <targets>  — clear this option on the item"),
    ("aamflight.help.cmd_clearall", "/aam clearall <目标>  —— 清空全部覆盖喵",
     "/aam clearall <targets>  — clear every override"),
    ("aamflight.help.cmd_debug", "/aam debug  —— 诊断手上枪状态喵", "/aam debug  — inspect the held gun"),
    ("aamflight.help.cmd_save", "/aam save / /aam reload  —— 保存 / 重读全局配置喵",
     "/aam save / /aam reload  — save or reload the global config"),
    ("aamflight.help.note_perm", "（全部指令需要权限等级 4，且只作用于手上拿着的枪喵）",
     "(All commands require permission level 4 and only work on the gun held in hand)"),
    ("", "", ""),

    ("aamflight.help.section.params", "--- 射击参数（lmg_*）---", "--- Gun parameters (lmg_*) ---"),
    ("aamflight.help.param_1", "lmg_dmg 伤害 / lmg_range 距离 / lmg_pierce 穿透 / lmg_rps 射速",
     "lmg_dmg damage / lmg_range range / lmg_pierce pierce / lmg_rps rounds per second"),
    ("aamflight.help.param_2", "lmg_bspeed 弹速 / lmg_spread 散布 / lmg_pellets 弹丸数",
     "lmg_bspeed bullet speed / lmg_spread spread / lmg_pellets pellets per shot"),
    ("aamflight.help.param_3", "lmg_mag 弹夹 / lmg_reload 换弹 / lmg_cost 换弹分数",
     "lmg_mag magazine / lmg_reload reload ticks / lmg_cost reload score cost"),
    ("aamflight.help.param_4", "lmg_bars 蓄力条数 / lmg_lockout 连发间隔 / lmg_kb 击退 / lmg_tracer 尾迹间隔",
     "lmg_bars charge bars / lmg_lockout burst gap / lmg_kb knockback / lmg_tracer tracer interval"),
    ("", "", ""),

    ("aamflight.help.section.missile", "--- 导弹参数（m*）---", "--- Missile parameters (m*) ---"),
    ("aamflight.help.missile_1", "mspeed 最高速 / mstart 出膛速 / maccelt 加速时间 / mseekdelay 索敌延迟",
     "mspeed max speed / mstart launch speed / maccelt accel ticks / mseekdelay seek delay"),
    ("aamflight.help.missile_2", "mturn 转向 / mlead 提前量 / mfuse 引信 / mpower 爆炸当量 / mlife 寿命 / mseek 索敌距离",
     "mturn turn rate / mlead lead / mfuse fuse radius / mpower blast power / mlife lifetime / mseek seek range"),
    ("aamflight.help.missile_3", "mretrack 重追踪次数 / mflare 抗干扰 / mstealth 隐蔽",
     "mretrack re-track count / mflare countermeasure resistance / mstealth stealth"),
    ("", "", ""),

    ("aamflight.help.section.bullets", "--- 子弹类型（点击直接设置喵）---", "--- Bullet types (click to apply) ---"),
    ("aamflight.bullet.1", "追踪[无尾迹]：无尾迹 · 破甲 22.5%", "Tracking [No Tracer]: no tracer · 22.5% armor pierce"),
    ("aamflight.bullet.2", "曳光燃烧弹[烟雾]：每 2 发尾迹 · 燃烧 4.5% · 破甲 9.5%",
     "Tracer Incendiary [Smoke]: tracer every 2 · 4.5% ignite · 9.5% pierce"),
    ("aamflight.bullet.3", "通用弹药[烟雾]：每 6 发尾迹 · 燃烧 0.5% · 破甲 16.5%",
     "Standard Ammo [Smoke]: tracer every 6 · 0.5% ignite · 16.5% pierce"),
    ("aamflight.bullet.4", "曳光燃烧弹[末地烛]：每 3 发尾迹 · 燃烧 2.5% · 破甲 10.5%",
     "Tracer Incendiary [End Rod]: tracer every 3 · 2.5% ignite · 10.5% pierce"),
    ("aamflight.bullet.5", "破甲弹药[烟雾]：每 8 发尾迹 · 燃烧 1.5% · 破甲 30%",
     "Armor-Piercing [Smoke]: tracer every 8 · 1.5% ignite · 30% pierce"),
    ("aamflight.help.bullet_note", "互斥：已有其它 id 时 att add 会被拒绝，请用 att set 覆盖喵",
     "Exclusive: att add is rejected when another id exists, use att set instead"),
    ("", "", ""),

    ("aamflight.help.section.recipes", "--- 配方：枪械（3x3：中间弩 + 铁锭 + 两侧材料 x2）---",
     "--- Gun recipes (3x3: crossbow center, iron ingots, 2x material) ---"),
]

for gid, cn, eng in GUNS:
    HELP.append(("aamflight.help.recipe." + gid,
                 "aamflight.gun." + gid,  # 占位，下面用特殊处理
                 "aamflight.gun." + gid))

# 枪械配方行单独构造（材料名也双语喵）
MATS = {
    'sniper_rifle': ('望远镜', 'Spyglass'), 'marksman_rifle': ('玻璃', 'Glass'),
    'assault_rifle': ('红石粉', 'Redstone'), 'smg': ('铜锭', 'Copper Ingot'),
    'shotgun': ('火药', 'Gunpowder'), 'gatling': ('红石块', 'Redstone Block'),
    'grenade_launcher': ('TNT', 'TNT'), 'pulse_rifle': ('紫水晶碎片', 'Amethyst Shard'),
    'plasma_rifle': ('荧石粉', 'Glowstone Dust'), 'railgun': ('铁轨', 'Rail'),
    'arc_smg': ('铜块', 'Copper Block'), 'phase_shotgun': ('末影珍珠', 'Ender Pearl'),
    'nano_mg': ('黏液球', 'Slimeball'), 'plasma_cannon': ('烈焰粉', 'Blaze Powder'),
    'antimatter_sniper': ('回响碎片', 'Echo Shard'), 'laser_rifle': ('下界石英', 'Nether Quartz'),
    'cryo_rifle': ('浮冰', 'Packed Ice'), 'venom_sprayer': ('蜘蛛眼', 'Spider Eye'),
    'tesla_gun': ('避雷针', 'Lightning Rod'), 'explosive_mg': ('火焰弹', 'Fire Charge'),
    'smart_pistol': ('比较器', 'Comparator'), 'kinetic_cannon': ('铁块', 'Iron Block'),
    'laser_sniper': ('钻石', 'Diamond'),
}

HELP_ZH = {}
HELP_EN = {}
final = []
for key, zh, en in HELP:
    final.append((key, zh, en))

# 用占位再追加配方行（去掉之前的占位喵）
final = [(k, z, e) for (k, z, e) in final if not k.startswith('aamflight.help.recipe.')]
for gid, cn, eng in GUNS:
    mcn, meng = MATS[gid]
    final.append(("aamflight.help.recipe." + gid,
                  f"{cn}：6 铁锭 + 2 {mcn} + 弩",
                  f"{eng}: 6 Iron Ingot + 2 {meng} + Crossbow"))

final += [
    ("", "", ""),
    ("aamflight.help.section.others", "--- 配方：其它 ---", "--- Other recipes ---"),
    ("aamflight.help.other_1", "轻机枪弹药：1 支箭 → 10 发（背包自动兑换分数喵）",
     "LMG Ammo: 1 Arrow -> 10 rounds (auto-converted to score)"),
    ("aamflight.help.other_2", "干扰弹：火药 + 红石 + 铁锭 → 4 个喵", "Flare: Gunpowder + Redstone + Iron Ingot -> 4"),
    ("aamflight.help.other_3", "RWR 头盔 / 推进装置：见创造栏喵", "RWR Helmet / Thruster: see the creative tab"),
    ("", "", ""),
    ("aamflight.help.section.rules", "--- 游戏规则 ---", "--- Game rules ---"),
    ("aamflight.help.rule_1", "/gamerule aamflightDestroyBlocks <true|false> —— 模组爆炸是否破坏方块喵",
     "/gamerule aamflightDestroyBlocks <true|false> — whether mod explosions destroy blocks"),
    ("aamflight.help.rule_2", "/gamerule aamflightMobArmor <true|false> —— 数据包是否为生物加护甲喵",
     "/gamerule aamflightMobArmor <true|false> — whether the datapack gives mobs armor"),
    ("", "", ""),
    ("aamflight.help.section.tips", "--- 玩法提示 ---", "--- Tips ---"),
    ("aamflight.help.tip_1", "· 准星对准目标保持 3 秒自动锁定，按 X 切换 / 取消锁定喵",
     "· Keep the crosshair on a target for 3s to auto-lock; press X to toggle"),
    ("aamflight.help.tip_2", "· 弹夹打空 → 换弹，消耗分数（弹药 1:1 换分数喵）",
     "· Empty magazine starts a reload which costs score (ammo converts 1:1)"),
    ("aamflight.help.tip_3", "· 榴弹发射器打的是追踪导弹，参数在物品属性里喵",
     "· The grenade launcher fires homing missiles; stats live on the item"),
]

zh = dict(BASE_ZH)
en = dict(BASE_EN)
for gid, cn, eng in GUNS:
    zh['aamflight.gun.' + gid] = cn
    en['aamflight.gun.' + gid] = eng
for key, z, e in final:
    if not key:
        continue
    zh[key] = z
    en[key] = e

for name, data in (('zh_cn', zh), ('en_us', en)):
    with open(os.path.join(lang_dir, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# ========== 生成 Help.java ==========
keys_java = []
for key, z, e in final:
    keys_java.append('        "%s",' % key)

src = '''package dev.aam.flight;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

/** /aam help 的帮助文本喵（全部走语言文件，支持中英双语；子弹类型可点击喵） */
public final class Help {

    private Help() {}

    /** 空字符串 = 空行；其余都是语言键喵 */
    private static final String[] LINES = {
%s
    };

    public static int send(CommandSourceStack src) {
        for (String key : LINES) {
            if (key.isEmpty()) {
                src.sendSuccess(() -> Component.literal(""), false);
                continue;
            }
            if (key.startsWith("aamflight.bullet.")) {
                final int idx = Integer.parseInt(key.substring("aamflight.bullet.".length()));
                src.sendSuccess(() -> Component.literal("\\u25b6 ")
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
''' % "\n".join(keys_java)

with open(JAVA, 'w', encoding='utf-8') as f:
    f.write(src)

print('lang 条目:', len(zh), '/', len(en), '  Help 行数:', len(final))
