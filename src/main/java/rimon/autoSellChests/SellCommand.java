package rimon.autoSellChests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Collections;

public class SellCommand implements CommandExecutor {
    private final AutoSellChests plugin;

    public SellCommand(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender;

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("claimall")) {
            double totalToClaim = 0;
            int chestCount = 0;

            for (SellChest chest : plugin.getChestManager().getAllChests()) {
                if (chest.getOwner().equals(p.getUniqueId())) {
                    if (chest.getStoredMoney() > 0) {
                        totalToClaim += chest.getStoredMoney();
                        chest.resetMoney();
                        chestCount++;
                    }
                }
            }

            if (totalToClaim > 0) {
                AutoSellChests.getEconomy().depositPlayer(p, totalToClaim);
                p.sendMessage("§a§lCLAIM SUCCESS! §7You collected §a$" + String.format("%.2f", totalToClaim) + " §7from §e" + chestCount + " §7chests.");
            } else {
                p.sendMessage("§cYou don't have any money stored in your chests to claim!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!p.hasPermission("autosellchests.admin")) {
                p.sendMessage("§cYou do not have permission to use admin commands.");
                return true;
            }

            if (args.length < 3) {
                p.sendMessage("§cUsage: /asc give <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                p.sendMessage("§cPlayer '" + args[1] + "' not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cInvalid amount. Please use a number.");
                return true;
            }

            ItemStack item = new ItemStack(Material.TRAPPED_CHEST, amount);
            ItemMeta meta = item.getItemMeta();

            String name = plugin.getConfig().getString("chest-name", "&a&lAutoSell Chest").replace("&", "§");
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList("§7Place this to start automatically selling items!"));

            item.setItemMeta(meta);

            target.getInventory().addItem(item);
            p.sendMessage("§aSent " + amount + "x AutoSell Chests to " + target.getName());
            target.sendMessage("§aYou received " + amount + "x AutoSell Chests!");
            return true;
        }

        sendHelp(p);
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage("§8§m---------------------------------");
        p.sendMessage("§a§lAutoSell Chests Help");
        p.sendMessage("§e/asc claimall §7- Claim money from all your chests.");
        if (p.hasPermission("autosellchests.admin")) {
            p.sendMessage("§e/asc give <p> <amt> §7- Give chests (Admin).");
        }
        p.sendMessage("§8§m---------------------------------");
    }
}