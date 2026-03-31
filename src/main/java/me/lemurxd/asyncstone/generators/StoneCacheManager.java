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
        ChunkKey key = new ChunkKey(loc.getWorld().getUID(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        Map<Location, StoneGenerator> chunkGenerators = cache.get(key);

        if (chunkGenerators != null) {
            return chunkGenerators.get(loc);
        }
        return null;
    }

    public void loadChunkIntoCache(ChunkKey key, List<StoneGenerator> generators) {
        Map<Location, StoneGenerator> chunkMap = new ConcurrentHashMap<>();
        for (StoneGenerator gen : generators) {
            chunkMap.put(gen.getLocation(), gen);
        }
        cache.put(key, chunkMap);
    }

    public boolean isChunkTracked(ChunkKey key) {
        return cache.containsKey(key);
    }

    public void markDirty(ChunkKey key) {
        dirtyChunks.add(key);
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
