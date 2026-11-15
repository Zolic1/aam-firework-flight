# 自动：初始化
# 注意：这里不给 @a 设值，避免 /reload 后误触发装填
# 未初始化的玩家由 check 里的兜底逻辑处理
scoreboard objectives add auto.cd dummy
