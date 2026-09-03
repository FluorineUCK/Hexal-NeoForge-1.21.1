package ram.talia.hexal.common.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;

import java.util.ArrayList;
import java.util.List;

public record MsgParticleLines(List<Vec3> positions, FrozenPigment colouriser) implements CustomPacketPayload {

    public static final Type<MsgParticleLines> TYPE = new Type<>(Hexal.modLoc("prtlns"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgParticleLines> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, MsgParticleLines>() {
        @Override
        public @NotNull MsgParticleLines decode(RegistryFriendlyByteBuf byteBuf) {
            int amount = byteBuf.readInt();
            List<Vec3> posList = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                posList.add(new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble()));
            }
            FrozenPigment pigment = FrozenPigment.STREAM_CODEC.decode(byteBuf);
            return new MsgParticleLines(posList, pigment);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MsgParticleLines msgSingleParticleAck) {
            int size = msgSingleParticleAck.positions.size();
            buf.writeInt(size);
            for (int i = 0; i < size; i++) {
                Vec3 pos = msgSingleParticleAck.positions.get(i);
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
            }
            FrozenPigment.STREAM_CODEC.encode(buf, msgSingleParticleAck.colouriser);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
