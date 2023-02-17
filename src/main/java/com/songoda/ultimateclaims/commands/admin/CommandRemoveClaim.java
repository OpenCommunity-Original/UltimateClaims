package com.songoda.ultimateclaims.commands.admin;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandRemoveClaim extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandRemoveClaim(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "admin removeclaim");
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

        OfflinePlayer offlineOwner = claim.getOwner().getPlayer();

        // If the owner is online, send him a dissolve message
        if (offlineOwner.isOnline()) {
            Player owner = offlineOwner.getPlayer();

            sendPrefixedMessage(owner, "general.claim.dissolve", "%claim%", claim.getName());
        }

        // Remove the whole claim
        claim.destroy(ClaimDeleteReason.ADMIN);

        // Send a message to player
        sendPrefixedMessage(sender, "command.removeclaim.success", "%claim%", claim.getName());

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.admin.removeclaim";
    }

    @Override
    public String getSyntax() {
        return "admin removeclaim";
    }

    @Override
    public String getDescription() {
        return "Remove a claim you're standing in.";
    }
}
