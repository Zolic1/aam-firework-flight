clear @s
give @s netherite_sword[unbreakable={},enchantments={sharpness:15},damage_type="scr:combo"]
item replace entity @s armor.head with netherite_helmet[max_damage=2480,enchantments={projectile_protection:4,protection:5}]
item replace entity @s armor.chest with netherite_chestplate[max_damage=2480,enchantments={projectile_protection:4,protection:5}]
item replace entity @s armor.legs with netherite_leggings[max_damage=2480,enchantments={projectile_protection:4,protection:5}]
item replace entity @s armor.feet with netherite_boots[max_damage=2480,enchantments={projectile_protection:4,protection:5}]
item replace entity @s weapon.offhand with shield[max_damage=3600,item_name={text:"防爆盾"},enchantments={blast_protection:4}]
give @s iron_sword[damage_type="scr:combo",item_model=blaze_rod,unbreakable={}]
give @s crossbow[enchantments={"enchantmentfile:powerpro":5,"enchantmentfile:tntcrossbow":1},max_damage=675,item_name={text:"AX50",color:"green"}]
give @s crossbow[enchantments={"auto:auto":1},max_damage=1280,item_name={text:"EV-10",color:"aqua"},lore=[{text:"★ 自动装填",color:"gold",italic:false},{text:"携带此弩射击后自动补箭",color:"gray",italic:false}]]
give @s golden_apple 128
give @s spectral_arrow[max_stack_size=60,item_name={text:"12.7毫米口径穿甲子弹",color:yellow}] 960
give @s minecraft:allay_spawn_egg[entity_data={id:marker,Tags:["arrow_rain_caster"]},minecraft:item_name={text:"呼叫炮火支援！",color:yellow}] 8
# ===== AX50 配件（附魔书形式，可叠加）=====
give @s enchanted_book[stored_enchantments={"minecraft:power":7},item_model=nether_star,item_name={text:"钢性弓弦",color:"light_purple"},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"+7 力量",italic:false}],custom_data={ax50_part:"bowstring"}]
give @s enchanted_book[stored_enchantments={"minecraft:flame":1},item_model=fire_charge,item_name={text:"燃烧弹",color:"gold"},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"+1 火矢",italic:false}],custom_data={ax50_part:"fireammo"}]
give @s enchanted_book[stored_enchantments={"minecraft:quick_charge":3},item_model=iron_ingot,item_name={text:"快装弹匣",color:"aqua"},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"+3 快速装填",italic:false}],custom_data={ax50_part:"quickmag"}]
give @s enchanted_book[stored_enchantments={"minecraft:piercing":4},item_model=flint,item_name={text:"穿甲弹头",color:"red"},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"+4 穿透",italic:false}],custom_data={ax50_part:"piercing"}]
give @s enchanted_book[stored_enchantments={"minecraft:multishot":1},item_model=amethyst_shard,item_name={text:"多联装弹匣",color:"yellow"},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"+1 多重射击",italic:false}],custom_data={ax50_part:"multishot"}]
give @s enchanted_book[stored_enchantments={"enchantmentfile:tntcrossbow":1},item_model=tnt,item_name={text:"爆炸子弹",color:red},lore=[{text:"★ AX50 专用配件",color:"gold",italic:false},{text:"命中实体：半径 2 爆炸",italic:false},{text:"命中方块：半径 8 爆炸",italic:false}],custom_data={ax50_part:"tntcrossbow"}]

# ===== 附魔书（概率发放）=====
execute if predicate {"condition":"minecraft:random_chance","chance":0.20} run give @s enchanted_book[stored_enchantments={"gravityless:gravityless":1},item_name={text:"附魔书 · 无重力",color:"aqua"},lore=[{text:"★ 箭矢无重力直线飞行",color:"gold",italic:false},{text:"★ 10 秒后自动消失",color:"gray",italic:false},{text:"弓 / 弩 通用 · 铁砧可附",color:"dark_gray",italic:false}]]
execute if predicate {"condition":"minecraft:random_chance","chance":0.20} run give @s enchanted_book[stored_enchantments={"tracer:tracer":1},item_name={text:"附魔书 · 曳光",color:"yellow"},lore=[{text:"★ 箭矢带末地烛光尾迹",color:"gold",italic:false},{text:"便于追踪弹道",color:"gray",italic:false},{text:"弓 / 弩 通用",color:"dark_gray",italic:false}]]
execute if predicate {"condition":"minecraft:random_chance","chance":0.20} run give @s enchanted_book[stored_enchantments={"smoker:smoker":1},item_name={text:"附魔书 · 曳光[烟雾]",color:"gray"},lore=[{text:"★ 箭矢带烟雾尾迹",color:"gold",italic:false},{text:"烟雾更明显，便于观察弹道",color:"gray",italic:false},{text:"弩专用",color:"dark_gray",italic:false}]]
#execute if predicate {"condition":"minecraft:random_chance","chance":0.10} run give @s enchanted_book[stored_enchantments={"lmg:lmg":1},item_name={text:"附魔书 · 轻机枪",color:"red"},lore=[{text:"★ 每 tick 无限填充普通箭",color:"gold",italic:false},{text:"不消耗背包弹药",color:"gray",italic:false},{text:"弩专用",color:"dark_gray",italic:false}]]
execute if predicate {"condition":"minecraft:random_chance","chance":0.15} run give @s enchanted_book[stored_enchantments={"vanishing:vanishing":1},item_name={text:"附魔书 · 匿踪",color:"dark_purple"},lore=[{text:"★ 箭矢完全隐形",color:"gold",italic:false},{text:"敌人无法看见弹道",color:"gray",italic:false},{text:"弓 / 弩 通用",color:"dark_gray",italic:false}]]
