# 1. 标记新生成的实体
execute as @e[tag=lxd,tag=!old] run tag @s add new

# 2. 设置初始分数
execute as @e[tag=new,tag=lxd,tag=!old] run scoreboard players set @s lxd 400

# 3. 标记为 old（已初始化）
execute as @e[tag=new,tag=lxd] run tag @s add old

# 4. 移除 new 标签（进入正常循环）
execute as @e[tag=new,tag=lxd,tag=old] run tag @s remove new

# 5. 每 tick 减 1（只对已初始化的）
execute as @e[tag=lxd,tag=old] run scoreboard players remove @s lxd 1

# 6. 归零时清除
execute as @e[tag=lxd,tag=old] if score @s lxd matches ..0 run effect clear @e[tag=jr] regeneration
execute as @e[tag=lxd,tag=old] if score @s lxd matches ..0 run kill @s