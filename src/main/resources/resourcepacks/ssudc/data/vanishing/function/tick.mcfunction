# 遗踪：每 tick 确保隐形标记未丢失（防止某些状态重置）
execute as @e[type=#minecraft:arrows,tag=vanishing.arrow] at @s unless data entity @s Invisible run data modify entity @s Invisible set value 1
