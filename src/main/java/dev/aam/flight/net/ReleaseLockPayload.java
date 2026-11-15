package dev.aam.flight.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 -> 服务端：手动解除锁定（按键触发喵） */
public record ReleaseLockPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReleaseLockPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("aamflight", "release_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseLockPayload> CODEC =
            StreamCodec.unit(new ReleaseLockPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
