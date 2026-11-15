# 自动：每 tick 驱动（纯 NBT 检测，不依赖任何事件）
execute as @a[scores={auto.cd=1..}] run scoreboard players remove @s auto.cd 1
execute as @a at @s run function auto:check
