package ram.talia.hexal.common.lib;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.HexRegistries;
import net.minecraft.tags.TagKey;
import ram.talia.hexal.Hexal;

public final class HexalTags {
    private HexalTags() {}

    public static final TagKey<IotaType<?>> ILLEGAL_INTERWORLD =
            TagKey.create(HexRegistries.IOTA_TYPE, Hexal.modLoc("illegal_interworld"));
}
