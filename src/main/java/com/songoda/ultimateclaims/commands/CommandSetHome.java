package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandSetHome extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandSetHome(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "sethome");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            sendPrefixedMessage(sender, "command.general.notclaimed");
            return ReturnType.FAILURE;
        }

        if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())) {
            sendPrefixedMessage(sender, "command.general.notyourclaim");
            return ReturnType.FAILURE;
        }

        claim.setHome(player.getLocation());

        plugin.getDataManager().updateClaim(claim);

        sendPrefixedMessage(sender, "command.sethome.set");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.sethome";
    }

    @Override
    public String getSyntax() {
        return "sethome";
    }

    @Override
    public String getDescription() {
        return "Set the home for your claim.";
    }
}
