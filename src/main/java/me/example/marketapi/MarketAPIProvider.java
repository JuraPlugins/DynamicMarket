package me.example.marketapi;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;


public class MarketAPIProvider {
    
    public static MarketAPI get(Plugin plugin) {
        ServicesManager sm = plugin.getServer().getServicesManager();
        return sm.load(MarketAPI.class);
    }
}

