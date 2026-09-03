package ram.talia.hexal.client.everbook;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.api.everbook.FileEncrypterDecrypter;
import ram.talia.hexal.common.network.MsgSyncEverbookC2S;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Client-only durable Everbook storage and delayed-save coordinator. */
public final class ClientEverbookStore {
    private ClientEverbookStore() {}

    private static Everbook local;
    private static UUID activePlayer;
    private static long saveAt = Long.MAX_VALUE;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            if (local != null) saveNow();
            local = null;
            activePlayer = null;
            saveAt = Long.MAX_VALUE;
            return;
        }

        UUID playerUuid = minecraft.player.getUUID();
        if (local == null || !playerUuid.equals(activePlayer)) {
            activePlayer = playerUuid;
            local = load(playerUuid);
            PacketDistributor.sendToServer(new MsgSyncEverbookC2S(local));
        }

        if (minecraft.level.getGameTime() >= saveAt) saveNow();
    }

    public static CompoundTag getIota(HexPattern key) {
        return local == null ? null : local.getClientIota(key);
    }

    public static HexPattern getPattern(int index) {
        return local == null ? null : local.getKey(index);
    }

    public static boolean isMacro(HexPattern key) {
        return local != null && local.isMacro(key);
    }

    public static void setIota(HexPattern key, CompoundTag iota) {
        if (local == null) return;
        local.setIota(key, iota);
        markDirty();
    }

    public static void removeIota(HexPattern key) {
        if (local == null) return;
        local.removeIota(key);
        markDirty();
    }

    public static void toggleMacro(HexPattern key) {
        if (local == null) return;
        local.toggleMacro(key);
        markDirty();
    }

    private static void markDirty() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        saveAt = now + HexalConfig.Client.EVERBOOK_SAVE_DELAY.get();
    }

    public static void saveNow() {
        if (local == null || activePlayer == null) return;
        Path path = pathFor(activePlayer);
        try {
            Files.createDirectories(path.getParent());
            rotateBackups(path, activePlayer);
            FileEncrypterDecrypter.Companion.forUuid(activePlayer).encrypt(local.serialiseToNBT(), path.toFile());
            saveAt = Long.MAX_VALUE;
        } catch (Exception ex) {
            Hexal.LOGGER.error("Failed to save Everbook {}", path, ex);
        }
    }

    private static Everbook load(UUID uuid) {
        Path path = pathFor(uuid);
        Path legacy = Minecraft.getInstance().gameDirectory.toPath().resolve("everbook.dat");
        Path chosen = Files.exists(path) ? path : legacy;
        CompoundTag tag = FileEncrypterDecrypter.Companion.forUuid(uuid)
                .decryptCompound(chosen.toFile(), HexalConfig.Server.EVERBOOK_MAX_SIZE.get());
        return tag == null ? new Everbook(uuid) : Everbook.fromNbt(tag, uuid);
    }

    private static Path pathFor(UUID uuid) {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("everbook").resolve("everbook-" + uuid + ".dat");
    }

    private static void rotateBackups(Path source, UUID uuid) throws IOException {
        if (!Files.exists(source)) return;
        Path directory = source.getParent();
        List<Path> backups;
        try (var stream = Files.list(directory)) {
            backups = stream.filter(path -> path.getFileName().toString().contains(uuid.toString())
                            && path.getFileName().toString().contains("backup"))
                    .sorted(Comparator.comparingLong(ClientEverbookStore::lastModified))
                    .toList();
        }
        Path destination;
        if (backups.size() < 6) {
            destination = directory.resolve("everbook-" + uuid + "-backup-" + backups.size() + ".dat");
        } else {
            destination = backups.getFirst();
        }
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return Long.MIN_VALUE;
        }
    }
}
