package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.common.lib.hex.HexalActions;
import ram.talia.hexal.eventhandlers.EverbookManager;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.ArrayList;
import java.util.List;

/** Expands Everbook macros and implements staff-cast transmission to the selected link target. */
@Mixin(CastingVM.class)
public abstract class MixinCastingVMEverbook {
    private static final HexPattern ESCAPE = HexPattern.fromAngles("qqqaw", HexDir.EAST);

    @Inject(method = "queueExecuteAndWrapIota", at = @At("HEAD"), cancellable = true, remap = false)
    private void hexal$expandMacroAndTransmit(Iota iota, ServerLevel world,
                                               CallbackInfoReturnable<ExecutionClientView> callback) {
        CastingVM vm = (CastingVM) (Object) this;
        if (!(vm.getEnv() instanceof StaffCastEnv) || vm.getEnv().getCaster() == null) return;

        // pre-39's Simulate must not expand a macro into multiple real casts or
        // transmit iotas as an out-of-band side effect. Let the VM probe the
        // original iota through its normal single-iota simulation path.
        if (vm.getImage().getSimulateNext()) return;

        boolean escaped = vm.getImage().getEscapeNext();
        boolean unescapedEscape = !escaped && iota instanceof PatternIota pattern
                && pattern.getPattern().sigsEqual(ESCAPE);

        List<Iota> toExecute = new ArrayList<>();
        if (!escaped && !unescapedEscape && vm.getEnv().isEnlightened() && iota instanceof PatternIota pattern) {
            List<Iota> macro = EverbookManager.getMacro(vm.getEnv().getCaster(), pattern.getPattern());
            if (macro != null) toExecute.addAll(macro);
            else toExecute.add(iota);
        } else {
            toExecute.add(iota);
        }

        ILinkable transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(vm.getEnv().getCaster());
        boolean wasTransmitting = transmittingTo != null;

        if (transmittingTo != null && !unescapedEscape) {
            boolean isCloseTransmit = !escaped && iota instanceof PatternIota
                    && Iota.tolerates(iota, new PatternIota(HexalActions.LINK_COMM_CLOSE_TRANSMIT.prototype()));

            if (!isCloseTransmit) {
                ILinkable sender = IXplatAbstractions.INSTANCE.getLinkstore(vm.getEnv().getCaster());
                for (Iota outgoing : toExecute) transmittingTo.receiveIota(sender, outgoing);
                toExecute.clear();
            }
        }

        // No macro and no transmission: let Hex Casting follow its normal path.
        if (toExecute.size() == 1 && toExecute.getFirst() == iota && !wasTransmitting) return;

        ExecutionClientView result = vm.queueExecuteAndWrapIotas(toExecute, world);
        transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(vm.getEnv().getCaster());
        boolean transmitting = transmittingTo != null;
        boolean edge = transmitting != wasTransmitting;

        callback.setReturnValue(new ExecutionClientView(
                result.isStackClear() && !transmitting,
                transmitting && !unescapedEscape && !edge ? ResolvedPatternType.ESCAPED : result.getResolutionType(),
                transmitting ? transmittingTo.getAsActionResult() : result.getStackDescs(),
                transmitting ? null : result.getRavenmind()));
    }
}
