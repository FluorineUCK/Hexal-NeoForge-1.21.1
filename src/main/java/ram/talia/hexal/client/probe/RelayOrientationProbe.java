package ram.talia.hexal.client.probe;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import ram.talia.hexal.client.blocks.BlockEntityRelayRenderer;

import java.lang.reflect.Method;

/** Headless regression probe for Hexal Relay's six-direction GeckoLib rendering seam. */
public final class RelayOrientationProbe {
    private RelayOrientationProbe() {
    }

    public static void main(String[] args) throws Exception {
        Method rotateBlock = BlockEntityRelayRenderer.class.getDeclaredMethod("rotateBlock", Direction.class, PoseStack.class);
        rotateBlock.setAccessible(true);
        BlockEntityRelayRenderer renderer = new BlockEntityRelayRenderer();

        for (Direction expected : Direction.values()) {
            PoseStack poseStack = new PoseStack();
            rotateBlock.invoke(renderer, expected, poseStack);
            Vector3f modelUp = poseStack.last().pose().transformDirection(new Vector3f(0, 1, 0));
            Vector3f expectedNormal = new Vector3f(expected.getStepX(), expected.getStepY(), expected.getStepZ());

            boolean rotationMatches = modelUp.distance(expectedNormal) < 0.0001f;
            boolean passed = rotationMatches;
            System.out.printf(
                    "[HEXAL-PROBE] direction=%s model_up=(%.3f,%.3f,%.3f) pass=%s%n",
                    expected,
                    modelUp.x(), modelUp.y(), modelUp.z(),
                    passed
            );

            if (!passed) {
                throw new AssertionError("Relay orientation validation failed for " + expected);
            }
        }

        System.out.println("[HEXAL-PROBE] relay_orientation=PASS directions=6");
    }
}
