

package me.example;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.*;

public class MarketRankingGUI {
    private final MarketManager marketManager;
    private final DynamicMarket plugin;
    private final int size = 27; 

    public MarketRankingGUI(MarketManager marketManager) {
        this.marketManager = marketManager;
        this.plugin = marketManager.getPlugin();
    }

    public void open(Player player) {
        // Başlık localization ile çözülsün
        String title = plugin.getLocalization().msg("gui.ranking.title");
        if (title == null || title.startsWith("gui.")) title = plugin.getLocalization().msgWithFallback("gui.ranking.title");
        Inventory inv = Bukkit.createInventory(null, size, title);
        fillBackgroundGlass(inv);
        inv.setItem(11, createBalanceItem(player));
        inv.setItem(15, createRankingItem(marketManager.getAllTimeEarnings(), 
            plugin.getLocalization().msg("gui.ranking.all"), 
            plugin.getLocalization().msg("gui.ranking.all.desc")));
        player.openInventory(inv);
    }

    private ItemStack createRankingItem(Map<UUID, Double> map, String displayName, String desc) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(map.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            List<String> lore = new ArrayList<>();
            lore.add(desc);
            lore.add("");
            for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(sorted.get(i).getKey());
                String name = p.getName() != null ? p.getName() : "Bilinmeyen";
                String color = i == 0 ? "§6" : (i == 1 ? "§7" : "§c");
                String emoji = i == 0 ? "🏆" : (i == 1 ? "🥈" : "🥉");
                lore.add(color + "#" + (i+1) + " " + name + " §7- §a" + String.format("%.2f", sorted.get(i).getValue()) + " ₺ " + emoji);
            }
            if (sorted.isEmpty()) lore.add(plugin.getLocalization().msg("gui.ranking.no_data"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBalanceItem(Player player) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLocalization().msg("gui.ranking.balance.name"));
            List<String> lore = new ArrayList<>();
            double daily = marketManager.getDailyEarnings().getOrDefault(player.getUniqueId(), 0.0);
            double all = marketManager.getAllTimeEarnings().getOrDefault(player.getUniqueId(), 0.0);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%daily%", String.format("%.2f", daily));
            lore.add(plugin.getLocalization().formatMsg("gui.ranking.balance.daily", placeholders));
            
            placeholders.clear();
            placeholders.put("%all%", String.format("%.2f", all));
            lore.add(plugin.getLocalization().formatMsg("gui.ranking.balance.all", placeholders));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackgroundGlass(Inventory inv) {
        Material glass = Material.GRAY_STAINED_GLASS_PANE;
        for (int i = 0; i < size; i++) {
            inv.setItem(i, createGlass(glass));
        }
    }
    private ItemStack createGlass(Material mat) {
        ItemStack glass = new ItemStack(mat);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        return glass;
    }
    public static void handleClick(InventoryClickEvent e, MarketManager marketManager) {
        // Sadece kendi menümüz için koruma uygula
        String guiTitle = marketManager.getPlugin().getLocalization().msg("gui.ranking.title");
        if (guiTitle == null || guiTitle.startsWith("gui.")) guiTitle = marketManager.getPlugin().getLocalization().msgWithFallback("gui.ranking.title");
        String currentTitle = stripColor(e.getView().getTitle());
        String expectedTitle = stripColor(guiTitle);
        if (!currentTitle.equals(expectedTitle)) return;
        // Tam koruma: her durumda iptal
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getCurrentItem() == null) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getSlot() == 15) {
            new MarketRankingListGUI(marketManager, false, 0).open(player);
        }
    }

    private static String stripColor(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)§[0-9A-FK-OR]", "").replaceAll("&[0-9a-fk-or]", "");
    }
}


