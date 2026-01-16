package rimon.autoSellChests;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSellChests extends JavaPlugin {

    private static AutoSellChests instance;
    private static Economy econ = null;
    private ChestManager chestManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.chestManager = new ChestManager(this);

        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getCommand("autosellchests").setExecutor(new SellCommand(this));

        startTasks();
    }

    @Override
    public void onDisable() {
        if (chestManager != null) {
            chestManager.saveChests();
            chestManager.removeAllHolograms();
        }
    }

    private void startTasks() {

        getServer().getScheduler().runTaskTimer(this, () -> chestManager.tickChests(), 20L, getConfig().getLong("check-interval"));
        getServer().getScheduler().runTaskTimer(this, () -> chestManager.updateHolograms(), 20L, getConfig().getLong("hologram-update-interval"));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static AutoSellChests getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }
    public ChestManager getChestManager() { return chestManager; }
    public ConfigManager getConfigManager() { return configManager; }
}
