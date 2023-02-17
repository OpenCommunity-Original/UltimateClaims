package com.songoda.ultimateclaims.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.utils.LocaleAPI;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListeners implements Listener {
    private final UltimateClaims plugin;

    public InteractListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        ClaimManager claimManager = UltimateClaims.getInstance().getClaimManager();

        Block block = event.getClickedBlock();
        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();

        boolean hasClaim = claimManager.hasClaim(chunk);
        if (event.getAction() == Action.PHYSICAL && hasClaim) {
            Claim claim = claimManager.getClaim(chunk);

            boolean canRedstone = isRedstone(block) && claim.playerHasPerms(player, ClaimPerm.REDSTONE);
            if (canRedstone) {
                return;
            } else if (isRedstone(block) && !claim.playerHasPerms(player, ClaimPerm.REDSTONE)) {
                LocaleAPI.getMessage(player,"event.general.nopermission");
                event.setCancelled(true);
                return;
            }

            if (!claim.playerHasPerms(player, ClaimPerm.PLACE)) {
                LocaleAPI.getMessage(player,"event.general.nopermission");
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasClaim) return;

        Claim claim = claimManager.getClaim(chunk);

        boolean canDoors = isDoor(block) && claim.playerHasPerms(player, ClaimPerm.DOORS);
        boolean canRedstone = isRedstone(block) && claim.playerHasPerms(player, ClaimPerm.REDSTONE);

        if (canRedstone || canDoors) {
            return;
        } else if (isRedstone(block) && !claim.playerHasPerms(player, ClaimPerm.REDSTONE)
                || isDoor(block) && !claim.playerHasPerms(player, ClaimPerm.DOORS)) {
            LocaleAPI.getMessage(player,"event.general.nopermission");
            event.setCancelled(true);
            return;
        }

        ClaimMember member = claim.getMember(player);

        if (claim.getPowerCell().hasLocation()
                && claim.getPowerCell().getLocation().equals(block.getLocation())
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && !player.isSneaking()) {

            // Make sure all items in the powercell are stacked.
            claim.getPowerCell().stackItems();
            if (member != null && member.getRole() != ClaimRole.VISITOR || player.hasPermission("ultimateclaims.powercell.view")) {
                plugin.getGuiManager().showGUI(player, claim.getPowerCell().getGui(player));
            } else {
                LocaleAPI.getMessage(player,"event.powercell.failopen");
            }
            event.setCancelled(true);
            return;
        }

        if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
            LocaleAPI.getMessage(player,"event.general.nopermission");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();

        onBucket(chunk, event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketFillEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();

        onBucket(chunk, event.getPlayer(), event);
    }

    private void onBucket(Chunk chunk, Player player, Cancellable event) {
        ClaimManager claimManager = plugin.getClaimManager();

        if (!claimManager.hasClaim(chunk)) return;

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.playerHasPerms(player, ClaimPerm.PLACE)) {
            LocaleAPI.getMessage(player,"event.general.nopermission");
            event.setCancelled(true);
        }
    }

    private boolean isDoor(Block block) {
        if (block == null) return false;

        switch (block.getType().name()) {
            case "DARK_OAK_DOOR":
            case "ACACIA_DOOR":
            case "BIRCH_DOOR":
            case "JUNGLE_DOOR":
            case "OAK_DOOR":
            case "SPRUCE_DOOR":
            case "ACACIA_TRAPDOOR":
            case "BIRCH_TRAPDOOR":
            case "DARK_OAK_TRAPDOOR":
            case "IRON_TRAPDOOR":
            case "JUNGLE_TRAPDOOR":
            case "OAK_TRAPDOOR":
            case "SPRUCE_TRAPDOOR":
            case "OAK_FENCE_GATE":
            case "ACACIA_FENCE_GATE":
            case "BIRCH_FENCE_GATE":
            case "DARK_OAK_FENCE_GATE":
            case "JUNGLE_FENCE_GATE":
            case "SPRUCE_FENCE_GATE":
            case "WOODEN_DOOR":
            case "WOOD_DOOR":
            case "TRAP_DOOR":
            case "FENCE_GATE":
                return true;
            default:
                return false;
        }
    }

    private boolean isRedstone(Block block) {
        if (block == null) return false;

        CompatibleMaterial material = CompatibleMaterial.getMaterial(block);
        if (material == null)
            return false;

        switch (material) {
            case LEVER:
            case BIRCH_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case STONE_PRESSURE_PLATE:
                return true;
            default:
                return false;
        }
    }
}
