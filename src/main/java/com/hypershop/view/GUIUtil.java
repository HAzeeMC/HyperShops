package com.hypershop.view;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class GUIUtil {
    private GUIUtil() {}

    public static Inventory createInv(String title, int rows, Object holder) {
        return Bukkit.createInventory(holder instanceof org.bukkit.inventory.InventoryHolder ih ? ih : null, rows * 9, title);
    }

    public static ItemStack item(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (name != null) meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) meta.setLore(lore.stream().map(s -> org.bukkit.ChatColor.translateAlternateColorCodes('&', s)).toList());
        it.setItemMeta(meta);
        return it;
    }
}
