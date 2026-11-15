# 自动：装填普通箭
item modify entity @s weapon.mainhand auto:set_arrow
clear @s minecraft:arrow 1
playsound minecraft:item.crossbow.loading_end master @a ~ ~ ~ 1 1
scoreboard players set @s auto.cd 2
