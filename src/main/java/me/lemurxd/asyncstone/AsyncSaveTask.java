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

    @Override
    public void run() {
        Set<ChunkKey> dirtyChunks = cacheManager.getDirtyChunks();

        if (dirtyChunks.isEmpty()) {
            return;
        }

        Map<ChunkKey, Collection<StoneGenerator>> batchToSave = new HashMap<>();
        Iterator<ChunkKey> iterator = dirtyChunks.iterator();

        while (iterator.hasNext()) {
            ChunkKey key = iterator.next();
            Collection<StoneGenerator> generators = cacheManager.getGeneratorsInChunk(key);

            batchToSave.put(key, generators != null ? generators : new ArrayList<>());

            iterator.remove();
        }

        if (!batchToSave.isEmpty()) {
            databaseManager.saveChunksBatch(batchToSave);
        }
    }
}
