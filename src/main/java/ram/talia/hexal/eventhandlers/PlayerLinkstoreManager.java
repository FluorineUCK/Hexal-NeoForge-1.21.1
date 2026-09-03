package ram.talia.hexal.eventhandlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persistent server link state plus lightweight client render-centre state. */
public final class PlayerLinkstoreManager {
    private static final String TAG_PLAYER_LINKSTORE = "hexal:player_linkstore";
    private static final Map<UUID, PlayerLinkstore> LINKSTORES = new HashMap<>();
    private static final Map<UUID, PlayerLinkstore.RenderCentre> RENDER_CENTRES = new HashMap<>();

    private PlayerLinkstoreManager() {
    }

    public static PlayerLinkstore getLinkstore(ServerPlayer player) {
        return LINKSTORES.computeIfAbsent(player.getUUID(), ignored -> loadLinkstore(player));
    }

    public static PlayerLinkstore.RenderCentre getRenderCentre(Player player) {
        var current = RENDER_CENTRES.get(player.getUUID());
        if (current == null || current.getPlayer() != player) {
            current = new PlayerLinkstore.RenderCentre(player);
            RENDER_CENTRES.put(player.getUUID(), current);
        }
        return current;
    }

    private static PlayerLinkstore loadLinkstore(ServerPlayer player) {
        var linkstore = new PlayerLinkstore(player);
        linkstore.loadAdditionalData(player.getPersistentData().getCompound(TAG_PLAYER_LINKSTORE));
        return linkstore;
    }

    private static void saveLinkstore(ServerPlayer player) {
        var linkstore = LINKSTORES.get(player.getUUID());
        if (linkstore == null) return;

        var tag = new CompoundTag();
        linkstore.saveAdditionalData(tag);
        player.getPersistentData().put(TAG_PLAYER_LINKSTORE, tag);
    }

    public static @Nullable ILinkable getTransmittingTo(ServerPlayer player) {
        return getLinkstore(player).getTransmittingTo();
    }

    public static void setTransmittingTo(ServerPlayer player, int index) {
        getLinkstore(player).setTransmittingTo(index);
    }

    public static void resetTransmittingTo(ServerPlayer player) {
        getLinkstore(player).resetTransmittingTo();
    }

    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LINKSTORES.put(player.getUUID(), loadLinkstore(player));
        }
    }

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            saveLinkstore(player);
            LINKSTORES.remove(player.getUUID());
        }
    }

    public static void playerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var originalTag = new CompoundTag();
        var liveOriginal = LINKSTORES.get(event.getOriginal().getUUID());
        if (liveOriginal != null) {
            liveOriginal.saveAdditionalData(originalTag);
        } else {
            originalTag = event.getOriginal().getPersistentData().getCompound(TAG_PLAYER_LINKSTORE).copy();
        }
        if (!originalTag.isEmpty()) {
            player.getPersistentData().put(TAG_PLAYER_LINKSTORE, originalTag);
        }
        LINKSTORES.remove(player.getUUID());
        LINKSTORES.put(player.getUUID(), loadLinkstore(player));
    }

    public static void playerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            getLinkstore(player).checkLinks();
            if (player.tickCount % 200 == 0) saveLinkstore(player);
        }
    }

    public static void clientTick(@Nullable Level level) {
        if (level == null) {
            RENDER_CENTRES.clear();
            return;
        }

        Set<UUID> present = new HashSet<>();
        for (Player player : level.players()) {
            present.add(player.getUUID());
            getRenderCentre(player).renderLinks();
        }
        RENDER_CENTRES.keySet().removeIf(uuid -> !present.contains(uuid));
    }
}
