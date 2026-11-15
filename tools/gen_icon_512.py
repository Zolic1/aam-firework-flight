import os, sys
from PIL import Image, ImageDraw

out_jar = sys.argv[1]     # 模组资源里的 icon.png
out_pub = sys.argv[2]     # 给 Modrinth 上传用的 512 版喵

S = 512
k = S / 128.0
im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(im)


def S_(v):
    return int(round(v * k))


# 深色圆角底喵
d.rounded_rectangle([S_(4), S_(4), S_(124), S_(124)], radius=S_(22), fill=(24, 30, 34, 255))
d.rounded_rectangle([S_(8), S_(8), S_(120), S_(120)], radius=S_(18), outline=(0, 255, 102, 255), width=S_(3))

# 雷达弧喵
for r, w in ((48, 3), (34, 2)):
    d.arc([S_(64 - r), S_(64 - r), S_(64 + r), S_(64 + r)], start=200, end=340,
          fill=(0, 255, 102, 255), width=S_(w))

# 导弹弹体喵
d.polygon([(S_(36), S_(92)), (S_(86), S_(42)), (S_(96), S_(52)), (S_(46), S_(102))],
          fill=(190, 195, 205, 255))
# 弹头喵
d.polygon([(S_(86), S_(42)), (S_(100), S_(28)), (S_(102), S_(46)), (S_(96), S_(52))],
          fill=(255, 90, 90, 255))
# 尾翼喵
d.polygon([(S_(46), S_(92)), (S_(30), S_(108)), (S_(44), S_(102))], fill=(255, 90, 90, 255))
# 尾焰喵
d.polygon([(S_(36), S_(92)), (S_(18), S_(110)), (S_(40), S_(104))], fill=(255, 200, 60, 255))
d.ellipse([S_(12), S_(104), S_(26), S_(118)], fill=(255, 140, 40, 255))

im.save(out_jar, 'PNG')
im.save(out_pub, 'PNG')
print('图标生成完成：', out_jar, out_pub, im.size)
