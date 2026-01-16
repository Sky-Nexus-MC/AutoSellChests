package rimon.autoSellChests;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestManager {
    private final AutoSellChests plugin;
    private final Map<Location, SellChest> chests = new HashMap<>();
    private final File dataFile;

    public ChestManager(AutoSellChests plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadChests();
    }
    public Collection<SellChest> getAllChests() {
        return chests.values();
    }

    public void tickChests() {
        long now = System.currentTimeMillis();
        for (SellChest chest : chests.values()) {
            if (!chest.isActive()) continue;
            if (chest.getLocation().getWorld() == null) continue;
            long interval = plugin.getConfigManager().getSpeedInterval(chest.getSpeedLevel()) * 1000L;
            if (now - chest.getLastSellTime() < interval) continue;

            sellContent(chest);
            chest.setLastSellTime(now);
        }
    }

    public void updateHolograms() {
        for (SellChest chest : chests.values()) {
            if (chest.getLocation().getWorld() != null && chest.getLocation().getChunk().isLoaded()) {
                chest.updateHologramLines();
            }
        }
    }

    private void sellContent(SellChest chest) {
        Block block = chest.getLocation().getBlock();
        if (block.getType() != Material.TRAPPED_CHEST && block.getType() != Material.CHEST) return;
        if (!(block.getState() instanceof Chest)) return;
        Chest chestBlock = (Chest) block.getState();
        Inventory inv = chestBlock.getBlockInventory();

        double totalValue = 0;
        int itemsSold = 0;
        double multiplier = plugin.getConfigManager().getMultiplier(chest.getMultiLevel());

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            double price = plugin.getConfigManager().getPrice(item.getType());
            if (price > 0) {
                double value = price * item.getAmount() * multiplier;
                totalValue += value;
                itemsSold += item.getAmount();
                inv.remove(item);
            }
        }

        if (totalValue > 0) {
            chest.addMoney(totalValue);
            chest.addItemsSold(itemsSold);
        }
    }

    public boolean createChest(Player p, Location loc) {
        int limit = getLimit(p);
        int current = (int) chests.values().stream().filter(c -> c.getOwner().equals(p.getUniqueId())).count();

        if (!p.hasPermission("autosellchests.admin") && current >= limit) {
            p.sendMessage("§cYou have reached your limit of AutoSell Chests!");
            return false;
        }

        SellChest chest = new SellChest(p.getUniqueId(), loc);
        chests.put(loc, chest);
        chest.createHologram();
        saveChests();
        return true;
    }

    public void removeChest(Location loc) {
        if (chests.containsKey(loc)) {
            chests.get(loc).removeHologram();
            chests.remove(loc);
            saveChests();
        }
    }

    public SellChest getChest(Location loc) {
        return chests.get(loc);
    }

    public void removeAllHolograms() {
        chests.values().forEach(SellChest::removeHologram);
    }

    private int getLimit(Player p) {
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            String perm = info.getPermission();
            if (perm.startsWith("autosellchests.amount.")) {
                try {
                    return Integer.parseInt(perm.substring("autosellchests.amount.".length()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 1;
    }
    public void saveChests() {
        YamlConfiguration data = new YamlConfiguration();

        for (Map.Entry<Location, SellChest> entry : chests.entrySet()) {
            SellChest chest = entry.getValue();
            String key = locToString(chest.getLocation());

            ConfigurationSection section = data.createSection("chests." + key);
            section.set("lifetime", chest.getLifetimeEarnings());
            section.set("owner", chest.getOwner().toString());
            section.set("world", chest.getLocation().getWorld().getName());
            section.set("x", chest.getLocation().getX());
            section.set("y", chest.getLocation().getY());
            section.set("z", chest.getLocation().getZ());

            section.set("money", chest.getStoredMoney());
            section.set("sold", chest.getItemsSold());
            section.set("speed", chest.getSpeedLevel());
            section.set("multi", chest.getMultiLevel());
            section.set("active", chest.isActive());
            section.set("holo", chest.isHologramEnabled());
        }

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save chest data!");
            e.printStackTrace();
        }
    }

    public void loadChests() {
        if (!dataFile.exists()) return;
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = data.getConfigurationSection("chests");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection c = section.getConfigurationSection(key);
            if (c == null) continue;

            try {
                String worldName = c.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Skipping chest in unloaded world: " + worldName);
                    continue;
                }

                double x = c.getDouble("x");
                double y = c.getDouble("y");
                double z = c.getDouble("z");
                Location loc = new Location(world, x, y, z);
                UUID owner = UUID.fromString(c.getString("owner"));

                SellChest chest = new SellChest(owner, loc);
                chest.addMoney(c.getDouble("money"));
                chest.addItemsSold((int) c.getLong("sold"));
                chest.setSpeedLevel(c.getInt("speed"));
                chest.setMultiLevel(c.getInt("multi"));
                chest.setActive(c.getBoolean("active"));
                chest.setLifetimeEarnings(c.getDouble("lifetime"));

                chests.put(loc, chest);
                chest.setHologramEnabled(c.getBoolean("holo"));

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load a chest: " + key);
                e.printStackTrace();
            }
        }
    }

    private String locToString(Location l) {
        return l.getWorld().getName() + "_" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ();
    }
}
