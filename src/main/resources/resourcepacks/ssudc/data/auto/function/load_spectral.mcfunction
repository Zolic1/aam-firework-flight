# 自动：装填光谱箭
item modify entity @s weapon.mainhand auto:set_spectral
clear @s minecraft:spectral_arrow 1
playsound minecraft:item.crossbow.loading_end master @a ~ ~ ~ 1 1
scoreboard players set @s auto.cd 2
