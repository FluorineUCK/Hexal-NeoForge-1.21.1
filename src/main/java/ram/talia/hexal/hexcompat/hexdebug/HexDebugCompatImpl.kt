package ram.talia.hexal.hexcompat.hexdebug

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import gay.`object`.hexdebug.core.api.HexDebugCoreAPI
import gay.`object`.hexdebug.core.api.exceptions.DebugException
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.common.entities.TickingWisp

/** Loaded reflectively by [HexDebugCompat] only when the full HexDebug mod is installed. */
class HexDebugCompatImpl : HexDebugCompat {
    override fun startChildDebugSession(
        parentEnv: CastingEnvironment,
        caster: ServerPlayer,
        wisp: TickingWisp,
        ravenmind: Iota?,
    ) {
        if (HexDebugCoreAPI.INSTANCE.getDebugEnv(parentEnv) == null) return

        val debugEnv = WispDebugEnv(caster, wisp.uuid, ravenmind)
        try {
            HexDebugCoreAPI.INSTANCE.createDebugThread(debugEnv, null)
            wisp.debugSessionId = debugEnv.sessionId
        } catch (exception: DebugException) {
            // A lack of free debug threads must not prevent the wisp from spawning normally.
            Hexal.LOGGER.debug("Not starting wisp in debug mode", exception)
        }
    }

    override fun startDebuggingWispCast(
        wisp: TickingWisp,
        env: WispCastEnv,
        iotas: List<Iota>,
        image: CastingImage,
    ): Boolean {
        val debugEnv = getDebugEnv(wisp) ?: return false
        debugEnv.isPaused = true
        try {
            HexDebugCoreAPI.INSTANCE.startDebuggingIotas(debugEnv, env, iotas, image)
        } catch (exception: DebugException) {
            Hexal.LOGGER.warn("Failed to start debugging wisp hex", exception)
        }
        return true
    }

    override fun hasLiveSession(wisp: TickingWisp) = getDebugEnv(wisp) != null

    override fun isPaused(wisp: TickingWisp): Boolean {
        if (wisp.debugSessionId == null) return false
        return getDebugEnv(wisp)?.isPaused != false
    }

    override fun removeSession(wisp: TickingWisp) {
        getDebugEnv(wisp)?.let(HexDebugCoreAPI.INSTANCE::removeDebugThread)
    }

    private fun getDebugEnv(wisp: TickingWisp): WispDebugEnv? {
        val sessionId = wisp.debugSessionId ?: return null
        val caster = wisp.caster as? ServerPlayer ?: return null
        return HexDebugCoreAPI.INSTANCE.getDebugEnv(caster, sessionId) as? WispDebugEnv
    }
}
