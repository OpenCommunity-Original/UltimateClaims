package com.songoda.ultimateclaims.commands.admin;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandName extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandName(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "admin name");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            sendPrefixedMessage(sender, "command.general.notclaimed");
            return ReturnType.FAILURE;
        }

        final String name = String.join(" ", args);

        claim.setName(name);

        plugin.getDataManager().updateClaim(claim);

        sendPrefixedMessage(sender, "command.name.set", "%name%", name);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.admin.name";
    }

    @Override
    public String getSyntax() {
        return "admin name <name>";
    }

    @Override
    public String getDescription() {
        return "Set the display name for the claim you are standing in.";
    }
}
