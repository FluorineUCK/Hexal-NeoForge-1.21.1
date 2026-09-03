package ram.talia.hexal.hexcompat.hexdebug

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.HexAttributes
import gay.`object`.hexdebug.core.api.HexDebugCoreAPI
import gay.`object`.hexdebug.core.api.debugging.StopReason
import gay.`object`.hexdebug.core.api.debugging.env.DebugEnvironment
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.wisp.WispCastingManager
import ram.talia.hexal.common.entities.TickingWisp
import java.util.UUID

class WispDebugEnv(
    caster: ServerPlayer,
    private val wispUUID: UUID,
    private val ravenmind: Iota?,
) : DebugEnvironment(caster) {
    var isPaused = false

    private val wisp: TickingWisp?
        get() = caster.serverLevel().getEntity(wispUUID) as? TickingWisp

    override fun resume(
        env: CastingEnvironment,
        image: CastingImage,
        resolutionType: ResolvedPatternType,
    ): Boolean {
        val wisp = wisp ?: return false
        WispCastingManager.WispCastResult(wisp, resolutionType.success, image).callback()
        if (!resolutionType.success) return false

        isPaused = false
        return true
    }

    override fun restart(threadId: Int) {
        val wisp = wisp ?: return
        wisp.setStack(listOf(EntityIota(wisp)))
        wisp.setRavenmind(ravenmind)
        wisp.currentMoveMultiplier = 1f
        wisp.clearTargetMovePos()
        isPaused = false
        HexDebugCoreAPI.INSTANCE.createDebugThread(this, threadId)
    }

    override fun terminate() {
        wisp?.discard()
    }

    override fun isCasterInRange(): Boolean {
        val range = caster.getAttributeValue(HexAttributes.AMBIT_RADIUS)
            .takeIf { it > 0.0 }
            ?: PlayerBasedCastEnv.DEFAULT_AMBIT_RADIUS
        return wisp?.let { caster.distanceToSqr(it) <= range * range } ?: false
    }

    override fun getName(): Component = Component.translatable("entity.hexal.wisp.ticking")

    override fun postStep(env: CastingEnvironment, image: CastingImage, reason: StopReason?) {
        if (reason == null || reason == StopReason.TERMINATED) return
        val wisp = wisp ?: return

        wisp.setStack(image.stack)
        val updatedRavenmind = if (image.userData.contains(HexAPI.RAVENMIND_USERDATA)) {
            runCatching {
                Hexal.deserializeIota(image.userData.getCompound(HexAPI.RAVENMIND_USERDATA))
            }.getOrElse { NullIota() }
        } else {
            NullIota()
        }
        wisp.setRavenmind(updatedRavenmind)
    }
}
