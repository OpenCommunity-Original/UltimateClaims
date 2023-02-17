package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerUnbanEvent;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandUnBan extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandUnBan(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "unban");
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

        OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

        if (toBan == null || !(toBan.hasPlayedBefore() || toBan.isOnline())) {
            sendPrefixedMessage(sender, "command.general.noplayer");
            return ReturnType.FAILURE;
        } else if (player.getUniqueId().equals(toBan.getUniqueId())) {
            sendPrefixedMessage(sender, "command.unban.notself");
            return ReturnType.FAILURE;
        }

        ClaimPlayerUnbanEvent event = new ClaimPlayerUnbanEvent(claim, player, toBan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toBan.isOnline())
            sendPrefixedMessage(toBan.getPlayer(), "command.unban.unbanned", "%claim%", claim.getName());

        sendPrefixedMessage(sender, "command.unban.unban", "%name%", toBan.getName(), "%claim%", claim.getName());

        claim.unBanPlayer(toBan.getUniqueId());
        plugin.getDataManager().deleteBan(claim, toBan.getUniqueId());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1 && sender instanceof Player) {
            // grab our ban list
            Claim claim = plugin.getClaimManager().getClaim((Player) sender);
            Set<UUID> bans;
            if (claim != null && !(bans = claim.getBannedPlayers()).isEmpty()) {
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p != sender && bans.stream().anyMatch(id -> p.getUniqueId().equals(id)))
                        .map(Player::getName).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.unban";
    }

    @Override
    public String getSyntax() {
        return "unban <member>";
    }

    @Override
    public String getDescription() {
        return "Unban a member from your claim.";
    }
}
