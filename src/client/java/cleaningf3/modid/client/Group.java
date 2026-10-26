package cleaningf3.modid.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;

public enum Group {
    SYSTEM("System", "\u00a7c", 0xE0F28B82),
    PERFORMANCE("Leistung", "\u00a7e", 0xE0FFE066),
    LOOKING_AT("Blickziel", "\u00a79", 0xE08AB4F8),
    POSITION("Position & Welt", "\u00a7a", 0xE08FD694),
    RENDERING("Rendering & Chunks", "\u00a7d", 0xE0F58FCB),
    MISC("Sonstiges", "\u00a76", 0xE0FFB347);

    private static final Pattern CODES = Pattern.compile("\u00a7.");
    private static final List<Group> ORDER = new ArrayList<>(
        List.of(PERFORMANCE, SYSTEM, RENDERING, POSITION, MISC, LOOKING_AT));

    private final String label;
    private final String marker;
    private final int color;

    Group(String label, String marker, int color) {
        this.label = label;
        this.marker = marker;
        this.color = color;
    }

    public String label() { return label; }
    public String marker() { return marker; }
    public int color() { return color; }

    public static List<Group> ordered() {
        return new ArrayList<>(ORDER);
    }

    /** Merkt sich die Reihenfolge, in der Gruppen im F3 erscheinen (links oben nach unten, dann rechts). */
    public static void learn(List<Group> seen) {
        if (seen.size() < 2) return;
        List<Integer> slots = new ArrayList<>();
        for (Group g : seen) slots.add(ORDER.indexOf(g));
        Collections.sort(slots);
        for (int k = 0; k < seen.size(); k++) ORDER.set(slots.get(k), seen.get(k));
    }

    public static Group fromLine(String line) {
        if (line == null || line.length() < 2) return null;
        for (Group g : values()) {
            if (line.startsWith(g.marker)) return g;
        }
        return null;
    }

    public static String strip(String line) {
        return CODES.matcher(line).replaceAll("");
    }

    public static Group of(Identifier id) {
        String p = id.getPath();
        if (has(p, "game_version", "system_specs", "gpu")) return SYSTEM;
        if (has(p, "fps", "tps", "memory", "performance")) return PERFORMANCE;
        if (has(p, "looking_at")) return LOOKING_AT;
        if (isVisualization(id)) return MISC;
        if (has(p, "position", "biome", "heightmap", "light", "difficulty", "day_count")) return POSITION;
        if (has(p, "chunk", "render", "particle", "post_effect")) return RENDERING;
        return MISC;
    }

    public static boolean isVisualization(Identifier id) {
        return has(id.getPath(), "visualize", "hitboxes", "borders", "crosshair", "paths", "octree", "visibility");
    }

    private static boolean has(String path, String... parts) {
        for (String part : parts) {
            if (path.contains(part)) return true;
        }
        return false;
    }
}
