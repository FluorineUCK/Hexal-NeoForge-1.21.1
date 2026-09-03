package ram.talia.hexal.api.everbook

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.mishaps.MishapIllegalInterworldIota
import ram.talia.hexal.hexcompat.LegacyIotaNbtMigrator
import java.util.UUID

/**
 * Loader-neutral Everbook data. Client disk I/O and NeoForge networking deliberately live outside this class so
 * dedicated servers never resolve client-only Minecraft classes.
 */
class Everbook(
    val uuid: UUID,
    private val entries: MutableMap<String, Entry> = linkedMapOf(),
    private val macros: MutableSet<String> = linkedSetOf(),
) {
    constructor(uuid: UUID) : this(uuid, linkedMapOf(), linkedSetOf())

    data class Entry(val pattern: HexPattern, val iota: CompoundTag)

    fun getIota(key: HexPattern): Iota {
        val tag = entries[keyOf(key)]?.iota ?: return NullIota()
        return runCatching { Hexal.deserializeIota(tag) }.getOrElse {
            Hexal.LOGGER.warn("Failed to decode Everbook entry {} for {}", key.anglesSignature(), uuid, it)
            NullIota()
        }
    }

    fun getClientIota(key: HexPattern): CompoundTag? = entries[keyOf(key)]?.iota?.copy()

    fun setIota(key: HexPattern, iota: Iota) = setIota(key, Hexal.serializeIota(iota) as CompoundTag)

    fun setIota(key: HexPattern, iota: CompoundTag) {
        entries[keyOf(key)] = Entry(key, iota.copy())
    }

    fun removeIota(key: HexPattern) {
        val storageKey = keyOf(key)
        entries.remove(storageKey)
        macros.remove(storageKey)
    }

    fun getKey(index: Int): HexPattern? = entries.values
        .map(Entry::pattern)
        .sortedBy(HexPattern::anglesSignature)
        .getOrNull(index)

    fun isMacro(key: HexPattern): Boolean = macros.contains(keyOf(key))

    fun toggleMacro(key: HexPattern) {
        val storageKey = keyOf(key)
        if (!macros.add(storageKey)) macros.remove(storageKey)
    }

    /** Returns a fully expanded macro, or null when the key is not a valid acyclic list macro. */
    fun getMacro(key: HexPattern): List<Iota>? = expandMacro(keyOf(key), linkedSetOf())

    private fun expandMacro(storageKey: String, resolving: MutableSet<String>): List<Iota>? {
        if (storageKey !in macros || !resolving.add(storageKey)) return null
        val root = entries[storageKey]?.let { runCatching { Hexal.deserializeIota(it.iota) }.getOrNull() }
        val list = (root as? ListIota)?.list?.toList() ?: return null.also { resolving.remove(storageKey) }
        val expanded = mutableListOf<Iota>()
        for (iota in list) {
            val nestedKey = (iota as? PatternIota)?.pattern?.let(::keyOf)
            if (nestedKey != null && nestedKey in macros) {
                val nested = expandMacro(nestedKey, resolving) ?: return null.also { resolving.remove(storageKey) }
                expanded.addAll(nested)
            } else {
                expanded.add(iota)
            }
        }
        resolving.remove(storageKey)
        return expanded
    }

    fun filterIotasIllegalInterworld(): Everbook {
        entries.replaceAll { _, entry ->
            val filtered = runCatching {
                MishapIllegalInterworldIota.replaceInNestedIota(Hexal.deserializeIota(entry.iota))
            }.getOrElse { NullIota() }
            Entry(entry.pattern, Hexal.serializeIota(filtered) as CompoundTag)
        }
        return this
    }

    fun serialiseToNBT(): CompoundTag {
        val root = CompoundTag()
        root.putUUID(TAG_UUID, uuid)
        val entryList = ListTag()
        entries.values.forEach { entry ->
            val tag = CompoundTag()
            tag.putString(TAG_ANGLES, entry.pattern.anglesSignature())
            tag.putString(TAG_START_DIR, entry.pattern.startDir.name)
            tag.put(TAG_IOTA, entry.iota.copy())
            entryList.add(tag)
        }
        root.put(TAG_ENTRIES, entryList)
        val macroList = ListTag()
        macros.sorted().forEach { macroList.add(StringTag.valueOf(it)) }
        root.put(TAG_MACROS, macroList)
        return root
    }

    companion object {
        private const val TAG_UUID = "uuid"
        private const val TAG_ENTRIES = "entries"
        private const val TAG_MACROS = "macros"
        private const val TAG_ANGLES = "angles"
        private const val TAG_START_DIR = "start_dir"
        private const val TAG_IOTA = "iota"
        private const val TAG_LEGACY_PATTERN = "pattern"

        private fun keyOf(pattern: HexPattern): String = pattern.anglesSignature().ifEmpty { "empty" }

        @JvmStatic
        @JvmOverloads
        fun fromNbt(root: CompoundTag, expectedUuid: UUID? = null): Everbook {
            val uuid = expectedUuid ?: if (root.hasUUID(TAG_UUID)) root.getUUID(TAG_UUID) else UUID(0L, 0L)
            val entries = linkedMapOf<String, Entry>()
            if (root.contains(TAG_ENTRIES, Tag.TAG_LIST.toInt())) {
                for (raw in root.getList(TAG_ENTRIES, Tag.TAG_COMPOUND.toInt())) {
                    val tag = raw as? CompoundTag ?: continue
                    val direction = runCatching { HexDir.valueOf(tag.getString(TAG_START_DIR)) }.getOrNull() ?: continue
                    val pattern = runCatching { HexPattern.fromAngles(tag.getString(TAG_ANGLES), direction) }.getOrNull() ?: continue
                    val iota = LegacyIotaNbtMigrator.migrate(tag.getCompound(TAG_IOTA))
                    if (!iota.isEmpty) entries[keyOf(pattern)] = Entry(pattern, iota.copy())
                }
            } else {
                // Hexal 1.20 stored each entry directly on the root compound. Keep this reader so an
                // existing encrypted everbook.dat survives the 1.21 migration.
                root.allKeys.asSequence()
                    .filterNot { it == TAG_UUID || it == TAG_MACROS }
                    .forEach { storageKey ->
                        val pair = root.getCompound(storageKey)
                        if (!pair.contains(TAG_LEGACY_PATTERN, Tag.TAG_COMPOUND.toInt()) ||
                            !pair.contains(TAG_IOTA, Tag.TAG_COMPOUND.toInt())) return@forEach

                        val pattern = runCatching {
                            LegacyIotaNbtMigrator.decodeLegacyPattern(pair.getCompound(TAG_LEGACY_PATTERN))
                        }.getOrNull() ?: return@forEach
                        val iota = LegacyIotaNbtMigrator.migrate(pair.getCompound(TAG_IOTA))
                        if (!iota.isEmpty) entries[keyOf(pattern)] = Entry(pattern, iota.copy())
                    }
            }
            val macros = linkedSetOf<String>()
            root.getList(TAG_MACROS, Tag.TAG_STRING.toInt()).forEach { macros.add(it.asString) }
            return Everbook(uuid, entries, macros)
        }
    }
}
