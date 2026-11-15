import json, os, sys

RES = sys.argv[1]
lang_dir = os.path.join(RES, 'assets', 'aamflight', 'lang')
os.makedirs(lang_dir, exist_ok=True)

GUNS = {
    'sniper_rifle': ('狙击枪', 'Sniper Rifle'),
    'marksman_rifle': ('射手步枪', 'Marksman Rifle'),
    'assault_rifle': ('突击步枪', 'Assault Rifle'),
    'smg': ('冲锋枪', 'SMG'),
    'shotgun': ('霰弹枪', 'Shotgun'),
    'gatling': ('加特林', 'Gatling Gun'),
    'grenade_launcher': ('榴弹发射器', 'Grenade Launcher'),
    'pulse_rifle': ('脉冲步枪', 'Pulse Rifle'),
    'plasma_rifle': ('等离子步枪', 'Plasma Rifle'),
    'railgun': ('磁轨炮', 'Railgun'),
    'arc_smg': ('电弧冲锋枪', 'Arc SMG'),
    'phase_shotgun': ('相位霰弹枪', 'Phase Shotgun'),
    'nano_mg': ('纳米机枪', 'Nano Machine Gun'),
    'plasma_cannon': ('等离子炮', 'Plasma Cannon'),
    'antimatter_sniper': ('反物质狙击炮', 'Antimatter Sniper Cannon'),
    'laser_rifle': ('激光步枪', 'Laser Rifle'),
    'cryo_rifle': ('霜冻枪', 'Cryo Rifle'),
    'venom_sprayer': ('毒液喷射器', 'Venom Sprayer'),
    'tesla_gun': ('链式特斯拉枪', 'Tesla Chain Gun'),
    'explosive_mg': ('爆裂机枪', 'Explosive MG'),
    'smart_pistol': ('智能追踪手枪', 'Smart Pistol'),
    'kinetic_cannon': ('冲压炮', 'Kinetic Cannon'),
    'laser_sniper': ('激光狙击炮', 'Laser Sniper Cannon'),
}

BULLETS = {
    '1': ('追踪[无尾迹]', 'Tracking [No Tracer]'),
    '2': ('曳光燃烧弹[烟雾]', 'Tracer Incendiary [Smoke]'),
    '3': ('通用弹药[烟雾]', 'Standard Ammo [Smoke]'),
    '4': ('曳光燃烧弹[末地烛]', 'Tracer Incendiary [End Rod]'),
    '5': ('破甲弹药[烟雾]', 'Armor-Piercing [Smoke]'),
}

zh = {
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

    "aamflight.help.title": "AAM 空对空导弹模组",
    "aamflight.help.subtitle": "简介：真·子弹枪械 + 追踪导弹 + 视线锁定 + 战雷式 RWR 雷达喵",
    "aamflight.help.commands": "--- 指令用法 ---",
    "aamflight.help.params": "--- 射击参数（lmg_*）---",
    "aamflight.help.missileparams": "--- 导弹参数（m*）---",
    "aamflight.help.bullets": "--- 子弹类型（点击直接设置喵）---",
    "aamflight.help.recipes": "--- 配方：枪械（3x3，中间弩 + 铁锭 + 两侧材料 x2 喵）---",
    "aamflight.help.otherrecipes": "--- 配方：其它 ---",
    "aamflight.help.rules": "--- 游戏规则 ---",
    "aamflight.help.tips": "--- 玩法提示 ---",
    "aamflight.help.tip1": "· 准星对准目标保持 3 秒自动锁定，按 X 切换 / 取消锁定喵",
    "aamflight.help.tip2": "· 弹夹打空 -> 换弹，消耗分数（弹药 1:1 换分数喵）",
    "aamflight.help.tip3": "· 榴弹发射器打的是追踪导弹，参数在物品属性里喵",
}

en = {
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

    "aamflight.help.title": "AAM Air-to-Air Missile Mod",
    "aamflight.help.subtitle": "Real-bullet guns, homing missiles, line-of-sight lock-on and a War Thunder style RWR",
    "aamflight.help.commands": "--- Commands ---",
    "aamflight.help.params": "--- Gun parameters (lmg_*) ---",
    "aamflight.help.missileparams": "--- Missile parameters (m*) ---",
    "aamflight.help.bullets": "--- Bullet types (click to apply) ---",
    "aamflight.help.recipes": "--- Gun recipes (3x3: crossbow center, iron ingots, 2x material) ---",
    "aamflight.help.otherrecipes": "--- Other recipes ---",
    "aamflight.help.rules": "--- Game rules ---",
    "aamflight.help.tips": "--- Tips ---",
    "aamflight.help.tip1": "Aim at a target for 3 seconds to auto-lock; press X to toggle",
    "aamflight.help.tip2": "Empty magazine triggers a reload which costs score (ammo converts 1:1)",
    "aamflight.help.tip3": "The grenade launcher fires homing missiles; stats live on the item",
}

for gid, (cn, eng) in GUNS.items():
    zh['aamflight.gun.' + gid] = cn
    en['aamflight.gun.' + gid] = eng

for i, (cn, eng) in BULLETS.items():
    zh['aamflight.bullet.' + i] = cn
    en['aamflight.bullet.' + i] = eng

for name, data in (('zh_cn', zh), ('en_us', en)):
    with open(os.path.join(lang_dir, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

print('lang 生成完成：', len(zh), '/', len(en), '条喵')
