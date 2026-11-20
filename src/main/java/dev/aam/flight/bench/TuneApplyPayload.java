package dev.aam.flight.bench;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端 → 服务端：保存调参结果喵
 * reset=true 表示全部恢复默认喵
 */
public record TuneApplyPayload(BlockPos pos, List<String> names, List<Double> values, boolean reset)
        implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("aamflight", "tune_apply");
    public static final Type<TuneApplyPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TuneApplyPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TuneApplyPayload::pos,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), TuneApplyPayload::names,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.DOUBLE), TuneApplyPayload::values,
            ByteBufCodecs.BOOL, TuneApplyPayload::reset,
            TuneApplyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
