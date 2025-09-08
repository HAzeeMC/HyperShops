package com.hypershop.view;

import com.hypershop.HyperShopPlugin;
import com.hypershop.model.Shop;
import com.hypershop.model.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI implements InventoryHolder {
    private final HyperShopPlugin plugin = HyperShopPlugin.get();
    private final Shop shop;
    private final int page;
    private final Inventory inv;

    public ShopGUI(Shop shop, int page) {
        this.shop = shop; this.page = page;
        var cfg = plugin.getConfig();
        int rows = Math.max(1, Math.min(6, cfg.getInt("ui.rows", 6)));
        String title = org.bukkit.ChatColor.translateAlternateColorCodes('&', cfg.getString("ui.title", "HyperShop")) + " | " + shop.getName();
        this.inv = GUIUtil.createInv(title, rows, this);
        render();
    }

    public static void open(Player p, Shop shop, int page) {
        ShopGUI gui = new ShopGUI(shop, page);
        p.openInventory(gui.getInventory());
    }

    private void render() {
        var filler = Material.matchMaterial(plugin.getConfig().getString("ui.filler", "GRAY_STAINED_GLASS_PANE"));
        if (filler == null) filler = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack fill = new ItemStack(filler);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

        int slot = 10;
        for (ShopItem it : new ArrayList<>(shop.getItems().values())) {
            var mat = it.getMaterial();
            if (mat == Material.AIR) continue;
            List<String> lore = new ArrayList<>();
            lore.add("&7Mua: &a" + plugin.shops().currentBuy(shop, it));
            lore.add("&7Bán: &c" + plugin.shops().currentSell(shop, it));
            lore.add("&8T-Click: Mua x1 | Shift: x64 | Q: Bán x1");
            ItemStack icon = GUIUtil.item(mat, "&f" + mat.name(), lore);
            inv.setItem(slot, icon);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot >= inv.getSize() - 9) break;
        }
    }

    @Override
    public Inventory getInventory() { return inv; }

    public void handleClick(InventoryClickEvent e) {
        if (!e.getClickedInventory().equals(inv)) return;
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        HumanEntity who = e.getWhoClicked();
        if (!(who instanceof Player p)) return;

        ShopItem item = shop.getItem(clicked.getType());
        if (item == null) return;
        boolean shift = e.isShiftClick();
        boolean dropKey = e.getClick().isKeyboardClick() && e.getClick().name().contains("DROP");
        int amount = shift ? 64 : 1;
        if (dropKey) {
            plugin.shops().sell(p, shop, item, amount);
        } else {
            plugin.shops().buy(p, shop, item, amount);
        }
    }
}
