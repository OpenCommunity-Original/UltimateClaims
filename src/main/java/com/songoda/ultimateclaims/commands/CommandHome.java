package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandHome extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandHome(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "home");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        StringBuilder claimBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String line = args[i];
            claimBuilder.append(line).append(" ");
        }
        String claimStr = claimBuilder.toString().trim();

        boolean bypass = sender.hasPermission("ultimateclaims.bypass.home");
        Optional<Claim> oClaim = plugin.getClaimManager().getRegisteredClaims().stream()
                .filter(c -> c.getName().equalsIgnoreCase(claimStr)
                        && (bypass || c.isOwnerOrMember(player))).findFirst();

        if (!oClaim.isPresent()) {
            sendPrefixedMessage(sender, "command.general.notapartclaim");
            return ReturnType.FAILURE;
        }
        Claim claim = oClaim.get();

        if (claim.getHome() == null) {
            sendPrefixedMessage(sender, "command.home.none");
            return ReturnType.FAILURE;
        }

        player.teleport(claim.getHome());

        sendPrefixedMessage(player, "command.home.success")
        ;

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (!(sender instanceof Player player)) return null;
        if (args.length == 1) {
            boolean bypass = sender.hasPermission("ultimateclaims.bypass.home");
            List<String> claims = new ArrayList<>();
            for (Claim claim : plugin.getClaimManager().getRegisteredClaims()) {
                if (!claim.isOwnerOrMember(player) && !bypass) continue;
                claims.add(claim.getName());
            }
            return claims;
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.home";
    }

    @Override
    public String getSyntax() {
        return "home <claim>";
    }

    @Override
    public String getDescription() {
        return "Go to a claims home.";
    }
}
