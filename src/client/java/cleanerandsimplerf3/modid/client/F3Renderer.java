package cleanerandsimplerf3.modid.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class F3Renderer {
    /** Schriftgroesse von F3 und Overlay, solange das Overlay offen ist. */
    public static final float EDIT_SCALE = 0.5f;
    /** Schriftgroesse von F3, solange das Overlay offen ist. */
    public static final float F3_SCALE = 0.5f;
    /** Schriftgroesse von F3, wenn das Overlay geschlossen ist. */
    public static final float NORMAL_SCALE = 0.75f;
    private static final int STEP = 11;

    private F3Renderer() {}

    public static float scale() {
        return Minecraft.getInstance().screen instanceof EditScreen ? F3_SCALE : NORMAL_SCALE;
    }

    public static void render(GuiGraphicsExtractor g, List<String> lines, boolean left) {
        Font font = Minecraft.getInstance().font;
        float s = scale();
        g.pose().pushMatrix();
        try {
            g.pose().scale(s, s);
            int w = Math.round(g.guiWidth() / s);
            for (int i = 0; i < lines.size(); i++) {
                String raw = lines.get(i);
                if (raw == null || raw.isEmpty()) continue;
                Group group = Group.fromLine(raw);
                String text = Group.strip(raw);
                if (text.isEmpty()) continue;
                if (group == null) {
                    if (!Settings.debugChartsShown()) continue;
                    group = Group.DEBUG_CHARTS;
                }
                int tw = font.width(text);
                int x = left ? 4 : w - 4 - tw;
                int y = 3 + i * STEP;
                roundedFill(g, x - 2, y - 1, x + tw + 2, y + 9, group.color());
                g.text(font, text, x, y, 0xFF000000, false);
            }
        } finally {
            g.pose().popMatrix();
        }
    }

    public static void roundedFill(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (x1 - x0 < 5 || y1 - y0 < 5) {
            g.fill(x0, y0, x1, y1, color);
            return;
        }
        g.fill(x0 + 2, y0, x1 - 2, y1, color);
        g.fill(x0 + 1, y0 + 1, x0 + 2, y1 - 1, color);
        g.fill(x1 - 2, y0 + 1, x1 - 1, y1 - 1, color);
        g.fill(x0, y0 + 2, x0 + 1, y1 - 2, color);
        g.fill(x1 - 1, y0 + 2, x1, y1 - 2, color);
    }
}
