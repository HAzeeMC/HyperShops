package com.hypershop.commands;

import com.hypershop.HyperShopPlugin;
import com.hypershop.view.ShopGUI;
import com.hypershop.view.ShopEditGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class HyperShopCommand implements CommandExecutor, TabCompleter {
    private final HyperShopPlugin plugin;

    public HyperShopCommand(HyperShopPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender, label); return true; }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "open" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Chỉ dùng trong game."); return true; }
                if (!sender.hasPermission("hypershop.open")) { sender.sendMessage(plugin.msg().fmt("no-permission")); return true; }
                String shopId = args.length >= 2 ? args[1] : "hypershop";
                var shop = plugin.shops().getShop(shopId);
                if (shop == null) { sender.sendMessage(plugin.msg().fmt("unknown-shop").replace("%shop%", shopId)); return true; }
                ShopGUI.open((Player)sender, shop, 0);
                sender.sendMessage(plugin.msg().fmt("opened").replace("%shop%", shopId));
            }
            case "reload" -> {
                if (!sender.hasPermission("hypershop.reload")) { sender.sendMessage(plugin.msg().fmt("no-permission")); return true; }
                plugin.reloadConfig();
                plugin.msg().reload();
                plugin.shops().loadAll();
                sender.sendMessage(plugin.msg().fmt("reloaded"));
            }
            case "setprice" -> {
                if (!sender.hasPermission("hypershop.edit")) { sender.sendMessage(plugin.msg().fmt("no-permission")); return true; }
                if (args.length < 4) { sender.sendMessage("/"+label+" setprice <shop> <material> <buy> [sell]"); return true; }
                var shop = plugin.shops().getShop(args[1]);
                if (shop == null) { sender.sendMessage(plugin.msg().fmt("unknown-shop").replace("%shop%", args[1])); return true; }
                try {
                    var mat = org.bukkit.Material.matchMaterial(args[2]);
                    if (mat == null) { sender.sendMessage("Material không hợp lệ."); return true; }
                    double buy = Double.parseDouble(args[3]);
                    Double sell = (args.length >= 5) ? Double.parseDouble(args[4]) : null;
                    var item = shop.getOrCreateItem(mat);
                    item.setBaseBuy(buy);
                    if (sell != null) item.setBaseSell(sell);
                    plugin.shops().markDirty(shop.getId());
                    sender.sendMessage("Đã đặt giá cho " + mat + ": buy=" + buy + (sell!=null?", sell="+sell:"") );
                } catch (NumberFormatException e) { sender.sendMessage("Giá không hợp lệ."); }
            }
            case "edit" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Chỉ dùng trong game."); return true; }
                if (!sender.hasPermission("hypershop.edit")) { sender.sendMessage(plugin.msg().fmt("no-permission")); return true; }
                String shopId = args.length >= 2 ? args[1] : "hypershop";
                var shop = plugin.shops().getShop(shopId);
                if (shop == null) { sender.sendMessage(plugin.msg().fmt("unknown-shop").replace("%shop%", shopId)); return true; }
                ShopEditGUI.open(p, shop);
            }
            default -> help(sender, label);
        }
        return true;
    }

    private void help(CommandSender s, String label) {
        s.sendMessage("§aHyperShop §7v" + plugin.getDescription().getVersion());
        s.sendMessage("§e/"+label+" open [shop] §7- mở shop");
        s.sendMessage("§e/"+label+" edit [shop] §7- chỉnh shop (phân quyền required)");
        s.sendMessage("§e/"+label+" reload §7- reload cấu hình");
        s.sendMessage("§e/"+label+" setprice <shop> <material> <buy> [sell] §7- chỉnh giá nhanh");
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("open", "reload", "setprice", "edit");
        if (args[0].equalsIgnoreCase("open") && args.length == 2) return new ArrayList<>(plugin.shops().getShopIds());
        if (args[0].equalsIgnoreCase("setprice")) {
            if (args.length == 2) return new ArrayList<>(plugin.shops().getShopIds());
            if (args.length == 3) return Arrays.stream(org.bukkit.Material.values()).map(Enum::name).toList();
        }
        return Collections.emptyList();
    }
}
