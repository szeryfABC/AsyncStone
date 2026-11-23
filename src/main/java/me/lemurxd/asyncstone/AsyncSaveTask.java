package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AsyncSaveTask extends BukkitRunnable {

    private final StoneCacheManager cacheManager;
    private final DatabaseManager databaseManager;

    public AsyncSaveTask(StoneCacheManager cacheManager, DatabaseManager databaseManager) {
        this.cacheManager = cacheManager;
        this.databaseManager = databaseManager;
    }

    // synchronized: onDisable calls run() too, while the timer run may still be mid-save.
    @Override
    public synchronized void run() {
        Set<ChunkKey> dirtyChunks = cacheManager.getDirtyChunks();

        if (dirtyChunks.isEmpty()) {
            return;
        }

        Map<ChunkKey, Collection<StoneGenerator>> batchToSave = new HashMap<>();
        Iterator<ChunkKey> iterator = dirtyChunks.iterator();

        while (iterator.hasNext()) {
            ChunkKey key = iterator.next();
            batchToSave.put(key, cacheManager.getGeneratorsInChunk(key));
            iterator.remove();
        }

        if (!databaseManager.saveChunksBatch(batchToSave)) {
            dirtyChunks.addAll(batchToSave.keySet());
        }
    }
}
