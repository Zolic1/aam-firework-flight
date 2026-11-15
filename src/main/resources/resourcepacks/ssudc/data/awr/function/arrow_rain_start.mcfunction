# 召唤一个不可见标记作为发射源
summon marker ~ ~ ~ {Tags:["arrow_rain_caster"]}

# 给标记初始化倒计时（比如 30 次 = 3 秒）
execute as @e[tag=arrow_rain_caster,limit=1] run scoreboard players set @s rain_timer 40

# 启动循环
execute as @e[tag=arrow_rain_caster,limit=1] at @s run function awr:arrow_rain_wave