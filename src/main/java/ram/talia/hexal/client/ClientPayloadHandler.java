package ram.talia.hexal.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import ram.talia.hexal.client.everbook.ClientEverbookStore;
import ram.talia.hexal.common.network.MsgAddRenderLinkS2C;
import ram.talia.hexal.common.network.MsgParticleLines;
import ram.talia.hexal.common.network.MsgRemoveEverbookS2C;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkS2C;
import ram.talia.hexal.common.network.MsgSetEverbookS2C;
import ram.talia.hexal.common.network.MsgSetRenderLinksS2C;
import ram.talia.hexal.common.network.MsgSingleParticleAck;
import ram.talia.hexal.common.network.MsgToggleMacroS2C;
import ram.talia.hexal.common.network.MsgWispCastSoundS2C;

/** Keeps every client-only packet effect out of the common payload data classes. */
@OnlyIn(Dist.CLIENT)
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handle(CustomPacketPayload payload) {
        if (payload instanceof MsgParticleLines message) {
            ClientParticlePacketHandler.handle(message);
        } else if (payload instanceof MsgSingleParticleAck message) {
            ClientParticlePacketHandler.handle(message);
        } else if (payload instanceof MsgWispCastSoundS2C message) {
            ClientWispSoundHandler.handle(message.getWispId());
        } else if (payload instanceof MsgSetEverbookS2C message) {
            ClientEverbookStore.setIota(message.key(), message.iota());
        } else if (payload instanceof MsgRemoveEverbookS2C message) {
            ClientEverbookStore.removeIota(message.key());
        } else if (payload instanceof MsgToggleMacroS2C message) {
            ClientEverbookStore.toggleMacro(message.key());
        } else if (payload instanceof MsgAddRenderLinkS2C message) {
            ClientLinkablePacketHandler.add(message);
        } else if (payload instanceof MsgRemoveRenderLinkS2C message) {
            ClientLinkablePacketHandler.remove(message);
        } else if (payload instanceof MsgSetRenderLinksS2C message) {
            ClientLinkablePacketHandler.set(message);
        } else {
            throw new IllegalArgumentException("Unhandled Hexal client payload " + payload.type().id());
        }
    }
}
