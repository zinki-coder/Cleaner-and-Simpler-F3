package cleaningf3.modid.client.mixin;

import cleaningf3.modid.client.F3Renderer;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "extractLines", at = @At("HEAD"), cancellable = true)
    private void cleaningf3$lines(GuiGraphicsExtractor graphics, List<String> lines, boolean left, CallbackInfo ci) {
        try {
            F3Renderer.render(graphics, lines, left);
            ci.cancel();
        } catch (RuntimeException e) {
            // bei einem Fehler zeichnet Minecraft den F3-Bildschirm wie gewohnt
        }
    }
}
