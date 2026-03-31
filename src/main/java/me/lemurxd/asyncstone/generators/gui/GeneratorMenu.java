package me.lemurxd.asyncstone.generators.gui;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GeneratorMenu implements InventoryHolder {

    private final Inventory inventory;
    private final StoneGenerator generator;

    public GeneratorMenu(Player player, StoneGenerator generator) {
        this.generator = generator;

        String title = Config.GUI_TITLE.getString();
        int size = Config.GUI_SIZE.getInt();

        this.inventory = Bukkit.createInventory(this, size, ChatColor.translateAlternateColorCodes('&', title));

        setupItems();
    }

    private void setupItems() {
        AsyncStone plugin = AsyncStone.getInstance();
        GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generator.getId());
        if (settings == null) return;

        if (settings.getUpgradeData() != null) {
            ItemStack upgradeItem = buildItem(
                    Config.GUI_UPGRADE_MATERIAL.getString(),
                    Config.GUI_UPGRADE_NAME.getString(),
                    Config.GUI_UPGRADE_LORE.getStringList(),
                    formatCost(settings.getUpgradeData().costs())
            );
            inventory.setItem(Config.GUI_UPGRADE_SLOT.getInt(), upgradeItem);
        } else {
            ItemStack maxItem = buildItem(
                    Config.GUI_MAX_MATERIAL.getString(),
                    Config.GUI_MAX_NAME.getString(),
                    Config.GUI_MAX_LORE.getStringList(),
                    null
            );
            inventory.setItem(Config.GUI_MAX_SLOT.getInt(), maxItem);
        }

        ItemStack pickupItem = buildItem(
                Config.GUI_PICKUP_MATERIAL.getString(),
                Config.GUI_PICKUP_NAME.getString(),
                Config.GUI_PICKUP_LORE.getStringList(),
                null
        );
        inventory.setItem(Config.GUI_PICKUP_SLOT.getInt(), pickupItem);
    }


    private ItemStack buildItem(String matName, String name, List<String> loreTemplate, String costReplacement) {
        Material mat = Material.matchMaterial(matName.toUpperCase());
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> finalLore = new ArrayList<>();
            for (String line : loreTemplate) {
                String translated = ChatColor.translateAlternateColorCodes('&', line);
                if (costReplacement != null) {
                    translated = translated.replace("<cost>", costReplacement);
                }
                finalLore.add(translated);
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatCost(List<GeneratorSettings.UpgradeCost> costs) {
        if (costs.isEmpty()) return "Free";

        List<String> costStrings = new ArrayList<>();
        for (GeneratorSettings.UpgradeCost cost : costs) {
            if (cost.type().equalsIgnoreCase("money")) {
                costStrings.add("$" + cost.amount());
            } else if (cost.type().equalsIgnoreCase("item")) {
                costStrings.add(cost.amount() + "x " + cost.value());
            }
        }
        return String.join(", ", costStrings);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public StoneGenerator getGenerator() {
        return generator;
    }
}
