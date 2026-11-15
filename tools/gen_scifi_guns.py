import json, os, sys
from PIL import Image, ImageDraw

RES = sys.argv[1]
tex = os.path.join(RES, 'assets', 'aamflight', 'textures', 'item')
mdl = os.path.join(RES, 'assets', 'aamflight', 'models', 'item')
itm = os.path.join(RES, 'assets', 'aamflight', 'items')
rec = os.path.join(RES, 'data', 'aamflight', 'recipe')
for d in (tex, mdl, itm, rec):
    os.makedirs(d, exist_ok=True)

UV = {'light': [0, 0, 8, 8], 'dark': [8, 0, 16, 8], 'black': [0, 8, 8, 16], 'accent': [8, 8, 16, 16]}


def palette(name, accent):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 7, 7], fill=(150, 152, 158, 255))
    d.rectangle([8, 0, 15, 7], fill=(88, 90, 96, 255))
    d.rectangle([0, 8, 7, 15], fill=(38, 38, 42, 255))
    d.rectangle([8, 8, 15, 15], fill=accent)
    im.save(os.path.join(tex, name + '.png'), 'PNG')


def cube(f, t, k):
    uv = UV[k]
    return {"from": f, "to": t, "faces": {x: {"uv": uv, "texture": "#body"} for x in
            ("north", "south", "east", "west", "up", "down")}}


def emit(name, texname, cubes, gui=0.85):
    m = {
        "credit": "Generated for AAM by Yuike",
        "textures": {"body": f"aamflight:item/{texname}", "particle": "#body"},
        "elements": cubes,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 50], "translation": [0, 3.5, 0.5], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_lefthand": {"rotation": [0, 90, -50], "translation": [0, 3.5, 0.5], "scale": [0.85, 0.85, 0.85]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [-0.4, 1.6, 0.2], "scale": [0.5, 0.5, 0.5]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [-0.4, 1.6, 0.2], "scale": [0.5, 0.5, 0.5]},
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [gui, gui, gui]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
        },
    }
    with open(os.path.join(mdl, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump(m, f, ensure_ascii=False, indent=2)
    with open(os.path.join(itm, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump({"model": {"type": "minecraft:model", "model": f"aamflight:item/{name}"}},
                  f, ensure_ascii=False, indent=2)


def recipe(gid, name, color, key_item, stats):
    cd = {}
    for k, v in stats.items():
        cd['b_' + k] = v
    cd['aam_lore_stale'] = 1
    r = {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["III", "XCX", "III"],
        "key": {"I": "minecraft:iron_ingot", "X": key_item, "C": "minecraft:crossbow"},
        "result": {
            "id": "aamflight:lmg_gun",
            "count": 1,
            "components": {
                "minecraft:item_name": {"text": name, "color": color, "italic": False},
                "minecraft:custom_data": cd,
                "minecraft:item_model": f"aamflight:{gid}_3d",
            },
        },
    }
    with open(os.path.join(rec, gid + '.json'), 'w', encoding='utf-8') as f:
        json.dump(r, f, ensure_ascii=False, indent=2)


# ============ 1. 脉冲步枪 pulse_rifle ============
palette('pulse_rifle_palette', (90, 200, 255, 255))
emit('pulse_rifle_3d', 'pulse_rifle_palette', [
    cube([2, 7.0, 1.8], [14, 9.2, 3.2], 'dark'),
    cube([13, 7.4, 2.1], [16, 8.8, 2.9], 'light'),
    cube([6, 9.2, 2.2], [10, 10.4, 3.0], 'accent'),
    cube([4, 4.0, 2.0], [7, 7.0, 3.0], 'black'),
    cube([8.5, 3.2, 2.0], [11, 7.0, 3.0], 'light'),
    cube([1, 7.2, 2.2], [3, 9.0, 2.8], 'light'),
])
recipe('pulse_rifle', '脉冲步枪', 'aqua', 'minecraft:amethyst_shard',
       {'lmg_dmg': 9, 'lmg_range': 200, 'lmg_spread': 1.2, 'lmg_rps': 12, 'lmg_mag': 40,
        'lmg_reload': 44, 'lmg_pierce': 2, 'lmg_bspeed': 700, 'lmg_pellets': 1, 'lmg_bullet_type': 3})

# ============ 2. 等离子步枪 plasma_rifle ============
palette('plasma_rifle_palette', (190, 110, 255, 255))
emit('plasma_rifle_3d', 'plasma_rifle_palette', [
    cube([2, 6.8, 1.8], [14, 9.4, 3.4], 'dark'),
    cube([13, 7.2, 2.0], [16, 8.8, 3.0], 'light'),
    cube([7, 9.4, 2.0], [11, 11.0, 3.2], 'accent'),
    cube([4, 3.6, 2.2], [7, 6.8, 3.2], 'black'),
    cube([8, 4.2, 2.2], [11, 6.8, 3.2], 'accent'),
    cube([1, 7.0, 2.2], [3, 9.2, 3.0], 'light'),
])
recipe('plasma_rifle', '等离子步枪', 'light_purple', 'minecraft:glowstone_dust',
       {'lmg_dmg': 16, 'lmg_range': 260, 'lmg_spread': 1.0, 'lmg_rps': 6, 'lmg_mag': 24,
        'lmg_reload': 52, 'lmg_pierce': 2, 'lmg_bspeed': 620, 'lmg_pellets': 1, 'lmg_bullet_type': 4})

# ============ 3. 磁轨炮 railgun ============
palette('railgun_palette', (80, 240, 230, 255))
emit('railgun_3d', 'railgun_palette', [
    cube([1, 7.2, 2.2], [15, 8.8, 3.0], 'dark'),
    cube([2, 6.6, 1.9], [14, 9.4, 3.3], 'light'),
    cube([14, 7.4, 2.3], [16, 8.6, 2.9], 'accent'),
    cube([9, 9.4, 2.4], [13, 10.6, 3.0], 'black'),
    cube([5, 4.6, 2.2], [8, 7.2, 3.0], 'black'),
    cube([1, 7.8, 2.4], [3, 8.4, 2.8], 'accent'),
])
recipe('railgun', '磁轨炮', 'dark_aqua', 'minecraft:rail',
       {'lmg_dmg': 70, 'lmg_range': 640, 'lmg_spread': 0.02, 'lmg_rps': 0.5, 'lmg_mag': 3,
        'lmg_reload': 70, 'lmg_pierce': 6, 'lmg_bspeed': 1800, 'lmg_pellets': 1, 'lmg_bullet_type': 5})

# ============ 4. 电弧冲锋枪 arc_smg ============
palette('arc_smg_palette', (255, 230, 90, 255))
emit('arc_smg_3d', 'arc_smg_palette', [
    cube([2, 7.0, 1.9], [13, 9.2, 3.3], 'dark'),
    cube([12, 7.4, 2.2], [16, 8.8, 3.0], 'light'),
    cube([5, 3.8, 2.1], [8, 7.0, 3.1], 'black'),
    cube([8, 2.6, 2.1], [11, 7.0, 3.1], 'light'),
    cube([2, 9.2, 2.4], [5, 10.2, 2.9], 'accent'),
])
recipe('arc_smg', '电弧冲锋枪', 'yellow', 'minecraft:copper_block',
       {'lmg_dmg': 4, 'lmg_range': 80, 'lmg_spread': 3.2, 'lmg_rps': 26, 'lmg_mag': 60,
        'lmg_reload': 38, 'lmg_pierce': 1, 'lmg_bspeed': 520, 'lmg_pellets': 1, 'lmg_bullet_type': 2})

# ============ 5. 相位霰弹枪 phase_shotgun ============
palette('phase_shotgun_palette', (255, 130, 220, 255))
emit('phase_shotgun_3d', 'phase_shotgun_palette', [
    cube([2, 7.0, 1.7], [14, 9.6, 3.6], 'dark'),
    cube([12, 6.8, 1.8], [16, 8.6, 3.5], 'black'),
    cube([9, 7.8, 1.5], [12, 9.0, 3.8], 'accent'),
    cube([4, 3.6, 2.0], [7, 7.0, 3.2], 'black'),
    cube([1, 7.4, 2.2], [3, 9.4, 3.0], 'light'),
    cube([6, 9.6, 2.0], [10, 10.6, 3.2], 'accent'),
])
recipe('phase_shotgun', '相位霰弹枪', 'light_purple', 'minecraft:ender_pearl',
       {'lmg_dmg': 5, 'lmg_range': 28, 'lmg_spread': 7.5, 'lmg_rps': 0.8, 'lmg_mag': 8,
        'lmg_reload': 60, 'lmg_pierce': 1, 'lmg_bspeed': 420, 'lmg_pellets': 12,
        'lmg_bars': 3, 'lmg_bullet_type': 2})

# ============ 6. 纳米机枪 nano_mg ============
palette('nano_mg_palette', (120, 255, 150, 255))
emit('nano_mg_3d', 'nano_mg_palette', [
    cube([1, 6.9, 1.7], [15, 9.5, 3.5], 'dark'),
    cube([14, 7.3, 2.0], [16, 9.0, 3.2], 'light'),
    cube([5, 3.7, 2.0], [8, 6.9, 3.2], 'black'),
    cube([8.5, 2.9, 1.9], [12, 6.9, 3.5], 'accent'),
    cube([2, 2.7, 2.2], [4, 6.9, 3.0], 'light'),
    cube([9, 9.5, 2.3], [12, 10.4, 3.0], 'accent'),
])
recipe('nano_mg', '纳米机枪', 'green', 'minecraft:slime_ball',
       {'lmg_dmg': 6, 'lmg_range': 120, 'lmg_spread': 2.4, 'lmg_rps': 18, 'lmg_mag': 150,
        'lmg_reload': 90, 'lmg_pierce': 2, 'lmg_bspeed': 640, 'lmg_pellets': 1, 'lmg_bullet_type': 3})

# ============ 7. 等离子炮 plasma_cannon（发射导弹喵）============
palette('plasma_cannon_palette', (255, 90, 200, 255))
emit('plasma_cannon_3d', 'plasma_cannon_palette', [
    cube([2, 6.8, 1.4], [14, 9.8, 3.8], 'dark'),
    cube([13, 7.0, 1.7], [16, 9.6, 3.5], 'black'),
    cube([6, 3.2, 1.9], [10, 6.8, 3.4], 'accent'),
    cube([4, 4.0, 2.1], [6, 6.8, 3.1], 'black'),
    cube([1, 7.2, 2.0], [3, 9.4, 3.2], 'light'),
    cube([7, 9.8, 2.0], [9, 10.8, 3.2], 'accent'),
])
recipe('plasma_cannon', '等离子炮', 'dark_purple', 'minecraft:blaze_powder',
       {'lmg_dmg': 0, 'lmg_range': 320, 'lmg_spread': 0.5, 'lmg_rps': 0.35, 'lmg_mag': 4,
        'lmg_reload': 70, 'lmg_pierce': 1, 'lmg_bspeed': 200, 'lmg_pellets': 1,
        'aam_launcher': 1, 'lmg_bars': 2,
        'mpower': 11, 'mspeed': 3.4, 'mstart': 1.4, 'maccelt': 34, 'mseekdelay': 8,
        'mturn': 0.24, 'mlead': 0.85, 'mfuse': 3.2, 'mlife': 600, 'mseek': 320})

# ============ 8. 反物质狙击炮 antimatter_sniper ============
palette('antimatter_sniper_palette', (200, 160, 255, 255))
emit('antimatter_sniper_3d', 'antimatter_sniper_palette', [
    cube([1, 7.4, 2.3], [15, 8.8, 3.0], 'dark'),
    cube([2, 6.8, 2.0], [14, 9.4, 3.3], 'light'),
    cube([14, 7.6, 2.4], [16, 8.6, 2.9], 'accent'),
    cube([8, 9.4, 2.2], [13, 10.8, 3.1], 'black'),
    cube([9, 10.8, 2.5], [12, 11.6, 2.9], 'accent'),
    cube([5, 4.8, 2.2], [8, 7.4, 3.0], 'black'),
    cube([1, 8.0, 2.6], [3, 8.4, 2.8], 'light'),
], gui=0.95)
recipe('antimatter_sniper', '反物质狙击炮', 'dark_purple', 'minecraft:echo_shard',
       {'lmg_dmg': 120, 'lmg_range': 768, 'lmg_spread': 0.0, 'lmg_rps': 0.25, 'lmg_mag': 1,
        'lmg_reload': 80, 'lmg_pierce': 8, 'lmg_bspeed': 2200, 'lmg_pellets': 1,
        'lmg_bars': 2, 'lmg_bullet_type': 5})

print('8 把科幻枪生成完成喵')
