package dev.revivalo.playerwarps.commandmanager.subcommand;

import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import dev.revivalo.playerwarps.commandmanager.SubCommand;
import dev.revivalo.playerwarps.hook.Hook;
import dev.revivalo.playerwarps.hook.HookRegister;
import dev.revivalo.playerwarps.util.PermissionUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AboutSubCommand implements SubCommand {
    @Override
    public @NotNull String getName() {
        return "sobre";
    }

    @Override
    public @NotNull String getDescription() {
        return "Mostra informações sobre o plugin";
    }

    @Override
    public @NotNull String getSyntax() {
        return "/go sobre";
    }

    @Override
    public PermissionUtil.Permission getPermission() {
        return PermissionUtil.Permission.ABOUT;
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, int index, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getAliases() {
        return List.of("about");
    }

    @Override
    public void perform(@NotNull CommandSender sender, String[] args) {
        PlayerWarpsPlugin plugin = PlayerWarpsPlugin.get();
        plugin.runAsync(() -> {
            sender.sendMessage(
                    "Desenvolvedores: " + String.join(", ", plugin.getDescription().getAuthors() )+ "\n" +
                            "Versão: " + plugin.getDescription().getVersion() + "\n" +
                            "Wiki: https://playerwarps.athelion.eu/\n" +
                            "Suporte: https://discord.athelion.eu/\n" +
                            "Plataforma: " + plugin.getServer().getName() + " " + plugin.getServer().getVersion()
            );
            Collection<Hook<?>> hooks = HookRegister.getHooks();
            if (!hooks.isEmpty()) {
                sender.sendMessage("Integrações (Hooks): " + (hooks.stream().noneMatch(Hook::isOn) ? "Nenhuma" : ""));
                hooks.stream().filter(Hook::isOn).forEach(hook -> sender.sendMessage(" " + hook.getName() + " - " + hook.getVersion()));
            }
        });
    }
}