package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandDissolve extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandDissolve(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "dissolve");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (!plugin.getClaimManager().hasClaim(player)) {
            sendPrefixedMessage(sender, "command.general.noclaim");
            return ReturnType.FAILURE;
        }

        Claim claim = plugin.getClaimManager().getClaim(player);

        claim.destroy(ClaimDeleteReason.PLAYER);
        sendPrefixedMessage(player, "general.claim.dissolve", "%claim%", claim.getName());

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.dissolve";
    }

    @Override
    public String getSyntax() {
        return "dissolve";
    }

    @Override
    public String getDescription() {
        return "Dissolve your claim.";
    }
}
