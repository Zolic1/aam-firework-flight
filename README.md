# AAM Firework Flight（Fabric 1.21.11 · v2.0.2）

空战一体化模组：**24 把枪械**、追踪导弹与拦截、视线锁定、战雷式 **RWR 雷达**、干扰弹与隐蔽弹，外加可选内置的 **SSUDC 数据包**喵😼

---

## 功能总览

### 🔫 武器系统
| 类别 | 内容 |
|---|---|
| 子弹 | 真·子弹射线判定（不是实体），支持穿透、散布、弹丸数，命中可附带冻结 / 毒素 / 链电 / 小爆炸 / 追踪 |
| 激光 | `lmg_bspeed ≥ 900` 自动转为 **hitscan**（发射瞬间完成整条线判定 + 光束粒子）|
| 蓄力条 | 条满才能开火，`lmg_bars` 控制条数（霰弹枪 / 榴弹发射器为 2~3 条，可连射）|
| 子弹类型 | 5 种预设（尾迹间隔 / 燃烧概率 / 破甲比例），互斥属性，可在 `/aam help` 里点击切换 |
| 枪械总数 | **24 把**（轻机枪 / 狙击 / 射手 / 突击 / 冲锋 / 霰弹 / 加特林 / 榴弹 + 8 把科幻枪 + 8 把机制枪）|

### 🚀 导弹系统
- 追踪、重追踪（越过目标消耗一次：转向降到 50%，8 秒后才开始用 5 秒恢复）
- **三段加速**：0~2.45s → 80 格/s，2.45~7.45s → 120 格/s，7.45~15.45s → 180 格/s
- **90% 引爆率**，未引爆则穿过目标，并按「越过未命中」结算
- 可被子弹 / 爆炸击毁（12 HP），可被玩家锁定后由己方导弹拦截
- 参数全部可做成**物品属性**（`mpower` / `mseek` / `mturn` / `mflare` / `mstealth` …）

### 📡 RWR 雷达
- 三层距离环：`>200` 外圈 / `120~200` 中圈 / `<120` 内圈
- 目标指示弧随视角与滚转一起转动；玩家红点（敌方）/ 蓝点（友方）
- 告警：**敌锁定**（急促警报）/ **敌导弹**（缓慢警报）
- 锁定框用相机基向量投影，滚筒飞行不偏移；目标被击毁保留 **0.7 秒「已击毁目标」残影**（手动取消 / 视野外超时立即消失）
- 锁定导弹时显示：耐久血条（无数值）、**机动时间**、**命中预警**倒计时

### 🛡 对抗
- **干扰弹**：右键投撒，75 格内敌方导弹按 `1 − mflare` 概率脱锁，并 3 秒内不再锁定
- **隐蔽弹** `mstealth`：RWR 完全不告警，只有「视野内手动锁定」才会暴露；**背后无法锁定**

### 📦 数据包
- **SSUDC** 已做成可选内置数据包：新建世界时在「数据包」列表勾选启用（默认关闭）

### 🎮 游戏规则
| 规则 | 说明 |
|---|---|
| `aamflight:destroy_blocks` | 模组爆炸是否破坏地形（关闭后只伤实体）|
| `aamflight:mob_armor` | 数据包是否为生物设置 20 点护甲 |

---

## 指令（权限等级 4）

```mcfunction
/aam help                                   # 简介 + 全部配方 + 指令用法（子弹类型可点击）
/aam list                                   # 全部选项名
/aam <选项> [数值|reset]                     # 查看 / 设置 / 重置全局值
/aam <选项> base set|add|get|clear <目标>     # 改手上的枪（基础值）
/aam <选项> att set|add|get|remove|list <目标> <id> [值]   # 物品属性叠加
/aam <选项> get|clear <目标>                  # 最终值 / 清空覆盖
/aam clearall <目标>                          # 清空全部覆盖
/aam debug                                   # 诊断手上枪状态
/aam save | /aam reload                      # 保存 / 重读全局配置
```

> 所有子指令默认操作「选中玩家**手上的枪**」；手上没有枪则执行失败喵。

---

## 配置文件

路径：`config/aam-flight.properties`（只存全局值；物品覆盖存在物品 NBT 里喵）

```properties
opt.mpower=7
opt.mspeed=9
opt.radar_interval=1     # 雷达发包间隔（1 = 每 tick，2 = 10 次/秒…）
opt.hud_interval=2       # 活动栏刷新间隔
opt.score_interval=8     # 得分消息间隔
opt.ammo_rate=64         # 每 tick 最多兑换弹药数
opt.rule_interval=20     # 规则 → 记分板同步间隔
```

### 参数分组
| 前缀 | 含义 |
|---|---|
| `lmg_*` | 枪械：伤害 / 射程 / 穿透 / 射速 / 弹速 / 散布 / 弹丸 / 弹夹 / 换弹 / 蓄力条 / 击退 / 尾迹 / 弹种 / 冻结 / 毒素 / 链电 / 爆裂 / 追踪 |
| `m*` | 导弹：速度 / 加速 / 索敌 / 转向 / 重追踪 / 抗干扰 / 隐蔽 / 提前量 / 当量 / 引信 / 寿命 |
| `lockrange` / `cone` / `hold` / `outview` | 锁定：距离 / 锥角 / 保持时间 / 脱锁时间 |
| `radar*` | 雷达：探测距离 / 光点上限 / 发包间隔 |
| `sc_*` | 计分：命中 / 击落 / 复仇 |
| `speed` / `thruster_accel` | 推进装置 |

---

## 代码结构

```
dev/aam/flight/
├── AamConfig.java            # 注册表：字段 + 选项定义（范围 / 默认值 / 互斥）
├── config/
│   ├── ConfigOption.java     # 单个选项
│   ├── ConfigCommand.java    # /aam 指令树
│   └── ConfigStore.java      # .properties 读写
├── AamMissile.java           # 导弹实体（制导 / 重追踪 / 近炸 / 起爆 / 血量）
├── LockManager.java LockOn.java   # 锁定与视线索敌（生物 + 导弹）
├── Flares.java               # 干扰弹
├── ModGameRules.java RuleMirror.java
├── gun/                      # LmgGun / BulletManager / BulletType / FireState
├── item/ItemStats.java       # 物品属性（base / att / lore）
├── net/                      # RadarPayload / RadarSender（服务端扫描 → 客户端绘制）
├── mixin/GuiMixin.java       # RWR / 锁定框 / 玩家标记
└── score/                    # Scoring / ScoreMessages / DamageTracker
```

设计原则：**判定全部在服务端**（单人时整合服务端出问题最多断线，不会崩客户端），客户端只负责绘制喵。

---

## 编译

```bash
cd aam-firework-flight
gradlew.bat build
```

产物：`build/libs/aam-firework-flight-2.0.2.jar`

> 需要 JDK 21；Loom `1.14-SNAPSHOT`、Mojang 官方映射、Minecraft `1.21.11`。
> 提示：打包需要约 1 GB 空闲内存，游戏运行时内存不足会失败喵。

## 安装

1. 把 jar 放进 `.minecraft/mods/`
2. 需要 **Fabric Loader ≥ 0.18.4**、**Fabric API ≥ 0.141.0**、**Minecraft 1.21.11**
3. 单人 / 服务器都要装（逻辑在服务端，RWR 绘制在客户端）

## 内置数据包（可选）

模组自带一份 **SSUDC** 内容包，打包在 jar 内的 `resourcepacks/ssudc/` 里，
以 Fabric「内置数据包」形式注册，**默认关闭，可按存档单独启用**喵。

**启用方法**

1. 新建世界 → 点「数据包」按钮 → 左侧「可用」列表找到 **SSUDC 数据包**
2. 点击它（或拖到右侧「已选用」）→ 点「完成」→ 创建世界
3. 已经存在的存档：ESC → 「对局域网开放」界面的数据包按钮，或退出后在存档的
   数据包管理里切换（需重载世界生效喵）

**说明**

- 关闭时：模组功能（枪械 / 导弹 / RWR / 干扰弹）**完全不受影响**，
  只是没有 SSUDC 附带的附魔、函数与规则喵
- 开启后：会在每 tick 执行 SSUDC 的 `tick` 函数（护甲加成、轻机枪检测等喵）
- 一个存档只应启用**一份** SSUDC：如果你之前在世界的 `datapacks/` 里放过同名数据包，
  请保留其中一份，避免同一套函数被执行两次喵

---

## 常用测试命令

```mcfunction
/function aamflight:test_missile          # 正前方 200 格生成只锁你的测试弹（不爆炸）
/aam debug                                # 看手上枪的弹夹 / 分数 / 状态
/gamerule aamflight:destroy_blocks false    # 关掉地形破坏
/aam radar_interval 2                     # 降低雷达发包频率
```

## 许可

MIT · 作者 Yuike
