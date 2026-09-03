package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Style
import ram.talia.hexal.Hexal
import ram.talia.hexal.client.everbook.ClientEverbookStore
import vazkii.patchouli.api.IComponentRenderContext
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.client.book.gui.GuiBook
import java.util.function.UnaryOperator


@Suppress("SameParameterValue", "unused")
class EverbookPatternComponent : AbstractPatternComponent() {
	@Transient
	var indexNum: Int = -1
	@Transient
	var isMacro = false
	@Transient
	var iota: CompoundTag? = null

	override fun build(x: Int, y: Int, pagenum: Int) {
		super.build(x, if (y != -1 && y != 70) { y } else { 50 }, pagenum)
		indexNum = pagenum - 1
	}

	override fun getPatterns(lookup: UnaryOperator<IVariable>): List<HexPattern> {
		val pattern = ClientEverbookStore.getPattern(indexNum) ?: return listOf()

		isMacro = ClientEverbookStore.isMacro(pattern)
		iota = ClientEverbookStore.getIota(pattern)

		return listOf(pattern)
	}

	override fun onDisplayed(context: IComponentRenderContext) {
		val level = Minecraft.getInstance().level ?: return
		onVariablesAvailable(UnaryOperator { it }, level.registryAccess())
	}

	override fun render(graphics: GuiGraphics, ctx: IComponentRenderContext, partialTicks: Float, mouseX: Int, mouseY: Int) {
		val poseStack = graphics.pose()
		poseStack.pushPose()
		poseStack.translate(HEADER_X.toDouble(), HEADER_Y.toDouble(), 0.0)

		val headerComponent = (if (isMacro) "hexal.everbook_pattern_entry.macro_header" else "hexal.everbook_pattern_entry.header")
				.asTranslatedComponent(indexNum)
				.setStyle(Style.EMPTY.withFont(Minecraft.UNIFORM_FONT))

		drawCenteredStringNoShadow(graphics, headerComponent.string, 0, 0, 0)
		poseStack.popPose()

		drawWrappedIota(graphics, iota, DATA_X, DATA_Y, 0)

		super.render(graphics, ctx, partialTicks, mouseX, mouseY)
	}

	override fun showStrokeOrder() = true

	private fun drawCenteredStringNoShadow(graphics: GuiGraphics, s: String, x: Int, y: Int, colour: Int) {
		val font = Minecraft.getInstance().font
		graphics.drawString(font, s, x - font.width(s) / 2, y, colour, false)
	}

	private fun drawWrappedIota(graphics: GuiGraphics, iota: CompoundTag?, x: Int, y: Int, colour: Int) {
		val ms = graphics.pose()

		if (iota == null)
			return

		val font = Minecraft.getInstance().font

		val iotaText = runCatching {
			font.split(Hexal.deserializeIota(iota).display(), GuiBook.PAGE_WIDTH)
		}.getOrElse {
			font.split("hexcasting.spelldata.unknown".asTranslatedComponent(), GuiBook.PAGE_WIDTH)
		}.iterator()

		var currentY = y

		while (iotaText.hasNext() && currentY <= y + 5 * 9) { // don't draw more lines than fit in the book.
			ms.pushPose()
			ms.translate(x.toDouble(), currentY.toDouble(), 0.0)
			val toDraw = if (currentY < y + 5 * 9) { iotaText.next() } else { "...".red.visualOrderText }
			graphics.drawString(font, toDraw, 0, 0, colour, false)
			ms.popPose()
			currentY += 9
		}
	}

	companion object {
		const val HEADER_X = GuiBook.PAGE_WIDTH / 2
		const val HEADER_Y = 0
		const val DATA_X = 0
		const val DATA_Y = 100
	}
}
