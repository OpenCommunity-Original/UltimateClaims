package com.songoda.core.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Get which hand is being used.
 */
public enum CompatibleHand {
    MAIN_HAND, OFF_HAND;

    private static final Map<String, Method> methodCache = new HashMap<>();

    /**
     * Use up whatever item the player is holding in their main hand
     *
     * @param player player to grab item from
     * @param amount number of items to use up
     */
    public void takeItem(Player player, int amount) {
        ItemStack item = this == CompatibleHand.MAIN_HAND
                ? player.getInventory().getItemInHand() : player.getInventory().getItemInOffHand();

        int result = item.getAmount() - amount;
        item.setAmount(result);

        if (this == CompatibleHand.MAIN_HAND) {
            player.setItemInHand(result > 0 ? item : null);
            return;
        }

        player.getInventory().setItemInOffHand(result > 0 ? item : null);
    }

}
