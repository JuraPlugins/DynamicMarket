package me.example.util;

import org.bukkit.entity.Player;

public final class MessageUtils {
    private MessageUtils() {}
    public static final String PREFIX = "§8│§3✦ §bMarket §8» §r";

    public static void send(Player player, String message) {
        if (player == null) return;
        player.sendMessage(PREFIX + message);
    }

    public static String money(double value) {
        return String.format("§x§0§d§f§f§0§0%.2f§r §7₺", value); 
    }

    public static String bold(String text) {
        return "§l" + text + "§r";
    }

    public static void success(Player p, String msg){ send(p, "§a"+msg); }
    public static void error(Player p, String msg){ send(p, "§c"+msg); }
    public static void info(Player p, String msg){ send(p, "§7"+msg); }
}

