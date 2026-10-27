package cleanerandsimplerf3.modid.client;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;

public enum Group {
    PERFORMANCE("Leistung", "\u00a7e", 0xE0FFE066),
    POSITION("Position & Welt", "\u00a7a", 0xE08FD694),
    RENDERING("Rendering & Chunks", "\u00a7d", 0xE0F58FCB),
    MISC("Sonstiges", "\u00a76", 0xE0FFB347),
    SYSTEM("System", "\u00a7c", 0xE0F28B82),
    LOOKING_AT("Blickziel", "\u00a79", 0xE08AB4F8),
    DEBUG_CHARTS("Debug Charts", "\u00a78", 0xE0C8A27A);

    public static final int ALL = Integer.MAX_VALUE;
    private static final Pattern CODES = Pattern.compile("\u00a7.");

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

    /** Feste Reihenfolge im Overlay. */
    public static List<Group> ordered() {
        return List.of(values());
    }

    public static Group fromLine(String line) {
        if (line == null || line.length() < 2) return null;
        for (Group g : values()) {
            if (g != DEBUG_CHARTS && line.startsWith(g.marker)) return g;
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

    /** Wie viele Zeilen eines Eintrags angezeigt werden: 0 = gar keine, ALL = alle. */
    public static int lineLimit(Identifier id) {
        String p = id.getPath();
        return switch (of(id)) {
            case PERFORMANCE -> has(p, "fps") ? ALL : 0;
            case POSITION -> (has(p, "biome", "day_count") || (has(p, "position") && !has(p, "section"))) ? ALL : 0;
            case RENDERING -> has(p, "chunk_generation", "post_effect") ? 0 : ALL;
            case LOOKING_AT -> (has(p, "looking_at_block") && !has(p, "tags")) ? 2 : 0;
            default -> ALL;
        };
    }

    /** Einträge, die im Overlay zur Gruppe zählen und im F3 etwas anzeigen. */
    public static boolean isKept(Identifier id) {
        return lineLimit(id) != 0 && !isVisualization(id);
    }

    private static boolean has(String path, String... parts) {
        for (String part : parts) {
            if (path.contains(part)) return true;
        }
        return false;
    }
}
