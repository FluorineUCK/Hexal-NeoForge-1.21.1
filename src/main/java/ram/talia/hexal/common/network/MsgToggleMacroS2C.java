package ram.talia.hexal.common.network;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;

public record MsgToggleMacroS2C(HexPattern key) implements CustomPacketPayload {
    public static final Type<MsgToggleMacroS2C> TYPE = new Type<>(Hexal.modLoc("everbook_toggle_macro"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgToggleMacroS2C> STREAM_CODEC =
            HexPattern.STREAM_CODEC.map(MsgToggleMacroS2C::new, MsgToggleMacroS2C::key);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
