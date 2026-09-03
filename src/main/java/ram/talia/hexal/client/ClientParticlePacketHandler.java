package ram.talia.hexal.client;

import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import ram.talia.hexal.api.FunUtilsKt;
import ram.talia.hexal.common.network.MsgParticleLines;
import ram.talia.hexal.common.network.MsgSingleParticleAck;

import java.util.List;

/** Client-only particle packet effects. */
public final class ClientParticlePacketHandler {
    private ClientParticlePacketHandler() {}

    public static void handle(MsgParticleLines payload) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            List<Vec3> positions = payload.positions();
            for (int i = 0; i + 1 < positions.size(); i++) {
                Vec3 start = positions.get(i);
                Vec3 end = positions.get(i + 1);
                int steps = Math.max(1, (int) (start.subtract(end).length() * 10));
                for (int step = 0; step <= steps; step++) {
                    Vec3 pos = start.add(end.subtract(start).scale((double) step / steps));
                    int colour = FunUtilsKt.nextColour(payload.colouriser(), level.random);
                    level.addParticle(new ConjureParticleOptions(colour), pos.x, pos.y, pos.z, 0, 0, 0);
                }
            }
        });
    }

    public static void handle(MsgSingleParticleAck payload) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            addParticle(level, payload, payload.pos());
            for (int i = 0; i < 11; i++) {
                Vec3 offset = new Vec3(
                        level.random.nextFloat() * 0.1 - 0.05,
                        level.random.nextFloat() * 0.1 - 0.05,
                        level.random.nextFloat() * 0.1 - 0.05);
                addParticle(level, payload, payload.pos().add(offset));
            }
        });
    }

    private static void addParticle(ClientLevel level, MsgSingleParticleAck payload, Vec3 pos) {
        int colour = FunUtilsKt.nextColour(payload.colouriser(), level.random);
        level.addParticle(new ConjureParticleOptions(colour), pos.x, pos.y, pos.z, 0, 0, 0);
    }
}
