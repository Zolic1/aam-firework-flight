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
    cd = {'b_' + k: v for k, v in stats.items()}
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


# 1. 激光步枪（hitscan，弹速 >= 900 触发喵）
palette('laser_rifle_palette', (120, 255, 255, 255))
emit('laser_rifle_3d', 'laser_rifle_palette', [
    cube([2, 7.0, 2.0], [14, 9.2, 3.2], 'dark'),
    cube([13, 7.4, 2.2], [16, 8.8, 3.0], 'accent'),
    cube([7, 9.2, 2.3], [12, 10.2, 3.0], 'black'),
    cube([4, 4.2, 2.0], [7, 7.0, 3.0], 'black'),
    cube([8.5, 3.4, 2.0], [11, 7.0, 3.0], 'light'),
    cube([2, 8.2, 2.4], [5, 9.6, 2.8], 'accent'),
])
recipe('laser_rifle', '激光步枪', 'aqua', 'minecraft:nether_quartz',
       {'lmg_dmg': 18, 'lmg_range': 128, 'lmg_spread': 0.0, 'lmg_rps': 8, 'lmg_mag': 30,
        'lmg_reload': 40, 'lmg_pierce': 5, 'lmg_bspeed': 1200, 'lmg_pellets': 1, 'lmg_bullet_type': 3})

# 2. 霜冻枪
palette('cryo_rifle_palette', (160, 220, 255, 255))
emit('cryo_rifle_3d', 'cryo_rifle_palette', [
    cube([2, 7.0, 1.9], [14, 9.2, 3.3], 'dark'),
    cube([13, 7.2, 2.1], [16, 9.0, 3.1], 'light'),
    cube([6, 9.2, 2.2], [11, 10.4, 3.0], 'accent'),
    cube([4, 4.0, 2.1], [7, 7.0, 3.1], 'black'),
    cube([1, 7.4, 2.2], [3, 9.0, 3.0], 'light'),
])
recipe('cryo_rifle', '霜冻枪', 'blue', 'minecraft:packed_ice',
       {'lmg_dmg': 6, 'lmg_range': 96, 'lmg_spread': 1.5, 'lmg_rps': 5, 'lmg_mag': 20,
        'lmg_reload': 50, 'lmg_pierce': 2, 'lmg_bspeed': 600, 'lmg_pellets': 1,
        'lmg_bullet_type': 2, 'lmg_freeze': 140})

# 3. 毒液喷射器
palette('venom_sprayer_palette', (150, 255, 90, 255))
emit('venom_sprayer_3d', 'venom_sprayer_palette', [
    cube([2, 7.0, 1.7], [13, 9.4, 3.5], 'dark'),
    cube([12, 7.2, 1.9], [16, 9.2, 3.3], 'accent'),
    cube([5, 3.6, 1.9], [8, 7.0, 3.3], 'black'),
    cube([8, 2.8, 1.9], [12, 7.0, 3.3], 'accent'),
    cube([1, 7.4, 2.0], [3, 9.2, 3.2], 'light'),
])
recipe('venom_sprayer', '毒液喷射器', 'green', 'minecraft:spider_eye',
       {'lmg_dmg': 3, 'lmg_range': 56, 'lmg_spread': 9.0, 'lmg_rps': 6, 'lmg_mag': 40,
        'lmg_reload': 45, 'lmg_pierce': 1, 'lmg_bspeed': 420, 'lmg_pellets': 6,
        'lmg_bullet_type': 2, 'lmg_poison': 4.0})

# 4. 链式特斯拉枪
palette('tesla_gun_palette', (140, 200, 255, 255))
emit('tesla_gun_3d', 'tesla_gun_palette', [
    cube([2, 7.0, 2.0], [14, 9.0, 3.2], 'dark'),
    cube([13, 7.4, 2.2], [16, 8.6, 3.0], 'light'),
    cube([3, 9.0, 2.2], [7, 11.2, 3.0], 'accent'),
    cube([4, 4.2, 2.1], [7, 7.0, 3.1], 'black'),
    cube([8, 9.0, 2.6], [10, 10.0, 2.9], 'accent'),
])
recipe('tesla_gun', '链式特斯拉枪', 'light_purple', 'minecraft:lightning_rod',
       {'lmg_dmg': 8, 'lmg_range': 160, 'lmg_spread': 0.6, 'lmg_rps': 8, 'lmg_mag': 24,
        'lmg_reload': 50, 'lmg_pierce': 2, 'lmg_bspeed': 800, 'lmg_pellets': 1,
        'lmg_bullet_type': 4, 'lmg_chain': 6.0, 'lmg_chain_n': 3})

# 5. 爆裂机枪
palette('explosive_mg_palette', (255, 170, 70, 255))
emit('explosive_mg_3d', 'explosive_mg_palette', [
    cube([1, 6.9, 1.8], [15, 9.4, 3.4], 'dark'),
    cube([14, 7.3, 2.0], [16, 8.9, 3.2], 'light'),
    cube([5, 3.7, 2.0], [8, 6.9, 3.2], 'black'),
    cube([8.5, 2.9, 1.9], [12, 6.9, 3.5], 'accent'),
    cube([9, 9.4, 2.3], [12, 10.3, 3.0], 'accent'),
])
recipe('explosive_mg', '爆裂机枪', 'gold', 'minecraft:fire_charge',
       {'lmg_dmg': 5, 'lmg_range': 140, 'lmg_spread': 2.2, 'lmg_rps': 12, 'lmg_mag': 60,
        'lmg_reload': 60, 'lmg_pierce': 1, 'lmg_bspeed': 700, 'lmg_pellets': 1,
        'lmg_bullet_type': 3, 'lmg_boom': 2.0})

# 6. 智能追踪手枪
palette('smart_pistol_palette', (200, 200, 255, 255))
emit('smart_pistol_3d', 'smart_pistol_palette', [
    cube([4, 7.4, 2.0], [13, 9.2, 3.2], 'dark'),
    cube([12, 7.6, 2.2], [15, 8.8, 3.0], 'light'),
    cube([5, 4.4, 2.1], [8, 7.4, 3.1], 'black'),
    cube([9, 9.2, 2.3], [11, 10.0, 2.9], 'accent'),
])
recipe('smart_pistol', '智能追踪手枪', 'white', 'minecraft:comparator',
       {'lmg_dmg': 10, 'lmg_range': 96, 'lmg_spread': 0.3, 'lmg_rps': 4, 'lmg_mag': 12,
        'lmg_reload': 40, 'lmg_pierce': 1, 'lmg_bspeed': 560, 'lmg_pellets': 1,
        'lmg_bullet_type': 1, 'lmg_homing': 0.35})

# 7. 冲压炮（超高击退）
palette('kinetic_cannon_palette', (220, 160, 90, 255))
emit('kinetic_cannon_3d', 'kinetic_cannon_palette', [
    cube([1, 6.8, 1.6], [14, 9.8, 3.6], 'dark'),
    cube([13, 7.0, 1.9], [16, 9.6, 3.3], 'black'),
    cube([5, 3.4, 2.0], [9, 6.8, 3.4], 'accent'),
    cube([2, 7.2, 2.2], [4, 9.4, 3.0], 'light'),
    cube([9, 9.8, 2.2], [12, 10.8, 3.0], 'accent'),
])
recipe('kinetic_cannon', '冲压炮', 'gold', 'minecraft:iron_block',
       {'lmg_dmg': 40, 'lmg_range': 120, 'lmg_spread': 0.5, 'lmg_rps': 0.8, 'lmg_mag': 4,
        'lmg_reload': 55, 'lmg_pierce': 3, 'lmg_bspeed': 880, 'lmg_pellets': 1,
        'lmg_bullet_type': 5, 'lmg_kb': 0.6})

# 8. 激光狙击炮（hitscan）
palette('laser_sniper_palette', (255, 120, 160, 255))
emit('laser_sniper_3d', 'laser_sniper_palette', [
    cube([1, 7.4, 2.3], [15, 8.8, 3.0], 'dark'),
    cube([2, 6.8, 2.0], [14, 9.4, 3.3], 'light'),
    cube([14, 7.6, 2.4], [16, 8.6, 2.9], 'accent'),
    cube([8, 9.4, 2.2], [13, 10.8, 3.1], 'black'),
    cube([9, 10.8, 2.5], [12, 11.6, 2.9], 'accent'),
    cube([5, 4.8, 2.2], [8, 7.4, 3.0], 'black'),
], gui=0.95)
recipe('laser_sniper', '激光狙击炮', 'light_purple', 'minecraft:diamond',
       {'lmg_dmg': 90, 'lmg_range': 512, 'lmg_spread': 0.0, 'lmg_rps': 0.4, 'lmg_mag': 2,
        'lmg_reload': 70, 'lmg_pierce': 10, 'lmg_bspeed': 1500, 'lmg_pellets': 1,
        'lmg_bullet_type': 5})

print('8 把机制枪生成完成喵')
