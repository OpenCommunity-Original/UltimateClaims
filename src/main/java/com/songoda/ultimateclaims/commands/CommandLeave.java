package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimMemberLeaveEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandLeave extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandLeave(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "leave");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        String claimStr = String.join(" ", args);

        Optional<Claim> oClaim = plugin.getClaimManager().getRegisteredClaims().stream()
                .filter(c -> c.getName().equalsIgnoreCase(claimStr)
                        && c.getMember(player) != null).findFirst();

        if (!oClaim.isPresent()) {
            sendPrefixedMessage(player, "command.general.notapartclaim");
            return ReturnType.FAILURE;
        }

        if (player.getUniqueId().equals((oClaim.get()).getOwner().getUniqueId())) {
            sendPrefixedMessage(player, "command.leave.owner");
            return ReturnType.FAILURE;
        }

        Claim claim = oClaim.get();

        ClaimMemberLeaveEvent event = new ClaimMemberLeaveEvent(claim, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        ClaimMember memberToRemove = claim.getMember(player);

        plugin.getDataManager().deleteMember(memberToRemove);

        claim.removeMember(player);

        sendPrefixedMessage(sender, "command.leave.youleft", "%claim%", claim.getName());

        for (ClaimMember member : claim.getOwnerAndMembers())
            this.notify(member);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (!(sender instanceof Player player)) return null;
        if (args.length == 1) {
            List<String> claims = new ArrayList<>();
            for (Claim claim : plugin.getClaimManager().getRegisteredClaims()) {
                if (!claim.isOwnerOrMember(player)) continue;
                claims.add(claim.getName());
            }
            return claims;
        }
        return null;
    }

    private void notify(ClaimMember member) {
        Player player = Bukkit.getPlayer(member.getUniqueId());
        if (player != null)
            sendPrefixedMessage(player, "command.leave.left", "%player%", player.getName(), "%claim%", member.getClaim().getName());
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.leave";
    }

    @Override
    public String getSyntax() {
        return "leave <claim>";
    }

    @Override
    public String getDescription() {
        return "Leave a claim that you are a member of.";
    }
}
