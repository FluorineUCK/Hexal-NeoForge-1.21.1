package ram.talia.hexal.common.network;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;

public record MsgSetEverbookS2C(HexPattern key, CompoundTag iota) implements CustomPacketPayload {
    public static final Type<MsgSetEverbookS2C> TYPE = new Type<>(Hexal.modLoc("everbook_set"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSetEverbookS2C> STREAM_CODEC = StreamCodec.composite(
            HexPattern.STREAM_CODEC, MsgSetEverbookS2C::key,
            ByteBufCodecs.COMPOUND_TAG, MsgSetEverbookS2C::iota,
            MsgSetEverbookS2C::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
