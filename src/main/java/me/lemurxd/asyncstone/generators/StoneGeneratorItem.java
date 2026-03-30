package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class StoneGeneratorItem {

    public static final NamespacedKey GENERATOR_KEY = new NamespacedKey(AsyncStone.getInstance(), "is_generator");

    public static ItemStack create() {
        String materialName = Config.GENERATOR_ITEM_MATERIAL.getString().toUpperCase();
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            AsyncStone.getInstance().getLogger().warning("Niepoprawny material stoniarki w configu: " + materialName + "! Uzyto domyslnego END_STONE.");
            material = Material.END_STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(Config.GENERATOR_ITEM_NAME.getString());

            meta.setLore(Config.GENERATOR_ITEM_LORE.getStringList());

            meta.getPersistentDataContainer().set(GENERATOR_KEY, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

        return item;
    }
}

