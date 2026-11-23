package me.lemurxd.asyncstone.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.records.ChunkKey;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// All methods are synchronized: the save task (async thread) and onDisable (main thread) share one connection.
public class DatabaseManager {

    private static final Type ROWS_TYPE = new TypeToken<List<Map<String, Object>>>(){}.getType();

    private final File file;
    private final Logger logger;
    private final Gson gson = new Gson();
    private Connection connection;

    public DatabaseManager(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public synchronized void connect() {
        try {
            file.getParentFile().mkdirs();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS stone_chunks (" +
                        "world TEXT, " +
                        "chunk_x INTEGER, " +
                        "chunk_z INTEGER, " +
                        "data TEXT, " +
                        "PRIMARY KEY (world, chunk_x, chunk_z))");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot open database " + file + ". Generators will NOT be saved!", e);
        }
    }

    // One query per world instead of one per chunk; uses the primary key prefix (world).
    public synchronized Map<ChunkKey, List<StoneGenerator>> loadWorld(World world) {
        Map<ChunkKey, List<StoneGenerator>> result = new HashMap<>();
        if (connection == null) return result;

        UUID worldUuid = world.getUID();
        String sql = "SELECT chunk_x, chunk_z, data FROM stone_chunks WHERE world = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, worldUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<StoneGenerator> generators = deserializeGenerators(world, rs.getString("data"));
                    if (!generators.isEmpty()) {
                        result.put(new ChunkKey(worldUuid, rs.getInt("chunk_x"), rs.getInt("chunk_z")), generators);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load generators for world " + world.getName(), e);
        }
        return result;
    }

    // Returns false when nothing was saved, so the caller can retry later.
    public synchronized boolean saveChunksBatch(Map<ChunkKey, ? extends Collection<StoneGenerator>> dataToSave) {
        if (dataToSave.isEmpty()) return true;
        if (connection == null) return false;

        String sql = "INSERT OR REPLACE INTO stone_chunks (world, chunk_x, chunk_z, data) VALUES (?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Map.Entry<ChunkKey, ? extends Collection<StoneGenerator>> entry : dataToSave.entrySet()) {
                    ChunkKey key = entry.getKey();

                    ps.setString(1, key.uuid().toString());
                    ps.setInt(2, key.x());
                    ps.setInt(3, key.z());
                    ps.setString(4, gson.toJson(serializeGenerators(entry.getValue())));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save generators to the database", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    private List<Map<String, Object>> serializeGenerators(Collection<StoneGenerator> generators) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (StoneGenerator g : generators) {
            Map<String, Object> map = new HashMap<>();
            map.put("x", g.getLocation().getBlockX());
            map.put("y", g.getLocation().getBlockY());
            map.put("z", g.getLocation().getBlockZ());
            map.put("id", g.getId());
            list.add(map);
        }
        return list;
    }

    private List<StoneGenerator> deserializeGenerators(World world, String json) {
        List<Map<String, Object>> data = gson.fromJson(json, ROWS_TYPE);
        List<StoneGenerator> list = new ArrayList<>();
        if (data == null) return list;

        for (Map<String, Object> map : data) {
            Location loc = new Location(world,
                    ((Number) map.get("x")).intValue(),
                    ((Number) map.get("y")).intValue(),
                    ((Number) map.get("z")).intValue());

            list.add(new StoneGenerator(loc, (String) map.get("id")));
        }
        return list;
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to close the database", e);
        }
    }
}
