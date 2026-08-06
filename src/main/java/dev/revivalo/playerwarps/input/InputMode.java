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
    SIGN,
    /**
     * The player types the value into a dialog screen. Needs Minecraft 1.21.6+ and a server
     * providing the Paper dialog API; falls back to {@link #CHAT} otherwise.
     */
    MODAL;

    private static boolean warned;

    public static InputMode fromString(String name) {
        if (name == null) {
            warnOnce("The 'input-mode' option is missing from config.yml (it belongs under the"
                    + " 'config:' section), falling back to CHAT.");
            return CHAT;
        }

        try {
            return valueOf(name.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            warnOnce("Unknown input-mode '" + name + "', expected one of CHAT, SIGN or MODAL."
                    + " Falling back to CHAT.");
            return CHAT;
        }
    }

    /**
     * Reported once - resolving the mode happens on every single input request.
     */
    private static void warnOnce(String message) {
        if (warned) {
            return;
        }

        warned = true;
        PlayerWarpsPlugin.get().getLogger().warning(message);
    }

    public static void resetWarning() {
        warned = false;
    }
}
