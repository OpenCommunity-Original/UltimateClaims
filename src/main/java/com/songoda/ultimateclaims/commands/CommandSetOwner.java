package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandSetOwner extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandSetOwner(UltimateClaims plugin) {
        super(true, "setowner");
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

        Claim claim = plugin.getClaimManager().getClaim(player);

        if (player.getUniqueId().equals(newOwner.getUniqueId())) {
            sendPrefixedMessage(sender, "command.invite.notself");
            return ReturnType.FAILURE;
        }

        if (!claim.getMembers().stream()
                .filter(m -> m.getRole() == ClaimRole.MEMBER)
                .anyMatch(m -> m.getUniqueId().equals(newOwner.getUniqueId()))) {
            sendPrefixedMessage(sender, "command.general.notinclaim");
            return ReturnType.FAILURE;
        }

        if (!plugin.getClaimManager().hasClaim(player)) {
            sendPrefixedMessage(sender, "command.general.noclaim");
            return ReturnType.FAILURE;
        }

        if (!plugin.getClaimManager().hasClaim(player)) {
            sendPrefixedMessage(sender, "command.general.noclaim");
            return ReturnType.FAILURE;
        }

        final ClaimMember toMember = claim.getOwner();

        if (claim.transferOwnership(newOwner))
            sendPrefixedMessage(sender, "command.transferownership.success", "%claim%", claim.getName());
        else
            sendPrefixedMessage(sender, "command.transferownership.failed", "%claim%", claim.getName());

        toMember.setRole(ClaimRole.MEMBER);

        ClaimMember toOwner = claim.getMember(newOwner.getUniqueId());
        toOwner.setRole(ClaimRole.OWNER);

        UltimateClaims.getInstance().getDataManager().transferClaimOwnership(claim, toMember, toOwner);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.setowner";
    }

    @Override
    public String getSyntax() {
        return "setowner <new_owner>";
    }

    @Override
    public String getDescription() {
        return "Transfer the right to manage the claim.";
    }
}
