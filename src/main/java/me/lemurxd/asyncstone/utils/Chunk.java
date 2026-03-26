package me.lemurxd.asyncstone.utils;

import me.lemurxd.asyncstone.records.ChunkKey;
import org.bukkit.Location;

import java.util.UUID;

public class Chunk {

    public static ChunkKey getKey(Location loc) {
        return new ChunkKey(
                loc.getWorld().getUID(),
                loc.getBlockX() >> 4,
                loc.getBlockZ() >> 4
        );

    }

}
