package ram.talia.hexal.api;

import com.google.common.base.Suppliers;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ram.talia.hexal.Hexal;

import java.util.function.Supplier;

/** Stable public entry point retained for addons compiled against Hexal's API. */
public interface HexalAPI {
    String MOD_ID = Hexal.MODID;
    Logger LOGGER = LogManager.getLogger(MOD_ID);

    Supplier<HexalAPI> INSTANCE = Suppliers.memoize(() -> new HexalAPI() {});

    static HexalAPI instance() {
        return INSTANCE.get();
    }

    static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
