package ram.talia.hexal.client;

import net.minecraft.client.Minecraft;
import ram.talia.hexal.client.sounds.WispCastingSoundInstance;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.common.lib.HexalSounds;

import java.util.HashMap;
import java.util.Map;

/** Client-only owner of wisp sound instances; keeps client classes out of common entities. */
public final class ClientWispSoundHandler {
    private static final Map<Integer, WispCastingSoundInstance> SOUNDS = new HashMap<>();

    private ClientWispSoundHandler() {}

    public static void handle(int wispId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        if (!(minecraft.level.getEntity(wispId) instanceof BaseCastingWisp wisp)) return;

        WispCastingSoundInstance sound = SOUNDS.get(wispId);
        if (sound == null || sound.isStopped()) {
            sound = new WispCastingSoundInstance(wisp);
            SOUNDS.put(wispId, sound);
            minecraft.getSoundManager().play(sound);
            HexalSounds.WISP_CASTING_START.playAt(
                    wisp.level(), wisp.position(), 0.3f,
                    1f + (wisp.getRandom().nextFloat() - 0.5f) * 0.2f, false);
        }
        sound.keepAlive();
    }
}
