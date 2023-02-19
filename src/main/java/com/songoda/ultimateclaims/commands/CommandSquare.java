package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.core.utils.NumberUtils;
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
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class CommandSquare extends AbstractCommand {

    private final UltimateClaims plugin;

    public CommandSquare(UltimateClaims plugin) {
        super(true, "square");
        this.plugin = plugin;
    }


    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (Settings.DISABLED_WORLDS.getStringList().contains(player.getWorld().getName())) {
            sendPrefixedMessage(sender, "command.claim.disabledworld");
            return ReturnType.FAILURE;
        }

        Chunk centerChunk = player.getLocation().getChunk();
        Claim claim;

        // firstly, can we even claim this chunk?
        Boolean flag;
        if ((flag = WorldGuardHook.getBooleanFlag(centerChunk, "allow-claims")) != null && !flag) {
            sendPrefixedMessage(sender, "command.claim.noregion");
            return ReturnType.FAILURE;
        }

        int warnload = 0;

        if (plugin.getClaimManager().hasClaim(player)) {
            claim = plugin.getClaimManager().getClaim(player);

            // monument check
            if (!claim.getPowerCell().hasLocation()) {
                sendPrefixedMessage(sender, "command.claim.nocell");
                return ReturnType.FAILURE;
            }

            ClaimedRegion region = claim.getPotentialRegion(centerChunk);

            // checking touch chunks nearby
            if (claim.getClaimSize() >= 2) {
                if (Settings.CHUNKS_MUST_TOUCH.getBoolean() && region == null) {
                    sendPrefixedMessage(sender, "command.claim.nottouching");
                    return ReturnType.FAILURE;
                }
            }

            int maxClaimable = claim.getMaxClaimSize(player);

            // check chunk limit
            if (claim.getClaimSize() >= maxClaimable) {
                sendPrefixedMessage(sender, "command.claim.toomany", "%amount%", Integer.toString(maxClaimable));
                return ReturnType.FAILURE;
            }

            ClaimChunkClaimEvent event = new ClaimChunkClaimEvent(claim, centerChunk);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }

            String radiuss = String.join(" ", args);

            // value - number?
            if (!NumberUtils.isNumeric(radiuss)) {
                sendPrefixedMessage(sender, "command.claim.notanumber");
                return ReturnType.FAILURE;
            }

            int radius = Integer.parseInt(radiuss);

            // limit 1-10 for players and 1-30 for admins
            if (!player.hasPermission("ultimateclaims.administrator")) {
                if ((radius < 1) || (radius > 10)) {
                    sendPrefixedMessage(sender, "command.claim.incorrectnumber");
                    return ReturnType.FAILURE;
                }
            } else {
                if ((radius < 1) || (radius > 30)) {
                    sendPrefixedMessage(sender, "command.claim.incorrectnumber")
                    ;
                    return ReturnType.FAILURE;
                }
            }


            // Get the world only once
            World world = player.getLocation().getWorld();
            player.playSound(player.getLocation(), CompatibleSound.ENTITY_PLAYER_LEVELUP.getSound(), 1F, .1F);

            // Loop through chunks within radius
            for (int x = centerChunk.getX() - radius; x <= centerChunk.getX() + radius; x++) {
                for (int z = centerChunk.getZ() - radius; z <= centerChunk.getZ() + radius; z++) {
                    // Check if chunk is loaded
                    if (world.isChunkLoaded(x, z)) {
                        // Get chunk and check if it's claimed
                        final Chunk chunk = centerChunk.getWorld().getChunkAt(x, z);
                        if (!plugin.getClaimManager().hasClaim(chunk)) {
                            // Call event and check max region limit
                            final ClaimChunkClaimEvent events = new ClaimChunkClaimEvent(claim, chunk);
                            Bukkit.getPluginManager().callEvent(events);
                            if (claim.isNewRegion(chunk) && claim.getClaimedRegions().size() >= Settings.MAX_REGIONS.getInt()) {
                                sendPrefixedMessage(sender, "command.claim.maxregions");
                                return ReturnType.FAILURE;
                            }
                            // Add claimed chunk and save to database
                            claim.addClaimedChunk(chunk, player);
                            final ClaimedChunk claimedChunk = claim.getClaimedChunk(chunk);
                            plugin.getDataManager().createClaimedChunk(claimedChunk);
                        }
                    } else {
                        // Warn player if chunk is not loaded
                        warnload++;
                    }
                }
            }

        } else {
            claim = new ClaimBuilder()
                    .setOwner(player)
                    .addClaimedChunk(centerChunk, player)
                    .build();

            ClaimCreateEvent event = new ClaimCreateEvent(claim);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }

            plugin.getClaimManager().addClaim(player, claim);

            plugin.getDataManager().createClaim(claim);

            sendPrefixedMessage(sender, "command.claim.info", "%time%", TimeUtils.makeReadable((long) Settings.STARTING_POWER.getInt() * 60 * 1000));
        }

        // warn player if chunks not loaded and skipped part2
        if (warnload > 0) {
            sendPrefixedMessage(sender, "command.claim.chunksnotloaded")
            ;
            warnload = 0;
        }

        if (Settings.POWERCELL_HOLOGRAMS.getBoolean())
            claim.getPowerCell().updateHologram();

        // we've just claimed the chunk we're in, so we've "moved" into the claim
        // Note: Can't use streams here because `Bukkit.getOnlinePlayers()` has a different protoype in legacy
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(centerChunk)) {
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
        return "ultimateclaims.square";
    }

    @Override
    public String getSyntax() {
        return "square <radius>";
    }

    @Override
    public String getDescription() {
        return "Expand the claim along the radius (square).";
    }
}
