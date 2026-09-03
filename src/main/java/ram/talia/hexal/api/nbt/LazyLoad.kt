package ram.talia.hexal.api.nbt

import com.mojang.datafixers.util.Either
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

@Suppress("UNCHECKED_CAST")
abstract class LazyLoad<L, U : Tag> private constructor(private var isLoaded: Boolean) {
	private var loaded: L? = null
	private var unloaded: U? = null

	constructor(default: L) : this(true) {
		loaded = default
	}

	constructor(default: U) : this(false) {
		unloaded = default
	}

	abstract fun load(unloaded: U, level: ServerLevel): L?
	abstract fun unload(loaded: L): U

	fun set(it: L) {
		loaded = it
		unloaded = null
		isLoaded = true
	}

	fun set(it: U) {
		loaded = null
		unloaded = it
		isLoaded = false
	}

	fun toEither(): Either<L, U> = if (isLoaded) Either.left(loaded) else Either.right(unloaded)

	// casts used here to get rid of problems with loaded/unloaded being null. The way I've got set written, one of them is guaranteed to be non-null (*unless U or L is
	// a nullable type*), meaning the conversion to U/L will work fine.
	open fun get(level: ServerLevel): L? {
		if (isLoaded) return loaded
		val resolved = load(unloaded as U, level)
		// Keep the serialized reference when its chunk/entity is not loaded yet so a later tick can retry.
		if (resolved != null) {
			loaded = resolved
			unloaded = null
			isLoaded = true
		}
		return resolved
	}
	open fun getUnloaded(): U = if (isLoaded) unload(loaded as L) else unloaded as U
}
