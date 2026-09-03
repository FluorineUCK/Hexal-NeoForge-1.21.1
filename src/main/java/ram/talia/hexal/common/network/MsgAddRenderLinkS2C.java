package ram.talia.hexal.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;

public record MsgAddRenderLinkS2C(CompoundTag source, CompoundTag sink) implements CustomPacketPayload {
    public static final Type<MsgAddRenderLinkS2C> TYPE = new Type<>(Hexal.modLoc("render_link_add"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgAddRenderLinkS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, MsgAddRenderLinkS2C::source,
            ByteBufCodecs.COMPOUND_TAG, MsgAddRenderLinkS2C::sink,
            MsgAddRenderLinkS2C::new);

    public MsgAddRenderLinkS2C(ILinkable source, ILinkable sink) {
        this(LinkableRegistry.wrapSync(source), LinkableRegistry.wrapSync(sink));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
