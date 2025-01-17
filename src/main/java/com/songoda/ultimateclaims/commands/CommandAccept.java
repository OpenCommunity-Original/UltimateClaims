package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimMemberAddEvent;
import com.songoda.ultimateclaims.invite.Invite;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandAccept extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandAccept(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "accept");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Invite invite = plugin.getInviteTask().getInvite(player.getUniqueId());

        if (invite == null) {
            sendPrefixedMessage(player, "command.accept.none");
        } else {
            if (Math.toIntExact(invite.getClaim().getMembers().stream()
                    .filter(member -> member.getRole() == ClaimRole.MEMBER).count()) >= Settings.MAX_MEMBERS.getInt()) {
                sendPrefixedMessage(player, "command.accept.maxed");
                return ReturnType.FAILURE;
            }


            ClaimMemberAddEvent event = new ClaimMemberAddEvent(invite.getClaim(), player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }


            ClaimMember newMember = invite.getClaim().getMember(player);
            if (newMember == null) {
                newMember = invite.getClaim().addMember(player, ClaimRole.MEMBER);
            } else if (newMember.getRole() == ClaimRole.VISITOR) {
                newMember.setRole(ClaimRole.MEMBER);
            }

            invite.accepted();

            plugin.getDataManager().createMember(newMember);

            sendPrefixedMessage(player, "command.accept.success", "%claim%", invite.getClaim().getName());

            OfflinePlayer owner = Bukkit.getPlayer(invite.getInviter());

            if (owner != null && owner.isOnline())
                sendPrefixedMessage(owner.getPlayer(), "command.accept.accepted", "%name%", player.getName());
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.accept";
    }

    @Override
    public String getSyntax() {
        return "accept";
    }

    @Override
    public String getDescription() {
        return "Accept the latest claim invitation.";
    }
}
