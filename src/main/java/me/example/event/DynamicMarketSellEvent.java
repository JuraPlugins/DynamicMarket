package me.example.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DynamicMarketSellEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Material material;
    private final int amount;
    private double earnings;
    private boolean paid = false; 
    
    public DynamicMarketSellEvent(Player player, Material material, int amount, double earnings) {
        this.player = player;
        this.material = material;
        this.amount = amount;
    this.earnings = earnings;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public double getEarnings() {
        return earnings;
    }
    public double getKazanc() {
        return earnings;
    }

    
    public void setEarnings(double earnings) {
        this.earnings = earnings;
    }
    public void setKazanc(double earnings) {
        this.earnings = earnings;
    }

    
    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

