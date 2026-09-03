package ram.talia.hexal.common.lib

import at.petrak.hexcasting.common.lib.HexCreativeTabs
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import ram.talia.hexal.Hexal
import ram.talia.hexal.common.items.ItemRelay
import java.util.function.BiConsumer

/** Standalone item registry retained for source/binary compatibility with Hexal addons. */
object HexalItems {
    private val ITEMS: MutableMap<ResourceLocation, Item> = LinkedHashMap()
    private val ITEM_TABS: MutableMap<Holder<CreativeModeTab>, MutableList<Item>> = LinkedHashMap()

    @JvmField
    val RELAY: ItemRelay = item("relay", ItemRelay(HexItems.props()), HexCreativeTabs.HEX)

    @JvmStatic
    fun registerItems(registrar: BiConsumer<Item, ResourceLocation>) {
        ITEMS.forEach { (id, item) -> registrar.accept(item, id) }
    }

    @JvmStatic
    fun registerItemCreativeTab(output: CreativeModeTab.Output, tab: CreativeModeTab) {
        ITEM_TABS.forEach { (tabHolder, items) ->
            if (tabHolder.value() == tab) items.forEach(output::accept)
        }
    }

    private fun <T : Item> item(name: String, item: T, tab: Holder<CreativeModeTab>?): T {
        val old = ITEMS.put(Hexal.modLoc(name), item)
        require(old == null) { "Typo? Duplicate id $name" }
        if (tab != null) ITEM_TABS.computeIfAbsent(tab) { arrayListOf() }.add(item)
        return item
    }
}
