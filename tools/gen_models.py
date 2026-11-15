import json, os, sys
from PIL import Image, ImageDraw

RES = sys.argv[1]
tex = os.path.join(RES, 'assets', 'aamflight', 'textures', 'item')
mdl = os.path.join(RES, 'assets', 'aamflight', 'models', 'item')
itm = os.path.join(RES, 'assets', 'aamflight', 'items')
for d in (tex, mdl, itm):
    os.makedirs(d, exist_ok=True)

# 16x16 调色板，四象限各 8x8（符合原版默认 texture_size = 16 喵）
UV = {
    'light': [0, 0, 8, 8],
    'dark': [8, 0, 16, 8],
    'black': [0, 8, 8, 16],
    'accent': [8, 8, 16, 16],
}


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


def emit(name, texname, cubes, gui=0.9):
    m = {
        "credit": "Generated for AAM by Yuike",
        "textures": {
            "body": f"aamflight:item/{texname}",
            "particle": "#body",
        },
        "elements": cubes,
        "display": {
            "thirdperson_righthand": {"rotation": [0, -90, 50], "translation": [0, 3.5, 0.5], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_lefthand": {"rotation": [0, 90, -50], "translation": [0, 3.5, 0.5], "scale": [0.85, 0.85, 0.85]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [-0.4, 1.6, 0.2], "scale": [0.5, 0.5, 0.5]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [-0.4, 1.6, 0.2], "scale": [0.5, 0.5, 0.5]},
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [gui * 0.85, gui * 0.85, gui * 0.85]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
        },
    }
    with open(os.path.join(mdl, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump(m, f, ensure_ascii=False, indent=2)
    with open(os.path.join(itm, name + '.json'), 'w', encoding='utf-8') as f:
        json.dump({"model": {"type": "minecraft:model", "model": f"aamflight:item/{name}"}},
                  f, ensure_ascii=False, indent=2)


palette('sniper_rifle_palette', (90, 170, 255, 255))
emit('sniper_rifle_3d', 'sniper_rifle_palette', [
    cube([1, 6.8, 1.8], [15, 9.0, 3.2], 'dark'),
    cube([14, 7.2, 2.2], [16, 8.6, 2.8], 'light'),
    cube([5, 4.5, 2.0], [8, 6.8, 3.0], 'black'),
    cube([2, 7.5, 2.0], [5, 8.6, 3.0], 'light'),
    cube([8, 9.0, 2.1], [12, 10.2, 2.9], 'black'),
    cube([8.5, 10.2, 2.3], [11.5, 10.9, 2.7], 'accent'),
])

palette('marksman_rifle_palette', (240, 200, 90, 255))
emit('marksman_rifle_3d', 'marksman_rifle_palette', [
    cube([2, 6.8, 1.8], [14, 9.2, 3.4], 'dark'),
    cube([13, 7.4, 2.2], [16, 8.8, 3.0], 'light'),
    cube([5, 3.8, 2.0], [8, 6.8, 3.2], 'black'),
    cube([8.5, 3.0, 2.0], [11.5, 6.8, 3.2], 'light'),
    cube([3, 7.0, 2.2], [5, 8.8, 3.0], 'light'),
    cube([9, 9.2, 2.4], [11.5, 10.0, 3.0], 'accent'),
])

palette('assault_rifle_palette', (240, 220, 90, 255))
emit('assault_rifle_3d', 'assault_rifle_palette', [
    cube([2, 7.0, 1.8], [14, 9.4, 3.4], 'dark'),
    cube([13, 7.4, 2.2], [16, 9.0, 3.0], 'light'),
    cube([4, 4.0, 2.0], [7, 7.0, 3.2], 'black'),
    cube([8, 2.6, 2.0], [11, 7.0, 3.2], 'light'),
    cube([1, 7.2, 2.2], [3, 9.0, 3.0], 'light'),
    cube([10, 9.4, 2.4], [12, 10.2, 3.0], 'accent'),
])

palette('shotgun_palette', (255, 90, 90, 255))
emit('shotgun_3d', 'shotgun_palette', [
    cube([2, 7.0, 1.8], [15, 9.4, 3.4], 'dark'),
    cube([12, 6.6, 1.8], [16, 8.2, 3.4], 'black'),
    cube([9, 7.6, 1.6], [12, 8.6, 3.6], 'light'),
    cube([4, 3.8, 2.0], [7, 7.0, 3.2], 'black'),
    cube([1, 7.2, 2.2], [3, 9.2, 3.0], 'light'),
])

palette('gatling_palette', (200, 60, 60, 255))
emit('gatling_3d', 'gatling_palette', [
    cube([3, 6.8, 1.6], [13, 10.0, 3.6], 'dark'),
    cube([13, 7.2, 1.9], [16, 8.2, 2.5], 'light'),
    cube([13, 8.0, 2.6], [16, 9.0, 3.2], 'light'),
    cube([13, 8.8, 1.3], [16, 9.8, 1.9], 'light'),
    cube([6, 3.0, 1.8], [11, 6.8, 3.4], 'accent'),
    cube([4, 3.8, 2.0], [6, 6.8, 3.2], 'black'),
])

palette('grenade_launcher_palette', (80, 190, 110, 255))
emit('grenade_launcher_3d', 'grenade_launcher_palette', [
    cube([2, 7.0, 1.6], [14, 9.6, 3.6], 'dark'),
    cube([13, 7.2, 1.9], [16, 9.4, 3.3], 'black'),
    cube([6, 3.4, 2.0], [10, 7.0, 3.2], 'accent'),
    cube([4, 4.2, 2.2], [6, 7.0, 3.0], 'black'),
    cube([1, 7.4, 2.2], [3, 9.2, 3.0], 'light'),
])

palette('lmg_palette', (190, 190, 200, 255))
emit('lmg_3d', 'lmg_palette', [
    cube([1, 6.8, 1.6], [15, 9.6, 3.6], 'dark'),
    cube([14, 7.2, 2.0], [16, 9.0, 3.2], 'light'),
    cube([5, 3.6, 2.0], [8, 6.8, 3.2], 'black'),
    cube([8.5, 2.8, 1.8], [12, 6.8, 3.6], 'accent'),
    cube([2, 2.6, 2.2], [4, 6.8, 3.0], 'light'),
])

palette('smg_3d_palette', (78, 190, 70, 255))
emit('smg_3d', 'smg_3d_palette', [
    cube([2, 6.5, 1.5], [14, 9.5, 3.5], 'dark'),
    cube([13, 7.0, 2.0], [16, 9.0, 3.0], 'light'),
    cube([4, 3.5, 2.0], [7, 6.5, 3.0], 'black'),
    cube([7.5, 2.0, 2.0], [10.5, 6.5, 3.0], 'light'),
    cube([1, 7.0, 2.0], [2, 9.0, 3.0], 'light'),
    cube([10, 9.5, 2.2], [12, 10.5, 2.8], 'accent'),
])

palette('missile_palette', (255, 140, 60, 255))
emit('missile_3d', 'missile_palette', [
    cube([6, 6, 2], [10, 10, 12], 'light'),
    cube([6.6, 6.6, 12], [9.4, 9.4, 15], 'accent'),
    cube([5, 8, 2], [6, 9, 6], 'black'),
    cube([10, 8, 2], [11, 9, 6], 'black'),
    cube([7, 10, 2], [9, 11, 6], 'black'),
    cube([7, 5, 2], [9, 6, 6], 'black'),
    cube([7, 7, 0], [9, 9, 2], 'dark'),
], gui=1.0)
print('regenerated with particle texture')
