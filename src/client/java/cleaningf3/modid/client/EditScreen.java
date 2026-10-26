package cleaningf3.modid.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class EditScreen extends Screen {
    private static final String[][] ORDER = {
        {"\u00a77", "System"},
        {"\u00a7e", "Leistung"},
        {"\u00a7b", "Blickziel"},
        {"\u00a7a", "Position & Welt"},
        {"\u00a7d", "Rendering & Chunks"},
        {"\u00a7f", "Sonstiges"}
    };
    private static final Map<Identifier, DebugScreenEntryStatus> SAVED = new HashMap<>();
    private final Map<String, List<Identifier>> groups = new LinkedHashMap<>();

    public EditScreen() {
        super(Component.literal("F3 anpassen"));
    }

    @Override
    protected void init() {
        groups.clear();
        for (Identifier id : DebugScreenEntries.allEntries().keySet()) {
            String color = GroupColors.colorFor(id);
            if (color.equals("\u00a76")) continue;
            groups.computeIfAbsent(color, c -> new ArrayList<>()).add(id);
        }
        int y = 40;
        int right = this.width / 2 + 100;
        for (String[] g : ORDER) {
            List<Identifier> ids = groups.get(g[0]);
            if (ids == null) continue;
            String label = isShown(ids) ? "-" : "+";
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                setGroup(ids, !isShown(ids));
                this.rebuildWidgets();
            }).bounds(right, y, 20, 20).build());
            y += 26;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
            .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int left = this.width / 2 - 120;
        g.text(this.font, "F3 anpassen: Gruppen ein- oder ausblenden", left, 16, 0xFFFFFFFF);
        int y = 40;
        for (String[] gr : ORDER) {
            List<Identifier> ids = groups.get(gr[0]);
            if (ids == null) continue;
            String state = isShown(ids) ? "" : " \u00a78(aus)";
            g.text(this.font, gr[0] + gr[1] + " \u00a77(" + ids.size() + ")" + state, left, y + 6, 0xFFFFFFFF);
            y += 26;
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
