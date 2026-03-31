package me.lemurxd.asyncstone.listeners.gui;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.generators.gui.GeneratorMenu;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GeneratorMenuListener implements Listener {

    private final AsyncStone plugin;
    private final StoneCacheManager cacheManager;

    public GeneratorMenuListener(AsyncStone plugin, StoneCacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GeneratorMenu) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            GeneratorMenu menu = (GeneratorMenu) event.getInventory().getHolder();
            StoneGenerator generator = menu.getGenerator();

            int clickedSlot = event.getRawSlot();
            GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generator.getId());

            if (settings == null) {
                player.closeInventory();
                return;
            }

            if (clickedSlot == Config.GUI_UPGRADE_SLOT.getInt()) {
                GeneratorSettings.UpgradeData upgradeData = settings.getUpgradeData();

                if (upgradeData == null) {
                    player.sendMessage("§cTen generator ma już maksymalny poziom!");
                    player.closeInventory();
                    return;
                }

                if (!canAfford(player, upgradeData)) {
                    player.sendMessage("§cNie stać cię na to ulepszenie!");
                    player.closeInventory();
                    return;
                }

                takePayment(player, upgradeData);

                String nextId = upgradeData.nextGeneratorId();
                GeneratorSettings nextSettings = plugin.getGeneratorsConfig().getSettings(nextId);

                if (nextSettings != null) {
                    Location loc = generator.getLocation();

                    cacheManager.removeGenerator(loc);
                    StoneGenerator upgradedGenerator = new StoneGenerator(loc, nextId);
                    cacheManager.addGenerator(upgradedGenerator);

                    loc.getBlock().setType(nextSettings.getMaterial());

                    player.sendMessage("§aPomyślnie ulepszono stoniarkę na poziom: §e" + nextSettings.getName());
                }

                player.closeInventory();
            }

            else if (clickedSlot == Config.GUI_PICKUP_SLOT.getInt()) {
                Location loc = generator.getLocation();

                cacheManager.removeGenerator(loc);

                loc.getBlock().setType(Material.AIR);

                if (settings.getType() == GeneratorSettings.Type.ABOVE) {
                    Block blockAbove = loc.clone().add(0, 1, 0).getBlock();
                    Material aboveMaterial = blockAbove.getType();

                    boolean isGeneratedBlock = false;
                    for (GeneratorSettings.DropEntry drop : settings.getDrops()) {
                        if (drop.material() == aboveMaterial) {
                            isGeneratedBlock = true;
                            break;
                        }
                    }

                    if (isGeneratedBlock) {
                        blockAbove.setType(Material.AIR);
                    }
                }

                ItemStack generatorItem = StoneGeneratorItem.create(generator.getId());
                if (generatorItem != null) {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(generatorItem);
                    if (!leftover.isEmpty()) {
                        loc.getWorld().dropItemNaturally(loc, generatorItem);
                    }
                }

                player.sendMessage("§aPomyślnie podniesiono stoniarkę!");
                player.closeInventory();
            }
        }
    }


    private boolean canAfford(Player player, GeneratorSettings.UpgradeData data) {
        for (GeneratorSettings.UpgradeCost cost : data.costs()) {
            if (cost.type().equalsIgnoreCase("money")) {
                if (plugin.getEconomy() == null || plugin.getEconomy().getBalance(player) < cost.amount()) {
                    return false;
                }
            } else if (cost.type().equalsIgnoreCase("item")) {
                Material mat = Material.matchMaterial(cost.value().toUpperCase());
                if (mat == null || !player.getInventory().contains(mat, cost.amount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void takePayment(Player player, GeneratorSettings.UpgradeData data) {
        for (GeneratorSettings.UpgradeCost cost : data.costs()) {
            if (cost.type().equalsIgnoreCase("money")) {
                plugin.getEconomy().withdrawPlayer(player, cost.amount());
            } else if (cost.type().equalsIgnoreCase("item")) {
                Material mat = Material.matchMaterial(cost.value().toUpperCase());
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
