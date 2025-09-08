package com.hypershop.model;

import org.bukkit.configuration.ConfigurationSection;

public enum PriceModel { STATIC, PERCENT, STOCK, INHERIT;

    public static class Dynamic {
        public final PriceModel mode;
        public final double upPerSale, downPerBuy, minMul, maxMul;
        public final int baseStock; public final double elasticity, minStockMul, maxStockMul;
        public Dynamic(PriceModel mode, double upPerSale, double downPerBuy, double minMul, double maxMul, int baseStock, double elasticity, double minStockMul, double maxStockMul) {
            this.mode = mode; this.upPerSale = upPerSale; this.downPerBuy = downPerBuy; this.minMul = minMul; this.maxMul = maxMul; this.baseStock = baseStock; this.elasticity = elasticity; this.minStockMul = minStockMul; this.maxStockMul = maxStockMul;
        }
        public static Dynamic from(ConfigurationSection cfg) {
            var mode = PriceModel.valueOf(cfg.getString("pricing.mode", "STATIC").toUpperCase());
            var p = cfg.getConfigurationSection("pricing.percent");
            var s = cfg.getConfigurationSection("pricing.stock");
            return new Dynamic(
                mode,
                p != null ? p.getDouble("up-per-sale", 0.01) : 0.01,
                p != null ? p.getDouble("down-per-buy", 0.01) : 0.01,
                p != null ? p.getDouble("min-multiplier", 0.5) : 0.5,
                p != null ? p.getDouble("max-multiplier", 2.0) : 2.0,
                s != null ? s.getInt("base-stock", 100) : 100,
                s != null ? s.getDouble("elasticity", 0.15) : 0.15,
                s != null ? s.getDouble("min-price-multiplier", 0.5) : 0.5,
                s != null ? s.getDouble("max-price-multiplier", 2.0) : 2.0
            );
        }
    }
}
