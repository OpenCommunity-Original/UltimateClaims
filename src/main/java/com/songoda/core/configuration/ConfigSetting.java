package com.songoda.core.configuration;

import com.songoda.core.SongodaCore;
import com.songoda.core.compatibility.CompatibleMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class ConfigSetting {
    final Config config;
    final String key;

    public ConfigSetting(@NotNull Config config, @NotNull String key, @NotNull Object defaultValue, String... comment) {
        this.config = config;
        this.key = key;

        config.setDefault(key, defaultValue, comment);
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public List<String> getStringList() {
        return config.getStringList(key);
    }

    public boolean getBoolean() {
        return config.getBoolean(key);
    }

    public int getInt() {
        return config.getInt(key);
    }

    public int getInt(int def) {
        return config.getInt(key, def);
    }

    public long getLong() {
        return config.getLong(key);
    }

    public double getDouble() {
        return config.getDouble(key);
    }

    public String getString() {
        return config.getString(key);
    }

    public Object getObject() {
        return config.get(key);
    }

    @NotNull
    public CompatibleMaterial getMaterial() {
        String val = config.getString(key);
        CompatibleMaterial mat = CompatibleMaterial.getMaterial(config.getString(key));

        if (mat == null) {
            SongodaCore.getLogger().log(Level.WARNING, String.format("Config value \"%s\" has an invalid material name: \"%s\"", key, val));
        }

        return mat != null ? mat : CompatibleMaterial.STONE;
    }

}
