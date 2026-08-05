package dev.revivalo.playerwarps.hook.register;

import dev.revivalo.playerwarps.configuration.file.Config;
import dev.revivalo.playerwarps.hook.Hook;
import dev.revivalo.playerwarps.hook.papiresolver.PAPIRegister;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderApiHook implements Hook<PlaceholderApiHook> {
    private boolean isHooked = false;

    @Override
    public @NotNull String getName() {
        return "PlaceholderAPI";
    }

    @Override
    public void register() {
        if (isPluginEnabled()) {
            new PAPIRegister().register();
            isHooked = true;
        }
    }

    /**
     * Kept here rather than in a utility class so that the PlaceholderAPI classes are only
     * touched once the hook is actually active.
     */
    public String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.containsPlaceholders(text)
                ? PlaceholderAPI.setPlaceholders(player, text)
                : text;
    }

    public List<String> setPlaceholders(Player player, List<String> lines) {
        final List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(setPlaceholders(player, line));
        }
        return result;
    }

    @Override
    public boolean isOn() {
        return isHooked;
    }

    @Override
    public Config getConfigPath() {
        return Config.PLACEHOLDER_API_HOOK_ENABLED;
    }

    @Nullable
    @Override
    public PlaceholderApiHook getApi() {
        return isHooked ? this : null;
    }
}