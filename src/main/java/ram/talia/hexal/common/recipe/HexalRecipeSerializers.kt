package ram.talia.hexal.common.recipe

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import ram.talia.hexal.Hexal
import java.util.function.BiConsumer

class HexalRecipeSerializers {
	companion object {
		@JvmStatic
		fun registerSerializers(r: BiConsumer<RecipeSerializer<*>, ResourceLocation>) {
			for ((key, value) in SERIALIZERS) {
				r.accept(value, key)
			}
		}

		private val SERIALIZERS: MutableMap<ResourceLocation, RecipeSerializer<*>> = LinkedHashMap()

		val FREEZE: RecipeSerializer<FreezeRecipe> = register("freeze", FreezeRecipe.Serializer())

		private fun <T : Recipe<*>?> register(name: String, rs: RecipeSerializer<T>): RecipeSerializer<T> {
			val old = SERIALIZERS.put(Hexal.modLoc(name), rs)
			require(old == null) { "Typo? Duplicate id $name" }
			return rs
		}
	}
}
