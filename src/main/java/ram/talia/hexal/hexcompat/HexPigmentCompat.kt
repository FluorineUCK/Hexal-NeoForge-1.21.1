package ram.talia.hexal.hexcompat

import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.ItemLike
import java.util.Random

/** Isolates the pre34 -> pre39 supplier change in Hex Casting's pigment maps. */
object HexPigmentCompat {
	fun dyePigmentItem(dye: DyeColor): ItemLike =
		requireNotNull(HexItems.DYE_PIGMENTS[dye]) {
			"Hex Casting did not register a dye pigment for $dye"
		}.get()

	fun dyePigmentItems(): List<ItemLike> = HexItems.DYE_PIGMENTS.values.map { it.get() }

	fun randomDyePigmentItem(random: Random): ItemLike {
		val pigments = dyePigmentItems()
		require(pigments.isNotEmpty()) { "Hex Casting registered no dye pigments" }
		return pigments[random.nextInt(pigments.size)]
	}
}
