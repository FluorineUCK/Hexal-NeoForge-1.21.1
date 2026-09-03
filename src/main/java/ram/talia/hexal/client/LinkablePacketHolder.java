package ram.talia.hexal.client;

import ram.talia.hexal.common.network.MsgSetRenderLinksS2C;

import java.util.ArrayList;
import java.util.List;

/** Bounded retry queue for links whose target chunk/entity is not client-loaded yet. */
public final class LinkablePacketHolder {
    private static final int MAX_RETRIES = 30;
    private static final List<Pending> PENDING = new ArrayList<>();
    private static int tick;

    private LinkablePacketHolder() {
    }

    public static void schedule(MsgSetRenderLinksS2C packet) {
        if (PENDING.stream().noneMatch(pending -> pending.packet.equals(packet))) {
            PENDING.add(new Pending(packet, 0));
        }
    }

    public static void maybeRetry() {
        if (++tick % 20 != 0 || PENDING.isEmpty()) return;

        var retrying = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Pending pending : retrying) {
            if (ClientLinkablePacketHandler.applySet(pending.packet)) continue;
            if (pending.retries + 1 < MAX_RETRIES) {
                PENDING.add(new Pending(pending.packet, pending.retries + 1));
            }
        }
    }

    public static void clear() {
        PENDING.clear();
        tick = 0;
    }

    private record Pending(MsgSetRenderLinksS2C packet, int retries) {
    }
}
