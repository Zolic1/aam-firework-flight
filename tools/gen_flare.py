import json, os, sys
from PIL import Image, ImageDraw

RES = sys.argv[1]
tex = os.path.join(RES, 'assets', 'aamflight', 'textures', 'item')
mdl = os.path.join(RES, 'assets', 'aamflight', 'models', 'item')
itm = os.path.join(RES, 'assets', 'aamflight', 'items')
rec = os.path.join(RES, 'data', 'aamflight', 'recipe')
for d in (tex, mdl, itm, rec):
    os.makedirs(d, exist_ok=True)

# 干扰弹图标：红白相间的筒 + 火焰喵
im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rectangle([5, 3, 10, 12], fill=(210, 210, 215, 255))
d.rectangle([5, 3, 10, 5], fill=(220, 40, 40, 255))
d.rectangle([5, 8, 10, 10], fill=(220, 40, 40, 255))
d.rectangle([6, 2, 9, 3], fill=(60, 60, 66, 255))
d.rectangle([6, 12, 9, 13], fill=(255, 200, 60, 255))
d.rectangle([7, 13, 8, 15], fill=(255, 140, 40, 255))
im.save(os.path.join(tex, 'flare.png'), 'PNG')

with open(os.path.join(mdl, 'flare.json'), 'w', encoding='utf-8') as f:
    json.dump({"parent": "minecraft:item/generated",
               "textures": {"layer0": "aamflight:item/flare"}}, f, ensure_ascii=False, indent=2)
with open(os.path.join(itm, 'flare.json'), 'w', encoding='utf-8') as f:
    json.dump({"model": {"type": "minecraft:model", "model": "aamflight:item/flare"}},
              f, ensure_ascii=False, indent=2)

# 配方：铁锭 + 火药 + 红石喵（一次做 4 个喵）
recipe = {
    "type": "minecraft:crafting_shapeless",
    "category": "equipment",
    "ingredients": ["minecraft:gunpowder", "minecraft:redstone", "minecraft:iron_ingot"],
    "result": {"id": "aamflight:flare", "count": 4},
}
with open(os.path.join(rec, 'flare.json'), 'w', encoding='utf-8') as f:
    json.dump(recipe, f, ensure_ascii=False, indent=2)

# 隐蔽弹参数加进等离子炮预设配方（示例喵）
p = os.path.join(rec, 'grenade_launcher.json')
with open(p, encoding='utf-8') as f:
    j = json.load(f)
cd = j['result']['components']['minecraft:custom_data']
cd['b_mflare'] = 0.35
j['result']['components']['minecraft:custom_data'] = cd
with open(p, 'w', encoding='utf-8') as f:
    json.dump(j, f, ensure_ascii=False, indent=2)

print('干扰弹资源生成完成喵')
