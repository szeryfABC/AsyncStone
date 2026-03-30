package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class GeneratorBreakEvent implements Listener {

    private final AsyncStone plugin;
    private final StoneCacheManager cacheManager;

    public GeneratorBreakEvent(AsyncStone plugin, StoneCacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.STONE) {

            org.bukkit.Location blockBelowLoc = block.getLocation().subtract(0, 1, 0);

            if (cacheManager.isStoneGenerator(blockBelowLoc)) {

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    block.setType(Material.STONE, false);
                }, 40L);

                return;
            }
        }

        if (block.getType() == Material.END_STONE) {

            org.bukkit.Location brokenLoc = block.getLocation();

            if (cacheManager.isStoneGenerator(brokenLoc)) {

                cacheManager.removeGenerator(brokenLoc);

                block.getWorld().dropItemNaturally(brokenLoc, StoneGeneratorItem.create());

            }
        }
    }
}
