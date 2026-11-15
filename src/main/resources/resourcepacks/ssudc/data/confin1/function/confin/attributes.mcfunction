execute as @a[tag=!NoConfin] run attribute @s max_health base set 100

tag @a remove jr
tag @e[type=#minecraft:no_heal] add no_heal
execute as @e[tag=easter_egg] run attribute @s minecraft:max_health base set 100
execute as @e[tag=easter_egg] run effect give @s regeneration infinite 4 true
execute as @e[tag=infstone] at @s unless block ~ ~ ~ stone run fill ~ ~ ~ ~ ~ ~ stone
execute as @e[tag=killinf] at @s run kill @e[distance=..5,limit=1,type=!player,tag=infblock]
kill @e[tag=killinf,type=!player]
execute store result storage gravityless:data crit.crit_chance double 0.01 run scoreboard players get @s crit.value
function awr:arrow_rain_wave
