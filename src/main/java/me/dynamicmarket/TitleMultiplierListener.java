package me.example;

import me.example.event.DynamicMarketSellEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import me.example.tagsintegration.TagsMarketIntegration;


public class TitleMultiplierListener implements Listener {

    private final TagsMarketIntegration tagsMarketIntegration;
    private final DynamicMarket plugin;

    public TitleMultiplierListener(DynamicMarket plugin, TagsMarketIntegration tagsMarketIntegration) {
        this.plugin = plugin;
        this.tagsMarketIntegration = tagsMarketIntegration;
    }

    @EventHandler
    public void onSell(DynamicMarketSellEvent e) {
        Player p = e.getPlayer();
        double base = e.getEarnings();
        double mult = getTitleMultiplier(p); 
        double newEarn = base * mult;
    plugin.getLogger().info("[Market] TitleMultiplierListener running for player=" + p.getName() + " base=" + base + " mult=" + mult + " new=" + newEarn);
        e.setEarnings(newEarn);
        String line = plugin.getLocalization().msg("transaction.sell.multiplier")
            .replace("%mult%", String.format("%.2f", mult))
            .replace("%earned%", String.format("%.2f", newEarn));
        if (!line.equals("transaction.sell.multiplier")) {
            p.sendMessage(plugin.getLocalization().getPrefix() + line);
        }
    }

    private double getTitleMultiplier(Player p) {
        return tagsMarketIntegration.getPlayerMarketMultiplier(p);
    }
}

