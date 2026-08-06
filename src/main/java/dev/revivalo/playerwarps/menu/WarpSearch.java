package dev.revivalo.playerwarps.menu;

import dev.revivalo.playerwarps.warp.Warp;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WarpSearch {
    private final List<Warp> warps;
    private final ExecutorService executor;

    public WarpSearch(List<Warp> warps) {
        this.warps = warps;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @param ownerId owner resolved from the query on the main thread, or null. Matching by id
     *                finds the warps even when the owner name was never cached on this server.
     */
    public CompletableFuture<List<Warp>> searchAsync(String query, @Nullable UUID ownerId) {
        return CompletableFuture.supplyAsync(() -> search(query, ownerId), executor);
    }

    private List<Warp> search(String query, @Nullable UUID ownerId) {
        final String lowerQuery = query.trim().toLowerCase(Locale.ENGLISH);

        return warps.stream()
                .filter(warp -> matchesName(warp, lowerQuery)
                        || matchesOwnerName(warp, lowerQuery)
                        || (ownerId != null && Objects.equals(warp.getOwner(), ownerId)))
                .collect(Collectors.toList());
    }

    private boolean matchesName(Warp warp, String lowerQuery) {
        return warp.getName() != null && warp.getName().toLowerCase(Locale.ENGLISH).contains(lowerQuery);
    }

    private boolean matchesOwnerName(Warp warp, String lowerQuery) {
        return warp.hasKnownOwnerName()
                && warp.getOwnerName().toLowerCase(Locale.ENGLISH).contains(lowerQuery);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
