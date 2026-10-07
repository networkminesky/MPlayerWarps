package dev.revivalo.playerwarps.warp.checker;

import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.hook.register.HuskClaimsHook;
import net.william278.huskclaims.api.HuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.position.World;
import net.william278.huskclaims.trust.Trustable;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class HuskClaimsChecker implements Checker {
    private final HuskClaimsAPI huskClaims;
    public HuskClaimsChecker(HuskClaimsHook huskClaimsHook) {
        this.huskClaims = huskClaimsHook.getApi();
    }

    @Override
    public boolean validate(Player player) {
        Location loc = player.getLocation();

        World world = huskClaims.getWorld(loc.getWorld().getName());
        Position position = huskClaims.getPosition(loc.getX(), loc.getY(), loc.getZ(), world);
        Claim claim = huskClaims.getExactClaimAt(position).orElse(null);
        if (claim == null) return true;

        Set<UUID> uuids = claim.getTrustedUsers().keySet();

        UUID owner = claim.getOwner().orElse(null);
        if (!uuids.contains(player.getUniqueId()) || (owner != null && !owner.equals(player.getUniqueId()))) {
            player.sendMessage(Lang.TRIED_TO_CREATE_WARP_IN_FOREIGN_CLAIM.asColoredString());
            return false;
        }

        return true;
    }
}
