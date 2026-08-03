package dev.revivalo.playerwarps.user;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler implements Listener {
    public UserHandler(PlayerWarpsPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Menus are built off the main thread, so this map is read from more than one thread.
    public final static Map<UUID, User> USERS = new ConcurrentHashMap<>();

    public static User createUser(final Player player) {
        return createUser(player, new HashMap<>());
    }

    public static User createUser(final Player player, final Map<DataSelectorType, Object> data) {
        User createdUser = new User(player, data);
        USERS.put(player.getUniqueId(), createdUser);
        return createdUser;
    }

    public static void removeUser(final Player player) {
        USERS.remove(player.getUniqueId());
    }

    public static User getUser(final UUID uuid){
        if (USERS.containsKey(uuid)) return USERS.get(uuid);
        else {
            return createUser(Bukkit.getPlayer(uuid));
        }
    }

    public static User getUser(final Player player){
        return getUser(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        UserHandler.createUser(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent event) {
        UserHandler.removeUser(event.getPlayer());
    }
}