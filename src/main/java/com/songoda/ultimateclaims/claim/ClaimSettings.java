package com.songoda.ultimateclaims.claim;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.util.LocaleAPI;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ClaimSettings {

    private final Set<ClaimSetting> settings = new HashSet<>();

    public ClaimSettings setEnabled(ClaimSetting setting, boolean enabled) {
        if (enabled)
            settings.add(setting);
        else
            settings.remove(setting);
        return this;
    }

    public boolean isEnabled(ClaimSetting setting) {
        return settings.contains(setting);
    }

    public String getStatus(ClaimSetting setting, Player player) {
        return isEnabled(setting) ? LocaleAPI.getMessage(player,"general.status.true") : LocaleAPI.getMessage(player,"general.status.false");
    }
}
