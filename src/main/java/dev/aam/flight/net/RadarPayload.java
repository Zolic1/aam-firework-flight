package dev.aam.flight.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端 -> 客户端 的雷达数据包。
 * 客户端拿不到远处实体（实体追踪距离），所以必须由服务端扫描后下发。
 *
 * 锁定信息里直接下发「眼睛 -> 目标中心」的偏移量（lockDx/Dy/Dz），
 * 客户端用相机基向量做精确投影，避免角度近似的误差喵。
 */
public record RadarPayload(
        List<Blip> blips,
        boolean threat,
        boolean locked,
        String lockName,
        float lockHp,
        float lockMaxHp,
        float lockDist,
        float lockBearing,
        float lockElev,
        float lockDx,
        float lockDy,
        float lockDz,
        float lockWidth,
        float lockHeight,
        float lockSpeed,
        float lockProgress,
        boolean lockInView,
        boolean lockFriendly,
        boolean lockedOnMe,
        boolean lockIsMissile,   // 锁的是导弹喵
        float lockManeuver,      // 机动时间（剩余寿命，秒喵）
        float lockEta,           // 预计到达倒计时（秒喵）
        boolean lockDestroyed    // 目标刚被击毁（显示 0.7 秒残影喵）
) implements CustomPacketPayload {

    /** 雷达光点：方位角（相对玩家朝向，度）、俯仰角（度）、距离（格）、类型 */
    public record Blip(float bearing, float elev, float dist, byte type,
                             float dx, float dy, float dz, boolean friendly) {}

    public static final byte TYPE_PLAYER = 0;
    public static final byte TYPE_MISSILE = 1;

    public static final CustomPacketPayload.Type<RadarPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("aamflight", "radar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadarPayload> CODEC = new StreamCodec<>() {
        @Override
        public RadarPayload decode(RegistryFriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<Blip> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                list.add(new Blip(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readByte(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean()));
            }
            boolean threat = buf.readBoolean();
            boolean locked = buf.readBoolean();
            String name = buf.readUtf(64);
            float hp = buf.readFloat();
            float maxHp = buf.readFloat();
            float dist = buf.readFloat();
            float bearing = buf.readFloat();
            float elev = buf.readFloat();
            float dx = buf.readFloat();
            float dy = buf.readFloat();
            float dz = buf.readFloat();
            float width = buf.readFloat();
            float height = buf.readFloat();
            float speed = buf.readFloat();
            float progress = buf.readFloat();
            boolean inView = buf.readBoolean();
            boolean friendly = buf.readBoolean();
            boolean lockedOnMe = buf.readBoolean();
            boolean isMissile = buf.readBoolean();
            float maneuver = buf.readFloat();
            float eta = buf.readFloat();
            boolean destroyed = buf.readBoolean();
            return new RadarPayload(list, threat, locked, name, hp, maxHp, dist, bearing, elev,
                    dx, dy, dz, width, height, speed, progress, inView, friendly,
                    lockedOnMe, isMissile, maneuver, eta, destroyed);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RadarPayload payload) {
            buf.writeVarInt(payload.blips().size());
            for (Blip b : payload.blips()) {
                buf.writeFloat(b.bearing());
                buf.writeFloat(b.elev());
                buf.writeFloat(b.dist());
                buf.writeByte(b.type());
                buf.writeFloat(b.dx());
                buf.writeFloat(b.dy());
                buf.writeFloat(b.dz());
                buf.writeBoolean(b.friendly());
            }
            buf.writeBoolean(payload.threat());
            buf.writeBoolean(payload.locked());
            buf.writeUtf(payload.lockName(), 64);
            buf.writeFloat(payload.lockHp());
            buf.writeFloat(payload.lockMaxHp());
            buf.writeFloat(payload.lockDist());
            buf.writeFloat(payload.lockBearing());
            buf.writeFloat(payload.lockElev());
            buf.writeFloat(payload.lockDx());
            buf.writeFloat(payload.lockDy());
            buf.writeFloat(payload.lockDz());
            buf.writeFloat(payload.lockWidth());
            buf.writeFloat(payload.lockHeight());
            buf.writeFloat(payload.lockSpeed());
            buf.writeFloat(payload.lockProgress());
            buf.writeBoolean(payload.lockInView());
            buf.writeBoolean(payload.lockFriendly());
            buf.writeBoolean(payload.lockedOnMe());
            buf.writeBoolean(payload.lockIsMissile());
            buf.writeFloat(payload.lockManeuver());
            buf.writeFloat(payload.lockEta());
            buf.writeBoolean(payload.lockDestroyed());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
