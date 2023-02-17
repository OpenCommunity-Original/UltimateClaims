package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.tasks.VisualizeTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandShow extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandShow(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "show");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Command must be called as a player");
            return ReturnType.FAILURE;
        }

        if (args.length != 0)
            return ReturnType.SYNTAX_ERROR;

        if (VisualizeTask.togglePlayer(player))
            sendPrefixedMessage(sender, "command.show.start");
        else
            sendPrefixedMessage(sender, "command.show.stop");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.show";
    }

    @Override
    public String getSyntax() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Visualize claims around you";
    }
}
