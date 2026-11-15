package dev.aam.flight.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 -> 服务端：右键是否按住（用于轻机枪连发喵） */
public record FirePayload(boolean pressed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FirePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("aamflight", "firing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FirePayload> CODEC =
            CustomPacketPayload.codec(FirePayload::write, FirePayload::new);

    private FirePayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(pressed);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}