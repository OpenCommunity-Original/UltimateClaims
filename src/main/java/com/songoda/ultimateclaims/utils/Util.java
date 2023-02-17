package com.songoda.ultimateclaims.utils;

import com.songoda.core.utils.TextUtils;
import org.bukkit.entity.Player;

public class Util {
    public static void sendTitle(String message, Player sender) {
        (sender).sendTitle("", TextUtils.formatText(message), 10, 30, 10);
    }
}
