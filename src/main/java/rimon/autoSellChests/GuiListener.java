package rimon.autoSellChests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {
    private final AutoSellChests plugin;

    public GuiListener(AutoSellChests plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiManager)) return;
        e.setCancelled(true);

        GuiManager gui = (GuiManager) e.getInventory().getHolder();
        SellChest chest = gui.getActiveChest();
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;
        Material type = e.getCurrentItem().getType();
        if (type == Material.GOLD_INGOT) {
            double money = chest.getStoredMoney();
            if (money > 0) {
                AutoSellChests.getEconomy().depositPlayer(p, money);
                chest.resetMoney();
                p.sendMessage("§aClaimed $" + String.format("%.2f", money));
                p.closeInventory();
            } else {
                p.sendMessage("§cNo money to claim.");
            }
        }
        else if (type == Material.LIME_DYE || type == Material.GRAY_DYE) {
            chest.setActive(!chest.isActive());
            new GuiManager(plugin).openChestGui(p, chest);
        }
        else if (type == Material.ARMOR_STAND) {
            chest.setHologramEnabled(!chest.isHologramEnabled());
            new GuiManager(plugin).openChestGui(p, chest);
        }
        else if (type == Material.SUGAR) {
            int nextLvl = chest.getSpeedLevel() + 1;
            double cost = plugin.getConfigManager().getSpeedCost(nextLvl);
            if (cost == -1) {
                p.sendMessage("§cMax level reached!");
                return;
            }
            if (AutoSellChests.getEconomy().getBalance(p) >= cost) {
                AutoSellChests.getEconomy().withdrawPlayer(p, cost);
                chest.setSpeedLevel(nextLvl);
                p.sendMessage("§aSpeed upgraded!");
                new GuiManager(plugin).openChestGui(p, chest);
            } else {
                p.sendMessage("§cInsufficient funds.");
            }
        }
        else if (type == Material.DIAMOND) {
            int nextLvl = chest.getMultiLevel() + 1;
            double cost = plugin.getConfigManager().getMultiCost(nextLvl);
            if (cost == -1) {
                p.sendMessage("§cMax level reached!");
                return;
            }
            if (AutoSellChests.getEconomy().getBalance(p) >= cost) {
                AutoSellChests.getEconomy().withdrawPlayer(p, cost);
                chest.setMultiLevel(nextLvl);
                p.sendMessage("§aMultiplier upgraded!");
                new GuiManager(plugin).openChestGui(p, chest);
            } else {
                p.sendMessage("§cInsufficient funds.");
            }
        }
    }
}