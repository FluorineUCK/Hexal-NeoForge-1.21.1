package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.isOfTag
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.api.casting.iota.IMappableIota
import ram.talia.hexal.common.lib.HexalTags

class MishapIllegalInterworldIota(val iota: Iota) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.GREEN)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component =
        error("illegal_interworld_iota", iota.display())

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        env.caster?.let { it.health /= 2f }
        return stack
    }

    companion object {
        private fun iotaTypeIsIllegal(iota: Iota): Boolean {
            val resourceKey = HexIotaTypes.REGISTRY.getKey(iota.type) ?: return false
            return isOfTag(HexIotaTypes.REGISTRY, resourceKey, HexalTags.ILLEGAL_INTERWORLD)
        }

        @JvmStatic
        fun getFromNestedIota(iota: Iota): Iota? {
            val pool = ArrayDeque<Iota>()
            pool.addLast(iota)
            while (pool.isNotEmpty()) {
                val current = pool.removeFirst()
                if (iotaTypeIsIllegal(current)) return current
                current.subIotas()?.let(pool::addAll)
            }
            return null
        }

        @JvmStatic
        fun replaceInNestedIota(iota: Iota): Iota = when {
            iotaTypeIsIllegal(iota) -> GarbageIota()
            iota is IMappableIota -> iota.mapSubIotas(::replaceInNestedIota)
            iota is ListIota -> iota.list.map(::replaceInNestedIota).asActionResult[0]
            else -> iota
        }
    }
}
