package me.example;

import org.bukkit.scheduler.BukkitRunnable;

public class PriceFluctuationTask extends BukkitRunnable {
    
    private final MarketManager marketManager;
    
    public PriceFluctuationTask(MarketManager marketManager) {
        this.marketManager = marketManager;
    }
    
    @Override
    public void run() {
        marketManager.fluctuatePrices();
    }
}

