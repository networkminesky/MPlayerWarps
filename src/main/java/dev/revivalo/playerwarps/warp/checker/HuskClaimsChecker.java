package dev.revivalo.playerwarps.warp.checker;

import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.hook.register.HuskClaimsHook;
import net.william278.huskclaims.api.HuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.position.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public class HuskClaimsChecker implements Checker {
    private final HuskClaimsAPI huskClaims;
    public HuskClaimsChecker(HuskClaimsHook huskClaimsHook) {
        this.huskClaims = huskClaimsHook.getApi();
    }

    @Override
    public boolean validate(Player player) {
        Location loc = player.getLocation();
        World world = fromBukkit(loc.getWorld());
        Position position = Position.at(loc.getX(), loc.getY(), loc.getZ(), world);
        Optional<Claim> claimOpt = huskClaims.getClaimAt(position);
        if (claimOpt.isEmpty()) return true;

        Claim claim = claimOpt.get();

        if (!claim.getOwner().get().equals(player.getUniqueId())) {
            player.sendMessage(Lang.TRIED_TO_CREATE_WARP_IN_FOREIGN_CLAIM.asColoredString());
            return false;
        }

        return true;
    }

    public static World fromBukkit(org.bukkit.World bukkitWorld) {
        if (bukkitWorld == null) {
            return null;
        }

        return net.william278.huskclaims.position.World.of(
                bukkitWorld.getName(),
                bukkitWorld.getUID(),
                bukkitWorld.getEnvironment().name()
        );
    }
}
