package dev.revivalo.playerwarps.hook.register;

import dev.revivalo.playerwarps.configuration.file.Config;
import dev.revivalo.playerwarps.hook.Hook;
import net.william278.huskclaims.api.HuskClaimsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HuskClaimsHook implements Hook<HuskClaimsAPI> {
    private HuskClaimsAPI huskClaims;
    private boolean isHooked;

    @Override
    public @NotNull String getName() {
        return "HuskClaims";
    }

    @Override
    public void register() {
        isHooked = isPluginEnabled();
        if (isHooked) {
            huskClaims = HuskClaimsAPI.getInstance();
        }
    }

    @Override
    public boolean isOn() {
        return isHooked;
    }

    @Override
    public Config getConfigPath() {
        return Config.GRIEF_PREVENTION_HOOK_ENABLED;
    }

    @Override
    public @Nullable HuskClaimsAPI getApi() {
        return huskClaims;
    }
}
