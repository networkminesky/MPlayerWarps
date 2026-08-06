package dev.revivalo.playerwarps.util;

import io.github.g00fy2.versioncompare.Version;
import org.bukkit.Bukkit;

public final class VersionUtil {
    /** Server dialogs were added to Minecraft in 1.21.6, Paper exposes them from 1.21.7. */
    private static final String DIALOG_MINIMUM_VERSION = "1.21.6";

    private static boolean legacyVersion;
    private static boolean hexSupport;
    private static boolean dialogSupport;
    public static boolean latestVersion;

    static {
        final String serverVersionFull = Bukkit.getBukkitVersion();
        String serverVersion = serverVersionFull.split("-", 2)[0];

        Version version = new Version(serverVersion);

        setHexSupport(version.isAtLeast("1.16"));
        setLegacyVersion(version.isLowerThan("1.13"));
        dialogSupport = version.isAtLeast(DIALOG_MINIMUM_VERSION);
    }

    /**
     * Whether the server is new enough for the dialog (modal) screens.
     * Says nothing about the server actually shipping the Paper dialog API.
     */
    public static boolean isDialogSupport() {
        return dialogSupport;
    }

    public static String getDialogMinimumVersion() {
        return DIALOG_MINIMUM_VERSION;
    }

    private static void setHexSupport(boolean hexSupport){
        VersionUtil.hexSupport = hexSupport;
    }

    public static boolean isHexSupport() {
        return hexSupport;
    }

    public static void setLegacyVersion(boolean legacyVersion) {
        VersionUtil.legacyVersion = legacyVersion;
    }

    public static boolean isLegacyVersion() {
        return legacyVersion;
    }

    public static boolean isLatestVersion() {
        return latestVersion;
    }

    public static void setLatestVersion(boolean latestVersion) {
        VersionUtil.latestVersion = latestVersion;
    }
}
