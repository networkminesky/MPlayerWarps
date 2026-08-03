package dev.revivalo.playerwarps.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class NumberUtil {
    /**
     * {@link DecimalFormat} is not thread-safe and menus are built off the main thread,
     * so every thread gets its own instance.
     */
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("###,###,###,###"));

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String formatNumber(long number) {
        return DECIMAL_FORMAT.get().format(number);
    }

    public static String format(long number) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        return numberFormat.format(number);
    }
}
