package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ExposureTester {

    private World world;
    private Location playerLocation;

    private int testRadius;

    public ExposureTester(int testRadius) {
        this.testRadius = testRadius;
    }

    private void initializeState(Player player) {
        this.world = player.getWorld();

        Location location = player.getLocation();
        this.playerLocation = new Location(this.world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean isPlayerExposed(Player player) {
        this.initializeState(player);

        Queue<Location> locations = new LinkedList<>();
        List<Location> visitedLocations = new ArrayList<>();
        locations.add(this.playerLocation);

        while (!locations.isEmpty()) {
            Location location = locations.poll();
            visitedLocations.add(location);

            if (this.isSkyVisibleFromLocation(this.world, location)) {
                return true;
            }

            this.maybeAddLocationToQueue(locations, visitedLocations, location, 1, 0, 0);
            this.maybeAddLocationToQueue(locations, visitedLocations, location, -1, 0, 0);
            this.maybeAddLocationToQueue(locations, visitedLocations, location, 0, 1, 0);
            this.maybeAddLocationToQueue(locations, visitedLocations, location, 0, -1, 0);
            this.maybeAddLocationToQueue(locations, visitedLocations, location, 0, 0, 1);
            this.maybeAddLocationToQueue(locations, visitedLocations, location, 0, 0, -1);
        }

        return false;
    }

    private void maybeAddLocationToQueue(Queue<Location> queue, List<Location> visitedLocations, Location oldLocation, int x, int y, int z) {
        Location location = new Location(this.world, oldLocation.getBlockX() + x, oldLocation.getBlockY() + y, oldLocation.getBlockZ() + z);

        if (queue.contains(location) || visitedLocations.contains(location)) {
            return;
        }

        Material blockMaterial = this.world.getBlockAt(location).getType();
        if (blockMaterial != Material.AIR || blockMaterial.isOccluding()) {
            return;
        }

        if (location.distance(this.playerLocation) > this.testRadius) {
            return;
        }

        queue.add(location);
    }

    private boolean isSkyVisibleFromLocation(World world, Location location) {
        return location.getBlockY() > world.getHighestBlockYAt(location);
    }

}
