package dev.revivalo.playerwarps.util;

import dev.revivalo.playerwarps.configuration.file.Config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public final class DateUtil {
    /**
     * {@link SimpleDateFormat} is not thread-safe and menus are built off the main thread, so
     * every thread gets its own instances. Keyed by pattern so a config reload is picked up.
     */
    private static final ThreadLocal<Map<String, DateFormat>> FORMATTERS = ThreadLocal.withInitial(HashMap::new);

    public static DateFormat getFormatter() {
        return FORMATTERS.get().computeIfAbsent(Config.DATE_FORMAT.asString(), SimpleDateFormat::new);
    }
}
