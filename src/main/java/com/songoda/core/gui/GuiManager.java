package com.songoda.core.gui;

import com.songoda.core.compatibility.ClientVersion;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages events for GUI screens
 */
public class GuiManager {
    final Plugin plugin;
    final UUID uuid = UUID.randomUUID(); // manager tracking to fix weird bugs from lazy programming
    final GuiListener listener = new GuiListener(this);
    final Map<Player, Gui> openInventories = new HashMap<>();
    private final Object lock = new Object();
    private boolean initialized = false;
    private boolean shutdown = false;

    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Initialize the GUI handlers
     */
    public void init() {
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        initialized = true;
        shutdown = false;
    }

    /**
     * Check to see if this manager cannot open any more GUI screens
     *
     * @return true if the owning plugin has shutdown
     */
    public boolean isClosed() {
        return shutdown;
    }

    /**
     * Create and display a GUI interface for a player
     *
     * @param player player to open the interface for
     * @param gui    GUI to use
     */
    public void showGUI(Player player, Gui gui) {
        if (shutdown) {
            if (!plugin.isEnabled()) {
                return;
            }

            // recover if reloaded without calling init manually
            init();
        } else if (!initialized) {
            init();
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Gui openInv = openInventories.get(player);

            if (openInv != null) {
                openInv.open = false;
            }

            Inventory inv = gui.getOrCreateInventory(this);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(inv);
                gui.onOpen(this, player);

                synchronized (lock) {
                    openInventories.put(player, gui);
                }
            });
        });
    }

    public void showPopup(Player player, String message) {
        showPopup(player, message, CompatibleMaterial.NETHER_STAR, BackgroundType.ADVENTURE);
    }

    public void showPopup(Player player, String message, CompatibleMaterial icon) {
        showPopup(player, message, icon, BackgroundType.ADVENTURE);
    }

    public void showPopup(Player player, String message, CompatibleMaterial icon, BackgroundType background) {
        if (ClientVersion.getClientVersion(player).isAtLeast(ServerVersion.V1_12)) {
            PopupMessage popup = new PopupMessage(plugin, icon, message, background);
            popup.add();
            popup.grant(player);

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                popup.revoke(player);
                popup.remove();
            }, 70);

            return;
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            player.sendTitle("", message, 10, 70, 10);
            return;
        }

        player.sendTitle("", message);
    }

    /**
     * Close all active GUIs
     */
    public void closeAll() {
        synchronized (lock) {
            openInventories.entrySet().stream()
                    .filter(e -> e.getKey().getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder)
                    .collect(Collectors.toList()) // to prevent concurrency exceptions
                    .forEach(e -> e.getKey().closeInventory());

            openInventories.clear();
        }
    }

    protected static class GuiListener implements Listener {
        final GuiManager manager;

        public GuiListener(GuiManager manager) {
            this.manager = manager;
        }

        @EventHandler(priority = EventPriority.LOW)
        void onDragGUI(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            Inventory openInv = event.getInventory();
            Gui gui;
            if (openInv.getHolder() != null && openInv.getHolder() instanceof GuiHolder
                    && ((GuiHolder) openInv.getHolder()).manager.uuid.equals(manager.uuid)) {
                gui = ((GuiHolder) openInv.getHolder()).getGUI();

                if (event.getRawSlots().stream()
                        .filter(slot -> gui.inventory.getSize() > slot)
                        .anyMatch(slot -> !gui.unlockedCells.getOrDefault(slot, false))) {
                    event.setCancelled(true);
                    event.setResult(Result.DENY);
                }
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onClickGUI(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof final Player player)) {
                return;
            }

            Inventory openInv = event.getInventory();

            Gui gui;
            if (openInv.getHolder() != null && openInv.getHolder() instanceof GuiHolder &&
                    ((GuiHolder) openInv.getHolder()).manager.uuid.equals(manager.uuid)) {
                gui = ((GuiHolder) openInv.getHolder()).getGUI();

                if (event.getClick() == ClickType.DOUBLE_CLICK) {
                    // always cancel this event if there are matching gui elements, since it tends to do bad things
                    ItemStack clicked = event.getCursor();
                    if (clicked != null && clicked.getType() != Material.AIR) {
                        int cell = 0;
                        for (ItemStack it : gui.inventory.getContents()) {
                            if (!gui.unlockedCells.getOrDefault(cell++, false) && clicked.isSimilar(it)) {
                                event.setCancelled(true);

                                break;
                            }
                        }
                    }
                }

                if (event.getSlotType() == SlotType.OUTSIDE) {
                    if (!gui.onClickOutside(manager, player, event)) {
                        event.setCancelled(true);
                    }
                } // did we click the gui or in the user's inventory?
                else if (event.getRawSlot() < gui.inventory.getSize()) { // or could use event.getClickedInventory() == gui.inventory
                    // allow event if this is not a GUI element
                    event.setCancelled(gui.unlockedCells.entrySet().stream().noneMatch(e -> event.getSlot() == e.getKey() && e.getValue()));

                    // process button press
                    if (gui.onClick(manager, player, openInv, event)) {
                        player.playSound(player.getLocation(), gui.getDefaultSound().getSound(), 1F, 1F);
                    }
                } else {
                    // Player clicked in the bottom inventory while GUI is open
                    if (gui.onClickPlayerInventory(manager, player, openInv, event)) {
                        player.playSound(player.getLocation(), gui.getDefaultSound().getSound(), 1F, 1F);
                    } else if (!gui.acceptsItems || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        void onCloseGUI(InventoryCloseEvent event) {
            Inventory openInv = event.getInventory();

            if (openInv.getHolder() != null && openInv.getHolder() instanceof GuiHolder &&
                    ((GuiHolder) openInv.getHolder()).manager.uuid.equals(manager.uuid)) {
                Gui gui = ((GuiHolder) openInv.getHolder()).getGUI();

                if (!gui.open) {
                    return;
                }

                final Player player = (Player) event.getPlayer();
                if (!gui.allowDropItems) {
                    player.setItemOnCursor(null);
                }

                if (manager.shutdown) {
                    gui.onClose(manager, player);
                } else {
                    Bukkit.getScheduler().runTaskLater(manager.plugin, () -> gui.onClose(manager, player), 1);
                }

                manager.openInventories.remove(player);
            }
        }

        @EventHandler
        void onDisable(PluginDisableEvent event) {
            if (event.getPlugin() == manager.plugin) {
                // uh-oh! Abandon ship!!
                manager.shutdown = true;
                manager.closeAll();
                manager.initialized = false;
            }
        }
    }
}
