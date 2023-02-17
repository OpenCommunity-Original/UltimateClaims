package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimChunkUnclaimEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import com.songoda.ultimateclaims.claim.PowerCell;
import com.songoda.ultimateclaims.claim.region.ClaimedChunk;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandUnClaim extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandUnClaim(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "unclaim");
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

        if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())) {
            sendPrefixedMessage(sender, "command.general.notyourclaim");
            return ReturnType.FAILURE;
        }

        if (claim.getPowerCell().hasLocation()) {
            PowerCell powerCell = claim.getPowerCell();
            if (powerCell.getLocation().getChunk() == chunk) {
                sendPrefixedMessage(sender, "command.unclaim.powercell");
                return ReturnType.FAILURE;
            }
        }

        ClaimChunkUnclaimEvent event = new ClaimChunkUnclaimEvent(claim, chunk);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        // we've just unclaimed the chunk we're in, so we've "moved" out of the claim
        // Note: Can't use streams here because `Bukkit.getOnlinePlayers()` has a different protoype in legacy
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(chunk)) {
                ClaimMember member = claim.getMember(p);
                if (member != null) {
                    if (member.getRole() == ClaimRole.VISITOR)
                        claim.removeMember(member);
                    else
                        member.setPresent(false);
                    plugin.getTrackerTask().toggleFlyOff(p);
                }
                if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
                    claim.getVisitorBossBar().removePlayer(p);
                    claim.getMemberBossBar().removePlayer(p);
                }
            }
        }

        // Remove chunk from claim
        ClaimedChunk removedChunk = claim.removeClaimedChunk(chunk, player);

        if (plugin.getDynmapManager() != null)
            plugin.getDynmapManager().refresh();

        if (claim.getClaimSize() == 0) {
            sendPrefixedMessage(sender, "general.claim.dissolve", "%claim%", claim.getName());

            claim.destroy(ClaimDeleteReason.PLAYER);
        } else {
            plugin.getDataManager().deleteClaimedChunk(removedChunk);

            sendPrefixedMessage(sender, "command.unclaim.success");
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.unclaim";
    }

    @Override
    public String getSyntax() {
        return "unclaim";
    }

    @Override
    public String getDescription() {
        return "Unclaim land from your claim.";
    }
}
