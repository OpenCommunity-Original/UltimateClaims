package com.songoda.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerUtils {
    static Random random = new Random();

    /**
     * Get a list of all the players that this player can "see"
     *
     * @param sender       user to check against, or null for all players
     * @param startingWith optional query to test: only players whose game names
     *                     start with this
     * @return list of player names that are "visible" to the player
     */
    public static List<String> getVisiblePlayerNames(CommandSender sender, String startingWith) {
        Player player = sender instanceof Player ? (Player) sender : null;
        final String startsWith = startingWith == null || startingWith.isEmpty() ? null : startingWith.toLowerCase();

        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != player)
                .filter(p -> startsWith == null || p.getName().toLowerCase().startsWith(startsWith))
                .filter(p -> player == null || (player.canSee(p) && p.getMetadata("vanished").isEmpty()))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static int getNumberFromPermission(Player player, String permission, int def) {
        final Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();

        boolean set = false;
        int highest = 0;

        for (PermissionAttachmentInfo info : permissions) {
            final String perm = info.getPermission();

            if (!perm.startsWith(permission)) {
                continue;
            }

            final int index = perm.lastIndexOf('.');

            if (index == -1 || index == perm.length()) {
                continue;
            }

            String numStr = perm.substring(perm.lastIndexOf('.') + 1);
            if (numStr.equals("*")) {
                return def;
            }

            final int number = Integer.parseInt(numStr);

            if (number >= highest) {
                highest = number;
                set = true;
            }
        }

        return set ? highest : def;
    }
}
