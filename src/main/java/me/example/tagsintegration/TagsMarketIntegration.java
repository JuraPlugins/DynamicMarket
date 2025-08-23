package me.example.tagsintegration;

import me.example.marketapi.MarketAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TagsMarketIntegration {
    private final Plugin plugin;
    private final TagsPlugin tagsPlugin;
    private MarketAPI marketAPI;

    public TagsMarketIntegration(Plugin plugin, TagsPlugin tagsPlugin) {
        this.plugin = plugin;
        this.tagsPlugin = tagsPlugin;
        RegisteredServiceProvider<MarketAPI> provider = Bukkit.getServicesManager().getRegistration(MarketAPI.class);
        if (provider != null) {
            this.marketAPI = provider.getProvider();
        } else {
            this.marketAPI = null;
        }
    }

    public void updateDiamondPrice(Player player) {
        if (marketAPI != null) {
            double fiyat = marketAPI.getPrice("DIAMOND");
            double multiplier = tagsPlugin.getPlayerMarketMultiplier(player);
            marketAPI.setPrice("DIAMOND", fiyat * multiplier);
        } else {
            plugin.getLogger().warning("MarketAPI bulunamadı!");
        }
    }
    public double getPlayerMarketMultiplier(Player player) {
        return tagsPlugin.getPlayerMarketMultiplier(player);
    }
}


