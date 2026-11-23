package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.Chunk;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StoneCacheManager {

    private final Map<ChunkKey, Map<Location, StoneGenerator>> cache = new ConcurrentHashMap<>();
    private final Set<ChunkKey> dirtyChunks = ConcurrentHashMap.newKeySet();

    public StoneGenerator getGeneratorAt(Location loc) {
        Map<Location, StoneGenerator> chunkGenerators = cache.get(Chunk.getKey(loc));
        return chunkGenerators != null ? chunkGenerators.get(loc) : null;
    }

    public boolean isStoneGenerator(Location loc) {
        return getGeneratorAt(loc) != null;
    }

    // Merges instead of replacing, so a generator placed before the load finished is kept.
    public void loadChunkIntoCache(ChunkKey key, List<StoneGenerator> generators) {
        Map<Location, StoneGenerator> chunkMap = cache.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        for (StoneGenerator gen : generators) {
            chunkMap.putIfAbsent(gen.getLocation(), gen);
        }
    }

    public Set<ChunkKey> getDirtyChunks() {
        return dirtyChunks;
    }

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
        }
    }

    public Collection<StoneGenerator> getGeneratorsInChunk(ChunkKey key) {
        Map<Location, StoneGenerator> chunkGenerators = cache.get(key);
        return chunkGenerators != null ? chunkGenerators.values() : Collections.emptyList();
    }
}
