package me.example;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class LocalizationManager {
    private final Plugin plugin;
    private String currentLang;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix = "";
    private String adminPrefix = "";

    public LocalizationManager(Plugin plugin, String lang) {
        this.plugin = plugin;
        ensureLanguageFiles();
        load(lang);
    }

    private void ensureLanguageFiles() {
        File langDir = new File(plugin.getDataFolder(), "language");
        if (!langDir.exists()) langDir.mkdirs();
        copyIfNotExists(langDir, "en.yml");
        copyIfNotExists(langDir, "tr.yml");
    upgradeLanguageFile(langDir, "en.yml");
    upgradeLanguageFile(langDir, "tr.yml");
    }

    private void copyIfNotExists(File dir, String name) {
        File out = new File(dir, name);
        if (out.exists()) return;
        try (InputStream in = plugin.getResource("language/" + name)) {
            if (in != null) Files.copy(in, out.toPath());
        } catch (IOException ignored) {}
    }
    private void upgradeLanguageFile(File dir, String name) {
        File out = new File(dir, name);
        if (!out.exists()) return;
        try (InputStream in = plugin.getResource("language/" + name)) {
            if (in == null) return;
            java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
            YamlConfiguration def = YamlConfiguration.loadConfiguration(reader);
            YamlConfiguration existing = YamlConfiguration.loadConfiguration(out);
            boolean changed = false;
            for (String key : def.getKeys(true)) {
                if (!existing.contains(key)) {
                    if (def.isString(key)) existing.set(key, def.getString(key));
                    else if (def.isList(key)) existing.set(key, def.getStringList(key));
                    changed = true;
                }
            }
            if (changed) {
                existing.save(out);
                plugin.getLogger().info("Upgraded language file: " + out.getName() + " with missing keys from bundled resource.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to upgrade language file " + name + ": " + e.getMessage());
        }
    }

    public void reload(String lang) { load(lang); }

    private void load(String lang) {
        this.currentLang = lang;
        messages.clear();
        File file = new File(plugin.getDataFolder(), "language/" + lang + ".yml");
        if (!file.exists()) { 
            file = new File(plugin.getDataFolder(), "language/en.yml");
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        for (String key : conf.getKeys(true)) {
            if (conf.isString(key)) messages.put(key, conf.getString(key));
            else if (conf.isList(key)) {
                List<String> list = conf.getStringList(key);
                messages.put(key, String.join("\n", list));
            }
        }
        prefix = messages.getOrDefault("prefix", "");
        adminPrefix = messages.getOrDefault("admin_prefix", prefix);
    }
    public String msg(String key) { return msgWithFallback(key); }
    public String msgWithFallback(String key) {
        if (messages.containsKey(key)) return messages.get(key);
        try (InputStream in = plugin.getResource("language/en.yml")) {
            if (in != null) {
                java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
                org.bukkit.configuration.file.YamlConfiguration def = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(reader);
                if (def.isString(key)) return def.getString(key);
                if (def.isList(key)) return String.join("\n", def.getStringList(key));
            }
        } catch (Exception ignored) {}
        return key;
    }
    public String pref(String key) { return prefix + msg(key); }
    public String admin(String key) { return adminPrefix + msg(key); }
    public String getPrefix() { return prefix; }
    public String getAdminPrefix() { return adminPrefix; }

    public String format(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace(e.getKey(), e.getValue());
        }
        return result;
    }

    public String formatMsg(String key, Map<String, String> placeholders) {
        return format(msg(key), placeholders);
    }
}

