package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.casting.mishaps.MishapNeedsCaster
import ram.talia.hexal.eventhandlers.EverbookManager

object OpEverbookDelete : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val key = args.getPattern(0, argc)

		val player = env.castingEntity as? ServerPlayer ?: throw MishapNeedsCaster()
		EverbookManager.removeIota(player, key)

		return listOf()
	}
}
