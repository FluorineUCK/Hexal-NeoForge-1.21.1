package ram.talia.hexal.eventhandlers;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.common.network.MsgRemoveEverbookS2C;
import ram.talia.hexal.common.network.MsgSetEverbookS2C;
import ram.talia.hexal.common.network.MsgToggleMacroS2C;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side owner of per-player Everbooks. The client remains the durable cross-world store. */
public final class EverbookManager {
    private EverbookManager() {}

    private static final Map<UUID, Everbook> EVERBOOKS = new ConcurrentHashMap<>();

    private static Everbook book(ServerPlayer player) {
        return EVERBOOKS.computeIfAbsent(player.getUUID(), Everbook::new);
    }

    public static Iota getIota(ServerPlayer player, HexPattern key) {
        Everbook book = EVERBOOKS.get(player.getUUID());
        return book == null ? new NullIota() : book.getIota(key);
    }

    public static void setIota(ServerPlayer player, HexPattern key, Iota iota) {
        book(player).setIota(key, iota);
        PacketDistributor.sendToPlayer(player, new MsgSetEverbookS2C(key, (CompoundTag) ram.talia.hexal.Hexal.serializeIota(iota)));
    }

    public static void removeIota(ServerPlayer player, HexPattern key) {
        book(player).removeIota(key);
        PacketDistributor.sendToPlayer(player, new MsgRemoveEverbookS2C(key));
    }

    public static List<Iota> getMacro(ServerPlayer player, HexPattern key) {
        Everbook book = EVERBOOKS.get(player.getUUID());
        return book == null ? null : book.getMacro(key);
    }

    public static void toggleMacro(ServerPlayer player, HexPattern key) {
        book(player).toggleMacro(key);
        PacketDistributor.sendToPlayer(player, new MsgToggleMacroS2C(key));
    }

    public static void setFull(ServerPlayer player, CompoundTag tag) {
        EVERBOOKS.put(player.getUUID(), Everbook.fromNbt(tag, player.getUUID()).filterIotasIllegalInterworld());
    }

    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) book(player);
    }

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EVERBOOKS.remove(event.getEntity().getUUID());
    }
}
