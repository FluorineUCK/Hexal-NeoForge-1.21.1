package ram.talia.hexal.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.eventhandlers.EverbookManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public record MsgSyncEverbookC2S(byte[] compressedBook) implements CustomPacketPayload {
    public static final int MAX_WIRE_BYTES = 72 * 1024 * 1024;
    public static final Type<MsgSyncEverbookC2S> TYPE = new Type<>(Hexal.modLoc("everbook_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSyncEverbookC2S> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull MsgSyncEverbookC2S decode(RegistryFriendlyByteBuf buf) {
                    return new MsgSyncEverbookC2S(buf.readByteArray(MAX_WIRE_BYTES));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, MsgSyncEverbookC2S payload) {
                    buf.writeByteArray(payload.compressedBook);
                }
            };

    public MsgSyncEverbookC2S(Everbook everbook) {
        this(compress(everbook.serialiseToNBT()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgSyncEverbookC2S payload, MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            long maxSize = HexalConfig.Server.EVERBOOK_MAX_SIZE.get();
            try {
                CompoundTag tag = NbtIo.readCompressed(
                        new ByteArrayInputStream(payload.compressedBook), NbtAccounter.create(maxSize));
                if (tag.sizeInBytes() <= maxSize) EverbookManager.setFull(sender, tag);
            } catch (Exception ex) {
                Hexal.LOGGER.warn("Rejected invalid or oversized Everbook from {}", sender.getGameProfile().getName(), ex);
            }
        });
    }

    private static byte[] compress(CompoundTag tag) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to encode Everbook", ex);
        }
    }
}
