package ram.talia.hexal.common.items

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.world.item.BlockItem
import ram.talia.hexal.client.items.ItemRelayRenderer
import ram.talia.hexal.common.lib.HexalBlocks
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.Animation
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.function.Consumer

class ItemRelay(properties: Properties) : BlockItem(HexalBlocks.RELAY, properties), GeoItem {
    private val cache = GeckoLibUtil.createInstanceCache(this)

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(AnimationController(this, "controller", 0, this::predicate))
    }

    private fun <E : GeoAnimatable> predicate(event: AnimationState<E>): PlayState {
        event.controller.setAnimation(
            RawAnimation.begin().then("animation.model.inv", Animation.LoopType.HOLD_ON_LAST_FRAME)
        )
        return PlayState.CONTINUE
    }

    override fun createGeoRenderer(consumer: Consumer<GeoRenderProvider>) {
        consumer.accept(object : GeoRenderProvider {
            private var renderer: ItemRelayRenderer? = null

            override fun getGeoItemRenderer(): BlockEntityWithoutLevelRenderer {
                if (renderer == null) renderer = ItemRelayRenderer()
                return renderer!!
            }
        })
    }
}
