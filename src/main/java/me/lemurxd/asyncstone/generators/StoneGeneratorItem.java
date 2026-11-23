package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class StoneGeneratorItem {

    public static final NamespacedKey GENERATOR_ID_KEY = new NamespacedKey("asyncstone", "generator_id");

    public static ItemStack create(String generatorId) {
        AsyncStone plugin = AsyncStone.getInstance();
        GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorId);

        if (settings == null) {
            plugin.getLogger().warning("Cannot create item! Generator with ID '" + generatorId + "' does not exist.");
            return null;
        }

        ItemStack item = new ItemStack(settings.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            plugin.getLogger().warning("Cannot create item! Generator '" + generatorId + "' has an invalid material: " + settings.getMaterial());
            return null;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', settings.getName()));

        if (settings.getLore() != null && !settings.getLore().isEmpty()) {
            List<String> coloredLore = settings.getLore().stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }

        meta.getPersistentDataContainer().set(GENERATOR_ID_KEY, PersistentDataType.STRING, generatorId);
        item.setItemMeta(meta);

        return item;
    }

    // Read-only PDC view: no ItemMeta copy, cheap enough for every BlockPlaceEvent.
    public static String getGeneratorId(ItemStack item) {
        if (item == null || item.isEmpty()) return null;
        return item.getPersistentDataContainer().get(GENERATOR_ID_KEY, PersistentDataType.STRING);
    }
}
