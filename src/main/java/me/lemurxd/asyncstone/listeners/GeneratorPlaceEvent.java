package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GeneratorPlaceEvent implements Listener {

    private final AsyncStone plugin;
    private final StoneCacheManager cacheManager;
    private final BlockGenerationEngine generationEngine;

    public GeneratorPlaceEvent(AsyncStone plugin, StoneCacheManager cacheManager, BlockGenerationEngine generationEngine) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.generationEngine = generationEngine;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (meta == null) return;

        if (meta.getPersistentDataContainer().has(StoneGeneratorItem.GENERATOR_ID_KEY, PersistentDataType.STRING)) {

            String generatorId = meta.getPersistentDataContainer().get(StoneGeneratorItem.GENERATOR_ID_KEY, PersistentDataType.STRING);
            Location placedLoc = event.getBlockPlaced().getLocation();

            StoneGenerator generator = new StoneGenerator(placedLoc, generatorId);

            cacheManager.addGenerator(generator);

            generationEngine.triggerGeneration(generator);

            event.getPlayer().sendMessage("§a[AsyncStone] Pomyslnie postawiono stoniarke!");
        }
    }
}
