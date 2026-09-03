package ram.talia.hexal.probe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay;
import ram.talia.hexal.common.lib.HexalBlocks;

@Mod(RelaySerializationProbe.MOD_ID)
public final class RelaySerializationProbe {
    static final String MOD_ID = "hexal_relay_serialization_probe";
    private static final String HOLDER_TAG = "hexal:linkable_holder";

    public RelaySerializationProbe() {
        NeoForge.EVENT_BUS.addListener(RelaySerializationProbe::onServerStarted);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var registries = server.registryAccess();
        var level = server.overworld();
        BlockPos livePos = level.getSharedSpawnPos().above(4);
        try {
            // Models the client BlockSnapshot path: no ServerLevel means no ServerLinkableHolder.
            var detached = new BlockEntityRelay(BlockPos.ZERO, HexalBlocks.RELAY.defaultBlockState());
            CompoundTag detachedSaved = detached.saveCustomOnly(registries);
            require(!detachedSaved.contains(HOLDER_TAG),
                    "fresh detached relay unexpectedly wrote a server holder");

            // Client-side save must preserve holder NBT received from server, without constructing a server holder.
            var loadedDetached = new BlockEntityRelay(BlockPos.ZERO, HexalBlocks.RELAY.defaultBlockState());
            CompoundTag original = new CompoundTag();
            CompoundTag holder = new CompoundTag();
            holder.putString("relay_probe", "preserved");
            original.put(HOLDER_TAG, holder);
            loadedDetached.loadCustomOnly(original, registries);
            CompoundTag roundTrip = loadedDetached.saveCustomOnly(registries);
            require(roundTrip.contains(HOLDER_TAG), "serialized holder was dropped");
            require("preserved".equals(roundTrip.getCompound(HOLDER_TAG).getString("relay_probe")),
                    "serialized holder contents changed");

            // Server serialization must still materialize and write the real ServerLinkableHolder.
            level.setBlockAndUpdate(livePos, HexalBlocks.RELAY.defaultBlockState());
            var live = level.getBlockEntity(livePos);
            require(live instanceof BlockEntityRelay, "live relay block entity was not created");
            CompoundTag liveSaved = live.saveCustomOnly(registries);
            require(liveSaved.contains(HOLDER_TAG), "server relay did not serialize its holder");

            Hexal.LOGGER.info("[HEXAL-RELAY-PROBE] detached_save=PASS preserved_holder=PASS server_holder=PASS");
        } catch (Throwable failure) {
            Hexal.LOGGER.error("[HEXAL-RELAY-PROBE] detached_save=FAIL", failure);
        } finally {
            level.removeBlock(livePos, false);
            server.execute(() -> server.halt(false));
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}