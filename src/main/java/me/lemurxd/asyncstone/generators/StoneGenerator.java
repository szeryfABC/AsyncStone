package me.lemurxd.asyncstone.generators;

import org.bukkit.Location;

import java.util.Objects;

public class StoneGenerator {

    private final Location location;
    private final String id; // Klucz z generators.yml (np. "stoniarka-zwykla")

    public StoneGenerator(Location location, String id) {
        // Normalizacja lokacji do pełnego bloku
        this.location = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoneGenerator that = (StoneGenerator) o;
        return location.getBlockX() == that.location.getBlockX() &&
                location.getBlockY() == that.location.getBlockY() &&
                location.getBlockZ() == that.location.getBlockZ() &&
                Objects.equals(location.getWorld().getUID(), that.location.getWorld().getUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(location.getWorld().getUID(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
}
