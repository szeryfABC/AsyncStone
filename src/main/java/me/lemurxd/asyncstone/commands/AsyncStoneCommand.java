package me.lemurxd.asyncstone.commands;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AsyncStoneCommand implements CommandExecutor, TabCompleter {

    private final AsyncStone plugin;

    public AsyncStoneCommand(AsyncStone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§8§m----------------------------------------");
            sender.sendMessage("§e§lAsyncStone §7v" + plugin.getDescription().getVersion());
            sender.sendMessage("§7Author: §elemurxd_");
            sender.sendMessage("§7GitHub: §bhttps://github.com/lemurxd_");
            sender.sendMessage("§8§m----------------------------------------");
            return true;
        }

        if (!sender.hasPermission("asyncstone.admin")) {
            sender.sendMessage("§cYou don't have permission to do that!");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                plugin.softReload();
                sender.sendMessage("§a[AsyncStone] Konfiguracja i receptury zostaly przeladowane!");
                break;

            case "give":
                if (args.length < 3) {
                    sender.sendMessage("§cPoprawne uzycie: /asyncstone give <nick> <id_stoniarki> [ilosc]");
                    break;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cGracz o nicku " + args[1] + " nie jest online!");
                    break;
                }

                String generatorId = args[2];
                GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorId);
                if (settings == null) {
                    sender.sendMessage("§cStoniarka o ID '" + generatorId + "' nie istnieje w pliku generators.yml!");
                    break;
                }

                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                        if (amount <= 0) amount = 1;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cIlosc musi byc liczba!");
                        break;
                    }
                }

                ItemStack item = new ItemStack(settings.getMaterial(), amount);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', settings.getName()));

                    NamespacedKey key = new NamespacedKey(plugin, "generator_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, generatorId);

                    item.setItemMeta(meta);
                }

                target.getInventory().addItem(item);
                sender.sendMessage("§a[AsyncStone] Pomyslnie nadano " + amount + "x stoniarka (" + generatorId + ") graczowi " + target.getName() + ".");
                target.sendMessage("§aOtrzymales " + amount + "x stoniarka!");
                break;

            case "help":
                sendHelpMenu(sender);
                break;

            default:
                sender.sendMessage("§cNieznana komenda. Wpisz §e/asyncstone help §caby wyswietlic liste.");
                break;
        }

        return true;
    }

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§e§lAsyncStone Admin Help:");
        sender.sendMessage("§8» §e/asyncstone give <nick> <id> [ilosc] §7- Daje stoniarke graczowi");
        sender.sendMessage("§8» §e/asyncstone reload §7- Przeladowuje konfiguracje");
        sender.sendMessage("§8» §e/asyncstone help §7- Pokazuje to menu");
        sender.sendMessage("§8§m----------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("asyncstone.admin")) {
            return completions;
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("reload".startsWith(partial)) completions.add("reload");
            if ("help".startsWith(partial)) completions.add("help");
            if ("give".startsWith(partial)) completions.add("give");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null;
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String partial = args[2].toLowerCase();

            for (String id : plugin.getGeneratorsConfig().getAllIds()) {
                if (id.toLowerCase().startsWith(partial)) {
                    completions.add(id);
                }
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("16");
            completions.add("64");
        }

        return completions;
    }
}