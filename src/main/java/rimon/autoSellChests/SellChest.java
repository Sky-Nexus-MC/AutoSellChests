package rimon.autoSellChests;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SellChest {
    private final UUID id;
    private final UUID ownerUUID;
    private final Location location;

    private double storedMoney;
    private long itemsSold;
    private int speedLevel = 1;
    private int multiLevel = 1;
    private boolean active = true;
    private boolean hologramEnabled = true;
    private double lifetimeEarnings;

    private long lastSellTime;
    private transient Hologram hologram;

    public SellChest(UUID owner, Location loc) {
        this.id = UUID.randomUUID();
        this.ownerUUID = owner;
        this.location = loc;
        this.storedMoney = 0;
        this.itemsSold = 0;
        this.lastSellTime = System.currentTimeMillis();
    }

    public void createHologram() {
        if (!hologramEnabled || location.getWorld() == null) return;
        String holoName = "asc_" + id.toString();

        if (DHAPI.getHologram(holoName) != null) DHAPI.removeHologram(holoName);

        Location holoLoc = location.clone().add(0.5, 3, 0.5);

        List<String> lines = Arrays.asList("Line1", "Line2", "Line3", "Line4", "Line5", "Line6");
        this.hologram = DHAPI.createHologram(holoName, holoLoc, lines);

        updateHologramLines();
    }

    public void updateHologramLines() {
        if (!hologramEnabled) return;

        String holoName = "asc_" + id.toString();
        if (this.hologram == null || DHAPI.getHologram(holoName) == null) {
            createHologram();
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
        double multiplier = AutoSellChests.getInstance().getConfigManager().getMultiplier(multiLevel);
        long nextSell = getNextSellSeconds();

        DHAPI.setHologramLine(hologram, 0, "&b&l" + ownerName + "'s Chest");
        DHAPI.setHologramLine(hologram, 1, "&fStored: &a$" + String.format("%.2f", storedMoney));
        DHAPI.setHologramLine(hologram, 2, "&fLifetime: &6$" + String.format("%.2f", lifetimeEarnings));
        DHAPI.setHologramLine(hologram, 3, "&fSold: &e" + itemsSold);
        DHAPI.setHologramLine(hologram, 4, "&fMultiplier: &d" + multiplier + "x");
        DHAPI.setHologramLine(hologram, 5, "&7Next Sell: &f" + (nextSell <= 0 ? "NOW" : nextSell + "s"));
    }

    public void removeHologram() {
        if (hologram != null) {
            DHAPI.removeHologram(hologram.getName());
            hologram = null;
        }
    }

    public long getNextSellSeconds() {
        long intervalMillis = AutoSellChests.getInstance().getConfigManager().getSpeedInterval(speedLevel) * 1000L;
        long timeSince = System.currentTimeMillis() - lastSellTime;
        long remaining = intervalMillis - timeSince;
        return remaining < 0 ? 0 : remaining / 1000;
    }

    public UUID getOwner() { return ownerUUID; }
    public Location getLocation() { return location; }
    public double getStoredMoney() { return storedMoney; }
    public void addMoney(double amt) {
        this.storedMoney += amt;
        this.lifetimeEarnings += amt;
    }
    public void resetMoney() { this.storedMoney = 0; }
    public void addItemsSold(int amt) { this.itemsSold += amt; }
    public int getSpeedLevel() { return speedLevel; }
    public void setSpeedLevel(int l) { this.speedLevel = l; }
    public int getMultiLevel() { return multiLevel; }
    public void setMultiLevel(int l) { this.multiLevel = l; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isHologramEnabled() { return hologramEnabled; }
    public long getItemsSold() { return itemsSold; }
    public void setHologramEnabled(boolean h) {
        this.hologramEnabled = h;
        if(h) createHologram(); else removeHologram();
    }
    public long getLastSellTime() { return lastSellTime; }
    public void setLastSellTime(long time) { this.lastSellTime = time; }
    public double getLifetimeEarnings() { return lifetimeEarnings; }
    public void setLifetimeEarnings(double earnings) { this.lifetimeEarnings = earnings; }
}