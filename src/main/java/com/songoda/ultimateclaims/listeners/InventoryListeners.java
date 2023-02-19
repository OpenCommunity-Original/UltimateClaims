package com.songoda.ultimateclaims.listeners;

import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.items.PowerCellItem;
import com.songoda.ultimateclaims.settings.Settings;
import com.songoda.ultimateclaims.utils.LocaleAPI;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryListeners implements Listener {

    private final UltimateClaims plugin;

    public InventoryListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        ClaimManager claimManager = plugin.getClaimManager();

        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;

        if (!claimManager.hasClaim(player)
                || chest.getLocation() == null) return;

        Chunk chunk = chest.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) return;

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())
                || claim.getPowerCell().hasLocation()) return;

        Map<Integer, PowerCellItem> recipe = plugin.getItemManager().getRecipe();

        boolean failed = false;
        for (int i = 0; i < 27; i++) {
            PowerCellItem item = recipe.get(i);
            if (item == null) continue;
            if (!item.isSimilar(event.getInventory().getItem(i))) {
                failed = true;
                break;
            }
        }

        if (failed) {
            return;
        }

        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null) continue;
            claim.getPowerCell().addItem(item);
        }
        event.getInventory().clear();
        Location location = chest.getLocation();
        claim.getPowerCell().setLocation(location.clone());

        plugin.getDataManager().updateClaim(claim);

        if (Settings.POWERCELL_HOLOGRAMS.getBoolean())
            claim.getPowerCell().createHologram();

        if (plugin.getDynmapManager() != null)
            plugin.getDynmapManager().refresh();

        float xx = (float) (0 + (Math.random() * 1));
        float yy = (float) (0 + (Math.random() * 2));
        float zz = (float) (0 + (Math.random() * 1));

        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.LAVA, location.add(.5, .5, .5), 25, xx, yy, zz);
        player.playSound(location, CompatibleSound.ENTITY_BLAZE_DEATH.getSound(), 1F, .4F);
        player.playSound(location, CompatibleSound.ENTITY_PLAYER_LEVELUP.getSound(), 1F, .1F);

        LocaleAPI.getMessage(player, "event.powercell.success");
    }
}
