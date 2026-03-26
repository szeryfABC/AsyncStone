package me.lemurxd.asyncstone.generators;

import org.bukkit.Location;

import java.util.Objects;

public class StoneGenerator {

    private final Location location;

    public StoneGenerator(Location location) {
        this.location = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoneGenerator that = (StoneGenerator) o;
        return Objects.equals(location.getWorld().getUID(), that.location.getWorld().getUID()) &&
                location.getBlockX() == that.location.getBlockX() &&
                location.getBlockY() == that.location.getBlockY() &&
                location.getBlockZ() == that.location.getBlockZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
