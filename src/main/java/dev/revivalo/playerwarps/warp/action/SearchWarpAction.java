package dev.revivalo.playerwarps.warp.action;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.menu.WarpSearch;
import dev.revivalo.playerwarps.menu.page.WarpsMenu;
import dev.revivalo.playerwarps.util.PermissionUtil;
import dev.revivalo.playerwarps.warp.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SearchWarpAction implements WarpAction<String>, Inputable {
    @Override
    public boolean execute(Player player, Warp warp, String input) {
        final List<Warp> warps = PlayerWarpsPlugin.getWarpHandler().getWarps().stream()
                .filter(Warp::isAccessible).collect(Collectors.toList());

        // Resolved here, on the main thread, so the search itself stays free of Bukkit calls.
        final Player owner = Bukkit.getPlayerExact(input.trim());
        final UUID ownerId = owner != null ? owner.getUniqueId() : null;

        final WarpSearch warpSearch = new WarpSearch(warps);

        // The menu must not be opened before the search finishes, otherwise it renders an empty result.
        warpSearch.searchAsync(input, ownerId).whenComplete((foundWarps, throwable) -> {
            warpSearch.shutdown();

            if (throwable != null) {
                PlayerWarpsPlugin.get().getLogger().log(Level.WARNING, "Warp search failed", throwable);
                return;
            }

            PlayerWarpsPlugin.get().runSync(() ->
                    new WarpsMenu.DefaultWarpsMenu()
                            .open(player, "all", PlayerWarpsPlugin.getWarpHandler().getSortingManager().getDefaultSortType(), foundWarps));
        });

        return true;
    }

    @Override
    public PermissionUtil.Permission getPermission() {
        return PermissionUtil.Permission.USE;
    }

    @Override
    public Lang getInputText() {
        return Lang.ENTER_WARPS_NAME;
    }

    @Override
    public boolean isPublicAction() {
        return true;
    }
}