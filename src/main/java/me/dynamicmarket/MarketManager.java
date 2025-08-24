package me.example;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import me.example.marketapi.MarketAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class MarketManager implements MarketAPI {
    private boolean removeExactItems(Player player, Material material, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        int toRemove = amount;
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount > toRemove) {
                    item.setAmount(stackAmount - toRemove);
                    player.getInventory().setItem(i, item);
                    return true;
                } else {
                    player.getInventory().setItem(i, null);
                    toRemove -= stackAmount;
                    if (toRemove == 0) return true;
                }
            }
        }
        return false; 
    }
    private EarningsStorage earningsStorage;
    public void initEarningsStorage(JavaPlugin plugin) {
        this.earningsStorage = new EarningsStorage(new java.io.File(plugin.getDataFolder(), "earnings.yml"));
    }
    private final Map<UUID, Double> dailyEarnings = new HashMap<>();
    private final Map<UUID, Double> allTimeEarnings = new HashMap<>();
    public Map<UUID, Double> getDailyEarnings() {
        return dailyEarnings;
    }

    public Map<UUID, Double> getAllTimeEarnings() {
        return allTimeEarnings;
    }
    public void resetDailyEarnings() {
        dailyEarnings.clear();
    }
    public void addSaleEarning(UUID playerUUID, double amount) {
        dailyEarnings.put(playerUUID, dailyEarnings.getOrDefault(playerUUID, 0.0) + amount);
        allTimeEarnings.put(playerUUID, allTimeEarnings.getOrDefault(playerUUID, 0.0) + amount);
        if (etkinlikAktif && System.currentTimeMillis() <= etkinlikBitis) {
            double bonusAmount = amount * etkinlikBonusCarpan;
            etkinlikKazanc.put(playerUUID, etkinlikKazanc.getOrDefault(playerUUID, 0.0) + bonusAmount);
        }
    }
    private boolean etkinlikAktif = false;
    private double etkinlikBonusCarpan = 1.0;
    private long etkinlikBaslangic = 0;
    private long etkinlikBitis = 0;
    private final Map<UUID, Double> etkinlikKazanc = new HashMap<>();
    public void etkinlikBaslat(double bonusCarpan, long sureMillis) {
        etkinlikAktif = true;
        etkinlikBonusCarpan = bonusCarpan;
        etkinlikBaslangic = System.currentTimeMillis();
        etkinlikBitis = etkinlikBaslangic + sureMillis;
        etkinlikKazanc.clear();
        sendMarketBroadcast("§eMarket Etkinliği başladı! Satışlarda bonus: x" + bonusCarpan);
    }
    public void etkinlikKontrolVeBitir() {
        if (!etkinlikAktif) return;
        if (System.currentTimeMillis() >= etkinlikBitis) {
            etkinlikAktif = false;
            sendMarketBroadcast("§cMarket Etkinliği sona erdi!");
            List<Map.Entry<UUID, Double>> topList = new ArrayList<>(etkinlikKazanc.entrySet());
            topList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int rank = 1;
            for (Map.Entry<UUID, Double> entry : topList) {
                if (rank > 3) break;
                org.bukkit.OfflinePlayer offline = plugin.getServer().getOfflinePlayer(entry.getKey());
                String playerName = offline != null && offline.getName() != null ? offline.getName() : entry.getKey().toString();
                Player p = plugin.getServer().getPlayer(entry.getKey());
                double odul = 1000 * rank; 
                plugin.getEconomy().depositPlayer(offline, odul);
                if (p != null) {
                    p.sendMessage("§6[Market Etkinliği] §aSıralama: " + rank + ". §eKazanç: " + String.format("%.2f", entry.getValue()) + " ₺ §bÖdül: " + odul + " ₺");
                }
                sendMarketBroadcast("§6[Market Etkinliği] §aSıralama: " + rank + ". §eOyuncu: §b" + playerName + " §eKazanç: " + String.format("%.2f", entry.getValue()) + " ₺ §bÖdül: " + odul + " ₺");
                rank++;
            }
        }
    }
    private final Map<String, Material> materialAliases = new HashMap<>();
    @Override
    public double getPrice(String itemId) {
        return 0.0; 
    }

    @Override
    public void setPrice(String itemId, double newPrice) {
    }

    
    public static void registerAPI(JavaPlugin plugin, MarketManager manager) {
        Bukkit.getServicesManager().register(MarketAPI.class, manager, plugin, ServicePriority.Normal);
    }
    private final String DATA_FILE = "marketdata.yml";
    public void loadData() {
        if (earningsStorage != null) {
            earningsStorage.loadEarnings(dailyEarnings, allTimeEarnings);
        }
        try {
            java.io.File file = new java.io.File(plugin.getDataFolder(), DATA_FILE);
            if (!file.exists()) return;
            org.bukkit.configuration.file.YamlConfiguration data = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            if (data.contains("taxProgress")) {
                for (String uuidStr : data.getConfigurationSection("taxProgress").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<MarketCategory, Double> map = new HashMap<>();
                    for (String cat : data.getConfigurationSection("taxProgress."+uuidStr).getKeys(false)) {
                        map.put(MarketCategory.valueOf(cat), data.getDouble("taxProgress."+uuidStr+"."+cat));
                    }
                    playerCategoryTaxProgress.put(uuid, map);
                }
            }
            if (data.contains("taxLevel")) {
                for (String uuidStr : data.getConfigurationSection("taxLevel").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<MarketCategory, Integer> map = new HashMap<>();
                    for (String cat : data.getConfigurationSection("taxLevel."+uuidStr).getKeys(false)) {
                        map.put(MarketCategory.valueOf(cat), data.getInt("taxLevel."+uuidStr+"."+cat));
                    }
                    playerCategoryTaxLevel.put(uuid, map);
                }
            }
            if (data.contains("questProgress")) {
                for (String uuidStr : data.getConfigurationSection("questProgress").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<Integer, Integer> map = new HashMap<>();
                    for (String qid : data.getConfigurationSection("questProgress."+uuidStr).getKeys(false)) {
                        map.put(Integer.parseInt(qid), data.getInt("questProgress."+uuidStr+"."+qid));
                    }
                    playerQuestProgress.put(uuid, map);
                }
            }
            if (data.contains("questClaimed")) {
                for (String uuidStr : data.getConfigurationSection("questClaimed").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    Set<Integer> set = new HashSet<>();
                    for (String qid : data.getConfigurationSection("questClaimed."+uuidStr).getKeys(false)) {
                        if (data.getBoolean("questClaimed."+uuidStr+"."+qid)) set.add(Integer.parseInt(qid));
                    }
                    playerQuestRewardClaimed.put(uuid, set);
                }
            }
            if (data.contains("categoryEarnings")) {
                for (String uuidStr : data.getConfigurationSection("categoryEarnings").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<MarketCategory, Double> map = new HashMap<>();
                    for (String cat : data.getConfigurationSection("categoryEarnings."+uuidStr).getKeys(false)) {
                        map.put(MarketCategory.valueOf(cat), data.getDouble("categoryEarnings."+uuidStr+"."+cat));
                    }
                    playerCategoryEarnings.put(uuid, map);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Market verileri yüklenirken hata: " + e.getMessage());
        }
    }
    public void saveData() {
        if (earningsStorage != null) {
            earningsStorage.saveEarnings(dailyEarnings, allTimeEarnings);
        }
        if (earningsStorage != null) {
            earningsStorage.saveEarnings(dailyEarnings, allTimeEarnings);
        }
        try {
            java.io.File file = new java.io.File(plugin.getDataFolder(), DATA_FILE);
            org.bukkit.configuration.file.YamlConfiguration data = new org.bukkit.configuration.file.YamlConfiguration();
            for (Map.Entry<UUID, Map<MarketCategory, Double>> entry : playerCategoryTaxProgress.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Map.Entry<MarketCategory, Double> cat : entry.getValue().entrySet()) {
                    data.set("taxProgress."+uuid+"."+cat.getKey().name(), cat.getValue());
                }
            }
            for (Map.Entry<UUID, Map<MarketCategory, Integer>> entry : playerCategoryTaxLevel.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Map.Entry<MarketCategory, Integer> cat : entry.getValue().entrySet()) {
                    data.set("taxLevel."+uuid+"."+cat.getKey().name(), cat.getValue());
                }
            }
            for (Map.Entry<UUID, Map<Integer, Integer>> entry : playerQuestProgress.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Map.Entry<Integer, Integer> q : entry.getValue().entrySet()) {
                    data.set("questProgress."+uuid+"."+q.getKey(), q.getValue());
                }
            }
            for (Map.Entry<UUID, Set<Integer>> entry : playerQuestRewardClaimed.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Integer qid : entry.getValue()) {
                    data.set("questClaimed."+uuid+"."+qid, true);
                }
            }
            for (Map.Entry<UUID, Map<MarketCategory, Double>> entry : playerCategoryEarnings.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Map.Entry<MarketCategory, Double> cat : entry.getValue().entrySet()) {
                    data.set("categoryEarnings."+uuid+"."+cat.getKey().name(), cat.getValue());
                }
            }
            data.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Market verileri kaydedilirken hata: " + e.getMessage());
        }
    }
    public Map<Integer, Integer> getPlayerQuestProgress(UUID uuid) {
        return playerQuestProgress.getOrDefault(uuid, new HashMap<>());
    }
    public DynamicMarket getPlugin() {
        return plugin;
    }
    public List<MarketQuest> getActiveQuests() {
        return activeQuests;
    }
    public Map<UUID, Set<Integer>> getPlayerQuestRewardClaimed() {
        return playerQuestRewardClaimed;
    }
    public List<Map.Entry<UUID, Integer>> getTopSellers(int limit) {
        Map<UUID, Integer> sellerTotals = new HashMap<>();
        for (Map.Entry<UUID, Map<Material, Integer>> entry : playerSold.entrySet()) {
            int total = 0;
            for (int v : entry.getValue().values()) total += v;
            sellerTotals.put(entry.getKey(), total);
        }
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(sellerTotals.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }
    public int sellAllCategory(Player player, MarketCategory category) {
        List<Material> categoryMaterials;
        if (category == MarketCategory.MADENCILIK) categoryMaterials = getMiningMaterials();
        else if (category == MarketCategory.TARIM) categoryMaterials = getFarmingMaterials();
        else if (category == MarketCategory.AVCILIK) categoryMaterials = getHuntingMaterials();
        else if (category == MarketCategory.BALIKCILIK) categoryMaterials = getFishingMaterials();
        else return 0;

        int totalSoldCount = 0;
        for (Material mat : categoryMaterials) {
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() == mat && item.getAmount() > 0) {
                    int amount = item.getAmount();
                    boolean result = sellToMarket(player, mat, amount, 1.0);
                    if (result) totalSoldCount += amount;
                }
            }
        }
        if (totalSoldCount > 0) {
            player.sendMessage("§aKategoriye ait tüm ürünler satıldı! Toplam: " + totalSoldCount);
        } else {
            player.sendMessage("§cEnvanterinizde bu kategoriye ait satılabilir ürün yok!");
        }
        return totalSoldCount;
    }
    private final Map<Material, Integer> totalBought = new HashMap<>();
    private final Map<Material, Integer> totalSold = new HashMap<>();
    private final Map<UUID, Map<Material, Integer>> playerBought = new HashMap<>();
    private final Map<UUID, Map<Material, Integer>> playerSold = new HashMap<>();
    private final Map<UUID, Map<MarketCategory, Double>> playerCategoryEarnings = new HashMap<>();
    private final Map<UUID, Map<MarketCategory, Double>> weeklyCategoryEarnings = new HashMap<>();
    private final Map<UUID, Map<MarketCategory, Double>> monthlyCategoryEarnings = new HashMap<>();
    private final Map<UUID, Map<MarketCategory, Double>> alltimeCategoryEarnings = new HashMap<>();
    private final Map<UUID, Map<MarketCategory, Double>> playerCategoryTaxProgress = new HashMap<>(); 
    private final Map<UUID, Map<MarketCategory, Integer>> playerCategoryTaxLevel = new HashMap<>(); 
    private final int[] TAX_LEVEL_THRESHOLDS = {100, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};
    private final String[] TAX_LEVEL_REWARDS = {"500 ₺", "1.000 ₺", "2.500 ₺", "5.000 ₺", "10.000 ₺", "25.000 ₺", "50.000 ₺", "100.000 ₺", "VIP"};

    private final DynamicMarket plugin;
    private final Map<Material, Double> buyPrices;
    private final Map<Material, Double> sellPrices;
    private final Map<Material, Double> basePrices;
    private final Set<UUID> sellingPlayers = Collections.newSetFromMap(new IdentityHashMap<UUID, Boolean>());
    private final Map<Material, Integer> stocks; 
    private final Map<Material, LinkedList<Double>> priceHistory; 
    private final List<Material> miningMaterials;
    private final List<Material> farmingMaterials;
    private final List<Material> huntingMaterials;
    private final List<Material> fishingMaterials;
    private final double commissionRate = 0.05; 
    private final Set<Material> blockedMaterials = new HashSet<>(); 
    private final Map<UUID, Set<Material>> playerLicenses = new HashMap<>(); 

    public enum MarketCategory {
        ANA, MADENCILIK, TARIM, AVCILIK, BALIKCILIK
    }

    public MarketManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.buyPrices = new HashMap<>();
        this.sellPrices = new HashMap<>();
        this.basePrices = new HashMap<>();
        this.stocks = new HashMap<>();
        this.priceHistory = new HashMap<>();
        this.miningMaterials = new ArrayList<>();
        this.farmingMaterials = new ArrayList<>();
        this.huntingMaterials = new ArrayList<>();
        this.fishingMaterials = new ArrayList<>();

        initializeMarketMaterials();
        loadPricesFromConfig();
        initializeStocks();
        initializePriceHistory();
        initializeQuestPool(); 
        startQuestRefreshScheduler(); 
    }
    
    private void initializeMarketMaterials() {
        miningMaterials.add(Material.COAL);
        miningMaterials.add(Material.IRON_INGOT);
        miningMaterials.add(Material.GOLD_INGOT);
        miningMaterials.add(Material.DIAMOND);
        miningMaterials.add(Material.EMERALD);
        miningMaterials.add(Material.NETHERITE_INGOT);
        miningMaterials.add(Material.COPPER_INGOT);
        miningMaterials.add(Material.REDSTONE);
        miningMaterials.add(Material.LAPIS_LAZULI);
        miningMaterials.add(Material.QUARTZ);
        materialAliases.put("coal", Material.COAL);
        materialAliases.put("kömür", Material.COAL);
        materialAliases.put("iron", Material.IRON_INGOT);
        materialAliases.put("demir", Material.IRON_INGOT);
        materialAliases.put("gold", Material.GOLD_INGOT);
        materialAliases.put("altın", Material.GOLD_INGOT);
        materialAliases.put("diamond", Material.DIAMOND);
        materialAliases.put("elmas", Material.DIAMOND);
        materialAliases.put("emerald", Material.EMERALD);
        materialAliases.put("zümrüt", Material.EMERALD);
        materialAliases.put("netherite", Material.NETHERITE_INGOT);
        materialAliases.put("copper", Material.COPPER_INGOT);
        materialAliases.put("bakır", Material.COPPER_INGOT);
        materialAliases.put("redstone", Material.REDSTONE);
        materialAliases.put("lapis", Material.LAPIS_LAZULI);
        materialAliases.put("lapis_lazuli", Material.LAPIS_LAZULI);
        materialAliases.put("quartz", Material.QUARTZ);
        farmingMaterials.add(Material.WHEAT);
        farmingMaterials.add(Material.POTATO);
        farmingMaterials.add(Material.CARROT);
        farmingMaterials.add(Material.BEETROOT);
        farmingMaterials.add(Material.MELON_SLICE);
        farmingMaterials.add(Material.PUMPKIN);
        farmingMaterials.add(Material.SUGAR_CANE);
        farmingMaterials.add(Material.BAMBOO);
        farmingMaterials.add(Material.CACTUS);
    farmingMaterials.add(Material.APPLE);
    farmingMaterials.add(Material.NETHER_WART);
    farmingMaterials.add(Material.GLOW_BERRIES);
    farmingMaterials.add(Material.SWEET_BERRIES);
    farmingMaterials.add(Material.COCOA_BEANS);
    farmingMaterials.add(Material.CHORUS_FRUIT);
        materialAliases.put("wheat", Material.WHEAT);
        materialAliases.put("buğday", Material.WHEAT);
        materialAliases.put("potato", Material.POTATO);
        materialAliases.put("patates", Material.POTATO);
        materialAliases.put("carrot", Material.CARROT);
        materialAliases.put("havuç", Material.CARROT);
        materialAliases.put("beetroot", Material.BEETROOT);
        materialAliases.put("melon", Material.MELON_SLICE);
        materialAliases.put("kavun", Material.MELON_SLICE);
        materialAliases.put("pumpkin", Material.PUMPKIN);
        materialAliases.put("balkabağı", Material.PUMPKIN);
        materialAliases.put("sugar_cane", Material.SUGAR_CANE);
        materialAliases.put("şeker kamışı", Material.SUGAR_CANE);
        materialAliases.put("bamboo", Material.BAMBOO);
        materialAliases.put("bambu", Material.BAMBOO);
        materialAliases.put("cactus", Material.CACTUS);
        materialAliases.put("kaktüs", Material.CACTUS);

    materialAliases.put("apple", Material.APPLE);
    materialAliases.put("elma", Material.APPLE);
    materialAliases.put("nether_wart", Material.NETHER_WART);
    materialAliases.put("netherwart", Material.NETHER_WART);
    materialAliases.put("nether mantarı", Material.NETHER_WART);
    materialAliases.put("glow_berries", Material.GLOW_BERRIES);
    materialAliases.put("parlayan dut", Material.GLOW_BERRIES);
    materialAliases.put("sweet_berries", Material.SWEET_BERRIES);
    materialAliases.put("tatlı dut", Material.SWEET_BERRIES);
    materialAliases.put("cocoa_beans", Material.COCOA_BEANS);
    materialAliases.put("kakao", Material.COCOA_BEANS);
    materialAliases.put("chorus_fruit", Material.CHORUS_FRUIT);
    materialAliases.put("chorus", Material.CHORUS_FRUIT);
        huntingMaterials.add(Material.ROTTEN_FLESH);
        huntingMaterials.add(Material.BONE);
        huntingMaterials.add(Material.STRING);
        huntingMaterials.add(Material.SPIDER_EYE);
        huntingMaterials.add(Material.GUNPOWDER);
        huntingMaterials.add(Material.FEATHER);
        huntingMaterials.add(Material.LEATHER);
        huntingMaterials.add(Material.RABBIT_FOOT);
        huntingMaterials.add(Material.MUTTON);
    huntingMaterials.add(Material.BEEF);
    huntingMaterials.add(Material.CHICKEN);
    huntingMaterials.add(Material.COOKED_CHICKEN);
    huntingMaterials.add(Material.PORKCHOP);
    huntingMaterials.add(Material.COOKED_PORKCHOP);
        materialAliases.put("rotten_flesh", Material.ROTTEN_FLESH);
        materialAliases.put("çürük et", Material.ROTTEN_FLESH);
        materialAliases.put("bone", Material.BONE);
        materialAliases.put("kemik", Material.BONE);
        materialAliases.put("string", Material.STRING);
        materialAliases.put("ip", Material.STRING);
        materialAliases.put("spider_eye", Material.SPIDER_EYE);
        materialAliases.put("örümcek gözü", Material.SPIDER_EYE);
        materialAliases.put("gunpowder", Material.GUNPOWDER);
        materialAliases.put("barut", Material.GUNPOWDER);
        materialAliases.put("feather", Material.FEATHER);
        materialAliases.put("tüy", Material.FEATHER);
        materialAliases.put("leather", Material.LEATHER);
        materialAliases.put("deri", Material.LEATHER);
        materialAliases.put("rabbit_foot", Material.RABBIT_FOOT);
        materialAliases.put("mutton", Material.MUTTON);
        materialAliases.put("koyun eti", Material.MUTTON);
    materialAliases.put("beef", Material.BEEF);
    materialAliases.put("sığır eti", Material.BEEF);
    materialAliases.put("chicken", Material.CHICKEN);
    materialAliases.put("çiğ tavuk", Material.CHICKEN);
    materialAliases.put("raw_chicken", Material.CHICKEN);
    materialAliases.put("cooked_chicken", Material.COOKED_CHICKEN);
    materialAliases.put("pişmiş tavuk", Material.COOKED_CHICKEN);
    materialAliases.put("porkchop", Material.PORKCHOP);
    materialAliases.put("çiğ domuz eti", Material.PORKCHOP);
    materialAliases.put("raw_porkchop", Material.PORKCHOP);
    materialAliases.put("cooked_porkchop", Material.COOKED_PORKCHOP);
    materialAliases.put("pişmiş domuz eti", Material.COOKED_PORKCHOP);
        fishingMaterials.add(Material.COD);
        fishingMaterials.add(Material.SALMON);
        fishingMaterials.add(Material.TROPICAL_FISH);
        fishingMaterials.add(Material.PUFFERFISH);
        fishingMaterials.add(Material.KELP);
        fishingMaterials.add(Material.SEA_PICKLE);
        fishingMaterials.add(Material.PRISMARINE_SHARD);
        fishingMaterials.add(Material.PRISMARINE_CRYSTALS);
        fishingMaterials.add(Material.NAUTILUS_SHELL);
        fishingMaterials.add(Material.HEART_OF_THE_SEA);
        materialAliases.put("cod", Material.COD);
        materialAliases.put("morina", Material.COD);
        materialAliases.put("salmon", Material.SALMON);
        materialAliases.put("somon", Material.SALMON);
        materialAliases.put("tropical_fish", Material.TROPICAL_FISH);
        materialAliases.put("tropikal balık", Material.TROPICAL_FISH);
        materialAliases.put("pufferfish", Material.PUFFERFISH);
        materialAliases.put("balon balığı", Material.PUFFERFISH);
        materialAliases.put("kelp", Material.KELP);
        materialAliases.put("deniz yosunu", Material.KELP);
        materialAliases.put("sea_pickle", Material.SEA_PICKLE);
        materialAliases.put("deniz turşusu", Material.SEA_PICKLE);
        materialAliases.put("prismarine_shard", Material.PRISMARINE_SHARD);
        materialAliases.put("prismarine parçası", Material.PRISMARINE_SHARD);
        materialAliases.put("prismarine_crystals", Material.PRISMARINE_CRYSTALS);
        materialAliases.put("prismarine kristali", Material.PRISMARINE_CRYSTALS);
        materialAliases.put("nautilus_shell", Material.NAUTILUS_SHELL);
        materialAliases.put("nautilus kabuğu", Material.NAUTILUS_SHELL);
        materialAliases.put("heart_of_the_sea", Material.HEART_OF_THE_SEA);
        materialAliases.put("denizin kalbi", Material.HEART_OF_THE_SEA);
    }
    public Material getMaterialByAlias(String input) {
        if (input == null) return null;
        String normalized = input.toLowerCase().replace(" ", "_");
        Material mat = materialAliases.get(normalized);
        if (mat != null) return mat;
        try {
            mat = Material.valueOf(input.toUpperCase().replace(" ", "_"));
            return mat;
        } catch (IllegalArgumentException e) {
        }
        mat = Material.matchMaterial(input.toUpperCase());
        return mat;
    }
    public String getClosestMaterialName(String input) {
        input = input.toLowerCase().replace(" ", "_");
        int minDist = Integer.MAX_VALUE;
        String closest = null;
        for (String key : materialAliases.keySet()) {
            int dist = levenshtein(input, key);
            if (dist < minDist) {
                minDist = dist;
                closest = key;
            }
        }
        for (Material mat : Material.values()) {
            String enumName = mat.name().toLowerCase();
            int dist = levenshtein(input, enumName);
            if (dist < minDist) {
                minDist = dist;
                closest = enumName;
            }
        }
        return closest;
    }
    private int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    
    private void initializeStocks() {
        for (Material m : getAllMaterials()) stocks.put(m, 100);
    }

    private void initializePriceHistory() {
        for (Material m : getAllMaterials()) {
            LinkedList<Double> history = new LinkedList<>();
            double price = getBuyPrice(m);
            for (int i = 0; i < 5; i++) history.add(price);
            priceHistory.put(m, history);
        }
    }
    
    private void loadPricesFromConfig() {
        FileConfiguration config = plugin.getConfig();
        Map<Material, Double> defaultPrices = new HashMap<>();
        defaultPrices.put(Material.COAL, 5.0);
        defaultPrices.put(Material.IRON_INGOT, 15.0);
        defaultPrices.put(Material.GOLD_INGOT, 25.0);
        defaultPrices.put(Material.DIAMOND, 100.0);
        defaultPrices.put(Material.EMERALD, 150.0);
        defaultPrices.put(Material.NETHERITE_INGOT, 500.0);
        defaultPrices.put(Material.COPPER_INGOT, 8.0);
        defaultPrices.put(Material.REDSTONE, 3.0);
        defaultPrices.put(Material.LAPIS_LAZULI, 12.0);
        defaultPrices.put(Material.QUARTZ, 6.0);
        
        for (Material material : getAllMaterials()) {
            String path = "prices." + material.name().toLowerCase();
            double basePrice = config.getDouble(path, defaultPrices.getOrDefault(material, 10.0));
            
            basePrices.put(material, basePrice);
            buyPrices.put(material, basePrice * 1.2); 
            sellPrices.put(material, basePrice * 0.8); 
            config.set(path, basePrice);
        }
        
        plugin.saveConfig();
    }
    
    public void openMarketGUI(Player player) {
        openCategoryGUI(player, MarketCategory.ANA);
    }

    public void openCategoryGUI(Player player, MarketCategory category) {
        int size = 54;
        Inventory inv;
        if (category == MarketCategory.ANA) {
            inv = Bukkit.createInventory(null, size, plugin.getLocalization().msg("gui.main.title"));
            Material[] glassTypes = new Material[] {
                Material.BLUE_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE,
                Material.WHITE_STAINED_GLASS_PANE
            };
            java.util.Random glassRand = new java.util.Random();
            for (int i = 0; i < 54; i++) {
                Material glassMat = glassTypes[glassRand.nextInt(glassTypes.length)];
                ItemStack glass = new ItemStack(glassMat);
                ItemMeta meta = glass.getItemMeta();
                if (meta != null) { meta.setDisplayName(" "); glass.setItemMeta(meta); }
                inv.setItem(i, glass);
            }
            ItemStack leftBanner = new ItemStack(Material.CYAN_BANNER);
            ItemMeta leftBannerMeta = leftBanner.getItemMeta();
            if (leftBannerMeta != null) {
                leftBannerMeta.setDisplayName(plugin.getLocalization().msg("gui.main.banner.left"));
                leftBanner.setItemMeta(leftBannerMeta);
            }
            inv.setItem(3, leftBanner);

            ItemStack rightBanner = new ItemStack(Material.MAGENTA_BANNER);
            ItemMeta rightBannerMeta = rightBanner.getItemMeta();
            if (rightBannerMeta != null) {
                rightBannerMeta.setDisplayName(plugin.getLocalization().msg("gui.main.banner.right"));
                rightBanner.setItemMeta(rightBannerMeta);
            }
            inv.setItem(5, rightBanner);
            ItemStack titleBanner = new ItemStack(Material.YELLOW_BANNER);
            ItemMeta titleMeta = titleBanner.getItemMeta();
            if (titleMeta != null) {
                titleMeta.setDisplayName(plugin.getLocalization().msg("gui.main.banner.center"));
                String centerLore = plugin.getLocalization().msg("gui.main.banner.center.lore");
                titleMeta.setLore(Arrays.asList(centerLore.split("\n")));
                titleBanner.setItemMeta(titleMeta);
            }
            inv.setItem(4, titleBanner);
            ItemStack miningIcon = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta miningMeta = miningIcon.getItemMeta();
            if (miningMeta != null) {
                miningMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.mining.name"));
                String miningLore = plugin.getLocalization().msg("gui.main.icon.mining.lore");
                miningMeta.setLore(Arrays.asList(miningLore.split("\n")));
                miningIcon.setItemMeta(miningMeta);
            }

            inv.setItem(19, miningIcon);

            ItemStack farmingIcon = new ItemStack(Material.WHEAT);
            ItemMeta farmingMeta = farmingIcon.getItemMeta();
            if (farmingMeta != null) {
                farmingMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.farming.name"));
                String farmingLore = plugin.getLocalization().msg("gui.main.icon.farming.lore");
                farmingMeta.setLore(Arrays.asList(farmingLore.split("\n")));
                farmingIcon.setItemMeta(farmingMeta);
            }
            inv.setItem(22, farmingIcon);
            ItemStack huntingIcon = new ItemStack(Material.BOW);
            ItemMeta huntingMeta = huntingIcon.getItemMeta();
            if (huntingMeta != null) {
                huntingMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.hunting.name"));
                String huntingLore = plugin.getLocalization().msg("gui.main.icon.hunting.lore");
                huntingMeta.setLore(Arrays.asList(huntingLore.split("\n")));
                huntingIcon.setItemMeta(huntingMeta);
            }
            inv.setItem(25, huntingIcon);
            ItemStack fishingIcon = new ItemStack(Material.FISHING_ROD);
            ItemMeta fishingMeta = fishingIcon.getItemMeta();
            if (fishingMeta != null) {
                fishingMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.fishing.name"));
                String fishingLore = plugin.getLocalization().msg("gui.main.icon.fishing.lore");
                fishingMeta.setLore(Arrays.asList(fishingLore.split("\n")));
                fishingIcon.setItemMeta(fishingMeta);
            }
            inv.setItem(31, fishingIcon);
            ItemStack questsIcon = new ItemStack(Material.LIGHT_BLUE_BANNER);
            org.bukkit.inventory.meta.BannerMeta bannerMeta = (org.bukkit.inventory.meta.BannerMeta) questsIcon.getItemMeta();
            if (bannerMeta != null) {
                bannerMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.quests.name"));
                String questsLore = plugin.getLocalization().msg("gui.main.icon.quests.lore");
                bannerMeta.setLore(Arrays.asList(questsLore.split("\n")));
                bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(org.bukkit.DyeColor.WHITE, org.bukkit.block.banner.PatternType.GLOBE));
                bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(org.bukkit.DyeColor.PINK, org.bukkit.block.banner.PatternType.BORDER));
                questsIcon.setItemMeta(bannerMeta);
            }
            inv.setItem(29, questsIcon);
            ItemStack statsIcon = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta statsMeta = statsIcon.getItemMeta();
            if (statsMeta != null) {
                statsMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.stats.name"));
                String statsLore = plugin.getLocalization().msg("gui.main.icon.stats.lore");
                statsMeta.setLore(Arrays.asList(statsLore.split("\n")));
                statsIcon.setItemMeta(statsMeta);
            }
            inv.setItem(40, statsIcon);
            ItemStack leaderboardIcon = new ItemStack(Material.NETHER_STAR);
            ItemMeta leaderboardMeta = leaderboardIcon.getItemMeta();
            if (leaderboardMeta != null) {
                leaderboardMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.leaderboard.name"));
                boolean isMaintenanceMode = plugin.getConfig().getBoolean("rankings.maintenance_mode", false);
                String loreKey = isMaintenanceMode ? "gui.main.icon.leaderboard.lore.maintenance" : "gui.main.icon.leaderboard.lore.normal";
                String leaderboardLore = plugin.getLocalization().msg(loreKey);
                leaderboardMeta.setLore(Arrays.asList(leaderboardLore.split("\n")));
                leaderboardIcon.setItemMeta(leaderboardMeta);
            }
            inv.setItem(33, leaderboardIcon);
            ItemStack playerInfo = new ItemStack(Material.SUNFLOWER);
            ItemMeta playerMeta = playerInfo.getItemMeta();
            if (playerMeta != null) {
                Map<String, String> playerPlaceholders = new HashMap<>();
                playerPlaceholders.put("%player%", player.getName());
                playerMeta.setDisplayName(plugin.getLocalization().formatMsg("gui.main.playerinfo.name", playerPlaceholders));
                
                List<String> lore = new ArrayList<>();
                Map<String, String> balancePlaceholders = new HashMap<>();
                balancePlaceholders.put("%balance%", String.format("%.2f", plugin.getEconomy().getBalance(player)));
                lore.add(plugin.getLocalization().formatMsg("gui.main.playerinfo.lore.balance", balancePlaceholders));
                double totalEarnings = 0;
                Map<MarketCategory, Double> earnings = getPlayerCategoryEarnings(player.getUniqueId());
                for (double earning : earnings.values()) totalEarnings += earning;
                
                Map<String, String> totalPlaceholders = new HashMap<>();
                totalPlaceholders.put("%total%", String.format("%.2f", totalEarnings));
                lore.add(plugin.getLocalization().formatMsg("gui.main.playerinfo.lore.total", totalPlaceholders));
                
                playerMeta.setLore(lore);
                playerInfo.setItemMeta(playerMeta);
            }
            inv.setItem(13, playerInfo);
            ItemStack closeIcon = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = closeIcon.getItemMeta();
            if (closeMeta != null) {
                closeMeta.setDisplayName(plugin.getLocalization().msg("gui.main.icon.close.name"));
                String closeLore = plugin.getLocalization().msg("gui.main.icon.close.lore");
                closeMeta.setLore(Arrays.asList(closeLore.split("\n")));
                closeIcon.setItemMeta(closeMeta);
            }
            inv.setItem(49, closeIcon);

            player.openInventory(inv);
            return;
        }
        List<Material> materials = new ArrayList<>();
        String categoryName = "";
        if (category == MarketCategory.MADENCILIK) {
            materials = miningMaterials;
            categoryName = plugin.getLocalization().msg("category.mining");
        } else if (category == MarketCategory.TARIM) {
            materials = farmingMaterials;
            categoryName = plugin.getLocalization().msg("category.farming");
        } else if (category == MarketCategory.AVCILIK) {
            materials = huntingMaterials;
            categoryName = plugin.getLocalization().msg("category.hunting");
        } else if (category == MarketCategory.BALIKCILIK) {
            materials = fishingMaterials;
            categoryName = plugin.getLocalization().msg("category.fishing");
        }
        
        Map<String, String> titlePlaceholders = new HashMap<>();
        titlePlaceholders.put("%category%", categoryName);
        String title = plugin.getLocalization().formatMsg("gui.category.title", titlePlaceholders);
        inv = Bukkit.createInventory(null, size, title);
        Material[] glassTypes = new Material[] {
            Material.BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE
        };
        java.util.Random glassRand = new java.util.Random();
        for (int i = 0; i < 54; i++) {
            Material glassMat = glassTypes[glassRand.nextInt(glassTypes.length)];
            ItemStack glass = new ItemStack(glassMat);
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) { meta.setDisplayName(" "); glass.setItemMeta(meta); }
            inv.setItem(i, glass);
        }
        ItemStack catTitle = new ItemStack(Material.NAME_TAG);
        ItemMeta catMeta = catTitle.getItemMeta();
        if (catMeta != null) {
            String catHdrName = plugin.getLocalization().formatMsg("gui.category.header.name", Map.of("%category%", categoryToString(category)));
            catMeta.setDisplayName(catHdrName);
            String headerLore = plugin.getLocalization().msg("gui.category.header.lore");
            if (headerLore != null) catMeta.setLore(Arrays.asList(headerLore.split("\n")));
            catTitle.setItemMeta(catMeta);
        }
        inv.setItem(4, catTitle);
        ItemStack balance = new ItemStack(Material.SUNFLOWER);
        ItemMeta balanceMeta = balance.getItemMeta();
        if (balanceMeta != null) {
            Map<String, String> balancePlaceholders = Map.of("%balance%", String.format("%.2f", plugin.getEconomy().getBalance(player)));
            balanceMeta.setDisplayName(plugin.getLocalization().formatMsg("gui.category.balance", balancePlaceholders));
            balance.setItemMeta(balanceMeta);
        }
        inv.setItem(49, balance);
        ItemStack stats = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = stats.getItemMeta();
            if (statsMeta != null) {
            String catName = categoryToString(category);
            statsMeta.setDisplayName(plugin.getLocalization().formatMsg("gui.category.stats.name", Map.of("%category%", catName)));
            List<String> lore = new ArrayList<>();
            double earnings = getPlayerCategoryEarnings(player.getUniqueId()).getOrDefault(category, 0.0);
            lore.add(plugin.getLocalization().formatMsg("gui.category.stats.lore.total", Map.of("%earnings%", String.format("%.2f", earnings))));
            UUID uuid = player.getUniqueId();
            int level = playerCategoryTaxLevel.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(category, 0);
            double progress = playerCategoryTaxProgress.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(category, 0.0);
            int nextThreshold = (level < TAX_LEVEL_THRESHOLDS.length) ? TAX_LEVEL_THRESHOLDS[level] : TAX_LEVEL_THRESHOLDS[TAX_LEVEL_THRESHOLDS.length-1];
            String reward = TAX_LEVEL_REWARDS[Math.min(level, TAX_LEVEL_REWARDS.length-1)];
            lore.add(plugin.getLocalization().formatMsg("gui.category.stats.lore.pass.level", Map.of("%level%", String.valueOf(level))));
            lore.add(plugin.getLocalization().formatMsg("gui.category.stats.lore.pass.progress", Map.of("%progress%", String.format("%.2f", progress), "%next%", String.valueOf(nextThreshold))));
            int barLength = 20;
            double percent = Math.min(1.0, progress / nextThreshold);
            int filled = (int)(barLength * percent);
            StringBuilder bar = new StringBuilder("§8[");
            for (int i = 0; i < barLength; i++) bar.append(i < filled ? "§a|" : "§7|");
            bar.append("§8]");
            lore.add(bar.toString());
            lore.add(plugin.getLocalization().formatMsg("gui.category.stats.lore.next_reward", Map.of("%reward%", reward)));
            statsMeta.setLore(lore);
            stats.setItemMeta(statsMeta);
        }
        inv.setItem(48, stats);
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(plugin.getLocalization().msg("gui.common.back"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);
        ItemStack sellAll = new ItemStack(Material.HOPPER);
        ItemMeta sellAllMeta = sellAll.getItemMeta();
        if (sellAllMeta != null) {
            sellAllMeta.setDisplayName(plugin.getLocalization().msg("gui.common.sellall.name"));
            String sellAllLoreMsg = plugin.getLocalization().msg("gui.common.sellall.lore");
            if (sellAllLoreMsg != null) sellAllMeta.setLore(Arrays.asList(sellAllLoreMsg.split("\n")));
            sellAll.setItemMeta(sellAllMeta);
        }
        inv.setItem(40, sellAll);
        ItemStack baltop = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta baltopMeta = baltop.getItemMeta();
        if (baltopMeta != null) {
            baltopMeta.setDisplayName(plugin.getLocalization().msg("gui.category.baltop.name"));
            List<String> lore = new ArrayList<>();
            int rank = 1;
            for (Map.Entry<UUID, Integer> entry : getTopSellers(5)) {
                String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                lore.add("§7" + rank + ". §b" + (name != null ? name : plugin.getLocalization().msg("gui.ranking.no_data")) + " §f(" + entry.getValue() + ")");
                rank++;
            }
            if (lore.isEmpty()) lore.add("§8Veri yok");
            baltopMeta.setLore(lore);
            baltop.setItemMeta(baltopMeta);
        }
        inv.setItem(13, baltop);
        int[] productSlots = {10,11,12,14,15,16,19,20,21,23,24,25,28,29,30,32,33,34};
        int slotIdx = 0;
        for (Material material : materials) {
            if (slotIdx >= productSlots.length) break;
            int slot = productSlots[slotIdx++];
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = formatMaterialName(material);
                String trend = "§7";
                LinkedList<Double> history = priceHistory.getOrDefault(material, new LinkedList<>());
                if (history.size() >= 2) {
                    double last = history.get(history.size()-1);
                    double prev = history.get(history.size()-2);
                    if (last > prev) trend = "§a▲";
                    else if (last < prev) trend = "§c▼";
                    else trend = "§e■";
                }
                meta.setDisplayName("§e" + displayName + " " + trend);
                List<String> lore = new ArrayList<>();
                double buy = getBuyPrice(material);
                double sell = getSellPrice(material);
                int stock = stocks.getOrDefault(material, 0);
                lore.add(plugin.getLocalization().formatMsg("gui.item.lore.buy", Map.of("%buy%", String.format("%.2f", buy))));
                lore.add(plugin.getLocalization().formatMsg("gui.item.lore.sell", Map.of("%sell%", String.format("%.2f", sell))));
                lore.add(plugin.getLocalization().formatMsg("gui.item.lore.stock", Map.of("%stock%", String.valueOf(stock))));
                lore.add(plugin.getLocalization().formatMsg("gui.item.lore.commission", Map.of("%commission%", String.valueOf((int)(commissionRate*100)))));
                if (history.size() > 1) {
                    StringBuilder hist = new StringBuilder();
                    for (double h : history) {
                        if (hist.length() > 0) hist.append(" ");
                        hist.append(String.format("%.0f", h));
                    }
                    lore.add(plugin.getLocalization().formatMsg("gui.item.lore.history", Map.of("%history%", hist.toString())));
                }
                lore.add("");
                lore.add(plugin.getLocalization().msg("gui.item.lore.actions.sell"));
                lore.add(plugin.getLocalization().msg("gui.item.lore.actions.buy"));
                lore.add(plugin.getLocalization().msg("gui.item.lore.actions.shift"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
        }

        player.openInventory(inv);
    }
    
    public static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (formatted.length() > 0) formatted.append(" ");
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return formatted.toString();
    }
    
    public void fluctuatePrices() {
        Random random = new Random();
        for (Material material : getAllMaterials()) {
            double basePrice = basePrices.get(material);
            int stock = stocks.getOrDefault(material, 100);
            double stockEffect = 1.0;
            if (stock < 20) stockEffect = 1.3;
            else if (stock < 50) stockEffect = 1.1;
            else if (stock > 200) stockEffect = 0.8;
            else if (stock > 120) stockEffect = 0.9;
            double fluctuation = (random.nextDouble() - 0.5) * 0.4;
            double newBasePrice = basePrice * (1 + fluctuation) * stockEffect;
            double minPrice = basePrice * 0.5;
            double maxPrice = basePrice * 2.0;
            newBasePrice = Math.max(minPrice, Math.min(maxPrice, newBasePrice));

            double dynamicSellPrice = newBasePrice * 0.8;
            double dynamicBuyPrice = dynamicSellPrice * 10.0;
            sellPrices.put(material, dynamicSellPrice);
            buyPrices.put(material, dynamicBuyPrice);
            LinkedList<Double> history = priceHistory.getOrDefault(material, new LinkedList<>());
            if (history.size() >= 5) history.removeFirst();
            history.add(dynamicBuyPrice); 
            priceHistory.put(material, history);
        }
        plugin.getLogger().info("Market fiyatları güncellendi!");
    }
    
    
    public void adjustPricesBasedOnStock() {
        fluctuatePrices();
    }
    
    public boolean buyFromMarket(Player player, Material material, int amount) {
        int stock = stocks.getOrDefault(material, 0);
        if (stock < amount) {
            player.sendMessage(plugin.getLocalization().getPrefix() + plugin.getLocalization().msg("not_enough_stock").replace("%stock%", String.valueOf(stock)));
            return false;
        }
        double totalCost = getBuyPrice(material) * amount;
        double commission = totalCost * commissionRate;
        double totalWithCommission = totalCost + commission;

        if (plugin.getEconomy().getBalance(player) < totalWithCommission) {
            player.sendMessage(plugin.getLocalization().getPrefix() + plugin.getLocalization().msg("not_enough_balance").replace("%amount%", String.format("%.2f", totalWithCommission)));
            return false;
        }

        ItemStack item = new ItemStack(material, amount);
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getLocalization().getPrefix() + plugin.getLocalization().msg("not_enough_inventory_space"));
            return false;
        }

        plugin.getEconomy().withdrawPlayer(player, totalWithCommission);
        player.getInventory().addItem(item);
        stocks.put(material, stock - amount);
    String buyMsg = plugin.getLocalization().msgWithFallback("bought_success")
            .replace("%amount%", String.valueOf(amount))
            .replace("%item%", formatMaterialName(material))
            .replace("%cost%", String.format("%.2f", totalWithCommission))
            .replace("%commission%", String.format("%.2f", commission));
    player.sendMessage(plugin.getLocalization().getPrefix() + buyMsg);
        totalBought.put(material, totalBought.getOrDefault(material, 0) + amount);
        UUID uuid = player.getUniqueId();
        playerBought.putIfAbsent(uuid, new HashMap<>());
        Map<Material, Integer> pMap = playerBought.get(uuid);
        pMap.put(material, pMap.getOrDefault(material, 0) + amount);
        if (amount >= 64) {
            String bulk = plugin.getLocalization().msg("broadcast.bulk_buy")
                .replace("%player%", player.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", formatMaterialName(material));
            Bukkit.broadcastMessage(plugin.getLocalization().getPrefix() + bulk);
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), "entity.player.levelup", 1f, 1.2f));
        } else {
            player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1f, 1.2f);
        }
        List<Map.Entry<Material, Integer>> topBoughtList = new ArrayList<>(totalBought.entrySet());
        topBoughtList.sort((a, b) -> b.getValue() - a.getValue());
        if (!topBoughtList.isEmpty() && topBoughtList.get(0).getKey() == material) {
            String msg = plugin.getLocalization().msg("broadcast.top_bought_changed").replace("%item%", formatMaterialName(material));
            if (!msg.equals("broadcast.top_bought_changed")) {
                Bukkit.broadcastMessage(msg);
            }
        }
        return true;
    }
    
    public boolean sellToMarket(Player player, Material material, int amount, double multiplier) {
        UUID playerId = player.getUniqueId();
    UUID saleId = UUID.randomUUID();
        if (sellingPlayers.contains(playerId)) {
            player.sendMessage(plugin.getLocalization().pref("transaction.sell.already_processing"));
            return false;
        }
        sellingPlayers.add(playerId);
        try {
            if (!miningMaterials.contains(material) && !farmingMaterials.contains(material) && !huntingMaterials.contains(material) && !fishingMaterials.contains(material)) {
                player.sendMessage(plugin.getLocalization().pref("transaction.sell.invalid_item"));
                return false;
            }
            System.out.println("[DEBUG] Satış denemesi: saleId=" + saleId + " Oyuncu=" + player.getName() + ", Materyal=" + material + ", Miktar=" + amount);
            plugin.getLogger().info("[Market] sellToMarket invoked: saleId=" + saleId + " player=" + player.getName() + " material=" + material + " amount=" + amount + " multiplier=" + multiplier);
            double etkinlikCarpan = multiplier;
            if (etkinlikAktif) {
                etkinlikCarpan *= etkinlikBonusCarpan;
            }
            if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                player.sendMessage("§cEnvanterinizde yeterli " + formatMaterialName(material) + " yok!");
                return false;
            }
            MarketCategory category = null;
            if (miningMaterials.contains(material)) category = MarketCategory.MADENCILIK;
            else if (farmingMaterials.contains(material)) category = MarketCategory.TARIM;
            else if (huntingMaterials.contains(material)) category = MarketCategory.AVCILIK;
            else if (fishingMaterials.contains(material)) category = MarketCategory.BALIKCILIK;

            double totalEarning = getSellPrice(material) * amount * etkinlikCarpan;
            double commission = totalEarning * commissionRate;
            double totalWithCommission = totalEarning - commission;

            boolean removed = removeExactItems(player, material, amount);
            if (!removed) {
                player.sendMessage("§cEnvanterinizde yeterli " + formatMaterialName(material) + " yok!");
                return false;
            }
            me.example.event.DynamicMarketSellEvent sellEvent = new me.example.event.DynamicMarketSellEvent(player, material, amount, totalWithCommission);
            org.bukkit.Bukkit.getPluginManager().callEvent(sellEvent);

            double finalEarnings = sellEvent.getEarnings();
            plugin.getLogger().info("[Market] After DynamicMarketSellEvent: saleId=" + saleId + " player=" + player.getName() + " earnings=" + finalEarnings + " isPaid=" + sellEvent.isPaid());
            if (etkinlikAktif) {
                etkinlikKazanc.put(player.getUniqueId(), etkinlikKazanc.getOrDefault(player.getUniqueId(), 0.0) + finalEarnings);
            }
            if (!sellEvent.isPaid()) {
                plugin.getLogger().info("[Market] Depositing (default) saleId=" + saleId + " player=" + player.getName() + " amount=" + finalEarnings);
                try {
                    double balanceBefore = plugin.getEconomy().getBalance(player);
                    plugin.getLogger().info("[Market] balanceBefore=" + balanceBefore + " saleId=" + saleId);
                    StackTraceElement[] st = Thread.currentThread().getStackTrace();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(8, st.length); i++) sb.append(st[i].toString()).append(" <- ");
                    plugin.getLogger().info("[Market] deposit stack (short): " + sb.toString());
                    plugin.getEconomy().depositPlayer(player, finalEarnings);
                    double balanceAfter = plugin.getEconomy().getBalance(player);
                    plugin.getLogger().info("[Market] balanceAfterImmediate=" + balanceAfter + " delta=" + (balanceAfter - balanceBefore) + " saleId=" + saleId);
                    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        double balanceLater = plugin.getEconomy().getBalance(player);
                        plugin.getLogger().info("[Market] balanceLater(1s)=" + balanceLater + " totalDelta=" + (balanceLater - balanceBefore) + " saleId=" + saleId);
                    }, 20L);
                } catch (Exception ex) {
                    plugin.getLogger().warning("[Market] Error while tracing deposit: " + ex.getMessage());
                    plugin.getEconomy().depositPlayer(player, finalEarnings);
                }
            } else {
                plugin.getLogger().info("[Market] Skipping default deposit because sellEvent.isPaid==true (saleId=" + saleId + ")");
            }

            stocks.put(material, stocks.getOrDefault(material, 0) + amount);
            String template = plugin.getLocalization().msg("transaction.sell.success");
            if (template == null || template.equals("transaction.sell.success")) {
                player.sendMessage(plugin.getLocalization().getPrefix() + "§aSatış başarılı! §f" + amount + "x §b" + formatMaterialName(material)
                    + " §7• Kazanç: §a" + String.format("%.2f", finalEarnings) + " ₺ §7(Komisyon: §c" + String.format("%.2f", commission) + " ₺§7)");
            } else {
                player.sendMessage(plugin.getLocalization().getPrefix() + template
                    .replace("{count}", String.valueOf(amount))
                    .replace("{item}", formatMaterialName(material))
                    .replace("{earned}", String.format("%.2f", finalEarnings))
                    .replace("{commission}", String.format("%.2f", commission)));
            }
            totalSold.put(material, totalSold.getOrDefault(material, 0) + amount);
            UUID uuid = player.getUniqueId();
            playerSold.putIfAbsent(uuid, new HashMap<>());
            Map<Material, Integer> pMap = playerSold.get(uuid);
            pMap.put(material, pMap.getOrDefault(material, 0) + amount);
            if (category != null) {
                playerCategoryEarnings.putIfAbsent(uuid, new HashMap<>());
                Map<MarketCategory, Double> earnings = playerCategoryEarnings.get(uuid);
                earnings.put(category, earnings.getOrDefault(category, 0.0) + totalWithCommission);
                updateEarnings(uuid, category, totalWithCommission);
                updateQuestProgress(player, MarketQuestType.SELL_ITEM, material, category, amount, totalWithCommission);
                updateQuestProgress(player, MarketQuestType.SELL_CATEGORY, null, category, amount, totalWithCommission);
                updateQuestProgress(player, MarketQuestType.EARN_MONEY, null, null, 0, totalWithCommission);
            }
                playerCategoryTaxProgress.putIfAbsent(uuid, new HashMap<>());
                Map<MarketCategory, Double> taxProgress = playerCategoryTaxProgress.get(uuid);
                double prevTax = taxProgress.getOrDefault(category, 0.0);
                double addedTax = commission; 
                double newTax = prevTax + addedTax;
                taxProgress.put(category, newTax);
                playerCategoryTaxLevel.putIfAbsent(uuid, new HashMap<>());
                Map<MarketCategory, Integer> taxLevel = playerCategoryTaxLevel.get(uuid);
                int currentLevel = taxLevel.getOrDefault(category, 0);
                boolean leveledUp = false;
                while (currentLevel < TAX_LEVEL_THRESHOLDS.length && newTax >= TAX_LEVEL_THRESHOLDS[currentLevel]) {
                    currentLevel++;
                    leveledUp = true;
                }
                if (leveledUp) {
                    taxLevel.put(category, currentLevel);
                    String reward = TAX_LEVEL_REWARDS[Math.min(currentLevel-1, TAX_LEVEL_REWARDS.length-1)];
                    player.sendMessage("§6[Vergi Pass] §aTebrikler! " + categoryToString(category) + " kategorisinde Vergi Pass seviye atladınız! Yeni seviye: §e" + currentLevel + " §7(Ödül: §b" + reward + "§7)");
                    if (reward.endsWith("₺")) {
                        try {
                            double money = Double.parseDouble(reward.replace("₺", "").replace(".", "").replace(",", ".").replace(" ", ""));
                            plugin.getEconomy().depositPlayer(player, money);
                            player.sendMessage("§a" + money + " ₺ hesabınıza yatırıldı!");
                        } catch (Exception ignored) {}
                    } else if (reward.equalsIgnoreCase("VIP")) {
                        player.sendMessage("§bVIP ödülünüzü almak için yetkiliyle iletişime geçin!");
                    }
                } else {
                    taxLevel.put(category, currentLevel);
                }
            if (amount >= 64) {
                sendMarketBroadcast(player.getName() + " toplu olarak " + amount + " " + formatMaterialName(material) + " sattı!");
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), "entity.villager.yes", 1f, 1.1f));
            } else {
                player.playSound(player.getLocation(), "entity.item.pickup", 1f, 1.1f);
            }
            List<Map.Entry<Material, Integer>> topSoldList = new ArrayList<>(totalSold.entrySet());
            topSoldList.sort((a, b) -> b.getValue() - a.getValue());
            if (!topSoldList.isEmpty() && topSoldList.get(0).getKey() == material) {
                String msg2 = plugin.getLocalization().msg("broadcast.top_sold_changed").replace("%item%", formatMaterialName(material));
                if (!msg2.equals("broadcast.top_sold_changed")) {
                    Bukkit.broadcastMessage(msg2);
                }
            }
        addSaleEarning(player.getUniqueId(), totalWithCommission);
        return true;
    } finally {
        sellingPlayers.remove(playerId);
    }
    }
    public String getMarketStatsSummary() {
        StringBuilder sb = new StringBuilder("§e=== Market İstatistikleri ===\n");
        List<Map.Entry<Material, Integer>> topBoughtList = new ArrayList<>(totalBought.entrySet());
        topBoughtList.sort((a, b) -> b.getValue() - a.getValue());
        sb.append("§aEn çok alınan: ");
        if (!topBoughtList.isEmpty()) {
            Map.Entry<Material, Integer> top = topBoughtList.get(0);
            sb.append(formatMaterialName(top.getKey())).append(" (" + top.getValue() + ")\n");
        } else sb.append("-\n");
        List<Map.Entry<Material, Integer>> topSoldList2 = new ArrayList<>(totalSold.entrySet());
        topSoldList2.sort((a, b) -> b.getValue() - a.getValue());
        sb.append("§cEn çok satılan: ");
        if (!topSoldList2.isEmpty()) {
            Map.Entry<Material, Integer> top = topSoldList2.get(0);
            sb.append(formatMaterialName(top.getKey())).append(" (" + top.getValue() + ")\n");
        } else sb.append("-\n");
        sb.append("§7Toplam işlem hacmi: §f");
        int total = 0;
        for (int v : totalBought.values()) total += v;
        for (int v : totalSold.values()) total += v;
        sb.append(total).append("\n");
        sb.append("§bKategori Kazançları (Kendi):\n");
        if (!playerCategoryEarnings.isEmpty()) {
            UUID any = playerCategoryEarnings.keySet().iterator().next();
            Map<MarketCategory, Double> earnings = playerCategoryEarnings.get(any);
            if (earnings != null) {
                for (Map.Entry<MarketCategory, Double> entry : earnings.entrySet()) {
                    sb.append("  - ").append(entry.getKey().name()).append(": ").append(String.format("%.2f ₺", entry.getValue())).append("\n");
                }
            }
        }
        return sb.toString();
    }
    public Map<MarketCategory, Double> getPlayerCategoryEarnings(UUID uuid) {
        return playerCategoryEarnings.getOrDefault(uuid, Collections.emptyMap());
    }
    public List<Map.Entry<Material, Integer>> getTopSold(int limit) {
        List<Map.Entry<Material, Integer>> list = new ArrayList<>(totalSold.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }
    public List<Map.Entry<Material, Integer>> getTopBought(int limit) {
        List<Map.Entry<Material, Integer>> list = new ArrayList<>(totalBought.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }
    public int getPlayerBought(UUID uuid, Material material) {
        return playerBought.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(material, 0);
    }

    public int getPlayerSold(UUID uuid, Material material) {
        return playerSold.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(material, 0);
    }
    
    public double getBuyPrice(Material material) {
        return buyPrices.getOrDefault(material, 10.0);
    }

    public double getSellPrice(Material material) {
        return sellPrices.getOrDefault(material, 8.0);
    }

    public List<Material> getAllMaterials() {
        List<Material> all = new ArrayList<>();
        all.addAll(miningMaterials);
        all.addAll(farmingMaterials);
        all.addAll(huntingMaterials);
        all.addAll(fishingMaterials);
        return all;
    }

    public List<Material> getMiningMaterials() { return miningMaterials; }
    public List<Material> getFarmingMaterials() { return farmingMaterials; }
    public List<Material> getHuntingMaterials() { return huntingMaterials; }
    public List<Material> getFishingMaterials() { return fishingMaterials; }

    public boolean isOnlySellCategory(MarketCategory category) {
        return false; 
    }

    public void reloadPrices() {
        loadPricesFromConfig();
    }
    public String categoryToString(MarketCategory category) {
        if (category == null) return "-";
        switch (category) {
            case MADENCILIK: return plugin.getLocalization().msg("category.mining");
            case TARIM: return plugin.getLocalization().msg("category.farming");
            case AVCILIK: return plugin.getLocalization().msg("category.hunting");
            case BALIKCILIK: return plugin.getLocalization().msg("category.fishing");
            default: return "-";
        }
    }
    public void replenishStocks(int amount) {
        for (Material m : getAllMaterials()) {
            stocks.put(m, stocks.getOrDefault(m, 0) + amount);
        }
        plugin.getLogger().info("Tüm market stokları " + amount + " kadar yenilendi.");
    }
    public boolean hasLicense(UUID uuid, Material material) {
        return !blockedMaterials.contains(material) && (playerLicenses.getOrDefault(uuid, Collections.emptySet()).contains(material) || !licenseRequired(material));
    }
    private boolean licenseRequired(Material material) {
        return material == Material.DIAMOND || material == Material.NETHERITE_INGOT;
    }
    public void blockMaterial(Material material) { blockedMaterials.add(material); }
    public void unblockMaterial(Material material) { blockedMaterials.remove(material); }
    public void giveLicense(UUID uuid, Material material) {
        playerLicenses.putIfAbsent(uuid, new HashSet<>());
        playerLicenses.get(uuid).add(material);
    }
    public void revokeLicense(UUID uuid, Material material) {
        if (playerLicenses.containsKey(uuid)) playerLicenses.get(uuid).remove(material);
    }
    public void sendDailyMarketReport() {
        StringBuilder sb = new StringBuilder("§e=== Günlük Market Raporu ===\n");
        sb.append("§7Toplam Ciro: §a").append(String.format("%.2f ₺", getTotalCiro())).append("\n");
        sb.append("§aEn çok satılan ürün: ");
        List<Map.Entry<Material, Integer>> topSold = getTopSold(1);
        if (!topSold.isEmpty()) {
            sb.append(formatMaterialName(topSold.get(0).getKey())).append(" (" + topSold.get(0).getValue() + ")\n");
        } else sb.append("-\n");
        sb.append("§bEn çok kazanan oyuncu: ");
        List<Map.Entry<UUID, Integer>> topSellers = getTopSellers(1);
        if (!topSellers.isEmpty()) {
            String name = Bukkit.getOfflinePlayer(topSellers.get(0).getKey()).getName();
            sb.append(name != null ? name : "Bilinmiyor").append(" (" + topSellers.get(0).getValue() + ")\n");
        } else sb.append("-\n");
        String rapor = sb.toString();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("dynamicmarket.admin")) p.sendMessage(rapor);
        }
    }
    private double getTotalCiro() {
        double ciro = 0;
        for (Map.Entry<Material, Integer> entry : totalSold.entrySet()) {
            ciro += getSellPrice(entry.getKey()) * entry.getValue();
        }
        return ciro;
    }
    public void sendMarketBroadcast(String message) {
        String prefix = "§d[Market] §f";
        Bukkit.broadcastMessage(prefix + message);
    }
    public void sendMarketAdminMessage(String message) {
        String prefix = "§6[Market Admin] §f";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("dynamicmarket.admin")) p.sendMessage(prefix + message);
        }
    }
    public void startDailyReportScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::sendDailyMarketReport, 20L * 60 * 60 * 24, 20L * 60 * 60 * 24); 
    }
    public enum MarketQuestType {
        SELL_ITEM, BUY_ITEM, EARN_MONEY, SELL_CATEGORY
    }
    private final Map<UUID, Map<Integer, Integer>> playerQuestProgress = new HashMap<>();
    private final Map<UUID, Set<Integer>> playerQuestRewardClaimed = new HashMap<>();
    private final List<MarketQuest> questPool = new ArrayList<>();
    private List<MarketQuest> activeQuests = new ArrayList<>();
    private final long QUEST_REFRESH_TICKS = 432000L;
    private long questRefreshEndMillis = 0;
    public void openMarketQuestsGUI(Player player) {
        int size = 36;
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, size, "§d§lMarket Görevleri");
        List<MarketQuest> quests = getDailyQuests();
        org.bukkit.inventory.ItemStack glass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLACK_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) { glassMeta.setDisplayName(" "); glass.setItemMeta(glassMeta); }
        for (int i = 0; i < size; i++) inv.setItem(i, glass);
        for (int i = 0; i < quests.size(); i++) {
            MarketQuest quest = quests.get(i);
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST_MINECART);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b§lGörev: " + quest.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Tür: §e" + quest.getType());
                if (quest.getMaterial() != null) lore.add("§7Ürün: §b" + formatMaterialName(quest.getMaterial()));
                if (quest.getCategory() != null) lore.add("§7Kategori: §a" + categoryToString(quest.getCategory()));
                lore.add("§eHedef: §f" + quest.getAmount());
                lore.add("§6Ödül: §e" + quest.getRewardDisplay());
                UUID uuid = player.getUniqueId();
                int progress = getPlayerQuestProgress(uuid).getOrDefault(quest.getId(), 0);
                boolean claimed = getPlayerQuestRewardClaimed().getOrDefault(uuid, new HashSet<>()).contains(quest.getId());
                lore.add("§7İlerleme: §a" + progress + "§7/§f" + quest.getAmount());
                lore.add(getProgressBar(progress, quest.getAmount(), 20));
                if (claimed) {
                    lore.add("§aÖdül alındı!");
                } else if (progress >= quest.getAmount()) {
                    lore.add("§eTamamlandı! Ödül almak için tıkla.");
                } else {
                    lore.add("§7Devam ediyor...");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(9 + i, item);
        }
        org.bukkit.inventory.ItemStack timeItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CLOCK);
        org.bukkit.inventory.meta.ItemMeta timeMeta = timeItem.getItemMeta();
        if (timeMeta != null) {
            long sec = getQuestRefreshRemainingSeconds();
            long hours = sec / 3600;
            long min = (sec % 3600) / 60;
            long remSec = sec % 60;
            timeMeta.setDisplayName("§eGörev yenileme: §b" + hours + " saat " + min + " dk " + remSec + " sn");
            timeItem.setItemMeta(timeMeta);
        }
        inv.setItem(35, timeItem);
    org.bukkit.inventory.ItemStack back = new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW);
    org.bukkit.inventory.meta.ItemMeta backMeta = back.getItemMeta();
    if (backMeta != null) { backMeta.setDisplayName("§cGeri"); back.setItemMeta(backMeta); }
    inv.setItem(27, back);
    player.openInventory(inv);
    }
    public void openMarketLeaderboardGUI(Player player) {
    new MarketRankingGUI(this).open(player); 
    }
    public List<Map.Entry<UUID, Double>> getTopCategoryEarnersFromMap(Map<UUID, Map<MarketCategory, Double>> map, MarketCategory category, int limit) {
        Map<UUID, Double> earnings = new HashMap<>();
        for (Map.Entry<UUID, Map<MarketCategory, Double>> entry : map.entrySet()) {
            double val = entry.getValue().getOrDefault(category, 0.0);
            if (val > 0) earnings.put(entry.getKey(), val);
        }
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(earnings.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }
    public void updateEarnings(UUID uuid, MarketCategory category, double amount) {
        weeklyCategoryEarnings.putIfAbsent(uuid, new HashMap<>());
        Map<MarketCategory, Double> weekMap = weeklyCategoryEarnings.get(uuid);
        weekMap.put(category, weekMap.getOrDefault(category, 0.0) + amount);
        monthlyCategoryEarnings.putIfAbsent(uuid, new HashMap<>());
        Map<MarketCategory, Double> monthMap = monthlyCategoryEarnings.get(uuid);
        monthMap.put(category, monthMap.getOrDefault(category, 0.0) + amount);
        alltimeCategoryEarnings.putIfAbsent(uuid, new HashMap<>());
        Map<MarketCategory, Double> allMap = alltimeCategoryEarnings.get(uuid);
        allMap.put(category, allMap.getOrDefault(category, 0.0) + amount);
    }
    public void resetWeeklyEarnings() {
        weeklyCategoryEarnings.clear();
    }
    public void resetMonthlyEarnings() {
        monthlyCategoryEarnings.clear();
    }
    public void startWeeklyResetScheduler() {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::resetWeeklyEarnings, 20L * 60 * 60 * 24 * 7, 20L * 60 * 60 * 24 * 7);
    }
    public void startMonthlyResetScheduler() {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::resetMonthlyEarnings, 20L * 60 * 60 * 24 * 30, 20L * 60 * 60 * 24 * 30);
    }
    public List<MarketQuest> getDailyQuests() {
        return activeQuests != null ? activeQuests : new ArrayList<>();
    }
    public List<Map.Entry<UUID, Double>> getTopCategoryEarners(MarketCategory category, int limit) {
        Map<UUID, Double> earnings = new HashMap<>();
        for (Map.Entry<UUID, Map<MarketCategory, Double>> entry : playerCategoryEarnings.entrySet()) {
            double val = entry.getValue().getOrDefault(category, 0.0);
            if (val > 0) earnings.put(entry.getKey(), val);
        }
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(earnings.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }
    public void assignQuestsIfNeeded(Player player) {
    }
    public class MarketQuest {
        private final int id;
        private final MarketQuestType type;
        private final Material material;
        private final MarketCategory category;
        private final int target;
        private final int reward;
        public MarketQuest(int id, MarketQuestType type, Material material, MarketCategory category, int target, int reward) {
            this.id = id;
            this.type = type;
            this.material = material;
            this.category = category;
            this.target = target;
            this.reward = reward;
        }
        public int getId() { return id; }
        public MarketQuestType getType() { return type; }
        public Material getMaterial() { return material; }
        public MarketCategory getCategory() { return category; }
        public int getTarget() { return target; }
        public int getAmount() { return target; }
        public int getReward() { return reward; }
        public String getDisplayName() {
            switch (type) {
                case SELL_ITEM:
                    return plugin.getLocalization().formatMsg("quest.title.sell_item", Map.of("%item%", material != null ? material.name() : "", "%amount%", String.valueOf(target)));
                case BUY_ITEM:
                    return plugin.getLocalization().formatMsg("quest.title.buy_item", Map.of("%item%", material != null ? material.name() : "", "%amount%", String.valueOf(target)));
                case EARN_MONEY:
                    return plugin.getLocalization().formatMsg("quest.title.earn_money", Map.of("%amount%", String.valueOf(target)));
                case SELL_CATEGORY:
                    return plugin.getLocalization().formatMsg("quest.title.sell_category", Map.of("%category%", categoryToString(category), "%amount%", String.valueOf(target)));
                default:
                    return plugin.getLocalization().msg("quest.display.prefix");
            }
        }
        public String getRewardDisplay() {
            return reward + " ₺";
        }
    }
    public void initializeQuestPool() {
        questPool.clear();
        int id = 1;
        for (int i = 0; i < 25; i++) {
            questPool.add(new MarketQuest(id++, MarketQuestType.SELL_ITEM, Material.WHEAT, null, (128 + i*10) * 15, (int)((500 + i*20) * 15 * 1.2)));
            questPool.add(new MarketQuest(id++, MarketQuestType.BUY_ITEM, Material.IRON_INGOT, null, (64 + i*5) * 15, (int)((400 + i*15) * 15 * 1.2)));
            questPool.add(new MarketQuest(id++, MarketQuestType.EARN_MONEY, null, null, (10000 + i*1000) * 15, (int)((1000 + i*50) * 15 * 1.2)));
            questPool.add(new MarketQuest(id++, MarketQuestType.SELL_CATEGORY, null, MarketCategory.MADENCILIK, (64 + i*8) * 15, (int)((600 + i*30) * 15 * 1.2)));
        }
    }
    private void refreshQuests() {
        Collections.shuffle(questPool);
        activeQuests = new ArrayList<>(questPool.subList(0, Math.min(5, questPool.size())));
    }
    public void startQuestRefreshScheduler() {
        refreshQuests();
        questRefreshEndMillis = System.currentTimeMillis() + (QUEST_REFRESH_TICKS * 50);
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            refreshQuests();
            questRefreshEndMillis = System.currentTimeMillis() + (QUEST_REFRESH_TICKS * 50);
        }, QUEST_REFRESH_TICKS, QUEST_REFRESH_TICKS);
    }
    public long getQuestRefreshRemainingSeconds() {
        long now = System.currentTimeMillis();
        long remaining = questRefreshEndMillis - now;
        return Math.max(0, remaining / 1000);
    }
    public String getProgressBar(int current, int max, int length) {
        double percent = Math.min(1.0, (double)current / max);
        int filled = (int)(length * percent);
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < length; i++) {
            if (i < filled) bar.append("§a▓");
            else bar.append("§7▒");
        }
        bar.append("§8]");
        return bar.toString();
    }
    public void openQuestGUI(Player player) {
        int size = 27;
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, size, "§dMarket Görevleri");
        org.bukkit.inventory.ItemStack timer = new org.bukkit.inventory.ItemStack(Material.CLOCK);
        org.bukkit.inventory.meta.ItemMeta timerMeta = timer.getItemMeta();
        if (timerMeta != null) {
            timerMeta.setDisplayName("§eGörevler yenileniyor");
            long sec = getQuestRefreshRemainingSeconds();
            long min = sec / 60;
            long remSec = sec % 60;
            timerMeta.setLore(java.util.Arrays.asList("§7Kalan süre: §b" + min + " dk " + remSec + " sn"));
            timer.setItemMeta(timerMeta);
        }
        inv.setItem(26, timer);
        int slot = 10;
        for (MarketQuest quest : activeQuests) {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.BOOK);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§bGörev: " + quest.getDisplayName());
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add("§eHedef: " + quest.getTarget());
                int progress = getPlayerQuestProgress(player.getUniqueId()).getOrDefault(quest.getId(), 0);
                lore.add("§aİlerleme: " + progress + " / " + quest.getTarget());
                lore.add(getProgressBar(progress, quest.getTarget(), 20));
                lore.add("§6Ödül: " + quest.getRewardDisplay());
                if (getPlayerQuestRewardClaimed().getOrDefault(player.getUniqueId(), java.util.Collections.emptySet()).contains(quest.getId())) {
                    lore.add("§bÖdül alındı!");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }
    public void updateQuestProgress(Player player, MarketQuestType type, Material material, MarketCategory category, int amount, double earned) {
        UUID uuid = player.getUniqueId();
        for (MarketQuest quest : activeQuests) {
            if (quest.getType() != type) continue;
            boolean matches = false;
            switch (type) {
                case SELL_ITEM:
                case BUY_ITEM:
                    matches = quest.getMaterial() == material;
                    break;
                case SELL_CATEGORY:
                    matches = quest.getCategory() == category;
                    break;
                case EARN_MONEY:
                    matches = true;
                    break;
            }
            if (!matches) continue;
            int progress = playerQuestProgress.computeIfAbsent(uuid, k -> new HashMap<>()).getOrDefault(quest.getId(), 0);
            int add = 0;
            if (type == MarketQuestType.EARN_MONEY) {
                add = (int) earned;
            } else {
                add = amount;
            }
            int newProgress = Math.min(progress + add, quest.getTarget());
            playerQuestProgress.get(uuid).put(quest.getId(), newProgress);
        }
    }
    public void openMarketStatsGUI(Player player) {
        int size = 36;
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, size, "§b§lMarket İstatistikleri");
        org.bukkit.inventory.ItemStack glass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLUE_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) { 
            glassMeta.setDisplayName(" "); 
            glass.setItemMeta(glassMeta); 
        }
        for (int i = 0; i < size; i++) inv.setItem(i, glass);
        List<Map.Entry<Material, Integer>> topSold = getTopSold(1);
        if (!topSold.isEmpty()) {
            org.bukkit.inventory.ItemStack topSoldItem = new org.bukkit.inventory.ItemStack(topSold.get(0).getKey());
            org.bukkit.inventory.meta.ItemMeta topSoldMeta = topSoldItem.getItemMeta();
            if (topSoldMeta != null) {
                topSoldMeta.setDisplayName("§c§lEn Çok Satılan");
                topSoldMeta.setLore(Arrays.asList("§7" + formatMaterialName(topSold.get(0).getKey()), "§7Miktar: §a" + topSold.get(0).getValue()));
                topSoldItem.setItemMeta(topSoldMeta);
            }
            inv.setItem(11, topSoldItem);
        }
        List<Map.Entry<Material, Integer>> topBought = getTopBought(1);
        if (!topBought.isEmpty()) {
            org.bukkit.inventory.ItemStack topBoughtItem = new org.bukkit.inventory.ItemStack(topBought.get(0).getKey());
            org.bukkit.inventory.meta.ItemMeta topBoughtMeta = topBoughtItem.getItemMeta();
            if (topBoughtMeta != null) {
                topBoughtMeta.setDisplayName("§a§lEn Çok Alınan");
                topBoughtMeta.setLore(Arrays.asList("§7" + formatMaterialName(topBought.get(0).getKey()), "§7Miktar: §a" + topBought.get(0).getValue()));
                topBoughtItem.setItemMeta(topBoughtMeta);
            }
            inv.setItem(15, topBoughtItem);
        }
        int totalTransactions = 0;
        for (int v : totalBought.values()) totalTransactions += v;
        for (int v : totalSold.values()) totalTransactions += v;
        
        org.bukkit.inventory.ItemStack totalItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST);
        org.bukkit.inventory.meta.ItemMeta totalMeta = totalItem.getItemMeta();
        if (totalMeta != null) {
            totalMeta.setDisplayName("§e§lToplam İşlemler");
            totalMeta.setLore(Arrays.asList("§7Toplam işlem sayısı: §a" + totalTransactions));
            totalItem.setItemMeta(totalMeta);
        }
        inv.setItem(13, totalItem);
        org.bukkit.inventory.ItemStack back = new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW);
        org.bukkit.inventory.meta.ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Geri");
            back.setItemMeta(backMeta);
        }
        inv.setItem(31, back);
        
        player.openInventory(inv);
    }
}



