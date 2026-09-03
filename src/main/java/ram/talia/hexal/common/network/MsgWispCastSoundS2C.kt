package ram.talia.hexal.common.network

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import ram.talia.hexal.Hexal.modLoc
import ram.talia.hexal.common.entities.BaseCastingWisp

class MsgWispCastSoundS2C private constructor(val wispId: Int) : CustomPacketPayload {
	constructor(wisp: BaseCastingWisp) : this(wisp.id)

	override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
		return TYPE;
	}

	companion object {
		@JvmField
		val STREAM_CODEC : StreamCodec<ByteBuf, MsgWispCastSoundS2C> = ByteBufCodecs.INT.map(::MsgWispCastSoundS2C,
			MsgWispCastSoundS2C::wispId);

		@JvmField
		val TYPE: CustomPacketPayload.Type<MsgWispCastSoundS2C> = CustomPacketPayload.Type(modLoc("wcstsnd"));
	}
}
