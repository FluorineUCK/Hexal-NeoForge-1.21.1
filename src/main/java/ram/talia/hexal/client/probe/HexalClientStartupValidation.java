package ram.talia.hexal.client.probe;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.client.blocks.BlockEntityRelayRenderer;
import ram.talia.hexal.common.blocks.BlockRelay;
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay;
import ram.talia.hexal.common.lib.HexalBlocks;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/** Opt-in title-screen smoke test used by the port validation run. */
@OnlyIn(Dist.CLIENT)
public final class HexalClientStartupValidation {
    public static final String ENABLE_PROPERTY = "hexal.probe.exitAtTitle";
    public static final String RELAY_ORIENTATION_PROPERTY = "hexal.probe.validateRelayOrientation";
    private static final AtomicBoolean COMPLETED = new AtomicBoolean();

    private HexalClientStartupValidation() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY) || COMPLETED.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TitleScreen && COMPLETED.compareAndSet(false, true)) {
            if (Boolean.getBoolean(RELAY_ORIENTATION_PROPERTY)) {
                validateRelayOrientation();
            }

            Hexal.LOGGER.info("[HEXAL-PROBE] client_startup=PASS title_screen=PASS resources_loaded=PASS");
            minecraft.stop();
        }
    }

    /**
     * Verifies the complete static orientation seam used by GeckoLib: relay state -> block entity state ->
     * GeoBlockRenderer#getFacing -> Hexal's custom rotation. This intentionally runs only in opt-in dev probes.
     */
    private static void validateRelayOrientation() {
        try {
            Method getFacing = GeoBlockRenderer.class.getDeclaredMethod("getFacing", BlockEntity.class);
            getFacing.setAccessible(true);
            Method rotateBlock = BlockEntityRelayRenderer.class.getDeclaredMethod("rotateBlock", Direction.class, PoseStack.class);
            rotateBlock.setAccessible(true);

            BlockEntityRelayRenderer renderer = new BlockEntityRelayRenderer();
            for (Direction expected : Direction.values()) {
                BlockState state = HexalBlocks.RELAY.defaultBlockState().setValue(BlockRelay.Companion.getFACING(), expected);
                BlockEntityRelay relay = new BlockEntityRelay(BlockPos.ZERO, state);
                Direction resolved = (Direction)getFacing.invoke(renderer, relay);

                PoseStack poseStack = new PoseStack();
                rotateBlock.invoke(renderer, resolved, poseStack);
                Vector3f modelUp = poseStack.last().pose().transformDirection(new Vector3f(0, 1, 0));
                Vector3f expectedNormal = new Vector3f(expected.getStepX(), expected.getStepY(), expected.getStepZ());

                boolean stateMatches = relay.getBlockState().getValue(BlockRelay.Companion.getFACING()) == expected;
                boolean facingMatches = resolved == expected;
                boolean rotationMatches = modelUp.distance(expectedNormal) < 0.0001f;
                Hexal.LOGGER.info(
                        "[HEXAL-PROBE] relay_orientation direction={} be_state={} gecko_facing={} model_up=({},{},{}) pass={}",
                        expected,
                        relay.getBlockState().getValue(BlockRelay.Companion.getFACING()),
                        resolved,
                        modelUp.x(), modelUp.y(), modelUp.z(),
                        stateMatches && facingMatches && rotationMatches
                );

                if (!stateMatches || !facingMatches || !rotationMatches) {
                    throw new IllegalStateException("Relay orientation validation failed for " + expected);
                }
            }

            Hexal.LOGGER.info("[HEXAL-PROBE] relay_orientation=PASS directions=6");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to validate Relay orientation", exception);
        }
    }
}
