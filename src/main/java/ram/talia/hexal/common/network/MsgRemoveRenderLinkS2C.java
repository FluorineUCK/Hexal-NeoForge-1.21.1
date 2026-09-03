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

public record MsgRemoveRenderLinkS2C(CompoundTag source, CompoundTag sink) implements CustomPacketPayload {
    public static final Type<MsgRemoveRenderLinkS2C> TYPE = new Type<>(Hexal.modLoc("render_link_remove"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgRemoveRenderLinkS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, MsgRemoveRenderLinkS2C::source,
            ByteBufCodecs.COMPOUND_TAG, MsgRemoveRenderLinkS2C::sink,
            MsgRemoveRenderLinkS2C::new);

    public MsgRemoveRenderLinkS2C(ILinkable source, ILinkable sink) {
        this(LinkableRegistry.wrapSync(source), LinkableRegistry.wrapSync(sink));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
