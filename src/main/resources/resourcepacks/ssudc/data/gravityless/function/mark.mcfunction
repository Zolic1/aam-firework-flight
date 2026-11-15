# 无重力：箭矢生成时调用（由附魔 projectile_spawned 触发）
# 作用：关闭重力 + 打标记，供 tick 追踪
data modify entity @s NoGravity set value 1
data modify entity @s item set value {id:"spectral_arrow",components:{max_stack_size:60,item_name:{text:"12.7毫米口径穿甲子弹",color:yellow}}}

# ===== 速度 +200%：直接把“已确定的飞行速度”放大 3 倍（原版碰撞上限约 9~10 格/tick） =====
# 沿箭矢自己的 Motion 缩放，所以方向永远和真实飞行方向一致，不会乱飞
execute store result score #gl_mx gravityless.timer run data get entity @s Motion[0] 1000
execute store result score #gl_my gravityless.timer run data get entity @s Motion[1] 1000
execute store result score #gl_mz gravityless.timer run data get entity @s Motion[2] 1000
scoreboard players set #gl_mul gravityless.timer 3
scoreboard players operation #gl_mx gravityless.timer *= #gl_mul gravityless.timer
scoreboard players operation #gl_my gravityless.timer *= #gl_mul gravityless.timer
scoreboard players operation #gl_mz gravityless.timer *= #gl_mul gravityless.timer
execute store result entity @s Motion[0] double 0.001 run scoreboard players get #gl_mx gravityless.timer
execute store result entity @s Motion[1] double 0.001 run scoreboard players get #gl_my gravityless.timer
execute store result entity @s Motion[2] double 0.001 run scoreboard players get #gl_mz gravityless.timer

function gravityless:crit with storage gravityless:data crit
tag @s add gravityless.arrow