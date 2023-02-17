package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerKickEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandKick extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandKick(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "kick");
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
        OfflinePlayer toKick;

        if (target != null) {
            toKick = target.getPlayer();
        } else {
            // unknown player: double-check
            toKick = Bukkit.getOfflinePlayer(args[0]);

            if (toKick == null || !(toKick.hasPlayedBefore() || toKick.isOnline())) {
                sendPrefixedMessage(sender, "command.general.noplayer");
                return ReturnType.FAILURE;
            } else if (player.getUniqueId().equals(toKick.getUniqueId())) {
                sendPrefixedMessage(sender, "command.kick.notself");
                return ReturnType.FAILURE;
            }

            // all good!
            target = claim.getMember(toKick.getUniqueId());
        }

        if (target == null || target.getRole() != ClaimRole.MEMBER) {
            sendPrefixedMessage(sender, "command.general.notinclaim");
            return ReturnType.FAILURE;
        }

        ClaimPlayerKickEvent event = new ClaimPlayerKickEvent(claim, toKick);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toKick.isOnline())
            sendPrefixedMessage(toKick.getPlayer(), "command.kick.kicked", "%claim%", claim.getName());

        sendPrefixedMessage(sender, "command.kick.kick", "%name%", toKick.getName(), "%claim%", claim.getName());

        // and YEET!
        target.setRole(ClaimRole.VISITOR);
        plugin.getDataManager().deleteMember(target);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            // todo: list out members in this player's owned claim
            return PlayerUtils.getVisiblePlayerNames(sender, args[0]);
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.kick";
    }

    @Override
    public String getSyntax() {
        return "kick <member>";
    }

    @Override
    public String getDescription() {
        return "Kick a member from your claim.";
    }
}
