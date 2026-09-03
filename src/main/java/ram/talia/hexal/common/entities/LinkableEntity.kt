package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.casting.iota.EntityIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.linkable.ClientLinkableHolder
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.LinkableTypes
import ram.talia.hexal.api.linkable.ServerLinkableHolder

/** Shared entity implementation for wisps and any future entity-backed linkables. */
abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level), ILinkable,
    ILinkable.IRenderCentre {

    override val asActionResult
        get() = listOf(EntityIota(this))

    override val linkableHolder: ServerLinkableHolder? =
        if (level.isClientSide) null else ServerLinkableHolder(this, level as ServerLevel)

    override val clientLinkableHolder: ClientLinkableHolder? =
        if (level.isClientSide) ClientLinkableHolder(this, level, random) else null

    override fun getLinkableType(): LinkableRegistry.LinkableType<LinkableEntity, *> =
        LinkableTypes.LINKABLE_ENTITY_TYPE

    override fun getPosition(): Vec3 = position()

    override fun shouldRemove(): Boolean = isRemoved && removalReason?.shouldDestroy() == true

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            renderLinks()
        } else {
            checkLinks()
        }
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        val holder = linkableHolder ?: return
        (compound.get(TAG_LINKABLE_HOLDER) as? CompoundTag)?.let(holder::readFromNbt)
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        val holder = linkableHolder ?: return
        compound.put(TAG_LINKABLE_HOLDER, holder.writeToNbt())
    }

    override fun getAddEntityPacket(entity: ServerEntity): Packet<ClientGamePacketListener> {
        linkableHolder?.syncAll()
        return ClientboundAddEntityPacket(this, entity)
    }

    companion object {
        const val TAG_LINKABLE_HOLDER = "hexal:linkable_holder"
    }
}
