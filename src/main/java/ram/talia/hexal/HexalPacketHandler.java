package ram.talia.hexal;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;
import ram.talia.hexal.client.ClientPayloadHandler;
import ram.talia.hexal.common.network.MsgParticleLines;
import ram.talia.hexal.common.network.MsgSingleParticleAck;
import ram.talia.hexal.common.network.MsgWispCastSoundS2C;
import ram.talia.hexal.common.network.MsgSyncEverbookC2S;
import ram.talia.hexal.common.network.MsgSetEverbookS2C;
import ram.talia.hexal.common.network.MsgRemoveEverbookS2C;
import ram.talia.hexal.common.network.MsgToggleMacroS2C;
import ram.talia.hexal.common.network.MsgAddRenderLinkS2C;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkS2C;
import ram.talia.hexal.common.network.MsgSetRenderLinksS2C;

public class HexalPacketHandler {
    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(MsgParticleLines.TYPE, MsgParticleLines.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgSingleParticleAck.TYPE, MsgSingleParticleAck.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgWispCastSoundS2C.TYPE, MsgWispCastSoundS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToServer(MsgSyncEverbookC2S.TYPE, MsgSyncEverbookC2S.STREAM_CODEC,
                    makeServerBoundHandler(MsgSyncEverbookC2S::handle));
            registrar.playToClient(MsgSetEverbookS2C.TYPE, MsgSetEverbookS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgRemoveEverbookS2C.TYPE, MsgRemoveEverbookS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgToggleMacroS2C.TYPE, MsgToggleMacroS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgAddRenderLinkS2C.TYPE, MsgAddRenderLinkS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgRemoveRenderLinkS2C.TYPE, MsgRemoveRenderLinkS2C.STREAM_CODEC,
                    makeClientBoundHandler());
            registrar.playToClient(MsgSetRenderLinksS2C.TYPE, MsgSetRenderLinksS2C.STREAM_CODEC,
                    makeClientBoundHandler());
        });
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeServerBoundHandler(
            TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
        return (m, ctx) -> {
            ctx.enqueueWork(() -> handler.accept(m, ctx.player().getServer(), (ServerPlayer) ctx.player()));
        };
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler() {
        return (m, ctx) -> {
            ctx.enqueueWork(() -> ClientPayloadHandler.handle(m));
        };
    }
}
