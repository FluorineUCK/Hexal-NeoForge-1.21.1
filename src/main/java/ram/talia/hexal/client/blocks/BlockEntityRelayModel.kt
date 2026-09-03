package ram.talia.hexal.client.blocks

import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.Hexal
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import software.bernie.geckolib.model.GeoModel

class BlockEntityRelayModel : GeoModel<BlockEntityRelay>() {
    override fun getModelResource(relay: BlockEntityRelay): ResourceLocation = Hexal.modLoc("geo/relay.geo.json")
    override fun getTextureResource(relay: BlockEntityRelay): ResourceLocation = Hexal.modLoc("textures/block/relay.png")
    override fun getAnimationResource(relay: BlockEntityRelay): ResourceLocation = Hexal.modLoc("animations/relay.animation.json")
}
