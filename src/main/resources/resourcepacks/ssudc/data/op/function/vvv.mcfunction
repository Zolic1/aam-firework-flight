# ============================================
# 怪物加强系统 - 让游戏更有挑战性
# ============================================

# 1. 僵尸家族 - 增加血量和攻击力
execute as @e[type=minecraft:zombie] run attribute @s minecraft:max_health base set 60
execute as @e[type=minecraft:zombie] run attribute @s minecraft:attack_damage base set 8
execute as @e[type=minecraft:husk] run attribute @s minecraft:max_health base set 70
execute as @e[type=minecraft:husk] run attribute @s minecraft:attack_damage base set 9
execute as @e[type=minecraft:drowned] run attribute @s minecraft:max_health base set 65
execute as @e[type=minecraft:drowned] run attribute @s minecraft:attack_damage base set 7

# 2. 骷髅家族 - 增加攻速和精准
execute as @e[type=minecraft:skeleton] run attribute @s minecraft:max_health base set 50
execute as @e[type=minecraft:skeleton] run attribute @s minecraft:attack_damage base set 6
execute as @e[type=minecraft:stray] run attribute @s minecraft:max_health base set 55
execute as @e[type=minecraft:wither_skeleton] run attribute @s minecraft:max_health base set 80
execute as @e[type=minecraft:wither_skeleton] run attribute @s minecraft:attack_damage base set 12

# 3. 蜘蛛家族 - 增加速度和毒伤
execute as @e[type=minecraft:spider] run attribute @s minecraft:max_health base set 40
execute as @e[type=minecraft:spider] run attribute @s minecraft:movement_speed base set 0.4
execute as @e[type=minecraft:cave_spider] run attribute @s minecraft:max_health base set 35
execute as @e[type=minecraft:cave_spider] run attribute @s minecraft:movement_speed base set 0.45

# 4. 灾厄村民 - 增加血量和法术强度
execute as @e[type=minecraft:pillager] run attribute @s minecraft:max_health base set 50
execute as @e[type=minecraft:vindicator] run attribute @s minecraft:max_health base set 60
execute as @e[type=minecraft:vindicator] run attribute @s minecraft:attack_damage base set 10
execute as @e[type=minecraft:ravager] run attribute @s minecraft:max_health base set 150
execute as @e[type=minecraft:ravager] run attribute @s minecraft:attack_damage base set 15
execute as @e[type=minecraft:evoker] run attribute @s minecraft:max_health base set 45

# 5. 地狱生物 - 大幅度强化
execute as @e[type=minecraft:blaze] run attribute @s minecraft:max_health base set 60
execute as @e[type=minecraft:ghast] run attribute @s minecraft:max_health base set 80
execute as @e[type=minecraft:magma_cube] run attribute @s minecraft:max_health base set 30
execute as @e[type=minecraft:hoglin] run attribute @s minecraft:max_health base set 100
execute as @e[type=minecraft:hoglin] run attribute @s minecraft:attack_damage base set 10
execute as @e[type=minecraft:piglin_brute] run attribute @s minecraft:max_health base set 100
execute as @e[type=minecraft:piglin_brute] run attribute @s minecraft:attack_damage base set 13

# 6. 末地生物 - 终极强化
execute as @e[type=minecraft:enderman] run attribute @s minecraft:max_health base set 80
execute as @e[type=minecraft:enderman] run attribute @s minecraft:attack_damage base set 10
execute as @e[type=minecraft:shulker] run attribute @s minecraft:max_health base set 60

# 7. BOSS 级生物 - 史诗强化
execute as @e[type=minecraft:warden] run attribute @s minecraft:max_health base set 1000
execute as @e[type=minecraft:warden] run attribute @s minecraft:attack_damage base set 45
execute as @e[type=minecraft:wither] run attribute @s minecraft:max_health base set 600
execute as @e[type=minecraft:ender_dragon] run attribute @s minecraft:max_health base set 400

# 8. 给 BOSS 添加发光效果（高亮显示）
effect give @e[type=minecraft:warden] minecraft:glowing infinite 0 true
effect give @e[type=minecraft:wither] minecraft:glowing infinite 0 true
effect give @e[type=minecraft:ender_dragon] minecraft:glowing infinite 0 true

# 9. 给所有敌对怪物添加无限生命恢复 II
execute as @e[type=#minecraft:no_heal] run effect give @s minecraft:regeneration infinite 1 true

# 10. BOSS 额外加强生命恢复 III
execute as @e[type=minecraft:warden] run effect give @s minecraft:regeneration infinite 2 true
execute as @e[type=minecraft:wither] run effect give @s minecraft:regeneration infinite 2 true
execute as @e[type=minecraft:ender_dragon] run effect give @s minecraft:regeneration infinite 2 true

# 9. 给所有敌对怪物添加无限生命恢复 II


# 10. 给所有敌对怪物添加抗性 II（减伤 40%）
execute as @e[type=#minecraft:no_heal] run effect give @s minecraft:resistance infinite 3

# 11. 给所有敌对怪物添加速度 II（移速 +40%）
execute as @e[type=#minecraft:no_heal] run effect give @s minecraft:speed infinite 1 true
execute as @e[type=minecraft:zombie] run effect give @s minecraft:strength infinite 5 true
execute as @e[type=minecraft:skeleton] run effect give @s minecraft:strength infinite 10 true
execute as @e[type=!player] run effect give @s resistance infinite 3
execute as @a if dimension minecraft:the_end run effect give @s minecraft:weakness 10 1
execute as @a if dimension minecraft:the_end run effect give @s minecraft:hunger 10 1
execute as @a if dimension minecraft:the_end run effect give @s minecraft:slowness 5 0
execute as @a if dimension minecraft:the_end run effect give @s minecraft:mining_fatigue 5 0
execute as @e[type=#minecraft:no_heal] run item replace entity @s armor.chest with netherite_chestplate[enchantments={"minecraft:vanishing_curse":1,"enchantmentfile:unbreakable":1,"minecraft:protection":10},unbreakable={}]
# 一行判断，避免中间状态
execute as @e[type=#minecraft:no_heal,tag=!bsd,type=!skeleton] run item replace entity @s weapon.offhand with totem_of_undying
execute as @e[type=#minecraft:no_heal,tag=!bsd] run tag @s add bsd

enchant @e[type=#minecraft:no_heal] sharpness 7
enchant @e[type=#minecraft:no_heal] power 5
execute as @e[type=creeper] run attribute @s minecraft:armor base set 40
execute as @e[type=creeper] run attribute @s movement_speed base set 1.50000