package cleanerandsimplerf3.modid.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class Settings {
    private static boolean debugChartsShown = true;

    private Settings() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("cleaner-and-simpler-f3.properties");
    }

    public static void load() {
        try {
            Path f = file();
            if (Files.exists(f)) {
                Properties p = new Properties();
                try (Reader r = Files.newBufferedReader(f)) {
                    p.load(r);
                }
                debugChartsShown = Boolean.parseBoolean(p.getProperty("debugCharts", "true"));
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static boolean debugChartsShown() {
        return debugChartsShown;
    }

    public static void setDebugChartsShown(boolean value) {
        debugChartsShown = value;
        try {
            Properties p = new Properties();
            p.setProperty("debugCharts", Boolean.toString(value));
            try (Writer w = Files.newBufferedWriter(file())) {
                p.store(w, "Cleaner and Simpler F3");
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }
}
