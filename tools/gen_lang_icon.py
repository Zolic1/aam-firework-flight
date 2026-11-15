import json, os, sys
from PIL import Image, ImageDraw

RES = sys.argv[1]
lang_dir = os.path.join(RES, 'assets', 'aamflight', 'lang')
os.makedirs(lang_dir, exist_ok=True)

zh = {
    "item.aamflight.lmg_gun": "模组枪械",
    "item.aamflight.ammo": "轻机枪弹药",
    "item.aamflight.flare": "干扰弹",
    "item.aamflight.missile_display": "导弹",

    "itemGroup.aamflight.main": "AAM 空对空导弹",

    "gamerule.aamflight.destroyBlocks": "模组爆炸破坏方块",
    "gamerule.aamflight.destroyBlocks.description": "导弹与榴弹爆炸是否破坏地形喵",
    "gamerule.aamflight.mobArmor": "生物护甲加成",
    "gamerule.aamflight.mobArmor.description": "是否让数据包为生物设置 20 点护甲喵",

    "enchantment.aamflight.aam_missile": "空对空导弹",
    "enchantment.aamflight.aam_missile.desc": "射出的弹丸会变成追踪导弹喵",

    "effect.aamflight.none": "",
    "aamflight.help.title": "AAM 空对空导弹模组",
}

en = {
    "item.aamflight.lmg_gun": "Modular Gun",
    "item.aamflight.ammo": "LMG Ammunition",
    "item.aamflight.flare": "Countermeasure Flare",
    "item.aamflight.missile_display": "Missile",

    "itemGroup.aamflight.main": "AAM Air-to-Air Missiles",

    "gamerule.aamflight.destroyBlocks": "Mod explosions destroy blocks",
    "gamerule.aamflight.destroyBlocks.description": "Whether missile and grenade explosions destroy terrain",
    "gamerule.aamflight.mobArmor": "Mob armor bonus",
    "gamerule.aamflight.mobArmor.description": "Whether the datapack gives mobs 20 armor points",

    "enchantment.aamflight.aam_missile": "Air-to-Air Missile",
    "enchantment.aamflight.aam_missile.desc": "Fired projectiles become homing missiles",

    "aamflight.help.title": "AAM Air-to-Air Missile Mod",
}

for name, data in (('zh_cn', zh), ('en_us', en)):
    with open(os.path.join(lang_dir, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# ---------- 图标 128x128 喵 ----------
S = 128
im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(im)

# 深色圆角底喵
d.rounded_rectangle([4, 4, S - 5, S - 5], radius=22, fill=(24, 30, 34, 255))
d.rounded_rectangle([8, 8, S - 9, S - 9], radius=18, outline=(0, 255, 102, 255), width=3)

# 雷达弧喵
for r, w in ((48, 3), (34, 2)):
    d.arc([S // 2 - r, S // 2 - r, S // 2 + r, S // 2 + r], start=200, end=340,
          fill=(0, 255, 102, 255), width=w)

# 导弹（斜向上飞喵）
d.polygon([(36, 92), (86, 42), (96, 52), (46, 102)], fill=(190, 195, 205, 255))
d.polygon([(86, 42), (100, 28), (102, 46), (96, 52)], fill=(255, 90, 90, 255))
d.polygon([(46, 92), (30, 108), (44, 102)], fill=(255, 90, 90, 255))
# 尾焰喵
d.polygon([(36, 92), (18, 110), (40, 104)], fill=(255, 200, 60, 255))
d.ellipse([12, 104, 26, 118], fill=(255, 140, 40, 255))

im.save(os.path.join(RES, 'assets', 'aamflight', 'icon.png'), 'PNG')
print('lang + icon 生成完成喵')
