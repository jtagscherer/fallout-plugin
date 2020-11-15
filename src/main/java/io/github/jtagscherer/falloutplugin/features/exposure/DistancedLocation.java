package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Location;

public class DistancedLocation {

    private Location location;
    private int distance;

    DistancedLocation(Location location, int distance) {
        this.location = location;
        this.distance = distance;
    }

    Location getLocation() {
        return location;
    }

    int getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        DistancedLocation otherLocation = (DistancedLocation) other;
        return this.getLocation().equals(otherLocation.getLocation());
    }

    @Override
    public int hashCode() {
        return this.getLocation().hashCode();
    }

}
