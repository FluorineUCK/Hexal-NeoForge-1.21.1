package ram.talia.hexal.hexcompat.hexdebug

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.level.ServerPlayer
import net.neoforged.fml.ModList
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.common.entities.TickingWisp

/**
 * Loader-neutral boundary around HexDebug. The implementation class is loaded by name only when
 * HexDebug is present, so the rest of Hexal remains usable without the optional addon.
 */
interface HexDebugCompat {
    fun startChildDebugSession(
        parentEnv: CastingEnvironment,
        caster: ServerPlayer,
        wisp: TickingWisp,
        ravenmind: Iota?,
    )

    /** Returns true when this cast belongs to a HexDebug session and was handed to it. */
    fun startDebuggingWispCast(
        wisp: TickingWisp,
        env: WispCastEnv,
        iotas: List<Iota>,
        image: CastingImage,
    ): Boolean

    fun hasLiveSession(wisp: TickingWisp): Boolean
    fun isPaused(wisp: TickingWisp): Boolean
    fun removeSession(wisp: TickingWisp)

    companion object {
        val INSTANCE: HexDebugCompat by lazy(::load)

        private fun load(): HexDebugCompat {
            if (!ModList.get().isLoaded("hexdebug")) return NoHexDebugCompat

            return runCatching {
                Class.forName("ram.talia.hexal.hexcompat.hexdebug.HexDebugCompatImpl")
                    .getDeclaredConstructor()
                    .newInstance() as HexDebugCompat
            }.onFailure {
                Hexal.LOGGER.warn("HexDebug is installed, but Hexal's compatibility adapter could not be loaded", it)
            }.getOrDefault(NoHexDebugCompat)
        }
    }
}

private object NoHexDebugCompat : HexDebugCompat {
    override fun startChildDebugSession(
        parentEnv: CastingEnvironment,
        caster: ServerPlayer,
        wisp: TickingWisp,
        ravenmind: Iota?,
    ) = Unit

    override fun startDebuggingWispCast(
        wisp: TickingWisp,
        env: WispCastEnv,
        iotas: List<Iota>,
        image: CastingImage,
    ) = false

    override fun hasLiveSession(wisp: TickingWisp) = false
    override fun isPaused(wisp: TickingWisp) = false
    override fun removeSession(wisp: TickingWisp) = Unit
}
