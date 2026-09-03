package ram.talia.hexal.common.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

/** A data-only block-state transform consumed by [ram.talia.hexal.common.casting.actions.spells.OpFreeze]. */
data class FreezeRecipe(val blockIn: Block, val result: BlockState) : Recipe<RecipeInput> {
	override fun matches(input: RecipeInput, level: Level) = false

	fun matches(blockIn: BlockState) = blockIn.`is`(this.blockIn)

	override fun assemble(input: RecipeInput, access: HolderLookup.Provider): ItemStack = ItemStack.EMPTY

	override fun canCraftInDimensions(width: Int, height: Int) = false

	override fun getResultItem(access: HolderLookup.Provider): ItemStack = ItemStack.EMPTY

	override fun getSerializer() = HexalRecipeSerializers.FREEZE

	override fun getType() = HexalRecipeTypes.FREEZE_TYPE

	class Serializer : RecipeSerializer<FreezeRecipe> {
		override fun codec(): MapCodec<FreezeRecipe> = CODEC

		override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> = STREAM_CODEC

		companion object {
			val CODEC: MapCodec<FreezeRecipe> = RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("blockIn").forGetter(FreezeRecipe::blockIn),
					BlockState.CODEC.fieldOf("result").forGetter(FreezeRecipe::result),
				).apply(instance, ::FreezeRecipe)
			}

			val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> = StreamCodec.composite(
				ByteBufCodecs.registry(Registries.BLOCK),
				FreezeRecipe::blockIn,
				ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC),
				FreezeRecipe::result,
				::FreezeRecipe,
			)
		}
	}
}
