package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord
import net.minecraft.world.phys.Vec3
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.casting.mishaps.MishapIllegalInterworldIota
import ram.talia.hexal.api.casting.mishaps.MishapNeedsCaster
import ram.talia.hexal.eventhandlers.EverbookManager

object OpEverbookWrite : ConstMediaAction {
	override val argc = 2


	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val pos = args.getBlockPos(0, argc)
		val key = args.getPattern(1, argc)

		env.assertVecInRange(Vec3.atCenterOf(pos))

		val record = env.world.getBlockState(pos).block
		if (record !is BlockAkashicRecord) {
			throw MishapNoAkashicRecord(pos)
		}

		val iota = record.lookupPattern(pos, key, env.world) ?: NullIota()

		val player = env.castingEntity as? ServerPlayer ?: throw MishapNeedsCaster()
		MishapOthersName.getTrueNameMishapFromDatum(env.world, iota, player)?.let { throw it }
		val illegalInterworldIota = MishapIllegalInterworldIota.getFromNestedIota(iota)
		if (illegalInterworldIota != null)
			throw MishapIllegalInterworldIota(illegalInterworldIota)

		EverbookManager.setIota(player, key, iota)

		return listOf()
	}
}
