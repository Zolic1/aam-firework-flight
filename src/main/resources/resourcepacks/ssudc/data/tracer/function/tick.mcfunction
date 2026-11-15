# 曳光：在每支带标记的箭矢位置生成末地烛粒子
execute as @e[type=#minecraft:arrows,tag=tracer.arrow] at @s run function tracer:internal/draw
