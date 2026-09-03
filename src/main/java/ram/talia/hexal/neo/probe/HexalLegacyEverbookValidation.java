package ram.talia.hexal.neo.probe;

import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.casting.iota.GateIota;
import ram.talia.hexal.api.casting.iota.MoteIota;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.hexcompat.LegacyIotaNbtMigrator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Runtime-only validation for real Hex Casting 0.11.x Everbook data. Disabled by default. */
public final class HexalLegacyEverbookValidation {
    public static final String ENABLE_PROPERTY = "hexal.probe.validateLegacyEverbook";
    public static final String EXIT_PROPERTY = "hexal.probe.exitAfterValidation";

    private HexalLegacyEverbookValidation() {
    }

    public static void runIfEnabled(MinecraftServer server) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }

        try {
            run();
            Hexal.LOGGER.info("[HEXAL-PROBE] legacy_everbook=PASS old_container=PASS old_iotas=PASS "
                    + "nested_list=PASS current_roundtrip=PASS");
        } catch (Throwable failure) {
            Hexal.LOGGER.error("[HEXAL-PROBE] legacy_everbook=FAIL", failure);
            throw new IllegalStateException("Hexal legacy Everbook validation failed", failure);
        } finally {
            if (Boolean.getBoolean(EXIT_PROPERTY)) {
                server.execute(() -> server.halt(false));
            }
        }
    }

    private static void run() {
        UUID owner = UUID.fromString("2f73b41b-7800-4e42-b9fc-4b6560e559dd");
        UUID entity = UUID.fromString("3c681fe4-e925-49dd-90f7-04ce15774ecf");
        UUID storage = UUID.fromString("d1754df8-a21b-43b7-9da6-354e36f4f196");
        CompoundTag legacyPattern = legacyPattern(
                HexDir.SOUTH_EAST,
                HexAngle.FORWARD,
                HexAngle.RIGHT,
                HexAngle.LEFT_BACK);

        ListTag values = new ListTag();
        values.add(legacy("hexcasting:double", DoubleTag.valueOf(42.5)));
        values.add(legacy("hexcasting:boolean", ByteTag.ONE));
        values.add(legacy("hexcasting:pattern", legacyPattern.copy()));

        CompoundTag vector = new CompoundTag();
        vector.putDouble("x", 1.25);
        vector.putDouble("y", -2.5);
        vector.putDouble("z", 8.75);
        values.add(legacy("hexcasting:vec3", vector));

        CompoundTag oldEntity = new CompoundTag();
        oldEntity.putUUID("uuid", entity);
        oldEntity.putString("name", "legacy entity");
        values.add(legacy("hexcasting:entity", oldEntity));

        CompoundTag locationGate = new CompoundTag();
        locationGate.putInt("index", 17);
        locationGate.putByte("target_type", (byte) 1);
        locationGate.putDouble("target_x", 4.0);
        locationGate.putDouble("target_y", 5.0);
        locationGate.putDouble("target_z", 6.0);
        values.add(legacy("hexal:gate", locationGate));

        values.add(legacy("hexal:gate", net.minecraft.nbt.IntTag.valueOf(23)));

        CompoundTag mote = new CompoundTag();
        mote.putUUID("storage", storage);
        mote.putInt("index", 9);
        values.add(legacy("hexal:item", mote));

        CompoundTag legacyRoot = new CompoundTag();
        legacyRoot.putUUID("uuid", owner);
        CompoundTag entry = new CompoundTag();
        entry.put("pattern", legacyPattern.copy());
        entry.put("iota", legacy("hexcasting:list", values));
        legacyRoot.put("legacy-key", entry);

        Everbook migrated = Everbook.fromNbt(legacyRoot, owner);
        HexPattern key = requireNonNull(migrated.getKey(0), "legacy entry was not loaded");
        HexPattern expectedPattern = LegacyIotaNbtMigrator.decodeLegacyPattern(legacyPattern);
        require(key.getStartDir() == expectedPattern.getStartDir(), "legacy start direction changed");
        require(key.anglesSignature().equals(expectedPattern.anglesSignature()), "legacy angles changed");

        Iota decoded = migrated.getIota(key);
        require(decoded instanceof ListIota, "legacy list did not decode");
        List<Iota> decodedValues = new ArrayList<>();
        for (Iota iota : ((ListIota) decoded).getList()) {
            decodedValues.add(iota);
        }
        require(decodedValues.size() == 8, "legacy list size changed");
        require(decodedValues.get(0) instanceof DoubleIota number && number.getDouble() == 42.5,
                "legacy number changed");
        require(decodedValues.get(1) instanceof BooleanIota bool && bool.getBool(),
                "legacy boolean changed");
        require(decodedValues.get(2) instanceof PatternIota pattern
                        && pattern.getPattern().anglesSignature().equals(expectedPattern.anglesSignature()),
                "nested legacy pattern changed");
        require(decodedValues.get(3) instanceof Vec3Iota vec
                        && vec.getVec3().equals(new Vec3(1.25, -2.5, 8.75)),
                "legacy vector changed");
        require(decodedValues.get(4) instanceof EntityIota, "legacy entity did not migrate");
        require(decodedValues.get(5) instanceof GateIota gate
                        && gate.getGateIndex() == 17 && gate.isLocationAnchored(),
                "legacy location gate changed");
        require(decodedValues.get(6) instanceof GateIota gate
                        && gate.getGateIndex() == 23 && gate.isDrifting(),
                "ancient drifting gate changed");
        require(decodedValues.get(7) instanceof MoteIota item
                        && item.getItemIndex().equals(new MediafiedItemManager.Index(storage, 9)),
                "legacy mote index changed");

        CompoundTag current = migrated.serialiseToNBT();
        require(current.contains("entries", Tag.TAG_LIST), "current Everbook container was not written");
        CompoundTag currentEntry = current.getList("entries", Tag.TAG_COMPOUND).getCompound(0);
        CompoundTag currentIota = currentEntry.getCompound("iota");
        require(currentIota.contains("type", Tag.TAG_STRING), "current iota discriminator is absent");
        require(!currentIota.contains(LegacyIotaNbtMigrator.LEGACY_TYPE), "legacy iota wrapper survived roundtrip");

        Everbook roundTripped = Everbook.fromNbt(current, owner);
        HexPattern roundTripKey = requireNonNull(roundTripped.getKey(0), "current entry was not reloaded");
        require(roundTripped.getIota(roundTripKey) instanceof ListIota,
                "current codec could not decode the migrated list");
    }

    private static CompoundTag legacyPattern(HexDir start, HexAngle... angles) {
        CompoundTag pattern = new CompoundTag();
        pattern.putByte("start_dir", (byte) start.ordinal());
        byte[] encoded = new byte[angles.length];
        for (int i = 0; i < angles.length; i++) {
            encoded[i] = (byte) angles[i].ordinal();
        }
        pattern.putByteArray("angles", encoded);
        return pattern;
    }

    private static CompoundTag legacy(String type, Tag data) {
        CompoundTag serialized = new CompoundTag();
        serialized.putString(LegacyIotaNbtMigrator.LEGACY_TYPE, type);
        serialized.put(LegacyIotaNbtMigrator.LEGACY_DATA, data);
        return serialized;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static <T> T requireNonNull(T value, String message) {
        require(value != null, message);
        return value;
    }
}
