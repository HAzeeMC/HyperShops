package com.hypershop.view;

import com.hypershop.HyperShopPlugin;
import com.hypershop.model.Shop;
import com.hypershop.model.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopEditGUI implements InventoryHolder {
    private final HyperShopPlugin plugin = HyperShopPlugin.get();
    private final Shop shop;
    private final Inventory inv;

    public ShopEditGUI(Shop shop) {
        this.shop = shop;
        int rows = 6;
        String title = "HyperShop Editor | " + shop.getName();
        this.inv = GUIUtil.createInv(title, rows, this);
        render();
    }

    public static void open(Player p, Shop shop) {
        ShopEditGUI gui = new ShopEditGUI(shop);
        p.openInventory(gui.getInventory());
    }

    private void render() {
        var filler = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack fill = new ItemStack(filler);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

        int slot = 10;
        for (ShopItem it : new ArrayList<>(shop.getItems().values())) {
            var mat = it.getMaterial();
            if (mat == Material.AIR) continue;
            List<String> lore = new ArrayList<>();
            lore.add("&7Giá mua (base): &a" + it.getBaseBuy());
            lore.add("&7Giá bán (base): &c" + it.getBaseSell());
            lore.add("&8Shift + Click để Xoá | Click phải để xem hướng dẫn chỉnh giá");
            ItemStack icon = GUIUtil.item(mat, "&f" + mat.name(), lore);
            inv.setItem(slot, icon);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot >= inv.getSize() - 9) break;
        }

        // control buttons
        inv.setItem(4, GUIUtil.item(Material.PAPER, "&eHướng dẫn", List.of("&7- Cầm 1 vật phẩm trên tay và click vào slot trống để thêm", "&7- Shift+Click vào item để xóa", "&7- Dùng /hypershop setprice để chỉnh giá chi tiết")));
        inv.setItem(8, GUIUtil.item(Material.BARRIER, "&cĐóng", List.of("&7Đóng cửa sổ editor")));
    }

    @Override
    public Inventory getInventory() { return inv; }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        HumanEntity who = e.getWhoClicked();
        if (!(who instanceof Player p)) return;

        int slot = e.getRawSlot();
        if (slot >= inv.getSize()) return;

        if (clicked.getType() == Material.BARRIER) {
            p.closeInventory();
            return;
        }

        // If clicked on PAPER (help) do nothing
        if (clicked.getType() == Material.PAPER) return;

        // If player holds an item and clicks on a filler (empty), add it
        ItemStack cursor = p.getInventory().getItemInMainHand();
        if (cursor != null && cursor.getType() != Material.AIR && e.getClickedInventory() == inv && GUIUtil.item(cursor.getType(),"","") != null) {
            // Add item to shop with default prices if slot was filler
            if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                ShopItem si = shop.getOrCreateItem(cursor.getType());
                si.setBaseBuy(10.0);
                si.setBaseSell(2.5);
                plugin.shops().markDirty(shop.getId());
                p.sendMessage(plugin.msg().fmt("opened").replace("%shop%", shop.getId()).replace("Đã mở cửa hàng: ","Đã thêm vật phẩm: "));
                p.closeInventory();
                ShopEditGUI.open(p, shop);
                return;
            }
        }

        // If clicking existing item:
        ShopItem item = shop.getItem(clicked.getType());
        if (item == null) return;

        // Shift-click to remove
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            shop.getItems().remove(item.getMaterial());
            plugin.shops().markDirty(shop.getId());
            p.sendMessage("Đã xóa " + item.getMaterial().name() + " khỏi shop.");
            p.closeInventory();
            ShopEditGUI.open(p, shop);
            return;
        }

        // Right-click to show quick instructions to edit price via command
        if (e.getClick() == ClickType.RIGHT) {
            p.sendMessage("Dùng: /hypershop setprice " + shop.getId() + " " + item.getMaterial().name() + " <buy> [sell]");
            return;
        }
    }
}
