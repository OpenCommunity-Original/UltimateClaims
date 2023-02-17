package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.invite.Invite;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandInvite extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandInvite(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "invite");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        if (!plugin.getClaimManager().hasClaim(player)) {
            sendPrefixedMessage(sender, "command.general.noclaim");
            return ReturnType.FAILURE;
        }

        Claim claim = plugin.getClaimManager().getClaim(player);

        OfflinePlayer invited = Bukkit.getPlayer(args[0]);

        if (invited == null || !invited.isOnline()) {
            sendPrefixedMessage(sender, "command.general.noplayer");
            return ReturnType.FAILURE;
        }

        if (player.getUniqueId().equals(invited.getUniqueId())) {
            sendPrefixedMessage(sender, "command.invite.notself");
            return ReturnType.FAILURE;
        }

        if (claim.getMembers().stream()
                .filter(m -> m.getRole() == ClaimRole.MEMBER)
                .anyMatch(m -> m.getUniqueId().equals(invited.getUniqueId()))) {
            sendPrefixedMessage(sender, "command.invite.already");
            return ReturnType.FAILURE;
        }

        if (plugin.getInviteTask().getInvite(player.getUniqueId()) != null) {
            sendPrefixedMessage(sender, "command.invite.alreadyinvited");
            return ReturnType.FAILURE;
        }

        plugin.getInviteTask().addInvite(new Invite(player.getUniqueId(), invited.getUniqueId(), claim));

        sendPrefixedMessage(player, "command.invite.invite", "%name%", invited.getName());

        sendPrefixedMessage(invited.getPlayer(), "command.invite.invited", "%claim%", claim.getName());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            return PlayerUtils.getVisiblePlayerNames(sender, args[0]);
        }
        return null;
    }


    @Override
    public String getPermissionNode() {
        return "ultimateclaims.invite";
    }

    @Override
    public String getSyntax() {
        return "invite <player>";
    }

    @Override
    public String getDescription() {
        return "Invite a player to join your claim.";
    }
}
