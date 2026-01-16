package rimon.autoSellChests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ChestListener implements Listener {
    private final AutoSellChests plugin;

    public ChestListener(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        String configName = plugin.getConfig().getString("chest-name", "&a&lAutoSell Chest").replace("&", "§");

        if (item.getType() == Material.TRAPPED_CHEST && item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals(configName)) {

            if (plugin.getChestManager().createChest(e.getPlayer(), e.getBlock().getLocation())) {
                SellChest chest = plugin.getChestManager().getChest(e.getBlock().getLocation());
                PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
                NamespacedKey speedKey = new NamespacedKey(plugin, "speed_lvl");

                if (data.has(speedKey, PersistentDataType.INTEGER)) {
                    chest.setSpeedLevel(data.get(speedKey, PersistentDataType.INTEGER));
                    chest.setMultiLevel(data.get(new NamespacedKey(plugin, "multi_lvl"), PersistentDataType.INTEGER));
                }
                e.getPlayer().sendMessage("§aAutoSell Chest placed successfully!");
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        SellChest chest = plugin.getChestManager().getChest(e.getBlock().getLocation());
        if (chest != null) {

            ItemStack drop = new ItemStack(Material.TRAPPED_CHEST);
            ItemMeta meta = drop.getItemMeta();
            meta.setDisplayName(plugin.getConfig().getString("chest-name").replace("&", "§"));
            org.bukkit.persistence.PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(new org.bukkit.NamespacedKey(plugin, "speed_lvl"), org.bukkit.persistence.PersistentDataType.INTEGER, chest.getSpeedLevel());
            data.set(new org.bukkit.NamespacedKey(plugin, "multi_lvl"), org.bukkit.persistence.PersistentDataType.INTEGER, chest.getMultiLevel());
            meta.setLore(java.util.Arrays.asList(
                    "§7Speed Level: §f" + chest.getSpeedLevel(),
                    "§7Multi Level: §f" + chest.getMultiLevel()
            ));
            drop.setItemMeta(meta);
            e.setDropItems(false);
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);

            plugin.getChestManager().removeChest(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || !e.getAction().isRightClick()) return;
        if (e.getPlayer().isSneaking()) return;

        Block b = e.getClickedBlock();
        if (b == null) return;
        SellChest chest = plugin.getChestManager().getChest(b.getLocation());
        if (chest != null) {
            e.setCancelled(true);

            if (!chest.getOwner().equals(e.getPlayer().getUniqueId()) && !e.getPlayer().hasPermission("autosellchests.admin")) {
                e.getPlayer().sendMessage("§cThis is not your chest.");
                return;
            }

            new GuiManager(plugin).openChestGui(e.getPlayer(), chest);
        }
    }
}
