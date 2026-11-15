# 遗踪：箭矢生成时立即变为隐形
# Invisible 设为 1 隐藏实体渲染
data modify entity @s Invisible set value 1

# 同时关闭拖尾粒子（隐形箭不该有可见线索）
data modify entity @s NoGravity set value 0
tag @s add vanishing.arrow
