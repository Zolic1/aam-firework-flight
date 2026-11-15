# 轻机枪：每 tick 无代价装填普通箭
# 判定：主手持带「轻机枪」附魔的弩 且 弩内无 charged_projectiles

# 主手必须是带轻机枪附魔的弩
execute unless predicate lmg:holding_lmg run return fail

# 弩已有 charged_projectiles（已装填） → 跳过
execute if data entity @s SelectedItem.components."minecraft:charged_projectiles" run return fail

# 无代价装填普通箭
item modify entity @s weapon.mainhand lmg:set_arrow
