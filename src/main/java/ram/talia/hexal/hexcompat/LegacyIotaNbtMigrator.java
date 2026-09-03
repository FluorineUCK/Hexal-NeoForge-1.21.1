package ram.talia.hexal.hexcompat;

import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.GarbageIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.casting.iota.GateIota;
import ram.talia.hexal.api.casting.iota.MoteIota;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Converts the serialized-iota format used by Hex Casting 0.11.x to the codec format used
 * by Hex Casting 0.12.x.  This is intentionally limited to data read from old Everbooks;
 * all newly written data continues to use Hex Casting's public {@link IotaType#TYPED_CODEC}.
 */
public final class LegacyIotaNbtMigrator {
    public static final String LEGACY_TYPE = "hexcasting:type";
    public static final String LEGACY_DATA = "hexcasting:data";
    private static final int MAX_DEPTH = 256;
    private static final int MAX_IOTAS = 1024;

    private LegacyIotaNbtMigrator() {
    }

    /** Returns a defensive copy in current format, replacing irrecoverable values with garbage. */
    public static CompoundTag migrate(CompoundTag serialized) {
        if (!isLegacy(serialized)) {
            return serialized.copy();
        }

        Counter counter = new Counter();
        Iota migrated;
        try {
            migrated = decodeLegacy(serialized, 0, counter);
        } catch (RuntimeException exception) {
            Hexal.LOGGER.warn("Failed to migrate a legacy Everbook iota", exception);
            migrated = new GarbageIota();
        }

        Tag encoded = Hexal.serializeIota(migrated);
        return encoded instanceof CompoundTag compound ? compound : serializeGarbage();
    }

    public static boolean isLegacy(CompoundTag serialized) {
        return serialized.contains(LEGACY_TYPE, Tag.TAG_STRING)
                && serialized.contains(LEGACY_DATA);
    }

    /** Decodes the byte-ordinal pattern representation written by Hex Casting 0.11.x. */
    public static HexPattern decodeLegacyPattern(CompoundTag patternTag) {
        if (!patternTag.contains("start_dir", Tag.TAG_ANY_NUMERIC)
                || !patternTag.contains("angles", Tag.TAG_BYTE_ARRAY)) {
            throw new IllegalArgumentException("Not a Hex Casting 0.11.x pattern");
        }

        int directionOrdinal = patternTag.getByte("start_dir");
        HexDir[] directions = HexDir.values();
        if (directionOrdinal < 0 || directionOrdinal >= directions.length) {
            throw new IllegalArgumentException("Invalid legacy start direction " + directionOrdinal);
        }

        HexAngle[] angles = HexAngle.values();
        List<HexAngle> decodedAngles = new ArrayList<>();
        for (byte encoded : patternTag.getByteArray("angles")) {
            int ordinal = Byte.toUnsignedInt(encoded);
            if (ordinal >= angles.length) {
                throw new IllegalArgumentException("Invalid legacy angle " + ordinal);
            }
            decodedAngles.add(angles[ordinal]);
        }
        return new HexPattern(directions[directionOrdinal], decodedAngles);
    }

    private static Iota decodeLegacy(CompoundTag serialized, int depth, Counter counter) {
        if (depth >= MAX_DEPTH || ++counter.iotas >= MAX_IOTAS) {
            return new GarbageIota();
        }

        String type = serialized.getString(LEGACY_TYPE);
        Tag data = serialized.get(LEGACY_DATA);
        if (data == null) {
            return new GarbageIota();
        }

        return switch (type) {
            case "hexcasting:null" -> new NullIota();
            case "hexcasting:double" -> data instanceof DoubleTag value
                    ? new DoubleIota(value.getAsDouble()) : new GarbageIota();
            case "hexcasting:boolean" -> data instanceof ByteTag value
                    ? new BooleanIota(value.getAsByte() != 0) : new GarbageIota();
            case "hexcasting:entity" -> decodeEntity(data);
            case "hexcasting:list" -> decodeList(data, depth, counter);
            case "hexcasting:pattern" -> data instanceof CompoundTag pattern
                    ? new PatternIota(decodeLegacyPattern(pattern)) : new GarbageIota();
            case "hexcasting:garbage" -> new GarbageIota();
            case "hexcasting:vec3" -> decodeVec3(data);
            // Continuation internals changed completely and were never legal inter-world data.
            case "hexcasting:continuation" -> new GarbageIota();
            case "hexal:gate" -> decodeGate(data);
            case "hexal:item" -> decodeMote(data);
            default -> decodeBestEffort(type, data);
        };
    }

    private static Iota decodeEntity(Tag data) {
        if (!(data instanceof CompoundTag entity) || !entity.hasUUID("uuid")) {
            return new GarbageIota();
        }
        // The old display name is JSON rendered with now-removed Inline APIs.  UUID is the
        // semantic identity; allowing the current client to derive/fallback the label is safer.
        // Legacy payloads did not record player-ness. Match Hex Casting's own
        // backward-compatible codec default and protect unresolved UUIDs as players
        // until validation can resolve and correct the flag.
        return new EntityIota(entity.getUUID("uuid"), null, true);
    }

    private static Iota decodeList(Tag data, int depth, Counter counter) {
        if (!(data instanceof ListTag list)) {
            return new GarbageIota();
        }
        List<Iota> values = new ArrayList<>(list.size());
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag child) || !isLegacy(child)) {
                values.add(new GarbageIota());
            } else {
                values.add(decodeLegacy(child, depth + 1, counter));
            }
        }
        return new ListIota(values);
    }

    private static Iota decodeVec3(Tag data) {
        if (data instanceof CompoundTag vector
                && vector.contains("x", Tag.TAG_ANY_NUMERIC)
                && vector.contains("y", Tag.TAG_ANY_NUMERIC)
                && vector.contains("z", Tag.TAG_ANY_NUMERIC)) {
            return new Vec3Iota(new Vec3(vector.getDouble("x"), vector.getDouble("y"), vector.getDouble("z")));
        }
        if (data instanceof LongArrayTag vector) {
            long[] values = vector.getAsLongArray();
            if (values.length == 3) {
                return new Vec3Iota(new Vec3(
                        Double.longBitsToDouble(values[0]),
                        Double.longBitsToDouble(values[1]),
                        Double.longBitsToDouble(values[2])));
            }
        }
        return new GarbageIota();
    }

    private static Iota decodeGate(Tag data) {
        // Very early Hexal versions stored only a drifting gate's numeric index.
        if (data instanceof net.minecraft.nbt.IntTag index) {
            return GateIota.fromLegacyDrifting(index.getAsInt());
        }
        if (!(data instanceof CompoundTag gate)) {
            return new GarbageIota();
        }

        int index = gate.getInt("index");
        int targetType = gate.getByte("target_type");
        if (targetType == 0) {
            return GateIota.fromLegacyDrifting(index);
        }

        Vec3 offset = new Vec3(
                gate.getDouble("target_x"),
                gate.getDouble("target_y"),
                gate.getDouble("target_z"));
        if (targetType == 1) {
            return GateIota.fromLegacyLocation(index, offset);
        }
        if (targetType == 2 && gate.hasUUID("target_uuid")) {
            return GateIota.fromLegacyEntity(
                    index,
                    gate.getUUID("target_uuid"),
                    gate.getString("target_name"),
                    offset);
        }
        return new GarbageIota();
    }

    private static Iota decodeMote(Tag data) {
        if (!(data instanceof CompoundTag mote) || !mote.hasUUID("storage")
                || !mote.contains("index", Tag.TAG_ANY_NUMERIC)) {
            return new GarbageIota();
        }
        UUID storage = mote.getUUID("storage");
        return new MoteIota(new MediafiedItemManager.Index(storage, mote.getInt("index")));
    }

    /**
     * Third-party iotas can sometimes be migrated without depending on their internals when
     * their old compound fields retained the same names.  Scalar payloads conventionally map
     * to the current codec's {@code value} field.
     */
    private static Iota decodeBestEffort(String type, Tag data) {
        CompoundTag candidate = new CompoundTag();
        candidate.putString("type", type);
        if (data instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                Tag value = compound.get(key);
                if (value != null) {
                    candidate.put(key, value.copy());
                }
            }
        } else {
            candidate.put("value", data.copy());
        }

        Iota decoded = IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, candidate).result().orElse(null);
        return decoded == null || decoded instanceof GarbageIota ? new GarbageIota() : decoded;
    }

    private static CompoundTag serializeGarbage() {
        Tag encoded = Hexal.serializeIota(new GarbageIota());
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    private static final class Counter {
        private int iotas;
    }
}
