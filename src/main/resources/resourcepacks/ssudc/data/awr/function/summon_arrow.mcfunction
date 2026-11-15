# 1. 先在地面召唤 1 支带标签的箭
execute at @s run summon arrow ~ ~1 ~ {Tags:["arrow_rain"],weapon:{id:crossbow,components:{enchantments:{flame:1,"enchantmentfile:tntcrossbow":1,"enchantmentfile:powerpro":2,"tracer:tracer":1}}}}



# 2. 地面散开（半径 4，高度差 1 防止卡墙）
execute at @s run spreadplayers ~ ~ 0.2 4 false @e[tag=arrow_rain]

# 3. 把它们全部拉高 45 格，并赋予向下的速度
execute as @e[tag=arrow_rain] at @s run tp @s ~ ~80 ~
execute as @e[tag=arrow_rain] run data modify entity @s Motion set value [0.0,-1,0.0]

# 4. 移除标签（防止后续误伤）
tag @e[tag=arrow_rain] remove arrow_rain