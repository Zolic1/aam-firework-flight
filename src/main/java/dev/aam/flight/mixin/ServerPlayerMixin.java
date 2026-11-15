package dev.aam.flight.mixin;

import dev.aam.flight.FlightBoost;
import dev.aam.flight.net.RadarSender;
import dev.aam.flight.gun.FireState;
import dev.aam.flight.gun.LmgGun;
import dev.aam.flight.score.ScoreMessages;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 每 tick：
 *  - 服务端雷达 + 锁定管理（内部含 3 秒持续判定）→ 下发给客户端
 *  - 穿着推进装置 + 鞘翅滑翔 → 推进并消耗耐久
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void aam$flightTick(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isSpectator()) return;

        // 雷达 + 锁定（每 tick 刷新，锁定需要连续 3 秒喵）
        RadarSender.tick(self);
        ScoreMessages.tick(self);

        // 轻机枪：弹夹 / 换弹 / 射击 / 活动栏喵
        LmgGun.tickHeld(self);
        // 背包弹药物品自动兑换成分数喵
        LmgGun.absorbAmmo(self);

        if (!self.isFallFlying()) return;

        if (FlightBoost.hasThruster(self)) {
            FlightBoost.apply(self);
            FlightBoost.consumeDurability(self);
        }
    }
}
