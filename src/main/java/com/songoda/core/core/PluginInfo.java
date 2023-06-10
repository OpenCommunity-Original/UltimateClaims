package com.songoda.core.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class PluginInfo {
    private final JavaPlugin javaPlugin;
    private final int songodaId;
    private final String coreIcon;
    private final String coreLibraryVersion;
    private final List<PluginInfoModule> modules = new ArrayList<>();
    private final boolean hasUpdate = false;
    private String latestVersion;
    private String notification;
    private String changeLog;
    private String marketplaceLink;
    private JSONObject json;

    public PluginInfo(JavaPlugin javaPlugin, int songodaId, String icon, String coreLibraryVersion) {
        this.javaPlugin = javaPlugin;
        this.songodaId = songodaId;
        this.coreIcon = icon;
        this.coreLibraryVersion = coreLibraryVersion;
    }

    public String getNotification() {
        return notification;
    }

    public JSONObject getJson() {
        return json;
    }

    public PluginInfoModule addModule(PluginInfoModule module) {
        modules.add(module);

        return module;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public int getSongodaId() {
        return songodaId;
    }

}
