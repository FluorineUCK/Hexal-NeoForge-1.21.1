package ram.talia.hexal.xplat;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.casting.wisp.WispCastingManager;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;

import java.util.List;
import java.util.UUID;

public interface IXplatAbstractions {
    boolean isPhysicalClient();

    void sendPacketToPlayer(ServerPlayer target, CustomPacketPayload packet);

    void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, CustomPacketPayload packet);

    void sendPacketTracking(Entity entity, CustomPacketPayload packet);
    void sendPacketTracking(BlockEntity blockEntity, CustomPacketPayload packet);
    void sendPacketTracking(BlockPos pos, ServerLevel dimension, CustomPacketPayload packet);
    void sendPacketTracking(ChunkPos pos, ServerLevel dimension, CustomPacketPayload packet);

    Packet<?> toVanillaClientboundPacket(CustomPacketPayload message);

    boolean isInteractingAllowed(Level level, BlockPos pos, Direction direction, InteractionHand hand, Player player);

    WispCastingManager getWispCastingManager(ServerPlayer caster);

    /**
     * Takes in a caster and wisp, and sets that caster's Seon (wisp that costs significantly less to maintain) to the
     * accepted wisp. The old Seon if one exists is unmarked.
     */
    void setSeon(ServerPlayer caster, @Nullable BaseCastingWisp wisp);

    @Nullable
    BaseCastingWisp getSeon(ServerPlayer caster);

    PlayerLinkstore getLinkstore(ServerPlayer player);

    PlayerLinkstore.RenderCentre getPlayerRenderCentre(Player player);

    void syncAddRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level);

    void syncRemoveRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level);

    void syncSetRenderLinks(ILinkable sourceLink, List<ILinkable> sinks, ServerLevel level);

    @Nullable ILinkable getPlayerTransmittingTo(ServerPlayer player);

    void setPlayerTransmittingTo(ServerPlayer player, int to);

    void resetPlayerTransmittingTo(ServerPlayer player);
    @Nullable UUID getBoundStorage(ServerPlayer player);

    void setBoundStorage(ServerPlayer player, @Nullable UUID storage);

    ServerPlayer getFakePlayer(ServerLevel level, UUID uuid);
    ServerPlayer getFakePlayer(ServerLevel level, GameProfile profile);

    boolean isBreakingAllowed(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player);

    boolean isPlacingAllowed(ServerLevel level, BlockPos pos, ItemStack stack, @Nullable Player player);

    IXplatAbstractions INSTANCE = new NeoForgeXplatImpl();
}
