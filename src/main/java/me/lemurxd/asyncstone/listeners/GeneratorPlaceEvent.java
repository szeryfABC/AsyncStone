package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.utils.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class GeneratorPlaceEvent implements Listener {

    private final StoneCacheManager cacheManager;
    private final BlockGenerationEngine generationEngine;

    public GeneratorPlaceEvent(StoneCacheManager cacheManager, BlockGenerationEngine generationEngine) {
        this.cacheManager = cacheManager;
        this.generationEngine = generationEngine;
    }

    // MONITOR + ignoreCancelled: only register the generator if no protection plugin blocked the placement.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        String generatorId = StoneGeneratorItem.getGeneratorId(event.getItemInHand());
        if (generatorId == null) return;

        StoneGenerator generator = new StoneGenerator(event.getBlockPlaced().getLocation(), generatorId);

        cacheManager.addGenerator(generator);
        generationEngine.triggerGeneration(generator);

        event.getPlayer().sendMessage(Config.MSG_GENERATOR_PLACED.getLegacyString());
    }
}
