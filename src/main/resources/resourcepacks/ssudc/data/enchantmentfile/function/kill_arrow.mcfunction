execute as @e[type=arrow] run scoreboard players add @s timer 1
execute as @e[type=arrow] if score @s timer matches 600.. run kill @s