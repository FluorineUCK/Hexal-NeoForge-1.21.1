package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpSendIota : SpellAction {
	override val argc = 2

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val linkThis = LinkableRegistry.linkableFromCastingEnvironment(env)

		val linkedIndex = args.getPositiveIntUnder(0, linkThis.numLinked(), argc)
		val iota = args[1]

		val other = linkThis.getLinked(linkedIndex)
			?: throw MishapInvalidIota.of(args[0], 1, "linkable.index")

		return SpellAction.Result(
			Spell(other, iota),
			Hexal.toMediaFromDust(HexalConfig.Server.SEND_IOTA_COST.get()),
			listOf()
		)
	}

	private data class Spell(val other: ILinkable, val iota: Iota) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			Hexal.LOGGER.debug("sending {} to {}", iota, other)
			other.receiveIota(LinkableRegistry.linkableFromCastingEnvironment(env), iota)
		}
	}
}
