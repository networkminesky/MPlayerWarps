package dev.revivalo.playerwarps.commandmanager.subcommand;

import dev.revivalo.playerwarps.commandmanager.SubCommand;
import dev.revivalo.playerwarps.hook.HookRegister;
import dev.revivalo.playerwarps.util.PermissionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ImportCommand implements SubCommand {
    @Override
    public String getName() {
        return "importar";
    }

    @Override
    public String getDescription() {
        return "Importa os warps";
    }

    @Override
    public String getSyntax() {
        return "/go importar";
    }

    @Override
    public PermissionUtil.Permission getPermission() {
        return PermissionUtil.Permission.ADMIN;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("import");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[WARPS] Apenas jogadores podem executar este comando!");
            return;
        }

        //HookRegister.getEssentialsHook().importWarps();
    }
}