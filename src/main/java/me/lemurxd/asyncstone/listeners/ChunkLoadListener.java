package me.lemurxd.asyncstone.listeners;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

public class ChunkLoadListener implements Listener {

    private final StoneCacheManager cacheManager;
    private final DatabaseManager databaseManager;

    public ChunkLoadListener(StoneCacheManager cacheManager, DatabaseManager databaseManager) {
        this.cacheManager = cacheManager;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        UUID world = event.getWorld().getUID();
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();
        ChunkKey key = new ChunkKey(world, x, z);

        if (cacheManager.isChunkTracked(key)) {
            return;
        }

        databaseManager.loadChunkAsync(world, x, z).thenAccept(generators -> {
            cacheManager.loadChunkIntoCache(key, generators);
        });
    }
}
