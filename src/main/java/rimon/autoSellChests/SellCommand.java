package rimon.autoSellChests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SellCommand implements CommandExecutor, TabCompleter {
    private final AutoSellChests plugin;

    public SellCommand(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("claimall")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can claim money from chests.");
                return true;
            }

            Player p = (Player) sender;
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
            if (!sender.hasPermission("autosellchests.admin")) {
                sender.sendMessage("§cYou do not have permission to use admin commands.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§cUsage: /asc give <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[1] + "' not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount. Please use a number.");
                return true;
            }

            ItemStack item = new ItemStack(Material.TRAPPED_CHEST, amount);
            ItemMeta meta = item.getItemMeta();

            String name = plugin.getConfig().getString("chest-name", "&a&lAutoSell Chest").replace("&", "§");
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList("§7Place this to start automatically selling items!"));

            item.setItemMeta(meta);

            target.getInventory().addItem(item);
            sender.sendMessage("§aSent " + amount + "x AutoSell Chests to " + target.getName());
            target.sendMessage("§aYou received " + amount + "x AutoSell Chests!");
            return true;
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("claimall");
            if (sender.hasPermission("autosellchests.admin")) {
                completions.add("give");
            }
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("16");
            completions.add("64");
            return filterCompletions(completions, args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m---------------------------------");
        sender.sendMessage("§a§lAutoSell Chests Help");
        sender.sendMessage("§e/asc claimall §7- Claim money from all your chests.");
        if (sender.hasPermission("autosellchests.admin")) {
            sender.sendMessage("§e/asc give <p> <amt> §7- Give chests (Admin/Console).");
        }
        sender.sendMessage("§8§m---------------------------------");
    }
}