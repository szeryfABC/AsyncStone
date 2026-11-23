package me.lemurxd.asyncstone.utils;

import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.records.ChunkKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void savedGeneratorsLoadBackPerWorld(@TempDir Path dir) {
        World world = fakeWorld(UUID.randomUUID());
        World otherWorld = fakeWorld(UUID.randomUUID());
        ChunkKey chunk = new ChunkKey(world.getUID(), 1, -2);
        ChunkKey emptiedChunk = new ChunkKey(world.getUID(), 5, 5);

        List<StoneGenerator> generators = List.of(
                new StoneGenerator(new Location(world, 16, 64, -32), "default-generator"),
                new StoneGenerator(new Location(world, 17, -5, -31), "generator-level-2"));

        DatabaseManager db = new DatabaseManager(dir.resolve("data.db").toFile(), Logger.getLogger("test"));
        db.connect();
        assertTrue(db.saveChunksBatch(Map.of(chunk, generators, emptiedChunk, List.of())));
        db.close();

        // reopen, like after a server restart
        db = new DatabaseManager(dir.resolve("data.db").toFile(), Logger.getLogger("test"));
        db.connect();
        Map<ChunkKey, List<StoneGenerator>> loaded = db.loadWorld(world);

        assertEquals(Set.of(chunk), loaded.keySet(), "empty chunks must not be loaded into the cache");
        assertEquals(describe(generators), describe(loaded.get(chunk)));
        assertTrue(db.loadWorld(otherWorld).isEmpty());
        db.close();
    }

    @Test
    void saveFailsWhenDatabaseIsNotConnected(@TempDir Path dir) {
        DatabaseManager db = new DatabaseManager(dir.resolve("data.db").toFile(), Logger.getLogger("test"));
        ChunkKey key = new ChunkKey(UUID.randomUUID(), 0, 0);
        assertFalse(db.saveChunksBatch(Map.of(key, List.of())), "caller must keep the chunk dirty and retry");
    }

    private static Set<String> describe(Collection<StoneGenerator> generators) {
        return generators.stream()
                .map(g -> g.getLocation().getWorld().getUID() + " " + g.getLocation().getBlockX() + " "
                        + g.getLocation().getBlockY() + " " + g.getLocation().getBlockZ() + " " + g.getId())
                .collect(Collectors.toSet());
    }

    private static World fakeWorld(UUID uuid) {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> uuid;
                    case "getName" -> "test";
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }
}
