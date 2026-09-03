package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.validateIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.Hexal

/**
 * A small, loader-independent queue of typed iotas.
 *
 * Hex Casting 1.21 serialises iotas through [at.petrak.hexcasting.api.casting.iota.IotaType.TYPED_CODEC],
 * so the old tag-inspection cache cannot be used. Keeping the canonical form as NBT also preserves entity UUIDs
 * while their entities are unloaded and avoids retaining live entity references in saved linkables.
 */
class SerialisedIotaList {
    private var tags = ListTag()

    constructor()

    constructor(iotas: MutableList<Iota>?) {
        if (iotas != null) set(iotas)
    }

    constructor(tags: ListTag?) {
        if (tags != null) set(tags)
    }

    fun clear() {
        tags = ListTag()
    }

    fun set(iotas: MutableList<Iota>) {
        tags = iotas.toNbtList()
    }

    fun set(newTags: ListTag) {
        tags = newTags.copy()
    }

    fun refreshIotas(level: ServerLevel) {
        tags = getIotas(level)
            .map { validateIota(it, level) }
            .toNbtList()
    }

    fun getIotas(level: ServerLevel): List<Iota> = tags.map { tag ->
        runCatching { Hexal.deserializeIota(tag) }.getOrElse { NullIota() }
    }

    fun getTag(): ListTag = tags.copy()

    fun add(iota: Iota) {
        tags.add(Hexal.serializeIota(iota))
    }

    fun add(tag: CompoundTag) {
        tags.add(tag.copy())
    }

    fun pop(level: ServerLevel): Iota? {
        if (tags.isEmpty()) return null
        val tag = tags.removeAt(0)
        return runCatching { Hexal.deserializeIota(tag) }.getOrElse { NullIota() }
    }

    fun size(): Int = tags.size
}
