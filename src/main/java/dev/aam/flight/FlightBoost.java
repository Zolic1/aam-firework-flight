package dev.aam.flight;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** 推进逻辑 & 推进装置判定（与指令共享 targetSpeed） */
public final class FlightBoost {

    private FlightBoost() {}

    /** 每 tick 追加的冲量系数（越大起步越猛） */

    /**
     * 沿「当前视线方向」追加冲量，直到速度达到 targetSpeed。
     * 只加冲量、不强改方向 —— 这样转向会实时生效，不会出现「方向锁死」。
     */
    public static void apply(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 motion = player.getDeltaMovement();
        double target = AamConfig.targetSpeed;

        double speed2 = motion.lengthSqr();
        if (speed2 >= target * target) {
            return; // 已经够快，不再推进
        }

        Vec3 next = motion.add(look.scale(AamConfig.thrusterAccel));
        if (next.lengthSqr() > target * target) {
            next = next.normalize().scale(target); // 限速
        }

        player.setDeltaMovement(next);
        player.hurtMarked = true; // 让客户端收到速度同步，表现平滑
    }

    /** 是否穿着「推进装置」（靠 custom_data 标记识别） */
    public static boolean hasThruster(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        var data = boots.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean("aam_thruster").orElse(false);
    }

    /** 滑翔时消耗耐久：每 tick -1，最低保留 10 点 */
    public static void consumeDurability(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Integer max = boots.get(DataComponents.MAX_DAMAGE);
        if (max == null || max <= 10) return;
        int dmg = boots.getOrDefault(DataComponents.DAMAGE, 0);
        int limit = max - 10; // 最多扣到还剩 10 点耐久
        if (dmg < limit) {
            boots.set(DataComponents.DAMAGE, Math.min(limit, dmg + 1));
        }
    }
}