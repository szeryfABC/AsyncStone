package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.AsyncStone;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class StoneGeneratorItem {

    public static final NamespacedKey GENERATOR_KEY = new NamespacedKey(AsyncStone.getInstance(), "is_generator");

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.END_STONE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§e§lStoniarka");
            meta.setLore(Arrays.asList(
                    "§7Postaw ten blok, aby",
                    "§7rozpocząć generowanie kamienia."
            ));

            meta.getPersistentDataContainer().set(GENERATOR_KEY, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

        return item;
    }
}

