package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class GeneratorBreakEvent implements Listener {

    private final AsyncStone plugin;
    private final StoneCacheManager cacheManager;
    private final BlockGenerationEngine generationEngine;

    public GeneratorBreakEvent(AsyncStone plugin, StoneCacheManager cacheManager, BlockGenerationEngine generationEngine) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.generationEngine = generationEngine;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        StoneGenerator generatorAtLoc = cacheManager.getGeneratorAt(loc);

        if (generatorAtLoc != null) {
            GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorAtLoc.getId());
            if (settings == null) return;

            if (settings.getType() == GeneratorSettings.Type.REPLACE) {
                if (event.getPlayer().isSneaking()) {
                    destroyGenerator(event, generatorAtLoc, loc);
                } else {
                    generationEngine.triggerGeneration(generatorAtLoc);
                }
            } else {
                destroyGenerator(event, generatorAtLoc, loc);
            }
            return;
        }

        Location belowLoc = loc.clone().subtract(0, 1, 0);
        StoneGenerator generatorBelow = cacheManager.getGeneratorAt(belowLoc);

        if (generatorBelow != null) {
            GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorBelow.getId());

            if (settings != null && settings.getType() == GeneratorSettings.Type.ABOVE) {
                generationEngine.triggerGeneration(generatorBelow);
            }
        }
    }

    private void destroyGenerator(BlockBreakEvent event, StoneGenerator generator, Location loc) {
        cacheManager.removeGenerator(generator.getLocation());

        ItemStack itemToDrop = StoneGeneratorItem.create(generator.getId());

        if (itemToDrop != null) {
            event.setCancelled(true);

            loc.getBlock().setType(Material.AIR);

            loc.getWorld().dropItemNaturally(loc, itemToDrop);
        }

        event.getPlayer().sendMessage("§c[AsyncStone] Zniszczono stoniarke!");
    }
}
