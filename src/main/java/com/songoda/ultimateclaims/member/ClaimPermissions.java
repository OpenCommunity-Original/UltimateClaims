package com.songoda.ultimateclaims.member;

import com.songoda.ultimateclaims.utils.LocaleAPI;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ClaimPermissions {

    private final Set<ClaimPerm> permissions = new HashSet<>();

    public ClaimPermissions setAllowed(ClaimPerm perm, boolean allowed) {
        if (allowed)
            permissions.add(perm);
        else
            permissions.remove(perm);
        return this;
    }

    public boolean hasPermission(ClaimPerm perm) {
        return permissions.contains(perm);
    }

    public String getStatus(ClaimPerm perm, Player player) {
        return hasPermission(perm) ? LocaleAPI.getMessage(player, "general.status.true") : LocaleAPI.getMessage(player, "general.status.false");
    }
}
