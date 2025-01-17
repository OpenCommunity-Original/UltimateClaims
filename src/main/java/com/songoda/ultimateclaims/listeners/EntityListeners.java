package com.songoda.ultimateclaims.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.claim.ClaimSetting;
import com.songoda.ultimateclaims.claim.PowerCell;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import com.songoda.ultimateclaims.tasks.VisualizeTask;
import com.songoda.ultimateclaims.utils.LocaleAPI;
import com.songoda.ultimateclaims.utils.Util;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Dispenser;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.Objects;

import static com.songoda.ultimateclaims.utils.LocaleAPI.sendPrefixedMessage;

public class EntityListeners implements Listener {

    private final UltimateClaims plugin;

    public EntityListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // update cached username on login
        final Player player = event.getPlayer();
        plugin.getClaimManager().getRegisteredClaims().stream()
                .map(claim -> claim.getMember(player))
                .filter(Objects::nonNull)
                .forEach(member -> member.setName(player.getName()));
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        VisualizeTask.removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (playerMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            if (event.getPlayer().isInsideVehicle())
                event.getPlayer().leaveVehicle();
            event.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(VehicleMoveEvent event) {
        Entity entity = event.getVehicle().getPassenger();
        if (!(entity instanceof Player)) return;
        if (playerMove(event.getFrom(), event.getTo(), (Player) entity)) {
            entity.leaveVehicle();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (playerMove(event.getFrom(), event.getTo(), event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void ongetIn(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) return;
        if (playerMove(event.getEntered().getLocation(),
                event.getVehicle().getLocation(), (Player) event.getEntered()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();

        if (claimManager.hasClaim(event.getLocation().getChunk())) {
            Claim claim = claimManager.getClaim(event.getLocation().getChunk());
            if (!claim.getClaimSettings().isEnabled(ClaimSetting.HOSTILE_MOB_SPAWNING) && event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChange(EntityChangeBlockEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();
        if (event.getEntity() instanceof Player
                || event.getEntity() instanceof FallingBlock) return;

        if (claimManager.hasClaim(event.getBlock().getLocation().getChunk())) {
            Claim claim = claimManager.getClaim(event.getBlock().getLocation().getChunk());
            if (!claim.getClaimSettings().isEnabled(ClaimSetting.MOB_GRIEFING)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmor(PlayerArmorStandManipulateEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();

        Entity entity = event.getRightClicked();

        Chunk chunk = entity.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) return;

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.playerHasPerms(event.getPlayer(), ClaimPerm.PLACE)) {
            sendPrefixedMessage(event.getPlayer(), "event.general.nopermission");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrame(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;
        ClaimManager claimManager = plugin.getClaimManager();

        Entity entity = event.getEntity();

        Chunk chunk = entity.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) return;

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.playerHasPerms((Player) event.getRemover(), ClaimPerm.PLACE)) {
            sendPrefixedMessage(event.getRemover(), "event.general.nopermission");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (event.getBlock().getType() != CompatibleMaterial.DISPENSER.getMaterial()) return;
        Dispenser dispenser = (Dispenser) event.getBlock().getState().getData();
        ClaimManager claimManager = plugin.getClaimManager();

        Chunk to = event.getBlock().getRelative(dispenser.getFacing()).getLocation().getChunk();
        Chunk from = event.getBlock().getLocation().getChunk();

        if (claimManager.hasClaim(to)) {
            Claim claim = claimManager.getClaim(to);
            if (claimManager.getClaim(from) != claim) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Claim claim = claimManager.getClaim(chunk);

        if (claim != null) {
            Entity source = event.getDamager();
            if (source instanceof Projectile) {
                ProjectileSource s = ((Projectile) source).getShooter();
                if (s instanceof Player) {
                    source = (Player) s;
                }
            }
            if (source instanceof Player) {
                if (!(event.getEntity() instanceof Player)) {
                    if (!(event.getEntity() instanceof LivingEntity) && event.getEntity().getType() != EntityType.ARMOR_STAND) {
                        event.setCancelled(!claim.playerHasPerms((Player) source, ClaimPerm.BREAK));
                    } else if (!claim.playerHasPerms((Player) source, ClaimPerm.MOB_KILLING)) {
                        event.setCancelled(true);
                    }
                    return;
                }

                if (!claim.getClaimSettings().isEnabled(ClaimSetting.PVP)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlowUp(EntityExplodeEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();

        // Who is responsible for this?
        Entity entity = event.getEntity();
        if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof Entity) {
            entity = (Entity) ((Projectile) entity).getShooter();
        }

        // Does this concern us?
        for (Block block : new ArrayList<>(event.blockList())) {
            if (!claimManager.hasClaim(block.getChunk()))
                continue; // nope - you're not important

            Claim claim = claimManager.getClaim(block.getChunk());
            PowerCell powerCell = claim.getPowerCell();

            // Pay special attention to mobs
            switch (entity.getType()) {
                case CREEPER:
                case GHAST:
                case FIREBALL:
                case WITHER:
                    // For explosions caused by mobs, check if allowed
                    if (!claim.getClaimSettings().isEnabled(ClaimSetting.MOB_GRIEFING)
                            || (powerCell.hasLocation() && powerCell.getLocation().equals(block.getLocation()))) {
                        event.blockList().remove(block);
                    }
                    break;
                case PRIMED_TNT:
                    if (!claim.getClaimSettings().isEnabled(ClaimSetting.TNT)
                            || (powerCell.hasLocation() && powerCell.getLocation().equals(block.getLocation()))) {
                        event.blockList().remove(block);
                    }
                    break;
                default:
                    // Cancel block damage from all other explosions
                    event.blockList().remove(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();
        Chunk chunk = event.getRightClicked().getLocation().getChunk();
        Claim claim = claimManager.getClaim(chunk);

        if (claim != null) {
            Entity source = event.getPlayer();
            Entity entity = event.getRightClicked();
            if (entity.getType().equals(EntityType.VILLAGER) && !claim.playerHasPerms((Player) source, ClaimPerm.TRADING)) {
                sendPrefixedMessage(source, "event.general.nopermission");
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        ClaimManager claimManager = plugin.getClaimManager();
        // todo? setting to allow/disallow these in a claim?
        event.blockList().removeIf(block -> claimManager.hasClaim(block.getChunk()));
    }

    private boolean playerMove(Location fromLocation, Location toLocation, Player player) {
        Chunk from = fromLocation.getChunk();
        Chunk to = toLocation.getChunk();
        if (from == to) return false;
        plugin.getTrackerTask().addLastBefore(player, fromLocation);

        ClaimManager claimManager = plugin.getClaimManager();

        if (claimManager.hasClaim(from)) {
            Claim claim = claimManager.getClaim(from);
            if (claimManager.getClaim(to) != claim) {
                ClaimMember member = claim.getMember(player);
                if (member != null) {
                    if (member.getRole() == ClaimRole.VISITOR)
                        claim.removeMember(member);
                    else
                        member.setPresent(false);
                    plugin.getTrackerTask().toggleFlyOff(player);
                }
                if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
                    claim.getVisitorBossBar().removePlayer(player);
                    claim.getMemberBossBar().removePlayer(player);
                } else {
                    Util.sendTitle(LocaleAPI.getMessage(player, "event.claim.exit", "%claim%", claim.getName()), player);
                    /*if (!player.hasPermission("ultimateclaims.offsounds")) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("BLOCK_BELL_RESONATE"), 0.5F, 1.0F);
                    }*/
                }
            }
        }

        if (claimManager.hasClaim(to)) {
            Claim claim = claimManager.getClaim(to);
            if (claimManager.getClaim(from) != claim) {
                ClaimMember member = claim.getMember(player);
                if (member == null) {
                    if (claim.isLocked() && !player.hasPermission("ultimateclaims.bypass.lock")) {
                        Util.sendTitle(LocaleAPI.getMessage(player, "event.claim.locked"), player);
                        return true;
                    }

                    if (!player.hasPermission("ultimateclaims.admin.invisible")) {
                        claim.addMember(player, ClaimRole.VISITOR);
                        member = claim.getMember(player);
                    }
                }
                if (member != null)
                    member.setPresent(true);

                if (member != null
                        && claim.isBanned(member.getUniqueId())
                        && !player.hasPermission("ultimateclaims.bypass.ban")) {
                    Util.sendTitle(LocaleAPI.getMessage(player, "event.claim.locked"), player);
                    return true;
                }

                if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
                    if (member == null || member.getRole() == ClaimRole.VISITOR) {
                        claim.getVisitorBossBar().addPlayer(player);
                    } else {
                        claim.getMemberBossBar().addPlayer(player);
                    }
                } else {
                    Util.sendTitle(LocaleAPI.getMessage(player, "event.claim.enter", "%claim%", claim.getName()), player);
                    /*if (!player.hasPermission("ultimateclaims.offsounds")) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("UI_TOAST_CHALLENGE_COMPLETE"), 0.5F, 1.0F);
                    }*/
                }
            }
        }
        return false;
    }
}
