# DynamicMarket Plugin Developer Documentation

## Architecture Overview

The DynamicMarket plugin is designed with a modular architecture consisting of several key components:

### Core Components

1. **DynamicMarket** - Main plugin class handling initialization and command registration
2. **MarketManager** - Core business logic for market operations
3. **LocalizationManager** - Handles multi-language support
4. **MarketListener** - GUI event handling and player interactions
5. **EarningsStorage** - Player earnings persistence and management

### API Components

- **MarketAPI** - External API interface for other plugins
- **MarketAPIProvider** - API provider implementation
- **DynamicMarketSellEvent** - Custom event for market transactions

### Integration Components

- **TagsMarketIntegration** - Integration with Tags plugin for enhanced features

### Task Components

- **PriceFluctuationTask** - Automated price adjustment system

### GUI Components

- **MarketRankingGUI** - Player ranking interface
- **MarketRankingListGUI** - Detailed ranking list interface

## Market Categories

The plugin supports four main market categories:

1. **Mining** (PICKAXE icon)
   - Stone, ores, minerals
   - Mining-related items

2. **Farming** (WHEAT icon)
   - Crops, seeds, food items
   - Agricultural products

3. **Hunting** (IRON_SWORD icon)
   - Mob drops, leather, bones
   - Combat-related items

4. **Fishing** (FISHING_ROD icon)
   - Fish, water-related items
   - Fishing products

## Quest System

### Quest Types
- **SELL_ITEMS** - Sell specific amounts of items
- **EARN_MONEY** - Earn specific amounts of money
- **COMPLETE_TRANSACTIONS** - Complete a number of transactions

### Quest Durations
- Daily quests (reset every 24 hours)
- Weekly quests (reset every 7 days)

## Configuration Structure

### config.yml
- Market item prices and categories
- Quest configurations
- Commission rates
- Price fluctuation settings
- Database settings (if applicable)

### Language Files
- messages_en.yml - English translations
- messages_tr.yml - Turkish translations
- Expandable for additional languages

## Database Schema (if implemented)

### Tables
- player_earnings
- market_transactions
- quest_progress
- ranking_data

## API Usage Examples

```java
// Get market API instance
MarketAPI api = MarketAPIProvider.getAPI();

// Sell items programmatically
api.sellItems(player, itemStack, amount);

// Get player earnings
double earnings = api.getPlayerEarnings(player);

// Listen for market events
@EventHandler
public void onMarketSell(DynamicMarketSellEvent event) {
    Player player = event.getPlayer();
    double amount = event.getAmount();
    // Handle event
}
```

## Performance Considerations

- GUI operations are async where possible
- Database operations use connection pooling
- Price fluctuation runs on scheduled tasks
- Memory-efficient data structures for rankings

## Security Features

- Input validation for all commands
- Permission-based access control
- Anti-exploit measures for transactions
- Logging of all market activities

## Testing Guidelines

- Unit tests for core business logic
- Integration tests for database operations
- GUI interaction testing
- Performance benchmarking

## Deployment Notes

- Requires Java 17 or higher
- Compatible with Spigot/Paper 1.20.1+
- Vault dependency required
- Optional Tags plugin integration
- Configuration migration support
