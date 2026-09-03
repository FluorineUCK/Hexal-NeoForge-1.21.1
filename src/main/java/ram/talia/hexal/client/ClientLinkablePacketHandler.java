package ram.talia.hexal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;
import ram.talia.hexal.common.network.MsgAddRenderLinkS2C;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkS2C;
import ram.talia.hexal.common.network.MsgSetRenderLinksS2C;

import java.util.LinkedHashMap;
import java.util.Map;

/** Applies link-render sync only after the referenced entity/block entity exists client-side. */
public final class ClientLinkablePacketHandler {
    private ClientLinkablePacketHandler() {
    }

    public static void add(MsgAddRenderLinkS2C packet) {
        Minecraft.getInstance().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            try {
                var source = LinkableRegistry.fromSync(packet.source(), level);
                var sink = LinkableRegistry.fromSync(packet.sink(), level);
                if (source != null && sink != null && source.getClientLinkableHolder() != null) {
                    source.getClientLinkableHolder().addRenderLink(packet.sink(), sink);
                }
            } catch (RuntimeException exception) {
                Hexal.LOGGER.debug("Ignoring unresolved add-render-link packet", exception);
            }
        });
    }

    public static void remove(MsgRemoveRenderLinkS2C packet) {
        Minecraft.getInstance().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            try {
                var source = LinkableRegistry.fromSync(packet.source(), level);
                if (source != null && source.getClientLinkableHolder() != null) {
                    source.getClientLinkableHolder().removeRenderLink(packet.sink());
                }
            } catch (RuntimeException exception) {
                Hexal.LOGGER.debug("Ignoring unresolved remove-render-link packet", exception);
            }
        });
    }

    public static void set(MsgSetRenderLinksS2C packet) {
        Minecraft.getInstance().execute(() -> applySet(packet));
    }

    public static boolean applySet(MsgSetRenderLinksS2C packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;
        try {
            var source = LinkableRegistry.fromSync(packet.source(), level);
            if (source == null || source.getClientLinkableHolder() == null) {
                LinkablePacketHolder.schedule(packet);
                return false;
            }

            Map<CompoundTag, ILinkable.IRenderCentre> sinks = new LinkedHashMap<>();
            for (var raw : packet.sinks()) {
                var tag = (CompoundTag) raw;
                var sink = LinkableRegistry.fromSync(tag, level);
                if (sink == null) {
                    LinkablePacketHolder.schedule(packet);
                    return false;
                }
                sinks.put(tag, sink);
            }
            source.getClientLinkableHolder().setRenderLinks(sinks);
            return true;
        } catch (RuntimeException exception) {
            LinkablePacketHolder.schedule(packet);
            return false;
        }
    }
}
