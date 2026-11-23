package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

// Loads all generators of a world with one query, instead of one query per chunk load.
public class WorldLoadListener implements Listener {

    private final StoneCacheManager cacheManager;
    private final DatabaseManager databaseManager;

    public WorldLoadListener(StoneCacheManager cacheManager, DatabaseManager databaseManager) {
        this.cacheManager = cacheManager;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        load(event.getWorld());
    }

    public void load(World world) {
        databaseManager.loadWorld(world).forEach(cacheManager::loadChunkIntoCache);
    }
}
