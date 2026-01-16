package rimon.autoSellChests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;


public class GuiManager implements InventoryHolder {
    private final AutoSellChests plugin;
    private SellChest activeChest;

    public GuiManager(AutoSellChests plugin) { this.plugin = plugin; }

    public void openChestGui(Player p, SellChest chest) {
        Inventory inv = Bukkit.createInventory(this, 27, plugin.getConfig().getString("gui-title"));
        this.activeChest = chest;

        ItemStack info = createItem(Material.GOLD_INGOT, "§e§lClaim Money",
                "§7Stored: §a$" + String.format("%.2f", chest.getStoredMoney()),
                "§7Click to claim!");
        inv.setItem(11, info);

        ItemStack toggle = createItem(chest.isActive() ? Material.LIME_DYE : Material.GRAY_DYE,
                "§6Toggle Selling", "§7Status: " + (chest.isActive() ? "§aON" : "§cOFF"));
        inv.setItem(13, toggle);

        ItemStack holo = createItem(Material.ARMOR_STAND, "§bToggle Hologram",
                "§7Status: " + (chest.isHologramEnabled() ? "§aVisible" : "§cHidden"));
        inv.setItem(15, holo);

        double speedCost = plugin.getConfigManager().getSpeedCost(chest.getSpeedLevel() + 1);
        ItemStack speed = createItem(Material.SUGAR, "§fUpgrade Speed",
                "§7Current Level: " + chest.getSpeedLevel(),
                "§7Cost: §a$" + speedCost);
        inv.setItem(21, speed);

        double multiCost = plugin.getConfigManager().getMultiCost(chest.getMultiLevel() + 1);
        ItemStack multi = createItem(Material.DIAMOND, "§dUpgrade Multiplier",
                "§7Current Level: " + chest.getMultiLevel(),
                "§7Cost: §a$" + multiCost);
        inv.setItem(23, multi);

        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() { return null; }
    public SellChest getActiveChest() { return activeChest; }
}
