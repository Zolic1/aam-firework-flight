tellraw @p [{"text":"[Server]"},{"text":"数据包加载成功!","color":"green"}]
scoreboard objectives add lxd dummy
scoreboard objectives add pre dummy
scoreboard objectives add timer dummy
scoreboard objectives add ExpLevel level
scoreboard objectives add groundtimer.timer dummy
data modify storage gravityless:data crit set value {crit_chance: 0.0}
scoreboard objectives add crit.value dummy
execute as @e run scoreboard players set @s crit.value 10
scoreboard objectives add rain_timer dummy