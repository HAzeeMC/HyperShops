package com.hypershop.economy;

import com.hypershop.HyperShopPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {
    private final HyperShopPlugin plugin;
    private Economy economy;

    public EconomyHook(HyperShopPlugin plugin) { this.plugin = plugin; }

    public boolean hook() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean withdraw(Player p, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(p, amount).transactionSuccess();
    }

    public boolean deposit(Player p, double amount) {
        if (economy == null) return false;
        return economy.depositPlayer(p, amount).transactionSuccess();
    }

    public double balance(Player p) { return economy != null ? economy.getBalance(p) : 0.0; }
}
