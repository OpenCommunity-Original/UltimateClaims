package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerBanEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandBan extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandBan(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "ban");
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
        ClaimMember target = claim.getMember(args[0]);
        OfflinePlayer toBan;

        if (target != null) {
            toBan = target.getPlayer();
        } else {
            // unknown player: double-check
            toBan = Bukkit.getOfflinePlayer(args[0]);

            if (toBan == null || !(toBan.hasPlayedBefore() || toBan.isOnline())) {
                sendPrefixedMessage(sender, "command.general.noplayer");
                return ReturnType.FAILURE;
            }

            // all good!
            target = claim.getMember(toBan.getUniqueId());
        }

        if (player.getUniqueId().equals(toBan.getUniqueId())) {
            sendPrefixedMessage(sender, "command.kick.notself");
            return ReturnType.FAILURE;
        }

        ClaimPlayerBanEvent event = new ClaimPlayerBanEvent(claim, player, toBan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toBan.isOnline())

            sendPrefixedMessage(toBan.getPlayer(), "command.ban.banned", "%claim%", claim.getName());

        sendPrefixedMessage(player, "command.ban.ban", "%name%", toBan.getName(), "%claim%", claim.getName());

        if (target != null) {
            claim.removeMember(toBan);
            target.eject(null);
            if (target.getRole() == ClaimRole.MEMBER)
                plugin.getDataManager().deleteMember(target);
        }

        claim.banPlayer(toBan.getUniqueId());
        plugin.getDataManager().createBan(claim, toBan.getUniqueId());
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
        return "ultimateclaims.ban";
    }

    @Override
    public String getSyntax() {
        return "ban <member>";
    }

    @Override
    public String getDescription() {
        return "Ban a member from your claim.";
    }
}
