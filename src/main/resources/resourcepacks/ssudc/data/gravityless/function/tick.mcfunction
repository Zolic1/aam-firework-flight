# 无重力：每 tick 处理所有被标记的箭矢
execute as @e[type=#minecraft:arrows,tag=gravityless.arrow] at @s run function gravityless:internal/tick_one
