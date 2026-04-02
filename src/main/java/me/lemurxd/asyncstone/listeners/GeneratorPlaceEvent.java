package me.lemurxd.asyncstone.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

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

        if (itemInHand == null || itemInHand.getType() == Material.AIR) return;

        NBTItem nbtItem = new NBTItem(itemInHand);

        if (nbtItem.hasTag(StoneGeneratorItem.GENERATOR_ID_KEY)) {

            String generatorId = nbtItem.getString(StoneGeneratorItem.GENERATOR_ID_KEY);
            Location placedLoc = event.getBlockPlaced().getLocation();

            StoneGenerator generator = new StoneGenerator(placedLoc, generatorId);

            cacheManager.addGenerator(generator);

            generationEngine.triggerGeneration(generator);

            event.getPlayer().sendMessage("§a[AsyncStone] Pomyslnie postawiono stoniarke!");
        }
    }
}
