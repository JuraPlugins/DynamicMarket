package me.example;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EarningsStorage {
    private final File file;
    private final YamlConfiguration config;

    public EarningsStorage(File file) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveEarnings(Map<UUID, Double> daily, Map<UUID, Double> allTime) {
        for (String key : config.getKeys(false)) config.set(key, null); 
        for (Map.Entry<UUID, Double> e : daily.entrySet()) {
            config.set("daily." + e.getKey().toString(), e.getValue());
        }
        for (Map.Entry<UUID, Double> e : allTime.entrySet()) {
            config.set("alltime." + e.getKey().toString(), e.getValue());
        }
        try { config.save(file); } catch (IOException ignored) {}
    }

    public void loadEarnings(Map<UUID, Double> daily, Map<UUID, Double> allTime) {
        daily.clear();
        allTime.clear();
        if (config.isConfigurationSection("daily")) {
            for (String key : config.getConfigurationSection("daily").getKeys(false)) {
                daily.put(UUID.fromString(key), config.getDouble("daily." + key));
            }
        }
        if (config.isConfigurationSection("alltime")) {
            for (String key : config.getConfigurationSection("alltime").getKeys(false)) {
                allTime.put(UUID.fromString(key), config.getDouble("alltime." + key));
            }
        }
    }
}

