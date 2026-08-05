package dev.revivalo.playerwarps.input;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.exception.SignGUIVersionException;
import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import dev.revivalo.playerwarps.configuration.file.Config;
import dev.revivalo.playerwarps.configuration.file.Lang;
import dev.revivalo.playerwarps.menu.page.ManageMenu;
import dev.revivalo.playerwarps.warp.Warp;
import dev.revivalo.playerwarps.warp.action.Inputable;
import dev.revivalo.playerwarps.warp.action.WarpAction;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Single entry point for asking a player to type a value in. The mode is picked by the
 * {@code input-mode} config option.
 */
public final class PlayerInput {
    private static final long CHAT_TIMEOUT_TICKS = 15 * 20;

    private PlayerInput() {
        throw new RuntimeException("This class cannot be instantiated");
    }

    public static InputMode getMode() {
        return InputMode.fromString(Config.INPUT_MODE.asString());
    }

    /**
     * Asks for the input an action needs. In chat mode the manage menu is reopened afterwards,
     * matching the behaviour the manage menu relied on before.
     */
    public static CompletableFuture<String> request(Player player, @Nullable Warp warp, WarpAction<?> action) {
        final Lang prompt = resolvePrompt(action);

        if (getMode() == InputMode.SIGN) {
            return requestViaSign(player, prompt);
        }

        return requestViaChat(player, warp, prompt, !action.hasFee());
    }

    /**
     * Asks for a plain value that does not belong to a warp action, such as a warp password.
     */
    public static CompletableFuture<String> request(Player player, Lang prompt) {
        return getMode() == InputMode.SIGN
                ? requestViaSign(player, prompt)
                : requestViaChat(player, null, prompt, false);
    }

    @Nullable
    private static Lang resolvePrompt(WarpAction<?> action) {
        if (action.getMessage() != null) {
            return action.getMessage();
        }

        return action instanceof Inputable inputable ? inputable.getInputText() : null;
    }

    private static String promptText(@Nullable Lang prompt, @Nullable Warp warp) {
        if (prompt == null) {
            return Lang.INVALID_INPUT.asColoredString();
        }

        final String text = prompt.asColoredString();
        return warp == null ? text : text.replace("%warp%", warp.getName());
    }

    private static CompletableFuture<String> requestViaSign(Player player, @Nullable Lang prompt) {
        final CompletableFuture<String> future = new CompletableFuture<>();

        final SignGUI gui;
        try {
            gui = SignGUI.builder()
                    .setType(Material.OAK_SIGN)
                    .setColor(DyeColor.BLACK)
                    .setLine(1, promptText(prompt, null))
                    .setHandler((signPlayer, result) -> {
                        final String input = result.getLineWithoutColor(0);

                        // An empty sign means the player cancelled - leave the future unfinished.
                        if (!input.isEmpty()) {
                            // Guarantees the same contract as the chat mode: callers continue on
                            // the main thread and may open menus straight away.
                            PlayerWarpsPlugin.get().runSync(() -> future.complete(input));
                        }

                        return Collections.emptyList();
                    })
                    .build();
        } catch (SignGUIVersionException ex) {
            PlayerWarpsPlugin.get().getLogger().warning(
                    "Sign input is not supported on this server version, using chat input instead.");
            return requestViaChat(player, null, prompt, false);
        }

        gui.open(player);
        return future;
    }

    private static CompletableFuture<String> requestViaChat(Player player, @Nullable Warp warp,
                                                            @Nullable Lang prompt, boolean reopenManageMenu) {
        final CompletableFuture<String> future = new CompletableFuture<>();

        player.closeInventory();
        player.sendMessage(promptText(prompt, warp));

        final BaseComponent[] cancelMessage = TextComponent.fromLegacyText(Lang.CANCEL_INPUT.asColoredString());
        for (BaseComponent component : cancelMessage) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(Lang.CLICK_TO_CANCEL_INPUT.asColoredString())));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pwcancel"));
        }

        player.spigot().sendMessage(cancelMessage);

        final Listener listener = new Listener() {
            @EventHandler
            public void onPlayerChat(final AsyncPlayerChatEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                event.setCancelled(true);
                HandlerList.unregisterAll(this);

                // The chat event is asynchronous, so hop back before handing the value over.
                PlayerWarpsPlugin.get().runSync(() -> future.complete(event.getMessage()));

                if (reopenManageMenu && warp != null) {
                    PlayerWarpsPlugin.get().runSync(() -> new ManageMenu(warp).openFor(player));
                }
            }

            @EventHandler
            public void onCommand(final PlayerCommandPreprocessEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                if (event.getMessage().equalsIgnoreCase("/pwcancel")) {
                    event.setCancelled(true);
                    HandlerList.unregisterAll(this);
                    player.sendMessage(Lang.INPUT_CANCELLED.asColoredString());
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, PlayerWarpsPlugin.get());

        PlayerWarpsPlugin.get().runDelayed(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Player did not respond in time"));
                HandlerList.unregisterAll(listener);
            }
        }, CHAT_TIMEOUT_TICKS);

        return future;
    }
}
