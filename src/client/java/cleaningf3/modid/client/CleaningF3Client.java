package cleaningf3.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class CleaningF3Client implements ClientModInitializer {
    public static KeyMapping openEditor;

    @Override
    public void onInitializeClient() {
        openEditor = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.cleaningf3.edit", GLFW.GLFW_KEY_K, KeyMapping.Category.DEBUG));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditor.consumeClick()) {
                if (client.screen == null && client.debugEntries.isOverlayVisible()) {
                    client.setScreen(new EditScreen());
                }
            }
        });
    }
}
