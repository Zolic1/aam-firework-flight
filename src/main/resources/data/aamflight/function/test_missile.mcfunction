# AAM 导弹追踪测试喵
# 用法：/function aamflight:test_missile
# 效果：在你正前方 200 格召唤一枚测试弹，它只锁定你、不会爆炸喵
execute as @s at @s run summon aamflight:aam_missile ^ ^1.5 ^200 {Tags:["aam_no_explode","aam_lock_player"],Motion:[0.0,0.0,0.0]}
tellraw @s {"text":"[AAM] 已在正前方 200 格生成测试弹（只锁你、不爆炸喵）","color":"aqua"}