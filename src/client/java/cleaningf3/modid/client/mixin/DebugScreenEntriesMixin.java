package cleaningf3.modid.client.mixin;

import cleaningf3.modid.client.ColoredDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugScreenEntries.class)
public class DebugScreenEntriesMixin {
    @Inject(method = "getEntry", at = @At("RETURN"), cancellable = true)
    private static void cleaningf3$color(Identifier id, CallbackInfoReturnable<DebugScreenEntry> cir) {
        DebugScreenEntry entry = cir.getReturnValue();
        if (entry != null && !(entry instanceof ColoredDebugEntry)) {
            cir.setReturnValue(new ColoredDebugEntry(id, entry));
        }
    }
}
