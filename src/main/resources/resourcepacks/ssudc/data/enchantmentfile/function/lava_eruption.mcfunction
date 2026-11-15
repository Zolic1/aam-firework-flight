# 大规模熔岩喷发粒子效果
# 执行位置：被攻击目标的位置

# 主爆发 - 大量熔岩粒子
particle minecraft:lava ~ ~ ~ 2.5 1.5 2.5 0.8 300
particle minecraft:lava ~ ~0.5 ~ 2.0 1.0 2.0 0.6 200
particle minecraft:lava ~ ~1 ~ 1.5 0.8 1.5 0.5 150

# 熔岩飞溅 - 向四周散射
particle minecraft:lava ~ ~0.5 ~ 3.0 0.5 0 0.5 80
particle minecraft:lava ~ ~0.5 ~ -3.0 0.5 0 0.5 80
particle minecraft:lava ~ ~0.5 ~ 0 0.5 3.0 0.5 80
particle minecraft:lava ~ ~0.5 ~ 0 0.5 -3.0 0.5 80

# 火焰粒子 - 增强视觉效果
particle minecraft:flame ~ ~ ~ 2.0 1.0 2.0 0.3 150
particle minecraft:campfire_cosy_smoke ~ ~ ~ 2.0 2.0 2.0 0.1 60
particle minecraft:large_smoke ~ ~ ~ 1.5 1.0 1.5 0.2 80

# 爆炸音效
playsound minecraft:entity.generic.explode neutral @a ~ ~ ~ 2.0 1.0
playsound minecraft:entity.blaze.shoot neutral @a ~ ~ ~ 1.0 0.5
playsound minecraft:block.lava.pop neutral @a ~ ~ ~ 2.0 1.5