package dev.revivalo.playerwarps.warp.action;

import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.util.PermissionUtil;
import dev.revivalo.playerwarps.util.PlayerUtil;
import dev.revivalo.playerwarps.warp.Warp;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

public class BlockPlayerAction implements WarpAction<String> {
    @Override
    public boolean execute(Player player, Warp warp, String playerToBlockName) {
        if (playerToBlockName == null || playerToBlockName.trim().isEmpty()) {
            player.sendMessage(Lang.INVALID_INPUT.asColoredString());
            return false;
        }

        final OfflinePlayer playerToBlock = PlayerUtil.getOfflinePlayer(playerToBlockName.trim());
        if (playerToBlock.getName() == null || (!playerToBlock.hasPlayedBefore() && !playerToBlock.isOnline())) {
            player.sendMessage(Lang.UNAVAILABLE_PLAYER.asColoredString());
            return false;
        }

        if (Objects.equals(playerToBlock.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(Lang.CANT_BLOCK_YOURSELF.asColoredString());
            return false;
        }

        if (warp.isBlocked(playerToBlock)) {
            player.sendMessage(Lang.PLAYER_ALREADY_BLOCKED.asColoredString()
                    .replace("%player%", playerToBlock.getName()));
            return false;
        }

        warp.block(playerToBlock);

        player.sendMessage(Lang.PLAYER_BLOCKED.asColoredString()
                .replace("%warp%", warp.getName())
                .replace("%player%", playerToBlock.getName()));

        return true;
    }

    @Override
    public boolean hasInput() {
        return true;
    }

    @Override
    public PermissionUtil.Permission getPermission() {
        return PermissionUtil.Permission.BLOCK_PLAYER;
    }

    @Override
    public Lang getMessage() {
        return Lang.BLOCKED_PLAYER_INPUT;
    }
}
