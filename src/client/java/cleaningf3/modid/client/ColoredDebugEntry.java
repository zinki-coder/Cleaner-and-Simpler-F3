package cleaningf3.modid.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ColoredDebugEntry implements DebugScreenEntry {
    private final DebugScreenEntry inner;
    private final String color;

    public ColoredDebugEntry(Identifier id, DebugScreenEntry inner) {
        this.inner = inner;
        this.color = GroupColors.colorFor(id);
    }

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        inner.display(new Coloring(displayer, color), level, clientChunk, serverChunk);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return inner.isAllowed(reducedDebugInfo);
    }

    @Override
    public DebugEntryCategory category() {
        return inner.category();
    }

    private record Coloring(DebugScreenDisplayer target, String color) implements DebugScreenDisplayer {
        @Override public void addPriorityLine(String line) { target.addPriorityLine(color + line); }
        @Override public void addLine(String line) { target.addLine(color + line); }
        @Override public void addToGroup(Identifier group, Collection<String> lines) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) colored.add(color + line);
            target.addToGroup(group, colored);
        }
        @Override public void addToGroup(Identifier group, String line) { target.addToGroup(group, color + line); }
    }
}
