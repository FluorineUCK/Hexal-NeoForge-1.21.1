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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.casting.wisp.WispCastingManager;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.eventhandlers.BoundStorageEventHandler;
import net.neoforged.fml.loading.FMLLoader;
import ram.talia.hexal.eventhandlers.WispCastingManagerEventHandler;
import ram.talia.hexal.eventhandlers.PlayerLinkstoreManager;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.common.network.MsgAddRenderLinkS2C;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkS2C;
import ram.talia.hexal.common.network.MsgSetRenderLinksS2C;

import java.util.List;
import java.util.UUID;

import static at.petrak.hexcasting.xplat.IXplatAbstractions.HEXCASTING;

public class NeoForgeXplatImpl implements IXplatAbstractions {

	@Override
	public boolean isPhysicalClient() {
		return FMLLoader.getDist() == Dist.CLIENT;
	}

	@Override
	public void sendPacketToPlayer(ServerPlayer target, CustomPacketPayload packet) {
		PacketDistributor.sendToPlayer(target, packet);
	}
	
	@Override
	public void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersNear(dimension, null, pos.x, pos.y, pos.z, radius, packet);
	}

	@Override
	public void sendPacketTracking(Entity entity, CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
	}

	@Override
	public void sendPacketTracking(BlockEntity blockEntity, CustomPacketPayload packet) {
		sendPacketTracking(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel(), packet);
	}

	@Override
	public void sendPacketTracking(BlockPos pos, ServerLevel dimension, CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersTrackingChunk(dimension, dimension.getChunk(pos).getPos(), packet);
	}

	@Override
	public void sendPacketTracking(ChunkPos pos, ServerLevel dimension, CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersTrackingChunk(dimension, pos, packet);
	}

	@Override
	public Packet<?> toVanillaClientboundPacket(CustomPacketPayload message) {
		return message.toVanillaClientbound();
	}

	@Override
	public boolean isInteractingAllowed(Level level, BlockPos pos, Direction direction, InteractionHand hand, Player player) {
		return !NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, hand, pos, new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, true))).isCanceled();
	}

	@Override
	public WispCastingManager getWispCastingManager(ServerPlayer caster) {
		return WispCastingManagerEventHandler.getCastingManager(caster);
	}

	@Override
	public void setSeon(ServerPlayer caster, @Nullable BaseCastingWisp wisp) {
		WispCastingManagerEventHandler.setSeon(caster, wisp);
	}

	@Override
	public @Nullable BaseCastingWisp getSeon(ServerPlayer caster) {
		return WispCastingManagerEventHandler.getSeon(caster);
	}

	@Override
	public PlayerLinkstore getLinkstore(ServerPlayer player) {
		return PlayerLinkstoreManager.getLinkstore(player);
	}

	@Override
	public PlayerLinkstore.RenderCentre getPlayerRenderCentre(Player player) {
		return PlayerLinkstoreManager.getRenderCentre(player);
	}

	@Override
	public void syncAddRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level) {
		sendPacketTracking(BlockPos.containing(sourceLink.getPosition()), level, new MsgAddRenderLinkS2C(sourceLink, sinkLink));
	}

	@Override
	public void syncRemoveRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level) {
		sendPacketTracking(BlockPos.containing(sourceLink.getPosition()), level, new MsgRemoveRenderLinkS2C(sourceLink, sinkLink));
	}

	@Override
	public void syncSetRenderLinks(ILinkable sourceLink, List<ILinkable> sinks, ServerLevel level) {
		sendPacketTracking(BlockPos.containing(sourceLink.getPosition()), level, new MsgSetRenderLinksS2C(sourceLink, sinks));
	}

	@Override
	public @Nullable ILinkable getPlayerTransmittingTo(ServerPlayer player) {
		return PlayerLinkstoreManager.getTransmittingTo(player);
	}

	@Override
	public void setPlayerTransmittingTo(ServerPlayer player, int to) {
		PlayerLinkstoreManager.setTransmittingTo(player, to);
	}

	@Override
	public void resetPlayerTransmittingTo(ServerPlayer player) {
		PlayerLinkstoreManager.resetTransmittingTo(player);
	}

	@Override
	public @Nullable UUID getBoundStorage(ServerPlayer player) {
		return BoundStorageEventHandler.getBoundStorage(player);
	}

	@Override
	public void setBoundStorage(ServerPlayer player, @Nullable UUID storage) {
		BoundStorageEventHandler.setBoundStorage(player, storage);
	}

    @Override
	public ServerPlayer getFakePlayer(ServerLevel level, UUID uuid) {
		return getFakePlayer(level, new GameProfile(uuid, "[Hexal]"));
	}

	@Override
	public ServerPlayer getFakePlayer(ServerLevel level, GameProfile profile) {
		return FakePlayerFactory.get(level, profile);
	}

	@Override
	public boolean isBreakingAllowed(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player) {
		if (player == null) {
			player = FakePlayerFactory.get(level, HEXCASTING);
		}

		return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled();
	}

	@Override
	public boolean isPlacingAllowed(ServerLevel world, BlockPos pos, ItemStack blockStack, @Nullable Player player) {
		if (player == null)
			player = FakePlayerFactory.get(world, HEXCASTING);
		ItemStack cached = player.getMainHandItem();
		player.setItemInHand(InteractionHand.MAIN_HAND, blockStack.copy());
		var evt = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, pos,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, true));
		player.setItemInHand(InteractionHand.MAIN_HAND, cached);
		return !evt.isCanceled();
	}
}
