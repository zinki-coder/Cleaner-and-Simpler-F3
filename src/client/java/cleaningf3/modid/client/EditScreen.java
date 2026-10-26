package cleaningf3.modid.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class EditScreen extends Screen {
    private static final float S = F3Renderer.SCALE;
    private static final int TITLE_TOP = 6;
    private static final int ROWS_TOP = 32;
    private static final int ROW_STEP = 20;
    private static final int BTN_SCALED = Math.round(12 / S);
    private static final Map<Identifier, DebugScreenEntryStatus> SAVED = new HashMap<>();

    private record Row(Group group, List<Identifier> ids, int x, int y) {}

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();

    public EditScreen() {
        this(null);
    }

    public EditScreen(Screen parent) {
        super(Component.literal("F3 anpassen"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_F3 || CleaningF3Client.openEditor.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private String label(Row r) {
        return r.group().label() + " (" + r.ids().size() + ")";
    }

    private int labelBoxW(Row r) {
        return this.font.width(label(r)) + 6;
    }

    private int statusW() {
        return 3 + this.font.width("(aus)") + 4 + this.font.width("(ein)") + 3;
    }

    private int totalWidth(Row r) {
        return labelBoxW(r) + 4 + statusW() + 3 + BTN_SCALED;
    }

    @Override
    protected void init() {
        rows.clear();
        List<Row> tmp = new ArrayList<>();
        for (Group gr : Group.ordered()) {
            List<Identifier> ids = new ArrayList<>();
            for (Identifier id : DebugScreenEntries.allEntries().keySet()) {
                if (Group.of(id) == gr && !Group.isVisualization(id)) ids.add(id);
            }
            if (!ids.isEmpty()) tmp.add(new Row(gr, ids, 0, 0));
        }
        int sw = Math.round(this.width / S);
        int maxW = 0;
        for (Row r : tmp) maxW = Math.max(maxW, totalWidth(r));
        int x0 = (sw - maxW) / 2;
        int y = ROWS_TOP;
        for (Row r : tmp) {
            Row row = new Row(r.group(), r.ids(), x0, y);
            rows.add(row);
            boolean shown = isShown(row.ids());
            int bx = Math.round((row.x() + labelBoxW(row) + 4 + statusW() + 3) * S);
            int by = Math.round((row.y() - 2) * S);
            this.addRenderableWidget(Button.builder(Component.literal(shown ? "-" : "+"), b -> {
                setGroup(row.ids(), !isShown(row.ids()));
                this.rebuildWidgets();
            }).bounds(bx, by, 12, 12).build());
            y += ROW_STEP;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
            .bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        Font f = this.font;
        g.pose().pushMatrix();
        try {
            g.pose().scale(S, S);
            int sw = Math.round(this.width / S);

            String a = "F3 anpassen:";
            String b = "Gruppen ein- oder ausblenden";
            int wa = f.width(a) + 12;
            int wb = f.width(b) + 12;
            int tw = wa + 1 + wb;
            int tx = (sw - tw) / 2;
            int th = 17;
            F3Renderer.roundedFill(g, tx - 1, TITLE_TOP - 1, tx + tw + 1, TITLE_TOP + th + 1, 0xFFFFFFFF);
            F3Renderer.roundedFill(g, tx, TITLE_TOP, tx + tw, TITLE_TOP + th, 0xF0141414);
            g.fill(tx + wa, TITLE_TOP + 1, tx + wa + 1, TITLE_TOP + th - 1, 0xFFFFFFFF);
            g.text(f, a, tx + 6, TITLE_TOP + 4, 0xFFFFFFFF, false);
            g.text(f, b, tx + wa + 1 + 6, TITLE_TOP + 4, 0xFFFFFFFF, false);

            for (Row r : rows) {
                boolean shown = isShown(r.ids());
                int lw = labelBoxW(r);
                F3Renderer.roundedFill(g, r.x(), r.y(), r.x() + lw, r.y() + 12, r.group().color());
                g.text(f, label(r), r.x() + 3, r.y() + 2, 0xFF000000, false);

                int sx = r.x() + lw + 4;
                int aw = f.width("(aus)");
                F3Renderer.roundedFill(g, sx, r.y(), sx + statusW(), r.y() + 12, 0xB0000000);
                g.text(f, "(aus)", sx + 3, r.y() + 2, shown ? 0xFF8A8A8A : 0xFFFF5555, false);
                g.text(f, "(ein)", sx + 3 + aw + 4, r.y() + 2, shown ? 0xFF7CFC7C : 0xFF8A8A8A, false);
            }
        } finally {
            g.pose().popMatrix();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean isShown(List<Identifier> ids) {
        DebugScreenEntryList list = Minecraft.getInstance().debugEntries;
        for (Identifier id : ids) {
            if (list.getStatus(id) != DebugScreenEntryStatus.NEVER) return true;
        }
        return false;
    }

    private static void setGroup(List<Identifier> ids, boolean show) {
        DebugScreenEntryList list = Minecraft.getInstance().debugEntries;
        for (Identifier id : ids) {
            if (show) {
                DebugScreenEntryStatus old = SAVED.get(id);
                list.setStatus(id, old != null ? old : DebugScreenEntryStatus.IN_OVERLAY);
            } else {
                DebugScreenEntryStatus cur = list.getStatus(id);
                if (cur != DebugScreenEntryStatus.NEVER) SAVED.put(id, cur);
                list.setStatus(id, DebugScreenEntryStatus.NEVER);
            }
        }
        list.save();
        list.rebuildCurrentList();
    }
}
