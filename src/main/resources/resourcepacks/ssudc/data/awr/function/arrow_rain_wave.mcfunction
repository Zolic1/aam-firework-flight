# 只要分数 >= 0，就召唤箭矢（注意：要在标记实体的位置召唤）
execute as @e[tag=arrow_rain_caster] at @s run particle end_rod ~ ~ ~ 0.2 0.2 0.2 0.8 5 normal @a

execute as @e[tag=arrow_rain_caster] if score @s rain_timer matches ..40 at @s run function awr:summon_arrow

# 扣分
execute as @e[tag=arrow_rain_caster] run scoreboard players add @s rain_timer 1

execute as @e[tag=arrow_rain_caster] if score @s rain_timer matches 40.. run kill @s

