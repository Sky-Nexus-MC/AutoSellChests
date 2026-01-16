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
        if (!sender.hasPermission("autosellchests.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount.");
                return true;
            }

            ItemStack item = new ItemStack(Material.TRAPPED_CHEST, amount);
            ItemMeta meta = item.getItemMeta();
            String name = plugin.getConfig().getString("chest-name", "&a&lAutoSell Chest").replace("&", "§");
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList("§7Place this to start selling!"));
            item.setItemMeta(meta);

            target.getInventory().addItem(item);
            sender.sendMessage("§aGave " + amount + " AutoSell Chests to " + target.getName());
            return true;
        }

        sender.sendMessage("§cUsage: /asc give <player> <amount>");
        return true;
    }
}