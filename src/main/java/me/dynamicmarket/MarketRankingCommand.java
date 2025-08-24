package me.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketRankingCommand implements CommandExecutor {
    
    private final MarketManager marketManager;
    
    public MarketRankingCommand(MarketManager marketManager) {
        this.marketManager = marketManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player player = (Player) sender;
    new MarketRankingListGUI(marketManager, false, 0).open(player);
        
        return true;
    }
}

