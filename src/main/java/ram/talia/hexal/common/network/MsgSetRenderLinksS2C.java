package ram.talia.hexal.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;

import java.util.List;

public record MsgSetRenderLinksS2C(CompoundTag source, CompoundTag payload) implements CustomPacketPayload {
    private static final String TAG_SINKS = "sinks";
    public static final Type<MsgSetRenderLinksS2C> TYPE = new Type<>(Hexal.modLoc("render_links_set"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSetRenderLinksS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, MsgSetRenderLinksS2C::source,
            ByteBufCodecs.COMPOUND_TAG, MsgSetRenderLinksS2C::payload,
            MsgSetRenderLinksS2C::new);

    public MsgSetRenderLinksS2C(ILinkable source, List<ILinkable> sinks) {
        this(LinkableRegistry.wrapSync(source), wrapSinks(sinks));
    }

    private static CompoundTag wrapSinks(List<ILinkable> sinks) {
        var list = new ListTag();
        for (ILinkable sink : sinks) {
            list.add(LinkableRegistry.wrapSync(sink));
        }
        var tag = new CompoundTag();
        tag.put(TAG_SINKS, list);
        return tag;
    }

    public ListTag sinks() {
        return payload.getList(TAG_SINKS, CompoundTag.TAG_COMPOUND);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
