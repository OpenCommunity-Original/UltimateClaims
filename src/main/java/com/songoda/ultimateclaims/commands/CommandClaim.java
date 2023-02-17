package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimChunkClaimEvent;
import com.songoda.ultimateclaims.api.events.ClaimCreateEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimBuilder;
import com.songoda.ultimateclaims.claim.region.ClaimedChunk;
import com.songoda.ultimateclaims.claim.region.ClaimedRegion;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandClaim extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandClaim(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "claim");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (Settings.DISABLED_WORLDS.getStringList().contains(player.getWorld().getName())) {
            sendPrefixedMessage(player, "command.claim.disabledworld");
            return ReturnType.FAILURE;
        }

        if (plugin.getClaimManager().hasClaim(player.getLocation().getChunk())) {
            sendPrefixedMessage(player, "command.general.claimed");
            return ReturnType.FAILURE;
        }

        Chunk chunk = player.getLocation().getChunk();
        Claim claim;

        // firstly, can we even claim this chunk?
        Boolean flag;
        if ((flag = WorldGuardHook.getBooleanFlag(chunk, "allow-claims")) != null && !flag) {
            sendPrefixedMessage(player, "command.claim.noregion");
            return ReturnType.FAILURE;
        }

        if (plugin.getClaimManager().hasClaim(player)) {
            claim = plugin.getClaimManager().getClaim(player);

            if (!claim.getPowerCell().hasLocation()) {
                sendPrefixedMessage(player, "command.claim.nocell");
                return ReturnType.FAILURE;
            }

            ClaimedRegion region = claim.getPotentialRegion(chunk);

            if (Settings.CHUNKS_MUST_TOUCH.getBoolean() && region == null) {
                sendPrefixedMessage(player, "command.claim.nottouching");
                return ReturnType.FAILURE;
            }

            int maxClaimable = claim.getMaxClaimSize(player);

            if (claim.getClaimSize() >= maxClaimable) {
                sendPrefixedMessage(player, "command.claim.toomany", "%amount%", String.valueOf(maxClaimable));
                return ReturnType.FAILURE;
            }

            ClaimChunkClaimEvent event = new ClaimChunkClaimEvent(claim, chunk);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }

            boolean newRegion = claim.isNewRegion(chunk);

            if (newRegion && claim.getClaimedRegions().size() >= Settings.MAX_REGIONS.getInt()) {
                sendPrefixedMessage(sender, "command.claim.maxregions");
                return ReturnType.FAILURE;
            }

            claim.addClaimedChunk(chunk, player);
            ClaimedChunk claimedChunk = claim.getClaimedChunk(chunk);
            plugin.getDataManager().createClaimedChunk(claimedChunk);

            if (newRegion) {
                plugin.getDataManager().createClaimedRegion(claimedChunk.getRegion());
            }

            if (plugin.getDynmapManager() != null)
                plugin.getDynmapManager().refresh();

            if (Settings.POWERCELL_HOLOGRAMS.getBoolean())
                claim.getPowerCell().updateHologram();
        } else {
            claim = new ClaimBuilder()
                    .setOwner(player)
                    .addClaimedChunk(chunk, player)
                    .build();

            ClaimCreateEvent event = new ClaimCreateEvent(claim);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }

            plugin.getClaimManager().addClaim(player, claim);
            if (plugin.getDynmapManager() != null)
                plugin.getDynmapManager().refresh();

            plugin.getDataManager().createClaim(claim);
            sendPrefixedMessage(sender, "command.claim.info", "%time%", TimeUtils.makeReadable(Settings.STARTING_POWER.getLong() * 60 * 1000));
        }

        // we've just claimed the chunk we're in, so we've "moved" into the claim
        // Note: Can't use streams here because `Bukkit.getOnlinePlayers()` has a different protoype in legacy
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(chunk)) {
                ClaimMember member = claim.getMember(p);

                if (member != null)
                    member.setPresent(true);
                else
                    // todo: expunge banned players
                    member = claim.addMember(p, ClaimRole.VISITOR);

                if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
                    if (member.getRole() == ClaimRole.VISITOR) {
                        claim.getVisitorBossBar().addPlayer(p);
                    } else {
                        claim.getMemberBossBar().addPlayer(p);
                    }
                }
            }
        }
        sendPrefixedMessage(sender, "command.claim.success");
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 1) {
            List<String> claims = new ArrayList<>();
            for (Claim claim : plugin.getClaimManager().getRegisteredClaims()) {
                if (claim.getMember((Player) sender) == null
                        || claim.getMember((Player) sender).getRole() == ClaimRole.VISITOR) continue;
                claims.add(claim.getName());
            }
            return claims;
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.claim";
    }

    @Override
    public String getSyntax() {
        return "claim";
    }

    @Override
    public String getDescription() {
        return "Claim the land you are currently standing in for your claim.";
    }
}
