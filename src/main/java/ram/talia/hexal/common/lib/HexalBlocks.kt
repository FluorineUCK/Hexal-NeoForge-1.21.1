package ram.talia.hexal.common.lib

import at.petrak.hexcasting.common.lib.HexCreativeTabs
import at.petrak.hexcasting.common.lib.HexItems
import com.mojang.datafixers.util.Pair
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import ram.talia.hexal.Hexal.modLoc
import ram.talia.hexal.common.blocks.BlockMediafiedStorage
import ram.talia.hexal.common.blocks.BlockSlipway
import ram.talia.hexal.common.blocks.BlockRelay
import java.util.function.BiConsumer
import java.util.function.Consumer


class HexalBlocks {

	companion object {
		@JvmStatic
		fun registerBlocks(r: BiConsumer<Block, ResourceLocation>) {
			for ((key, value) in BLOCKS) {
				r.accept(value, key)
			}
		}

		@JvmStatic
		fun registerBlockItems(r: BiConsumer<Item, ResourceLocation>) {
			for ((key, value) in BLOCK_ITEMS) {
				r.accept(BlockItem(value.first, value.second), key)
			}
			HexalItems.registerItems(r)
		}

		@JvmStatic
		fun registerBlockCreativeTab(r: Consumer<Block>, tab: CreativeModeTab) {
			for ((tabHolder, blocks) in BLOCK_TABS) {
				if (tabHolder.value() == tab) {
					blocks.forEach(r::accept)
				}
			}
		}

        private val BLOCKS: MutableMap<ResourceLocation, Block> = LinkedHashMap()
		private val BLOCK_ITEMS: MutableMap<ResourceLocation, Pair<Block, Item.Properties>> = LinkedHashMap()
		private val BLOCK_TABS: MutableMap<Holder<CreativeModeTab>, MutableList<Block>> = LinkedHashMap()

		@JvmField
		val SLIPWAY = blockNoItem("slipway", BlockSlipway(
			//Material.Builder.notSolidBlocking is for some unimaginable reason package-private, so we're doing this instead
			// setting the slipway as blocksMotion even though it doesn't so that fluids can't replace it.
			BlockBehaviour.Properties.of()
				.pushReaction(PushReaction.BLOCK)
				.noLootTable()
				.strength(-1.0f, 3600000.0f)
				.noCollission()
				.noOcclusion()
		))

		@JvmField
		val MEDIAFIED_STORAGE = blockItem("mediafied_storage", BlockMediafiedStorage(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.AMETHYST).noOcclusion().strength(30.0f)
		), HexCreativeTabs.HEX)

		@JvmField
		val RELAY = blockNoItem("relay", BlockRelay(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.AMETHYST).noOcclusion().strength(3.0f)
		))


		private fun <T : Block> blockNoItem(name: String, block: T): T {
			val old = BLOCKS.put(modLoc(name), block)
			require(old == null) { "Typo? Duplicate id $name" }
			return block
		}

		private fun <T : Block> blockItem(name: String, block: T, tab: Holder<CreativeModeTab>?): T {
			return blockItem(name, block, HexItems.props(), tab)
		}

		private fun <T : Block> blockItem(name: String, block: T, props: Item.Properties, tab: Holder<CreativeModeTab>?): T {
			blockNoItem(name, block)
			val old = BLOCK_ITEMS.put(modLoc(name), Pair(block, props))
			require(old == null) { "Typo? Duplicate id $name" }
			if (tab != null) {
				BLOCK_TABS.computeIfAbsent(tab) { t -> ArrayList() }.add(block)
			}
			return block
		}
	}
}
