import json, os, sys
from PIL import Image, ImageDraw

RES = sys.argv[1]
tex = os.path.join(RES, 'assets', 'aamflight', 'textures', 'item')
mdl = os.path.join(RES, 'assets', 'aamflight', 'models', 'item')
itm = os.path.join(RES, 'assets', 'aamflight', 'items')
for d in (tex, mdl, itm):
    os.makedirs(d, exist_ok=True)

# 16x16 平面枪图标（原型枪喵）
im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
metal = (150, 152, 158, 255)
dark = (88, 90, 96, 255)
black = (38, 38, 42, 255)
accent = (255, 90, 90, 255)

d.rectangle([1, 6, 13, 9], fill=dark)          # 机匣
d.rectangle([12, 7, 15, 9], fill=metal)        # 枪管
d.rectangle([4, 9, 6, 13], fill=black)         # 握把
d.rectangle([7, 9, 9, 12], fill=metal)         # 弹匣
d.rectangle([2, 5, 4, 6], fill=metal)          # 枪托
d.rectangle([9, 5, 11, 6], fill=accent)        # 瞄具
im.save(os.path.join(tex, 'lmg_gun.png'), 'PNG')

with open(os.path.join(mdl, 'lmg_gun.json'), 'w', encoding='utf-8') as f:
    json.dump({"parent": "minecraft:item/generated",
               "textures": {"layer0": "aamflight:item/lmg_gun"}}, f, ensure_ascii=False, indent=2)

with open(os.path.join(itm, 'lmg_gun.json'), 'w', encoding='utf-8') as f:
    json.dump({"model": {"type": "minecraft:model", "model": "aamflight:item/lmg_gun"}},
              f, ensure_ascii=False, indent=2)

print('原型枪图标生成完成喵')
