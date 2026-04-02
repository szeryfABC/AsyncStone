package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class GeneratorProtectionListener implements Listener {

    private final StoneCacheManager cacheManager;

    public GeneratorProtectionListener(StoneCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> cacheManager.isStoneGenerator(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> cacheManager.isStoneGenerator(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            // Jeśli tłok próbuje popchnąć stoniarkę, anulujemy całe przesunięcie
            if (cacheManager.isStoneGenerator(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (cacheManager.isStoneGenerator(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}