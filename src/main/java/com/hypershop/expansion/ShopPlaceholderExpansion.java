package com.hypershop.expansion;

import com.hypershop.HyperShopPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class ShopPlaceholderExpansion extends PlaceholderExpansion {
    private final HyperShopPlugin plugin;
    public ShopPlaceholderExpansion(HyperShopPlugin plugin) { this.plugin = plugin; }

    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }
    @Override public String getIdentifier() { return "hypershop"; }
    @Override public String getAuthor() { return plugin.getDescription().getAuthors().toString(); }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public String onPlaceholderRequest(OfflinePlayer player, String identifier) {
        if (identifier.equalsIgnoreCase("tenshop")) {
            var ids = plugin.shops().getShopIds();
            return ids.stream().findFirst().map(plugin.shops()::getShop).map(s -> s.getName()).orElse("-");
        }
        if (identifier.startsWith("buy_")) {
            String[] parts = identifier.split("_");
            if (parts.length >= 3) {
                var shop = plugin.shops().getShop(parts[1]);
                if (shop != null) {
                    var item = shop.getItem(org.bukkit.Material.matchMaterial(parts[2]));
                    if (item != null) return String.valueOf(plugin.shops().currentBuy(shop, item));
                }
            }
        }
        return "";
    }
}
