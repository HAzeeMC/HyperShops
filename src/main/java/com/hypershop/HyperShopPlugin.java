package com.hypershop;

import com.hypershop.commands.HyperShopCommand;
import com.hypershop.config.Messages;
import com.hypershop.economy.EconomyHook;
import com.hypershop.service.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HyperShopPlugin extends JavaPlugin {
    private static HyperShopPlugin instance;

    private EconomyHook economy;
    private ShopManager shopManager;
    private Messages messages;

    @Override
    public void onLoad() { instance = this; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("shops/hypershop.yml", false);
        saveResource("shops/categories.yml", false);

        this.messages = new Messages(this);
        this.economy = new EconomyHook(this);
        if (!economy.hook() && getConfig().getBoolean("economy.require-vault", true)) {
            getLogger().severe("Vault không khả dụng hoặc không có Economy provider. Vô hiệu hoá plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.shopManager = new ShopManager(this);
        shopManager.loadAll();

        var cmd = new HyperShopCommand(this);
        getCommand("hypershop").setExecutor(cmd);
        getCommand("hypershop").setTabCompleter(cmd);

        Bukkit.getPluginManager().registerEvents(shopManager, this);

        long interval = getConfig().getLong("autosave-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, shopManager::asyncSaveDirty, interval, interval);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.hypershop.expansion.ShopPlaceholderExpansion(this).register();
            getLogger().info("Đã phát hiện PlaceholderAPI — HyperShop đã đăng ký placeholder");
        }

        getLogger().info("HyperShop enabled.");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) shopManager.asyncSaveDirty();
        getLogger().info("HyperShop disabled.");
    }

    public static HyperShopPlugin get() { return instance; }
    public EconomyHook economy() { return economy; }
    public ShopManager shops() { return shopManager; }
    public Messages msg() { return messages; }
}
