package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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

        Iterator<ChunkKey> iterator = dirtyChunks.iterator();
        int savedCount = 0;

        while (iterator.hasNext()) {
            ChunkKey key = iterator.next();

            Collection<StoneGenerator> generators = cacheManager.getGeneratorsInChunk(key);

            databaseManager.saveChunkAsync(key.uuid(), key.x(), key.z(), generators);

            iterator.remove();
            savedCount++;
        }

    }
}
