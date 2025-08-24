package me.example;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class MarketListener implements Listener {
    
    private final DynamicMarket plugin;
    
    public MarketListener(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        MarketManager marketManager = plugin.getMarketManager();
        if (title.startsWith("§eGünlük Sıralama") || title.startsWith("§6Tüm Zamanlar Sıralama")) {
            MarketRankingListGUI.handleClick(event, marketManager);
            return;
        }
        String mainMenuTitle = plugin.getLocalization().msg("gui.main.title");
        if (title.equals(mainMenuTitle)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Material type = clickedItem.getType();
            int slot = event.getRawSlot();
            if (type == Material.DIAMOND_PICKAXE) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.MADENCILIK);
            } else if (type == Material.WHEAT) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.TARIM);
            } else if (type == Material.BOW) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.AVCILIK);
            } else if (type == Material.FISHING_ROD) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.BALIKCILIK);
            } else if (type == Material.BARRIER) {
                player.closeInventory();
            } else if (type == Material.LIGHT_BLUE_BANNER && slot == 29) {
                marketManager.openMarketQuestsGUI(player);
            } else if (type == Material.ENCHANTED_BOOK && slot == 31) {
                marketManager.openMarketStatsGUI(player);
            } else if (type == Material.NETHER_STAR && slot == 33) {
                marketManager.openMarketLeaderboardGUI(player);
            }
            return;
        }
        String miningTitle = plugin.getLocalization().formatMsg("gui.category.title", 
            Map.of("%category%", plugin.getLocalization().msg("category.mining")));
        String farmingTitle = plugin.getLocalization().formatMsg("gui.category.title", 
            Map.of("%category%", plugin.getLocalization().msg("category.farming")));
        String huntingTitle = plugin.getLocalization().formatMsg("gui.category.title", 
            Map.of("%category%", plugin.getLocalization().msg("category.hunting")));
        String fishingTitle = plugin.getLocalization().formatMsg("gui.category.title", 
            Map.of("%category%", plugin.getLocalization().msg("category.fishing")));
            
        if (title.equals(miningTitle) || title.equals(farmingTitle) || 
            title.equals(huntingTitle) || title.equals(fishingTitle)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Material type = clickedItem.getType();
            if (type == Material.ARROW) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.ANA);
                return;
            }
            if (type == Material.BARRIER || type == Material.SUNFLOWER) {
                player.closeInventory();
                return;
            }
            if (type == Material.HOPPER) {
                if (title.equals(miningTitle)) marketManager.sellAllCategory(player, MarketManager.MarketCategory.MADENCILIK);
                else if (title.equals(farmingTitle)) marketManager.sellAllCategory(player, MarketManager.MarketCategory.TARIM);
                else if (title.equals(huntingTitle)) marketManager.sellAllCategory(player, MarketManager.MarketCategory.AVCILIK);
                else if (title.equals(fishingTitle)) marketManager.sellAllCategory(player, MarketManager.MarketCategory.BALIKCILIK);
                marketManager.openCategoryGUI(player, 
                    title.equals(miningTitle) ? MarketManager.MarketCategory.MADENCILIK :
                    title.equals(farmingTitle) ? MarketManager.MarketCategory.TARIM :
                    title.equals(huntingTitle) ? MarketManager.MarketCategory.AVCILIK :
                    MarketManager.MarketCategory.BALIKCILIK);
                return;
            }
            if (!marketManager.getAllMaterials().contains(type)) return;
            int amount = 1;
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) amount = 64;
            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                plugin.getLogger().info("[Market] MarketListener: sell click detected player=" + player.getName() + " item=" + type + " amount=" + amount + " click=" + event.getClick());
                marketManager.sellToMarket(player, type, amount, 1.0);
            } else if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                marketManager.buyFromMarket(player, type, amount);
            }
            marketManager.openCategoryGUI(player, 
                title.equals(miningTitle) ? MarketManager.MarketCategory.MADENCILIK :
                title.equals(farmingTitle) ? MarketManager.MarketCategory.TARIM :
                title.equals(huntingTitle) ? MarketManager.MarketCategory.AVCILIK :
                MarketManager.MarketCategory.BALIKCILIK);
            return;
        }
        if (title.equals(miningTitle) || title.equals(farmingTitle) || title.equals(huntingTitle) || title.equals(fishingTitle)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Material type = clickedItem.getType();
            if (type == Material.ARROW) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.ANA);
                return;
            }
            if (type == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            if (!marketManager.getAllMaterials().contains(type)) return;
            int amount = 1;
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) amount = 64;
            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                plugin.getLogger().info("[Market] MarketListener: sell click detected player=" + player.getName() + " item=" + type + " amount=" + amount + " click=" + event.getClick());
                marketManager.sellToMarket(player, type, amount, 1.0);
            } else if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                marketManager.buyFromMarket(player, type, amount);
            }
            marketManager.openCategoryGUI(player, 
                title.equals(miningTitle) ? MarketManager.MarketCategory.MADENCILIK :
                title.equals(farmingTitle) ? MarketManager.MarketCategory.TARIM :
                title.equals(huntingTitle) ? MarketManager.MarketCategory.AVCILIK :
                MarketManager.MarketCategory.BALIKCILIK);
            return;
        }
        if (title.equals("§d§lMarket Görevleri")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            int slot = event.getRawSlot();
            if (clickedItem.getType() == Material.ARROW && slot == 27) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.ANA);
                return;
            }
            if (slot >= 10 && slot < 10 + marketManager.getDailyQuests().size()) {
                int questId = slot - 10;
                UUID uuid = player.getUniqueId();
                Set<Integer> claimed = marketManager.getPlayerQuestRewardClaimed().getOrDefault(uuid, new HashSet<>());
                Map<Integer, Integer> progress = marketManager.getPlayerQuestProgress(uuid);
                MarketManager.MarketQuest quest = marketManager.getDailyQuests().get(questId);
                int prog = progress.getOrDefault(questId, 0);
                    if (claimed.contains(questId)) {
                    me.example.util.MessageUtils.send(player, "§cBu görevin ödülünü zaten aldınız!");
                } else if (prog >= quest.getAmount()) {
                    marketManager.getPlayerQuestRewardClaimed().computeIfAbsent(uuid, k -> new HashSet<>()).add(questId);
                    progress.put(questId, quest.getAmount()); 
                    me.example.util.MessageUtils.send(player, "§aGörev ödülünü aldınız! " + me.example.util.MessageUtils.money(quest.getReward()));
                    marketManager.getPlugin().getEconomy().depositPlayer(player, quest.getReward());
                } else {
                    me.example.util.MessageUtils.send(player, "§eBu görevi tamamlamadan ödül alamazsınız.");
                }
                marketManager.openMarketQuestsGUI(player);
            }
            return;
        }
    String rankingTitle = plugin.getLocalization().msg("gui.ranking.title");
    if (title.equals(rankingTitle)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            int slot = event.getRawSlot();
            if (clickedItem.getType() == Material.ARROW) {
                marketManager.openCategoryGUI(player, MarketManager.MarketCategory.ANA);
                return;
            }
            if (slot >= 10 && slot < 15) {
                java.util.List<java.util.Map.Entry<java.util.UUID, Double>> topEarners = marketManager.getTopCategoryEarners(MarketManager.MarketCategory.MADENCILIK, 5);
                int idx = slot - 10;
                if (idx < topEarners.size()) {
                    java.util.UUID target = topEarners.get(idx).getKey();
                    String name = org.bukkit.Bukkit.getOfflinePlayer(target).getName();
                    double total = topEarners.get(idx).getValue();
                    player.sendMessage("§6[Market Sıralama] §e" + (name != null ? name : "Bilinmiyor") + " §7- Toplam Kazanç: §a" + String.format("%.2f ₺", total));
                }
            }
        }
    if (title.equals(mainMenuTitle)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            if (slot == 29) {
                marketManager.openMarketQuestsGUI(player);
                return;
            }
            if (slot == 31) {
                marketManager.openMarketStatsGUI(player);
                return;
            }
            if (slot == 33) {
                marketManager.openMarketLeaderboardGUI(player);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getMarketManager().assignQuestsIfNeeded(player);
    }
}

