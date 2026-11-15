execute as @e[tag=lxd] at @s run tag @a[distance=..10] add jr
execute as @e[tag=lxd] at @s run particle minecraft:end_rod ~ ~ ~ 10 10 10 0 64 force
execute as @e[tag=lxd] at @s run effect give @e[distance=..10,tag=!no_heal] minecraft:regeneration infinite 2
execute as @e[tag=lxd] at @s run effect give @e[distance=..10,tag=!no_heal] minecraft:speed 30 1
execute as @e[tag=lxd] at @s run effect give @e[distance=..10,tag=!no_heal] minecraft:strength 10 0
execute as @e[tag=lxd] at @s run effect give @e[distance=..10,tag=!no_heal] minecraft:haste 10 2