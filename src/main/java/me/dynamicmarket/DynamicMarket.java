package me.example;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;


public class DynamicMarket extends JavaPlugin {
    
    private Economy economy = null;
    private MarketManager marketManager;
    private PriceFluctuationTask priceTask;
    private LocalizationManager localization;
    
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault ekonomisi bulunamadı! Eklenti kapatılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    saveDefaultConfig();
    String lang = getConfig().getString("language", "en");
        localization = new LocalizationManager(this, lang);
        marketManager = new MarketManager(this);
        marketManager.initEarningsStorage(this);
        marketManager.loadData();
        marketManager.initializeQuestPool();
        marketManager.startQuestRefreshScheduler();
        marketManager.startDailyReportScheduler();
        me.example.tagsintegration.TagsPlugin tagsPlugin = (player) -> 1.0; 
        if (getServer().getPluginManager().getPlugin("Tags") != null) {
        }
        me.example.tagsintegration.TagsMarketIntegration tagsMarketIntegration = new me.example.tagsintegration.TagsMarketIntegration(this, tagsPlugin);
        getServer().getPluginManager().registerEvents(new MarketListener(this), this);
    getServer().getPluginManager().registerEvents(new TitleMultiplierListener(this, tagsMarketIntegration), this);
        getCommand("market").setExecutor(this);
        getCommand("marketadmin").setExecutor(this);
        getCommand("marketsat").setExecutor(this);
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
                MarketRankingGUI.handleClick(e, marketManager);
            }
        }, this);
        priceTask = new PriceFluctuationTask(marketManager);
        int intervalMinutes = getConfig().getInt("price_fluctuation.interval_minutes", 1);
        if (intervalMinutes < 1) intervalMinutes = 1; 
        long ticks = intervalMinutes * 60L * 20L; 
        priceTask.runTaskTimer(this, ticks, ticks);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                marketManager.replenishStocks(100);
            }
        }.runTaskTimer(this, 12000L, 12000L); 

        getLogger().info("DynamicMarket eklentisi başarıyla yüklendi!");
    }
    
    @Override
    public void onDisable() {
        if (marketManager != null) {
            marketManager.saveData();
        }
        if (priceTask != null) {
            priceTask.cancel();
        }
        getLogger().info("DynamicMarket eklentisi kapatıldı!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public Economy getEconomy() {
        return economy;
    }

    public LocalizationManager getLocalization() {
        return localization;
    }
    
    public MarketManager getMarketManager() {
        MarketManager.registerAPI(this, marketManager);
        return marketManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("market")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(localization.msg("cmd_player_only"));
                return true;
            }
            Player player = (Player) sender;
            marketManager.openMarketGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("marketgorev")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(localization.msg("cmd_player_only"));
                return true;
            }
            Player player = (Player) sender;
            marketManager.openQuestGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("marketsat")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(localization.msg("cmd_player_only"));
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 1) {
                player.sendMessage(localization.msg("invalid_item"));
                return true;
            }
            Material mat = marketManager.getMaterialByAlias(args[0]);
            if (mat == null) {
                String suggestion = marketManager.getClosestMaterialName(args[0]);
                if (suggestion != null) {
                    player.sendMessage(localization.msg("invalid_item") + " §e" + suggestion);
                } else {
                    player.sendMessage(localization.msg("invalid_item"));
                }
                return true;
            }
            int amount = 1;
            double multiplier = 1.0;
            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(localization.msg("invalid_amount"));
                    return true;
                }
            }
            if (args.length > 2) {
                try {
                    multiplier = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(localization.msg("invalid_multiplier"));
                    return true;
                }
            }
            double sellPrice = marketManager.getSellPrice(mat);
            double totalEarning = sellPrice * amount * multiplier;
            double commission = totalEarning * 0.05; 
            boolean sold = marketManager.sellToMarket(player, mat, amount, multiplier);
            if (sold) {
                player.sendMessage(localization.msg("sold_success")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%item%", mat.name())
                    .replace("%mult%", String.valueOf(multiplier))
                    .replace("%earned%", String.format("%.2f", (totalEarning - commission)))
                    .replace("%commission%", String.format("%.2f", commission)));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("marketadmin")) {
            if (!sender.hasPermission("dynamicmarket.admin")) {
                sender.sendMessage(localization.msg("cmd_no_permission"));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage("§e/marketadmin <reload|info|maintenance|setprice>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    reloadConfig();
                    marketManager.reloadPrices();
                    localization.reload(getConfig().getString("language", "en"));
                    sender.sendMessage(localization.msg("reload_done"));
                    break;
                case "info":
                    sender.sendMessage("§e=== Market Bilgileri ===");
                    for (Material material : marketManager.getAllMaterials()) {
                        double buyPrice = marketManager.getBuyPrice(material);
                        double sellPrice = marketManager.getSellPrice(material);
                        sender.sendMessage(String.format("§f%s: §aAlış: %.2f §cSatış: %.2f", 
                            material.name(), buyPrice, sellPrice));
                    }
                    break;
                case "maintenance":
                    try {
                        java.lang.reflect.Field f = marketManager.getClass().getDeclaredField("rankingMaintenance");
                        f.setAccessible(true);
                        boolean current = f.getBoolean(marketManager);
                        boolean newState = !current;
                        f.setBoolean(marketManager, newState);
                        sender.sendMessage(localization.msg(newState ? "maintenance_enabled" : "maintenance_disabled"));
                    } catch (Exception ex) {
                        sender.sendMessage("§cUnable to toggle maintenance: " + ex.getMessage());
                    }
                    break;
                case "setprice":
                    if (args.length < 3) {
                        sender.sendMessage("§cKullanım: /marketadmin setprice <item> <fiyat>");
                        return true;
                    }
                    Material mat = marketManager.getMaterialByAlias(args[1]);
                    if (mat == null) {
                        sender.sendMessage("§cGeçersiz ürün adı!");
                        return true;
                    }
                    double price;
                    try {
                        price = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cGeçersiz fiyat!");
                        return true;
                    }
                    marketManager.setPrice(mat.name(), price);
                    sender.sendMessage("§a" + mat.name() + " fiyatı güncellendi: " + price);
                    break;
                default:
                    sender.sendMessage("§e/marketadmin <reload|info|maintenance|setprice>");
                    break;
            }
            return true;
        }

        return false;
    }
}
