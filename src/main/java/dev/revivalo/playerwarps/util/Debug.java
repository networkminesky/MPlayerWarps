package dev.revivalo.playerwarps.util;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import dev.revivalo.playerwarps.configuration.file.Config;

/**
 * Verbose logging behind the {@code debug} config option.
 */
public final class Debug {

    private Debug() {
        throw new RuntimeException("This class cannot be instantiated");
    }

    public static boolean isEnabled() {
        // A missing key parses to false, so this is safe before/without the option being set.
        return Config.DEBUG.asBoolean();
    }

    public static void log(String message) {
        if (isEnabled()) {
            PlayerWarpsPlugin.get().getLogger().info("[DEBUG] " + message);
        }
    }

    public static void log(String format, Object... args) {
        if (isEnabled()) {
            PlayerWarpsPlugin.get().getLogger().info("[DEBUG] " + String.format(format, args));
        }
    }
}
