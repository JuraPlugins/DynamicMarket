package me.example;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketRankingListGUI {
    private final MarketManager marketManager;
    private final Map<UUID, Double> earningsMap;
    private final boolean isDaily;
    private final int page;
    private final int size = 27; 

    public MarketRankingListGUI(MarketManager marketManager, boolean isDaily, int page) {
        this.marketManager = marketManager;
    this.page = page;
    this.isDaily = isDaily;
    this.earningsMap = isDaily ? marketManager.getDailyEarnings() : marketManager.getAllTimeEarnings();
    }

    public void open(Player player) {
    String resolvedTitle = marketManager.getPlugin().getLocalization().msg(isDaily ? "gui.ranking.daily" : "gui.ranking.alltime");
    String separator = marketManager.getPlugin().getLocalization().msg("gui.ranking.page.separator");
    if (resolvedTitle == null || resolvedTitle.startsWith("gui.")) resolvedTitle = marketManager.getPlugin().getLocalization().msgWithFallback(isDaily ? "gui.ranking.daily" : "gui.ranking.alltime");
    if (separator == null || separator.startsWith("gui.")) separator = marketManager.getPlugin().getLocalization().msgWithFallback("gui.ranking.page.separator");
    String fullTitle = resolvedTitle + separator + (page+1);
        Inventory inv = Bukkit.createInventory(null, size, fullTitle);
        fillBackgroundGlass(inv);
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(earningsMap.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        int start = page * 13;
        int end = Math.min(start + 13, sorted.size());
    // Centered on the top row: slots 3-9 (inclusive)
    int[] displaySlots = new int[] {3,4,5,6,7,8,9};
        for (int i = start; i < end && i - start < displaySlots.length; i++) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(sorted.get(i).getKey());
            String name = p.getName() != null ? p.getName() : "Bilinmeyen";
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                String color = i == 0 ? "§6" : (i == 1 ? "§b" : (i == 2 ? "§a" : "§7"));
                meta.setDisplayName(color + "#" + (i+1) + " §l" + name);
                List<String> lore = new ArrayList<>();
                lore.add("§8───────────────");
                // Localization: earnings line
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%earnings%", String.format("%,.0f", sorted.get(i).getValue()));
                String earningsLine = marketManager.getPlugin().getLocalization().formatMsg("gui.category.stats.lore.total", placeholders);
                lore.add(earningsLine);
                lore.add("§8───────────────");
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            int posIndex = i - start;
            if (posIndex >= 0 && posIndex < displaySlots.length) {
                inv.setItem(displaySlots[posIndex], skull);
            }
        }
    // Back button always at slot 18 (bottom left)
    inv.setItem(18, createButton(Material.ARROW, marketManager.getPlugin().getLocalization().msg("gui.ranking.list.back")));
    // (Optional: next/prev buttons can be added if needed, but not required for 1 page)
        player.openInventory(inv);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackgroundGlass(Inventory inv) {
        Material glass = Material.GRAY_STAINED_GLASS_PANE;
        for (int i = 0; i < size; i++) {
            inv.setItem(i, createButton(glass, " "));
        }
    }

    public static void handleClick(InventoryClickEvent e, MarketManager marketManager) {
        
        // Menu protection: match any page title (alltime/daily)
        String alltimeTitle = marketManager.getPlugin().getLocalization().msg("gui.ranking.alltime");
        String dailyTitle = marketManager.getPlugin().getLocalization().msg("gui.ranking.daily");
        String separator = marketManager.getPlugin().getLocalization().msg("gui.ranking.page.separator");
        if (alltimeTitle == null || alltimeTitle.startsWith("gui.")) alltimeTitle = marketManager.getPlugin().getLocalization().msgWithFallback("gui.ranking.alltime");
        if (dailyTitle == null || dailyTitle.startsWith("gui.")) dailyTitle = marketManager.getPlugin().getLocalization().msgWithFallback("gui.ranking.daily");
        if (separator == null || separator.startsWith("gui.")) separator = marketManager.getPlugin().getLocalization().msgWithFallback("gui.ranking.page.separator");
        String currentTitle = e.getView().getTitle();
        boolean matches = false;
        for (int i = 1; i <= 10; i++) {
            if (currentTitle.equals(alltimeTitle + separator + i) || currentTitle.equals(dailyTitle + separator + i)) {
                matches = true;
                break;
            }
        }
        if (!matches) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getSlot() == 18) {
            new MarketRankingGUI(marketManager).open(player);
        }
    }
}

