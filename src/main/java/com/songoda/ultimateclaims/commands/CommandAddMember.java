package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimMemberAddEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandAddMember extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandAddMember(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "addmember");
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

        OfflinePlayer toInvite = Bukkit.getOfflinePlayer(args[0]);

        if (!(toInvite.hasPlayedBefore() || toInvite.isOnline())) {
            sendPrefixedMessage(sender, "command.general.noplayer");
            return ReturnType.FAILURE;
        }

        if (player.getUniqueId().equals(toInvite.getUniqueId())) {
            sendPrefixedMessage(sender, "command.invite.notself");
            return ReturnType.FAILURE;
        }

        if (claim.getMembers().stream()
                .filter(m -> m.getRole() == ClaimRole.MEMBER)
                .anyMatch(m -> m.getUniqueId().equals(toInvite.getUniqueId()))) {
            sendPrefixedMessage(sender, "command.invite.already");
            return ReturnType.FAILURE;
        }

        ClaimMemberAddEvent event = new ClaimMemberAddEvent(claim, toInvite);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        ClaimMember newMember = claim.addMember(toInvite, ClaimRole.MEMBER);
        plugin.getDataManager().createMember(newMember);

        if (toInvite.isOnline())
            sendPrefixedMessage(toInvite.getPlayer(), "command.addmember.added", "%claim%", claim.getName());

        sendPrefixedMessage(player, "command.addmember.add", "%name%", toInvite.getName());

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
        return "ultimateclaims.addmember";
    }

    @Override
    public String getSyntax() {
        return "addmember <player>";
    }

    @Override
    public String getDescription() {
        return "Add a player to access your claim.";
    }
}
