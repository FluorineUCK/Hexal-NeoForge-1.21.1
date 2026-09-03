package ram.talia.hexal.common.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static ram.talia.hexal.Hexal.modLoc;

public record MsgSingleParticleAck(Vec3 pos, FrozenPigment colouriser) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgSingleParticleAck> TYPE = new CustomPacketPayload.Type<>(modLoc("sngprt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSingleParticleAck> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, MsgSingleParticleAck>() {
        @Override
        public @NotNull MsgSingleParticleAck decode(RegistryFriendlyByteBuf byteBuf) {
            Vec3 position = new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
            FrozenPigment pigment = FrozenPigment.STREAM_CODEC.decode(byteBuf);
            return new MsgSingleParticleAck(position, pigment);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MsgSingleParticleAck msgSingleParticleAck) {
            buf.writeDouble(msgSingleParticleAck.pos.x);
            buf.writeDouble(msgSingleParticleAck.pos.y);
            buf.writeDouble(msgSingleParticleAck.pos.z);
            FrozenPigment.STREAM_CODEC.encode(buf, msgSingleParticleAck.colouriser);
        }
    };

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
