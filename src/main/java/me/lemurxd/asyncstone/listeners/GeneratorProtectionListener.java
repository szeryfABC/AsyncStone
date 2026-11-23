package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class GeneratorProtectionListener implements Listener {

    private final StoneCacheManager cacheManager;
    private final BlockGenerationEngine generationEngine;

    public GeneratorProtectionListener(StoneCacheManager cacheManager, BlockGenerationEngine generationEngine) {
        this.cacheManager = cacheManager;
        this.generationEngine = generationEngine;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        protectAndRegenerate(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        protectAndRegenerate(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        protectAndRegenerate(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        protectAndRegenerate(event, event.getBlocks());
    }

    private void protectAndRegenerate(List<Block> exploded) {
        exploded.removeIf(block -> cacheManager.isStoneGenerator(block.getLocation()));
        exploded.forEach(this::regenerateBelow);
    }

    private void protectAndRegenerate(BlockPistonEvent event, List<Block> moved) {
        for (Block block : moved) {
            if (cacheManager.isStoneGenerator(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
        moved.forEach(this::regenerateBelow);
    }

    // A block removed from above a generator would otherwise only regenerate after a player breaks something there.
    private void regenerateBelow(Block removed) {
        StoneGenerator below = cacheManager.getGeneratorAt(removed.getLocation().subtract(0, 1, 0));
        if (below != null) {
            generationEngine.triggerGeneration(below);
        }
    }
}
