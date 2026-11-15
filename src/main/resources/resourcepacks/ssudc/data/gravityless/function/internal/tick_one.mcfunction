# 无重力：单个箭矢逻辑
# 1. 飞到 y=330 以上销毁（防止飞向太空）
execute positioned ~ 330 ~ if entity @s[distance=..10] run return run kill @s

# 2. 存活超过 200 tick（10 秒）销毁
execute if score @s gravityless.timer matches 200.. run return run kill @s

# 3. 计时 +1
scoreboard players add @s gravityless.timer 1
