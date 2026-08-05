package dev.revivalo.playerwarps.hook.papiresolver;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import dev.revivalo.playerwarps.warp.Warp;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Resolves {@code %playerwarps_visits_<warp>%} and {@code %playerwarps_unique_visits_<warp>%}.
 * Without a warp name the totals across all warps are returned.
 */
public class WarpVisitsResolver implements PlaceholderResolver {
    private static final String UNIQUE_PREFIX = "unique_visits";
    private static final String TOTAL_PREFIX = "visits";

    @Override
    public boolean canResolve(String rawPlaceholder) {
        return rawPlaceholder.startsWith(UNIQUE_PREFIX) || rawPlaceholder.startsWith(TOTAL_PREFIX);
    }

    @Override
    public String resolve(Player p, String rawPlaceholder) {
        final boolean unique = rawPlaceholder.startsWith(UNIQUE_PREFIX);
        final String prefix = unique ? UNIQUE_PREFIX : TOTAL_PREFIX;
        final String warpName = rawPlaceholder.length() > prefix.length()
                ? rawPlaceholder.substring(prefix.length() + 1)
                : "";

        if (warpName.isEmpty()) {
            return String.valueOf(PlayerWarpsPlugin.getWarpHandler().getWarps().stream()
                    .mapToInt(warp -> unique ? warp.getUniqueVisits() : warp.getVisits())
                    .sum());
        }

        final Optional<Warp> warp = PlayerWarpsPlugin.getWarpHandler().getWarpFromName(warpName);
        if (warp.isEmpty()) {
            return "0";
        }

        return String.valueOf(unique ? warp.get().getUniqueVisits() : warp.get().getVisits());
    }
}
