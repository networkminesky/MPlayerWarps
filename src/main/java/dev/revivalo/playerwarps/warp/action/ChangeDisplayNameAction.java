package dev.revivalo.playerwarps.warp.action;

import dev.revivalo.playerwarps.configuration.file.Config;
import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.util.PermissionUtil;
import dev.revivalo.playerwarps.util.TextUtil;
import dev.revivalo.playerwarps.warp.Warp;
import org.bukkit.entity.Player;

public class ChangeDisplayNameAction implements WarpAction<String> {

    @Override
    public boolean execute(Player player, Warp warp, String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            player.sendMessage(Lang.INVALID_INPUT.asColoredString());
            return false;
        }

        if (!Config.ALLOW_COLORS_IN_WARP_DISPLAY_NAMES.asBoolean()) {
            displayName = TextUtil.removeColors(TextUtil.colorize(displayName));
        }

        int limit = Config.WARP_NAME_MAX_LENGTH.asInteger();
        if (TextUtil.removeColors(TextUtil.colorize(displayName)).length() > limit) {
            player.sendMessage(Lang.WARP_NAME_IS_ABOVE_LETTERS_LIMIT.asColoredString().replace("%limit%", String.valueOf(limit)));
            return false;
        }

        warp.setDisplayName(displayName);
        player.sendMessage(Lang.DISPLAY_NAME_CHANGED.asColoredString()
                .replace("%warp%", warp.getName())
                .replace("%displayName%", warp.getDisplayName()));

        return true;
    }

    @Override
    public boolean hasInput() {
        return true;
    }

    @Override
    public PermissionUtil.Permission getPermission() {
        return PermissionUtil.Permission.CHANGE_DISPLAY_NAME;
    }

    @Override
    public Lang getMessage() {
        return Lang.WRITE_NEW_DISPLAY_NAME;
    }

    @Override
    public int getFee() {
        return Config.SET_DISPLAY_NAME_FEE.asInteger();
    }
}
