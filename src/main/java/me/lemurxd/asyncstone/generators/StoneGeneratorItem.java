package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StoneGeneratorItem {

    // Używaj tego klucza wszędzie (nawet w AsyncStoneCommand!), żeby uniknąć literówek
    public static final NamespacedKey GENERATOR_ID_KEY = new NamespacedKey(AsyncStone.getInstance(), "generator_id");

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
            // Ustawiamy kolorową nazwę
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', settings.getName()));

            // Ustawiamy kolorowe lore (jeśli istnieje)
            if (settings.getLore() != null && !settings.getLore().isEmpty()) {
                List<String> coloredLore = settings.getLore().stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(coloredLore);
            }

            // Zapisujemy ID stoniarki (np. "stoniarka-zwykla") bezpiecznie w NBT przedmiotu
            meta.getPersistentDataContainer().set(GENERATOR_ID_KEY, PersistentDataType.STRING, generatorId);

            item.setItemMeta(meta);
        }

        return item;
    }
}

