package com.songoda.ultimateclaims.commands.admin;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandTransferOwnership extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandTransferOwnership(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "admin transferownership");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        OfflinePlayer newOwner = Bukkit.getPlayer(args[0]);

        if (newOwner == null || !newOwner.isOnline()) {
            sendPrefixedMessage(sender, "command.general.noplayer");
            return ReturnType.FAILURE;
        }

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            sendPrefixedMessage(sender, "command.general.notclaimed");
            return ReturnType.FAILURE;
        }

        if (claim.transferOwnership(newOwner))
            sendPrefixedMessage(sender, "command.transferownership.success", "%claim%", claim.getName());

        else
            sendPrefixedMessage(sender, "command.transferownership.failed", "%claim%", claim.getName());

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.admin.transferownership";
    }

    @Override
    public String getSyntax() {
        return "admin transferownership <player>";
    }

    @Override
    public String getDescription() {
        return "Transfer the claim your are standing in to another player.";
    }
}
