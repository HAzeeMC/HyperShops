package com.hypershop.model;

import org.bukkit.Material;
import java.util.*;

public class Shop {
    private final String id;
    private final String name;
    private final Material icon;
    private final Map<Material, ShopItem> items = new EnumMap<>(Material.class);

    public Shop(String id, String name, Material icon) {
        this.id = id; this.name = name; this.icon = icon;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getIcon() { return icon; }

    public Map<Material, ShopItem> getItems() { return items; }
    public ShopItem getItem(Material m) { return items.get(m); }
    public ShopItem getOrCreateItem(Material m) { return items.computeIfAbsent(m, x -> new ShopItem(m, 0, 0)); }
}
