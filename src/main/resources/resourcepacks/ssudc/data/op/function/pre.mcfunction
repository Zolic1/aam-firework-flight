# 每 tick +1
scoreboard players add #count pre 1

# 15 分钟奖励
execute if score #count pre matches 18000 run give @a gold_ingot
execute if score #count pre matches 18000 run xp add @a 1 levels
execute if score #count pre matches 18000 run title @a title {"text":"15分钟在线奖励：金锭+经验","color":"yellow"}

# 30 分钟奖励
execute if score #count pre matches 36000 run give @a diamond
execute if score #count pre matches 36000 run xp add @a 2 levels
execute if score #count pre matches 36000 run title @a title {"text":"30分钟在线奖励：钻石+经验","color":"gold"}

# 60 分钟奖励
execute if score #count pre matches 72000 run give @a creeper_spawn_egg[entity_data={id:"minecraft:armor_stand",Tags:["lxd"],Invisible:true,Invulnerable:true,NoGravity:true,Marker:true,Small:true},item_model="minecraft:pearlescent_froglight",item_name='{"text":"END-领域弹","color":"dark_purple","bold":true}',lore=['{"text":"右键地面召唤末地领域","color":"yellow"}','{"text":"10范围内获得：生命恢复、速度、力量","color":"gray"}','{"text":"持续20秒，敌对生物无效","color":"gray"}','{"text":"消耗品","color":"dark_gray","italic":true}']]
execute if score #count pre matches 72000 run xp add @a 5 levels
execute if score #count pre matches 72000 run title @a title {"text":"60分钟在线奖励：领域x1+经验","color":"light_purple"}

# 重置计数器
execute if score #count pre matches 72000 run scoreboard players set #count pre 0