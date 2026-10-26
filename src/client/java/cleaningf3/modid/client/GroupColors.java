package cleaningf3.modid.client;

import net.minecraft.resources.Identifier;

public final class GroupColors {
    private GroupColors() {}

    public static String colorFor(Identifier id) {
        String p = id.getPath();
        if (has(p, "game_version", "system_specs", "gpu")) return "\u00a77";
        if (has(p, "fps", "tps", "memory", "performance")) return "\u00a7e";
        if (has(p, "looking_at")) return "\u00a7b";
        if (has(p, "visualize", "hitboxes", "borders", "crosshair", "paths", "octree", "visibility")) return "\u00a76";
        if (has(p, "position", "biome", "heightmap", "light", "difficulty", "day_count")) return "\u00a7a";
        if (has(p, "chunk", "render", "particle", "post_effect")) return "\u00a7d";
        return "\u00a7f";
    }

    private static boolean has(String path, String... parts) {
        for (String part : parts) {
            if (path.contains(part)) return true;
        }
        return false;
    }
}
