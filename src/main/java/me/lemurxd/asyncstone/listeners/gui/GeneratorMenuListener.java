package me.lemurxd.asyncstone.listeners.gui;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.generators.gui.GeneratorMenu;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class GeneratorMenuListener implements Listener {

    private final AsyncStone plugin;
    private final StoneCacheManager cacheManager;

    public GeneratorMenuListener(AsyncStone plugin, StoneCacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof GeneratorMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        // getHolder(false): no block-state snapshot, this runs on every inventory click on the server
        if (!(event.getInventory().getHolder(false) instanceof GeneratorMenu menu)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        StoneGenerator generator = menu.getGenerator();

        // Another player may have picked up, broken or upgraded it while this menu was open.
        if (cacheManager.getGeneratorAt(generator.getLocation()) != generator) {
            player.sendMessage(Config.MSG_GENERATOR_GONE.getLegacyString());
            player.closeInventory();
            return;
        }

        int clickedSlot = event.getRawSlot();
        GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generator.getId());

        if (settings == null) {
            player.closeInventory();
            return;
        }

        if (clickedSlot == Config.GUI_UPGRADE_SLOT.getInt()) {
            upgrade(player, generator, settings);
            player.closeInventory();
        } else if (clickedSlot == Config.GUI_PICKUP_SLOT.getInt()) {
            pickUp(player, generator, settings);
            player.closeInventory();
        }
    }

    private void upgrade(Player player, StoneGenerator generator, GeneratorSettings settings) {
        GeneratorSettings.UpgradeData upgradeData = settings.getUpgradeData();

        if (upgradeData == null) {
            player.sendMessage(Config.MSG_UPGRADE_MAX_LEVEL.getLegacyString());
            return;
        }

        // Check the next level exists before taking any payment.
        String nextId = upgradeData.nextGeneratorId();
        GeneratorSettings nextSettings = plugin.getGeneratorsConfig().getSettings(nextId);

        if (nextSettings == null) {
            plugin.getLogger().warning("Generator '" + generator.getId() + "' upgrades to '" + nextId + "', which does not exist in generators.yml!");
            player.sendMessage(Config.MSG_UPGRADE_UNAVAILABLE.getLegacyString());
            return;
        }

        if (!canAfford(player, upgradeData)) {
            player.sendMessage(Config.MSG_UPGRADE_CANNOT_AFFORD.getLegacyString());
            return;
        }

        takePayment(player, upgradeData);

        Location loc = generator.getLocation();

        cacheManager.removeGenerator(loc);
        cacheManager.addGenerator(new StoneGenerator(loc, nextId));

        loc.getBlock().setType(nextSettings.getMaterial());

        player.sendMessage(Config.MSG_UPGRADE_SUCCESS.getLegacyString()
                .replace("<name>", ChatColor.translateAlternateColorCodes('&', nextSettings.getName())));
    }

    private void pickUp(Player player, StoneGenerator generator, GeneratorSettings settings) {
        Location loc = generator.getLocation();

        cacheManager.removeGenerator(loc);

        loc.getBlock().setType(Material.AIR);

        if (settings.getType() == GeneratorSettings.Type.ABOVE) {
            Block blockAbove = loc.clone().add(0, 1, 0).getBlock();
            Material aboveMaterial = blockAbove.getType();

            for (GeneratorSettings.DropEntry drop : settings.getDrops()) {
                if (drop.material() == aboveMaterial) {
                    blockAbove.setType(Material.AIR);
                    break;
                }
            }
        }

        ItemStack generatorItem = StoneGeneratorItem.create(generator.getId());
        if (generatorItem != null) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(generatorItem);
            if (!leftover.isEmpty()) {
                loc.getWorld().dropItemNaturally(loc, generatorItem);
            }
        }

        player.sendMessage(Config.MSG_GENERATOR_PICKED_UP.getLegacyString());
    }

    // Sums costs first, so "item:DIAMOND:5" twice needs 10 diamonds, not 5.
    private boolean canAfford(Player player, GeneratorSettings.UpgradeData data) {
        double money = 0;
        Map<Material, Integer> items = new EnumMap<>(Material.class);

        for (GeneratorSettings.UpgradeCost cost : data.costs()) {
            if (cost.type().equalsIgnoreCase("money")) {
                money += cost.amount();
            } else if (cost.type().equalsIgnoreCase("item")) {
                Material mat = Material.matchMaterial(cost.value());
                if (mat == null) return false;
                items.merge(mat, cost.amount(), Integer::sum);
            }
        }

        if (money > 0 && (plugin.getEconomy() == null || plugin.getEconomy().getBalance(player) < money)) {
            return false;
        }

        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            if (!player.getInventory().contains(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void takePayment(Player player, GeneratorSettings.UpgradeData data) {
        for (GeneratorSettings.UpgradeCost cost : data.costs()) {
            if (cost.type().equalsIgnoreCase("money")) {
                plugin.getEconomy().withdrawPlayer(player, cost.amount());
            } else if (cost.type().equalsIgnoreCase("item")) {
                Material mat = Material.matchMaterial(cost.value());
                if (mat != null) {
                    removeItemsFromInventory(player, mat, cost.amount());
                }
            }
        }
    }

    private void removeItemsFromInventory(Player player, Material material, int amount) {
        int toRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= toRemove) {
                    toRemove -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                }
                if (toRemove <= 0) break;
            }
        }
    }
}
