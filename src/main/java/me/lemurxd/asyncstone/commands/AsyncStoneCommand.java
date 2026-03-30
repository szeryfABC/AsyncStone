package me.lemurxd.asyncstone.commands;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
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
                File configFile = new File(plugin.getDataFolder(), "config.yml");
                Config.load(configFile);

                sender.sendMessage("§a[AsyncStone] Configuration successfully reloaded!");
                break;

            case "help":
                sendHelpMenu(sender);
                break;

            default:
                sender.sendMessage("§cUnknown subcommand. Type §e/asyncstone help §cfor a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§e§lAsyncStone Admin Help:");
        sender.sendMessage("§8» §e/asyncstone reload §7- Reloads the config.yml");
        sender.sendMessage("§8» §e/asyncstone help §7- Shows this menu");
        sender.sendMessage("§8§m----------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("asyncstone.admin")) {
            String partial = args[0].toLowerCase();
            if ("reload".startsWith(partial)) completions.add("reload");
            if ("help".startsWith(partial)) completions.add("help");
        }

        return completions;
    }

}
