package me.lemurxd.asyncstone.generators;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class StoneGeneratorItem {

    public static final String GENERATOR_ID_KEY = "generator_id";

    public static ItemStack create(String generatorId) {
        AsyncStone plugin = AsyncStone.getInstance();
        GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorId);

        if (settings == null) {
            plugin.getLogger().warning("Nie mozna stworzyc przedmiotu! Stoniarka o ID '" + generatorId + "' nie istnieje.");
            return null;
        }

        ItemStack item = new ItemStack(settings.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', settings.getName()));

            if (settings.getLore() != null && !settings.getLore().isEmpty()) {
                List<String> coloredLore = settings.getLore().stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        NBTItem nbti = new NBTItem(item);
        nbti.setString(GENERATOR_ID_KEY, generatorId);

        return nbti.getItem();
    }
}

