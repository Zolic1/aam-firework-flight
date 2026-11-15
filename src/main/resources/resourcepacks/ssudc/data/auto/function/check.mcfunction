# 自动：检测并装填
# 判定完全基于 NBT，无需事件、无需标记

# 冷却未归零 → 跳过
execute if score @s auto.cd matches 1.. run return fail

# 主手必须是带自动附魔的弩
execute unless predicate auto:holding_auto run return fail

# 主手弩已有 charged_projectiles（已装填） → 跳过
execute if data entity @s SelectedItem.components."minecraft:charged_projectiles" run return fail

# 背包有普通箭 → 装填
execute if predicate auto:has_arrow run return run function auto:load_arrow

# 背包有光谱箭 → 装填
execute if predicate auto:has_spectral run return run function auto:load_spectral
