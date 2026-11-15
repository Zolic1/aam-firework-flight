# 曳光[烟雾]：每 tick 在箭矢位置生成烟雾粒子
execute as @e[type=#minecraft:arrows,tag=smoker.arrow] at @s run function smoker:internal/draw
