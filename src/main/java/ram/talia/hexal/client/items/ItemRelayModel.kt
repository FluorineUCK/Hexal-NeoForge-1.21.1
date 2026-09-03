package ram.talia.hexal.client.items

import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.Hexal
import ram.talia.hexal.common.items.ItemRelay
import software.bernie.geckolib.model.GeoModel

class ItemRelayModel : GeoModel<ItemRelay>() {
    override fun getModelResource(relay: ItemRelay): ResourceLocation = Hexal.modLoc("geo/relay.geo.json")
    override fun getTextureResource(relay: ItemRelay): ResourceLocation = Hexal.modLoc("textures/block/relay.png")
    override fun getAnimationResource(relay: ItemRelay): ResourceLocation = Hexal.modLoc("animations/relay.animation.json")
}
