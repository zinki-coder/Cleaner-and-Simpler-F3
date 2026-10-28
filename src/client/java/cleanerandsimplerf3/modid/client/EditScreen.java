package cleanerandsimplerf3.modid.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private static final float S = F3Renderer.EDIT_SCALE;
    private static final int BTN = 10;
    private static final int BTN_SC = Math.round(BTN / S);
    private static final int BOX_H = 12;
    private static final int TITLE_TOP = 6;
    private static final int ROWS_TOP = 34;
    private static final int ROW_STEP = 26;
    private static final int GAP = 4;
    private static final Map<Identifier, DebugScreenEntryStatus> SAVED = new HashMap<>();

    private record Row(Group group, List<Identifier> ids, int count, int x, int y) {}

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();

    public EditScreen() {
        this(null);
    }

    public EditScreen(Screen parent) {
        super(Component.translatable("csf3.title.a"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_F3 || CleanerAndSimplerF3Client.openEditor.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private String label(Row r) {
        return r.group().label() + " (" + r.count() + ")";
    }

    private int labelBoxW(Row r) {
        return this.font.width(label(r)) + 6;
    }

    private int statusW() {
        return 3 + this.font.width(offText()) + 4 + this.font.width(onText()) + 3;
    }

    private int totalWidth(Row r) {
        return labelBoxW(r) + GAP + statusW() + GAP + BTN_SC;
    }

    @Override
    protected void init() {
        rows.clear();
        int sw = Math.round(this.width / S);
        int y = ROWS_TOP;
        for (Group gr : Group.ordered()) {
            List<Identifier> ids = new ArrayList<>();
            int count;
            if (gr == Group.DEBUG_CHARTS) {
                count = 3;
            } else {
                for (Identifier id : DebugScreenEntries.allEntries().keySet()) {
                    if (Group.of(id) == gr && Group.isKept(id)) ids.add(id);
                }
                if (ids.isEmpty()) continue;
                count = ids.size();
            }
            Row probe = new Row(gr, ids, count, 0, y);
            int x = (sw - totalWidth(probe)) / 2;
            Row row = new Row(gr, ids, count, x, y);
            rows.add(row);
            boolean shown = isShown(row);
            int bx = Math.round((x + labelBoxW(row) + GAP + statusW() + GAP) * S);
            int by = Math.round((y + BOX_H / 2f) * S - BTN / 2f);
            this.addRenderableWidget(Button.builder(Component.literal(shown ? "-" : "+"), b -> {
                toggle(row);
                this.rebuildWidgets();
            }).bounds(bx, by, BTN, BTN).build());
            y += ROW_STEP;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
            .bounds(this.width / 2 - 40, this.height - 50, 80, 16).build());
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

            String a = Component.translatable("csf3.title.a").getString();
            String b = Component.translatable("csf3.title.b").getString();
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
                boolean shown = isShown(r);
                int lw = labelBoxW(r);
                F3Renderer.roundedFill(g, r.x(), r.y(), r.x() + lw, r.y() + BOX_H, r.group().color());
                g.text(f, label(r), r.x() + 3, r.y() + 2, 0xFF000000, false);

                int sx = r.x() + lw + GAP;
                int aw = f.width(offText());
                F3Renderer.roundedFill(g, sx, r.y(), sx + statusW(), r.y() + BOX_H, 0xB0000000);
                g.text(f, offText(), sx + 3, r.y() + 2, shown ? 0xFF8A8A8A : 0xFFFF5555, false);
                g.text(f, onText(), sx + 3 + aw + 4, r.y() + 2, shown ? 0xFF7CFC7C : 0xFF8A8A8A, false);
            }
        } finally {
            g.pose().popMatrix();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean isShown(Row r) {
        if (r.group() == Group.DEBUG_CHARTS) return Settings.debugChartsShown();
        DebugScreenEntryList list = Minecraft.getInstance().debugEntries;
        for (Identifier id : r.ids()) {
            if (list.getStatus(id) != DebugScreenEntryStatus.NEVER) return true;
        }
        return false;
    }

    private static void toggle(Row r) {
        if (r.group() == Group.DEBUG_CHARTS) {
            Settings.setDebugChartsShown(!Settings.debugChartsShown());
            return;
        }
        boolean show = !isShown(r);
        DebugScreenEntryList list = Minecraft.getInstance().debugEntries;
        for (Identifier id : r.ids()) {
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

    private static String offText() {
        return word("csf3.status.off", "options.off", "off");
    }

    private static String onText() {
        return word("csf3.status.on", "options.on", "on");
    }

    private static String word(String own, String vanilla, String fallback) {
        String mine = Component.translatableWithFallback(own, "csf3-missing").getString();
        String w = "csf3-missing".equals(mine)
            ? Component.translatableWithFallback(vanilla, fallback).getString().toLowerCase(Locale.ROOT)
            : mine;
        return "(" + w + ")";
    }
}
