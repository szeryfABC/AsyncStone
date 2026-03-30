package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GeneratorPlaceEvent implements Listener {

    private final StoneCacheManager cacheManager;

    public GeneratorPlaceEvent(StoneCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (meta == null) return;

        if (meta.getPersistentDataContainer().has(StoneGeneratorItem.GENERATOR_KEY, PersistentDataType.BYTE)) {

            StoneGenerator generator = new StoneGenerator(event.getBlockPlaced().getLocation());

            cacheManager.addGenerator(generator);

            event.getPlayer().sendMessage("§aPomyślnie postawiono stoniarkę!");
        }
    }

}
