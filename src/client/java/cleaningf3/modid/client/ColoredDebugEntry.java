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
    private final int limit;

    public ColoredDebugEntry(Identifier id, DebugScreenEntry inner) {
        this.inner = inner;
        this.marker = Group.of(id).marker();
        this.limit = Group.lineLimit(id);
    }

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        inner.display(new Tagging(displayer, marker, limit), level, clientChunk, serverChunk);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return inner.isAllowed(reducedDebugInfo);
    }

    @Override
    public DebugEntryCategory category() {
        return inner.category();
    }

    private static final class Tagging implements DebugScreenDisplayer {
        private final DebugScreenDisplayer target;
        private final String marker;
        private int remaining;

        Tagging(DebugScreenDisplayer target, String marker, int limit) {
            this.target = target;
            this.marker = marker;
            this.remaining = limit;
        }

        private boolean take() {
            if (remaining <= 0) return false;
            remaining--;
            return true;
        }

        private String tag(String line) {
            return (line == null || line.isEmpty()) ? line : marker + line;
        }

        @Override public void addPriorityLine(String line) { if (take()) target.addPriorityLine(tag(line)); }
        @Override public void addLine(String line) { if (take()) target.addLine(tag(line)); }
        @Override public void addToGroup(Identifier group, Collection<String> lines) {
            List<String> out = new ArrayList<>();
            for (String line : lines) {
                if (take()) out.add(tag(line));
            }
            if (!out.isEmpty()) target.addToGroup(group, out);
        }
        @Override public void addToGroup(Identifier group, String line) { if (take()) target.addToGroup(group, tag(line)); }
    }
}
