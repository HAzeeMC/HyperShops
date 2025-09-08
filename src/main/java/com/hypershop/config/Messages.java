package com.hypershop.config;

import com.hypershop.HyperShopPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;

public class Messages {
    private final HyperShopPlugin plugin;
    private FileConfiguration cfg;

    public Messages(HyperShopPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public String fmt(String key) {
        String prefix = color(cfg.getString("prefix", ""));
        return prefix + color(cfg.getString(key, key));
    }
    private String color(String s) {
        if (s == null) return "";
        return LegacyComponentSerializer.legacyAmpersand().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserializeOr(s, s)
        );
    }
}
