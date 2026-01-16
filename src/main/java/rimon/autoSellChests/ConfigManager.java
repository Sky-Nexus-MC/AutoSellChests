package rimon.autoSellChests;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final AutoSellChests plugin;
    private File worthFile;
    private FileConfiguration worthConfig;

    public ConfigManager(AutoSellChests plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        createWorthFile();
    }

    private void createWorthFile() {
        worthFile = new File(plugin.getDataFolder(), "worth.yml");
        if (!worthFile.exists()) {
            plugin.saveResource("worth.yml", false);
        }
        worthConfig = YamlConfiguration.loadConfiguration(worthFile);
    }

    public void reloadWorth() {
        worthConfig = YamlConfiguration.loadConfiguration(worthFile);
    }

    public double getPrice(Material mat) {
        return worthConfig.getDouble(mat.name(), 0.0);
    }


    public double getSpeedCost(int level) {
        String path = "speed-upgrades." + level + ".cost";
        if (!plugin.getConfig().contains("speed-upgrades." + level)) return -1;
        return plugin.getConfig().getDouble(path);
    }

    public long getSpeedInterval(int level) {
        return plugin.getConfig().getLong("speed-upgrades." + level + ".interval", 60);
    }

    public double getMultiCost(int level) {
        String path = "multiplier-upgrades." + level + ".cost";
        if (!plugin.getConfig().contains("multiplier-upgrades." + level)) return -1;
        return plugin.getConfig().getDouble(path);
    }

    public double getMultiplier(int level) {
        return plugin.getConfig().getDouble("multiplier-upgrades." + level + ".amount", 1.0);
    }
}
