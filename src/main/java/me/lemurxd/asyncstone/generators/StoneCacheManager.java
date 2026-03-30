package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.Chunk;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StoneCacheManager {

    private final Map<ChunkKey, Map<Location, StoneGenerator>> cache = new ConcurrentHashMap<>();
    private final Set<ChunkKey> dirtyChunks = ConcurrentHashMap.newKeySet();

    public void addGenerator(StoneGenerator generator) {
        Location loc = generator.getLocation();
        ChunkKey key = Chunk.getKey(loc);

        cache.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(loc, generator);
        dirtyChunks.add(key);
    }

    public void removeGenerator(Location loc) {
        ChunkKey key = Chunk.getKey(loc);
        Map<Location, StoneGenerator> chunkGenerators = cache.get(key);

        if (chunkGenerators != null) {
            chunkGenerators.remove(loc);
            dirtyChunks.add(key);

            if (chunkGenerators.isEmpty()) {
                cache.remove(key);
            }
        }
    }

    public StoneGenerator getGenerator(Location loc) {
        ChunkKey key = Chunk.getKey(loc);
        Map<Location, StoneGenerator> chunkGenerators = cache.get(key);

        return chunkGenerators != null ? chunkGenerators.get(loc) : null;
    }

    public boolean isStoneGenerator(Location loc) {
        return getGenerator(loc) != null;
    }

    public Collection<StoneGenerator> getGeneratorsInChunk(ChunkKey key) {
        Map<Location, StoneGenerator> chunkGenerators = cache.get(key);
        return chunkGenerators != null ? chunkGenerators.values() : Collections.emptyList();
    }
}
