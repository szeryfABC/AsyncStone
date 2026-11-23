package me.lemurxd.asyncstone.commands;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            String version = plugin.getPluginMeta().getVersion();
            Config.MSG_INFO.getLegacyStringList().forEach(line -> sender.sendMessage(line.replace("<version>", version)));
            return true;
        }

        if (!sender.hasPermission("asyncstone.admin")) {
            sender.sendMessage(Config.MSG_NO_PERMISSION.getLegacyString());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.softReload();
                sender.sendMessage(Config.MSG_RELOADED.getLegacyString());
            }
            case "give" -> give(sender, args);
            case "help" -> Config.MSG_HELP.getLegacyStringList().forEach(sender::sendMessage);
            default -> sender.sendMessage(Config.MSG_UNKNOWN_COMMAND.getLegacyString());
        }

        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Config.MSG_GIVE_USAGE.getLegacyString());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Config.MSG_GIVE_PLAYER_OFFLINE.getLegacyString().replace("<player>", args[1]));
            return;
        }

        String generatorId = args[2];

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                sender.sendMessage(Config.MSG_GIVE_INVALID_AMOUNT.getLegacyString());
                return;
            }
        }

        ItemStack item = StoneGeneratorItem.create(generatorId);
        if (item == null) {
            sender.sendMessage(Config.MSG_GIVE_UNKNOWN_GENERATOR.getLegacyString().replace("<id>", generatorId));
            return;
        }

        item.setAmount(amount);
        // Full inventory: drop the rest at the player's feet instead of losing it.
        target.getInventory().addItem(item).values()
                .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));

        String amountText = String.valueOf(amount);
        sender.sendMessage(Config.MSG_GIVE_SENT.getLegacyString()
                .replace("<amount>", amountText)
                .replace("<id>", generatorId)
                .replace("<player>", target.getName()));
        target.sendMessage(Config.MSG_GIVE_RECEIVED.getLegacyString().replace("<amount>", amountText));
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
