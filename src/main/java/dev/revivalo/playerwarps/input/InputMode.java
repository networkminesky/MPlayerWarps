package dev.revivalo.playerwarps.input;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;

import java.util.Locale;

/**
 * How the plugin asks a player to type something in.
 */
public enum InputMode {
    /** The player types the value into chat. Works on every server version. */
    CHAT,
    /** The player types the value into a sign GUI. Needs a supported server version. */
    SIGN;

    public static InputMode fromString(String name) {
        if (name == null) {
            return CHAT;
        }

        try {
            return valueOf(name.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            PlayerWarpsPlugin.get().getLogger().warning("Unknown input-mode '" + name + "', falling back to CHAT.");
            return CHAT;
        }
    }
}
