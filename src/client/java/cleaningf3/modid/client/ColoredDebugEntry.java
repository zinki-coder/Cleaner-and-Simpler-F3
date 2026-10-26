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
    private final String marker;

    public ColoredDebugEntry(Identifier id, DebugScreenEntry inner) {
        this.inner = inner;
        this.marker = Group.of(id).marker();
    }

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        inner.display(new Tagging(displayer, marker), level, clientChunk, serverChunk);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return inner.isAllowed(reducedDebugInfo);
    }

    @Override
    public DebugEntryCategory category() {
        return inner.category();
    }

    private record Tagging(DebugScreenDisplayer target, String marker) implements DebugScreenDisplayer {
        private String tag(String line) {
            return (line == null || line.isEmpty()) ? line : marker + line;
        }

        @Override public void addPriorityLine(String line) { target.addPriorityLine(tag(line)); }
        @Override public void addLine(String line) { target.addLine(tag(line)); }
        @Override public void addToGroup(Identifier group, Collection<String> lines) {
            List<String> tagged = new ArrayList<>();
            for (String line : lines) tagged.add(tag(line));
            target.addToGroup(group, tagged);
        }
        @Override public void addToGroup(Identifier group, String line) { target.addToGroup(group, tag(line)); }
    }
}
