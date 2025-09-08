package com.hypershop.service;

import com.hypershop.HyperShopPlugin;
import com.hypershop.model.PriceModel;
import com.hypershop.model.Shop;
import com.hypershop.model.ShopItem;
import com.hypershop.view.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager implements Listener {
    private final HyperShopPlugin plugin;
    private final Map<String, Shop> shops = new ConcurrentHashMap<>();
    private final Set<String> dirty = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private PriceModel.Dynamic priceCfg;

    public ShopManager(HyperShopPlugin plugin) {
        this.plugin = plugin;
        this.priceCfg = PriceModel.Dynamic.from(plugin.getConfig());
    }

    public void loadAll() {
        shops.clear();
        this.priceCfg = PriceModel.Dynamic.from(plugin.getConfig());
        File dir = new File(plugin.getDataFolder(), "shops");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml") && !n.equals("categories.yml"));
        if (files == null) return;
        for (File f : files) loadShopFile(f);
    }

    private void loadShopFile(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String id = yml.getString("id");
        String name = yml.getString("name", id);
        var icon = Material.matchMaterial(Objects.requireNonNullElse(yml.getString("icon"), "CHEST"));
        if (id == null || icon == null) {
            plugin.getLogger().warning("Bỏ qua shop file: " + file.getName());
            return;
        }
        Shop shop = new Shop(id, name, icon);
        var items = yml.getMapList("items");
        for (var m : items) {
            Material mat = Material.matchMaterial(String.valueOf(m.get("material")));
            if (mat == null) continue;
            double buy = m.containsKey("buy") ? Double.parseDouble(String.valueOf(m.get("buy"))) : 0.0;
            double sell = m.containsKey("sell") ? Double.parseDouble(String.valueOf(m.get("sell"))) : 0.0;
            ShopItem item = new ShopItem(mat, buy, sell);
            if (m.containsKey("pricing-mode")) {
                try { item.setPriceMode(PriceModel.valueOf(String.valueOf(m.get("pricing-mode")).toUpperCase())); } catch (Exception ignored) {}
            }
            shop.getItems().put(mat, item);
        }
        shops.put(id, shop);
    }

    public Collection<String> getShopIds() { return shops.keySet(); }
    public Shop getShop(String id) { return shops.get(id); }

    public void markDirty(String shopId) { dirty.add(shopId); }

    public void asyncSaveDirty() {
        if (dirty.isEmpty()) return;
        Set<String> copy = Set.copyOf(dirty);
        dirty.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String id : copy) saveShop(id);
        });
    }

    private void saveShop(String id) {
        Shop shop = shops.get(id);
        if (shop == null) return;
        File file = new File(plugin.getDataFolder(), "shops/" + id + ".yml");
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("id", shop.getId());
        yml.set("name", shop.getName());
        yml.set("icon", shop.getIcon().name());
        List<Map<String, Object>> list = new ArrayList<>();
        for (ShopItem it : shop.getItems().values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("material", it.getMaterial().name());
            map.put("buy", it.getBaseBuy());
            map.put("sell", it.getBaseSell());
            map.put("pricing-mode", it.getPriceMode().name());
            list.add(map);
        }
        yml.set("items", list);
        try { yml.save(file); } catch (IOException e) { plugin.getLogger().warning("Lỗi lưu shop " + id + ": " + e.getMessage()); }
    }

    // Pricing helpers
    public double currentBuy(Shop shop, ShopItem item) {
        double base = item.getBaseBuy();
        PriceModel mode = item.getPriceMode() == PriceModel.INHERIT ? priceCfg.mode : item.getPriceMode();
        return switch (mode) {
            case STATIC -> base;
            case PERCENT -> clamp(base * getPercentMultiplier(item));
            case STOCK -> clamp(base * getStockMultiplier(item));
            default -> base;
        };
    }

    public double currentSell(Shop shop, ShopItem item) {
        double base = item.getBaseSell();
        PriceModel mode = item.getPriceMode() == PriceModel.INHERIT ? priceCfg.mode : item.getPriceMode();
        return switch (mode) {
            case STATIC -> base;
            case PERCENT -> clamp(base * getPercentMultiplier(item));
            case STOCK -> clamp(base * getStockMultiplier(item));
            default -> base;
        };
    }

    private double getPercentMultiplier(ShopItem item) {
        double mul = 1.0 - (item.getStock() * priceCfg.downPerBuy) + (Math.max(0, -item.getStock()) * priceCfg.upPerSale);
        return Math.max(priceCfg.minMul, Math.min(priceCfg.maxMul, mul));
    }

    private double getStockMultiplier(ShopItem item) {
        double stock = priceCfg.baseStock + item.getStock();
        if (stock < 1) stock = 1;
        double ratio = stock / (double) priceCfg.baseStock;
        double mul = 1.0 - (ratio - 1.0) * priceCfg.elasticity;
        return Math.max(priceCfg.minStockMul, Math.min(priceCfg.maxStockMul, mul));
    }

    private double clamp(double v) { return Math.max(0.0, Math.round(v * 100.0) / 100.0); }

    // Events
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof ShopGUI gui) {
            gui.handleClick(e);
        } else if (e.getView().getTopInventory().getHolder() instanceof com.hypershop.view.ShopEditGUI edit) {
            edit.handleClick(e);
        }
    }

    // Transaction API
    public boolean buy(Player p, Shop shop, ShopItem item, int amount) {
        double price = currentBuy(shop, item) * amount;
        if (!plugin.economy().withdraw(p, price)) {
            p.sendMessage(plugin.msg().fmt("not-enough-money").replace("%price%", String.valueOf(price)));
            return false;
        }
        ItemStack stack = new ItemStack(item.getMaterial(), amount);
        var excess = p.getInventory().addItem(stack);
        excess.values().forEach(drop -> p.getWorld().dropItemNaturally(p.getLocation(), drop));
        item.addStock(-amount);
        markDirty(shop.getId());
        p.sendMessage(plugin.msg().fmt("buy-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", item.getMaterial().name())
                .replace("%price%", String.valueOf(price)));
        return true;
    }

    public boolean sell(Player p, Shop shop, ShopItem item, int amount) {
        ItemStack check = new ItemStack(item.getMaterial());
        int has = countPlayerItem(p, check);
        if (has < amount) {
            p.sendMessage(plugin.msg().fmt("not-enough-items"));
            return false;
        }
        removeItems(p, check, amount);
        double price = currentSell(shop, item) * amount;
        plugin.economy().deposit(p, price);
        item.addStock(amount);
        markDirty(shop.getId());
        p.sendMessage(plugin.msg().fmt("sell-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", item.getMaterial().name())
                .replace("%price%", String.valueOf(price)));
        return true;
    }

    private int countPlayerItem(Player p, ItemStack template) {
        int total = 0;
        for (ItemStack it : p.getInventory().getStorageContents()) {
            if (it != null && it.getType() == template.getType()) total += it.getAmount();
        }
        return total;
    }

    private void removeItems(Player p, ItemStack template, int amount) {
        for (int i = 0; i < p.getInventory().getSize() && amount > 0; i++) {
            ItemStack it = p.getInventory().getItem(i);
            if (it == null || it.getType() != template.getType()) continue;
            int take = Math.min(amount, it.getAmount());
            it.setAmount(it.getAmount() - take);
            amount -= take;
            if (it.getAmount() <= 0) p.getInventory().setItem(i, null);
        }
    }
}
