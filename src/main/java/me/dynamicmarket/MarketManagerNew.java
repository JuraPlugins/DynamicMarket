package me.example;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;

import java.util.*;

public class MarketManagerNew {
    
    private final DynamicMarket plugin;
    private final Map<Material, Double> prices;
    private final Map<Material, Integer> stocks;
    
    public MarketManagerNew(DynamicMarket plugin) {
        this.plugin = plugin;
        this.prices = new HashMap<>();
        this.stocks = new HashMap<>();
        initializePrices();
    }
    
    private void initializePrices() {
        prices.put(Material.DIAMOND, 50.0);
        prices.put(Material.GOLD_INGOT, 10.0);
        prices.put(Material.IRON_INGOT, 5.0);
        prices.put(Material.COAL, 2.0);
        prices.put(Material.EMERALD, 75.0);
        for (Material material : prices.keySet()) {
            stocks.put(material, 1000);
        }
    }
    
    public void openMarketGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "§6Market");
        
        int slot = 0;
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            if (slot >= 45) break;
            
            Material material = entry.getKey();
            double price = entry.getValue();
            int stock = stocks.getOrDefault(material, 0);
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6" + material.name());
            
            List<String> lore = Arrays.asList(
                "§7Fiyat: §a" + String.format("%.2f", price) + " ₺",
                "§7Stok: §b" + stock,
                "§e Sol tık: Satın al",
                "§e Sağ tık: Sat"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot, item);
            slot++;
        }
        
        player.openInventory(inv);
    }
    
    public boolean buyFromMarket(Player player, Material material, int amount) {
        if (!prices.containsKey(material)) {
            me.example.util.MessageUtils.send(player, "§cBu ürün markette mevcut değil!");
            return false;
        }
        
        double totalCost = prices.get(material) * amount;
        int currentStock = stocks.getOrDefault(material, 0);
        
        if (currentStock < amount) {
            me.example.util.MessageUtils.send(player, "§cMarkette yeterli stok yok!");
            return false;
        }
        
        if (plugin.getEconomy().getBalance(player) < totalCost) {
            me.example.util.MessageUtils.send(player, "§cYeterli paranız yok!");
            return false;
        }
        
        plugin.getEconomy().withdrawPlayer(player, totalCost);
        player.getInventory().addItem(new ItemStack(material, amount));
        stocks.put(material, currentStock - amount);
        
        me.example.util.MessageUtils.send(player, "§a" + amount + " adet " + material.name() + 
            " başarıyla satın alındı! Ödenen: " + String.format("%.2f", totalCost) + " ₺");
        
        return true;
    }
    
    public boolean sellToMarket(Player player, Material material, int amount) {
        if (!prices.containsKey(material)) {
            player.sendMessage("§cBu ürün markette satılamaz!");
            return false;
        }
        
        if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
            player.sendMessage("§cYeterli ürününüz yok!");
            return false;
        }
        
        double totalEarnings = prices.get(material) * amount * 0.9; 
        
        player.getInventory().removeItem(new ItemStack(material, amount));
        plugin.getEconomy().depositPlayer(player, totalEarnings);
        
        int currentStock = stocks.getOrDefault(material, 0);
        stocks.put(material, currentStock + amount);
        
        player.sendMessage("§a" + amount + " adet " + material.name() + 
            " başarıyla satıldı! Kazanç: " + String.format("%.2f", totalEarnings) + " ₺");
        
        return true;
    }
    
    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }
    
    public void setPrice(Material material, double price) {
        prices.put(material, price);
    }
    
    public int getStock(Material material) {
        return stocks.getOrDefault(material, 0);
    }
}

