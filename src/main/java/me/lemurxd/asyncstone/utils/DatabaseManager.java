package me.lemurxd.asyncstone.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final AsyncStone plugin;
    private Connection connection;
    private final Gson gson = new Gson();

    public DatabaseManager(AsyncStone plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File dataFolder = new File(plugin.getDataFolder(), "data.db");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS stone_chunks (" +
                    "world TEXT, " +
                    "chunk_x INTEGER, " +
                    "chunk_z INTEGER, " +
                    "data TEXT, " +
                    "PRIMARY KEY (world, chunk_x, chunk_z))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void saveChunkAsync(UUID worldUuid, int cx, int cz, Collection<StoneGenerator> generators) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String json = gson.toJson(serializeGenerators(generators));

            String sql = "INSERT OR REPLACE INTO stone_chunks (world, chunk_x, chunk_z, data) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, worldUuid.toString());
                ps.setInt(2, cx);
                ps.setInt(3, cz);
                ps.setString(4, json);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    public CompletableFuture<List<StoneGenerator>> loadChunkAsync(UUID worldUuid, int cx, int cz) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT data FROM stone_chunks WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, worldUuid.toString());
                ps.setInt(2, cx);
                ps.setInt(3, cz);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String json = rs.getString("data");
                    return deserializeGenerators(worldUuid, json);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        });
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

    private List<StoneGenerator> deserializeGenerators(UUID worldUuid, String json) {
        List<Map<String, Object>> data = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());
        List<StoneGenerator> list = new ArrayList<>();

        if (data == null) return list;

        org.bukkit.World world = Bukkit.getWorld(worldUuid);

        if (world == null) {
            plugin.getLogger().warning("Probowano zaladowac stoniarki dla usunietego swiata (UUID: " + worldUuid + ")");
            return list;
        }

        for (Map<String, Object> map : data) {
            Location loc = new Location(world,
                    ((Double)map.get("x")).intValue(),
                    ((Double)map.get("y")).intValue(),
                    ((Double)map.get("z")).intValue());

            list.add(new StoneGenerator(loc, (String)map.get("id")));
        }
        return list;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
