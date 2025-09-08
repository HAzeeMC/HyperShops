package com.hypershop.model;

import org.bukkit.Material;

public class ShopItem {
    private final Material material;
    private double baseBuy;
    private double baseSell;
    private PriceModel priceMode = PriceModel.INHERIT;
    private int stock = 0;

    public ShopItem(Material material, double baseBuy, double baseSell) {
        this.material = material; this.baseBuy = baseBuy; this.baseSell = baseSell; this.stock = 0;
    }

    public Material getMaterial() { return material; }
    public double getBaseBuy() { return baseBuy; }
    public double getBaseSell() { return baseSell; }
    public void setBaseBuy(double v) { baseBuy = v; }
    public void setBaseSell(double v) { baseSell = v; }
    public PriceModel getPriceMode() { return priceMode; }
    public void setPriceMode(PriceModel mode) { this.priceMode = mode; }
    public int getStock() { return stock; }
    public void addStock(int delta) { this.stock += delta; }
}
