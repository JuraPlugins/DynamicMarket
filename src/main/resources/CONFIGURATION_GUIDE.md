# Advanced Market Configuration Guide

## Introduction
This comprehensive guide covers all aspects of configuring the DynamicMarket plugin for optimal performance and user experience. The plugin supports dynamic pricing, multi-category markets, quest systems, and extensive customization options.

## Basic Configuration Structure

### Main Configuration File (config.yml)
The primary configuration file controls the core behavior of the market system:

```yaml
# Market settings
market:
  enabled: true
  commission_rate: 0.05  # 5% commission on all sales
  auto_save_interval: 300  # Save data every 5 minutes
  price_fluctuation_enabled: true
  price_fluctuation_interval: 60  # 30 minutes
  max_price_change_percent: 50
  
# Database configuration
database:
  type: "file"  # Options: file, mysql, sqlite
  host: "localhost"
  port: 3306
  database: "dynamicmarket"
  username: "username"
  password: "password"
  
# GUI settings
gui:
  category_rows: 3
  items_per_page: 45
  auto_refresh: true
  refresh_interval: 30
```

### Category Configuration
Each market category can be customized with specific settings:

```yaml
categories:
  mining:
    icon: DIAMOND_PICKAXE
    display_name: "&b&lMining Market"
    description:
      - "&7Sell your mining materials"
      - "&7for competitive prices!"
    enabled: true
    commission_override: 0.03  # 3% for mining items
    
  farming:
    icon: GOLDEN_HOE
    display_name: "&2&lFarming Market"
    description:
      - "&7Trade your agricultural"
      - "&7products for profit!"
    enabled: true
    bonus_multiplier: 1.1  # 10% bonus for farming
    
  hunting:
    icon: IRON_SWORD
    display_name: "&c&lHunting Market"
    description:
      - "&7Sell monster drops and"
      - "&7combat materials here!"
    enabled: true
    danger_bonus: 1.2  # 20% bonus for dangerous items
    
  fishing:
    icon: FISHING_ROD
    display_name: "&9&lFishing Market"
    description:
      - "&7Trade your aquatic"
      - "&7treasures for coins!"
    enabled: true
    rarity_multiplier: 1.5  # Extra bonus for rare fish
```

## Advanced Item Pricing System

### Dynamic Pricing Algorithm
The plugin uses a sophisticated algorithm to calculate item prices based on:

1. **Base Price**: Starting price defined in configuration
2. **Stock Levels**: Lower stock = higher prices
3. **Demand**: Recent sales affect pricing
4. **Market Events**: Seasonal bonuses and events
5. **Player Activity**: Server population influences prices

### Price Calculation Formula
```
Final_Price = Base_Price × Stock_Multiplier × Demand_Multiplier × Event_Multiplier × Activity_Multiplier
```

Where:
- Stock_Multiplier = (Max_Stock / Current_Stock) × Stock_Impact_Factor
- Demand_Multiplier = 1 + (Recent_Sales / Time_Period) × Demand_Impact_Factor
- Event_Multiplier = Current active event bonus (default 1.0)
- Activity_Multiplier = (Online_Players / Max_Players) × Activity_Impact_Factor

### Item-Specific Configuration
```yaml
items:
  DIAMOND:
    category: mining
    base_price: 50.0
    max_stock: 100
    min_stock: 10
    max_price: 150.0
    min_price: 25.0
    rarity: RARE
    fluctuation_sensitivity: 0.8
    seasonal_bonus:
      summer: 1.2
      winter: 0.9
    
  WHEAT:
    category: farming
    base_price: 2.0
    max_stock: 5000
    min_stock: 500
    max_price: 5.0
    min_price: 1.0
    rarity: COMMON
    fluctuation_sensitivity: 0.3
    harvest_season_bonus: 1.5
```

## Quest System Configuration

### Quest Types and Parameters
The quest system supports multiple quest types with customizable parameters:

#### 1. Sell Items Quest
```yaml
sell_items_quest:
  type: SELL_ITEMS
  target_items:
    - "DIAMOND:10"
    - "EMERALD:5"
  reward_money: 500.0
  reward_exp: 250
  time_limit: 86400  # 24 hours
  difficulty: MEDIUM
```

#### 2. Earn Money Quest
```yaml
earn_money_quest:
  type: EARN_MONEY
  target_amount: 1000.0
  category_restriction: "mining"
  reward_money: 200.0
  reward_items:
    - "DIAMOND_PICKAXE:1"
  time_limit: 172800  # 48 hours
```

#### 3. Complete Transactions Quest
```yaml
transaction_quest:
  type: COMPLETE_TRANSACTIONS
  target_count: 50
  min_transaction_value: 10.0
  reward_money: 300.0
  bonus_multiplier: 1.2
```

### Quest Difficulty Scaling
```yaml
difficulty_scaling:
  EASY:
    reward_multiplier: 1.0
    time_multiplier: 1.5
    target_reduction: 0.7
    
  MEDIUM:
    reward_multiplier: 1.3
    time_multiplier: 1.0
    target_reduction: 1.0
    
  HARD:
    reward_multiplier: 1.8
    time_multiplier: 0.8
    target_reduction: 1.3
    
  EXPERT:
    reward_multiplier: 2.5
    time_multiplier: 0.6
    target_reduction: 1.6
```

## Ranking System Configuration

### Ranking Categories
```yaml
rankings:
  daily:
    enabled: true
    reset_time: "00:00"  # Midnight
    display_count: 10
    rewards:
      1: 
        money: 1000.0
        items: ["DIAMOND:5"]
        title: "&6&l#1 Daily Trader"
      2:
        money: 500.0
        items: ["GOLD_INGOT:10"]
        title: "&e&l#2 Daily Trader"
      3:
        money: 250.0
        items: ["IRON_INGOT:20"]
        title: "&7&l#3 Daily Trader"
        
  weekly:
    enabled: true
    reset_day: "MONDAY"
    display_count: 15
    special_rewards: true
    
  monthly:
    enabled: true
    reset_date: 1  # First day of month
    display_count: 20
    legendary_rewards: true
```

## Event System Configuration

### Seasonal Events
```yaml
events:
  summer_market_boom:
    start_date: "06-01"  # June 1st
    end_date: "08-31"    # August 31st
    price_multiplier: 1.3
    categories: ["farming", "fishing"]
    special_items:
      - "TROPICAL_FISH"
      - "MELON"
      - "PUMPKIN"
    announcement: "&e&lSummer Market Boom is active!"
    
  winter_mining_rush:
    start_date: "12-01"  # December 1st
    end_date: "02-28"    # February 28th
    price_multiplier: 1.5
    categories: ["mining"]
    bonus_ores: true
    snow_bonus: 1.2
```

### Custom Events
```yaml
custom_events:
  double_xp_weekend:
    trigger: "manual"  # or "automatic"
    duration: 172800   # 48 hours
    exp_multiplier: 2.0
    money_multiplier: 1.0
    announcement_interval: 3600  # 1 hour
    
  market_crash:
    trigger: "condition"  # Triggered by market conditions
    condition: "total_sales > 100000"
    price_multiplier: 0.7
    duration: 43200  # 12 hours
    recovery_rate: 0.1  # 10% recovery per hour
```

## Performance Optimization

### Caching Configuration
```yaml
cache:
  player_data_cache_size: 1000
  price_cache_duration: 300  # 5 minutes
  ranking_cache_duration: 600  # 10 minutes
  quest_cache_duration: 1800  # 30 minutes
  
async_operations:
  database_operations: true
  price_calculations: true
  ranking_updates: true
  file_operations: true
  
batch_processing:
  enabled: true
  batch_size: 100
  processing_interval: 1000  # 1 second
```

### Memory Management
```yaml
memory_management:
  auto_cleanup: true
  cleanup_interval: 3600  # 1 hour
  max_cached_players: 500
  max_price_history: 1000
  garbage_collection_hints: true
```

## Security and Anti-Cheat

### Transaction Validation
```yaml
security:
  max_transactions_per_minute: 60
  max_transaction_value: 100000.0
  duplicate_transaction_prevention: true
  price_manipulation_detection: true
  
anti_cheat:
  unusual_activity_detection: true
  rapid_sale_threshold: 10  # items per second
  price_anomaly_detection: true
  automated_ban_system: false  # Manual review recommended
```

## Integration with Other Plugins

### Vault Integration
```yaml
vault:
  enabled: true
  economy_provider: "auto"  # or specific plugin name
  currency_symbol: "$"
  decimal_places: 2
```

### PlaceholderAPI Support
```yaml
placeholderapi:
  enabled: true
  custom_placeholders:
    - "%dynamicmarket_balance%"
    - "%dynamicmarket_rank%"
    - "%dynamicmarket_sales_today%"
    - "%dynamicmarket_quest_progress%"
```

### WorldGuard Integration
```yaml
worldguard:
  enabled: true
  allowed_regions:
    - "market"
    - "spawn"
    - "trading_post"
  blocked_worlds:
    - "creative_world"
    - "testing_world"
```

## Troubleshooting Common Issues

### Performance Issues
1. **High CPU Usage**: Reduce price fluctuation frequency
2. **Memory Leaks**: Enable automatic cleanup
3. **Database Lag**: Implement connection pooling
4. **GUI Lag**: Reduce items per page

### Configuration Problems
1. **Items Not Selling**: Check category assignments
2. **Price Anomalies**: Verify min/max price limits
3. **Quest Failures**: Review target configurations
4. **Ranking Issues**: Check reset timings

### Plugin Conflicts
1. **Economy Conflicts**: Ensure single economy provider
2. **GUI Conflicts**: Check for inventory event interference
3. **Command Conflicts**: Review command aliases
4. **Permission Conflicts**: Verify permission nodes

This configuration guide provides comprehensive coverage of all plugin features and should help administrators create the perfect market environment for their server.
