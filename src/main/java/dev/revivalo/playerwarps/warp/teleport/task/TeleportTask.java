package dev.revivalo.playerwarps.warp.teleport.task;

import dev.revivalo.playerwarps.configuration.file.Config;
import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.warp.teleport.Teleport;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class TeleportTask implements Consumer<ScheduledTask> {
    private final Teleport teleport;
    private final Player player;
    private final Location location;

    private final int locationXZ;

    private Teleport.Status status = Teleport.Status.PROCESSING;
    private int cycle = 0;

    private ScheduledTask scheduledTask;

    public TeleportTask(Teleport teleport) {
        this.teleport = teleport;
        this.player = teleport.getPlayer();
        this.location = teleport.getTargetLocation();
        this.locationXZ = this.player.getLocation().getBlockX() + this.player.getLocation().getBlockZ();
    }

    public ScheduledTask start(Plugin plugin) {
        return player.getScheduler().runAtFixedRate(plugin, this, null, 1L, 10L);
    }

    @Override
    public void accept(ScheduledTask task) {
        this.scheduledTask = task;
        this.run();
    }

    public void run() {
        if (teleport.shouldRunTimer()) {
            if (!player.isOnline()) {
                cancel();
                status = Teleport.Status.ERROR;
                return;
            }

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(Lang.TELEPORTATION_PROGRESS.asColoredString()
                            .replace("%time%", String.valueOf(Config.TELEPORT_DELAY.asInteger() - (cycle / 2))))
            );

            if (locationXZ != (player.getLocation().getBlockX() + player.getLocation().getBlockZ())) {
                cancel();
                status = Teleport.Status.ERROR;
                return;
            }

            if (cycle == teleport.getDelay() * 2) {
                cancel();
                player.teleportAsync(location);
                status = Teleport.Status.SUCCESS;
                return;
            }

            ++cycle;
        } else {
            cancel();
            player.teleportAsync(location);
            status = Teleport.Status.SUCCESS;
        }
    }

    public void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }

    public boolean isResulted() {
        return status != Teleport.Status.PROCESSING;
    }

    public Teleport.Status getStatus() {
        return status;
    }
}