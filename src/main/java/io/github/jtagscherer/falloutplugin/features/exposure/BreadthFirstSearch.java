package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

class BreadthFirstSearch {

    private World world;
    private Location startLocation;
    private int radius;

    private boolean stopSearchingWhenEncounteringTarget = false;
    private Collection<Location> allowedLocations;

    private Set<DistancedLocation> targetLocations;
    private Set<DistancedLocation> visitedLocations;

    BreadthFirstSearch(int radius, World world, Location startLocation) {
        this.radius = radius;
        this.world = world;
        this.startLocation = startLocation;
    }

    BreadthFirstSearch withStopSearchingWhenEncounteringTarget(boolean value) {
        this.stopSearchingWhenEncounteringTarget = value;
        return this;
    }

    BreadthFirstSearch withAllowedLocations(Collection<Location> value) {
        this.allowedLocations = value;
        return this;
    }

    BreadthFirstSearch search(TargetLocationFunction<DistancedLocation, World, Boolean> targetLocationFunction) {
        Queue<DistancedLocation> locations = new LinkedList<>();
        locations.add(new DistancedLocation(this.startLocation, 0));

        this.visitedLocations = new HashSet<>();
        this.targetLocations = new HashSet<>();

        while (!locations.isEmpty()) {
            DistancedLocation location = locations.poll();
            this.visitedLocations.add(location);

            if (targetLocationFunction.apply(location, this.world)) {
                this.targetLocations.add(location);

                if (this.stopSearchingWhenEncounteringTarget) {
                    continue;
                }
            }

            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, 1, 0, 0);
            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, -1, 0, 0);
            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, 0, 1, 0);
            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, 0, -1, 0);
            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, 0, 0, 1);
            this.maybeAddLocationToQueue(locations, this.visitedLocations, location, 0, 0, -1);
        }

        return this;
    }

    Set<DistancedLocation> getTargetLocations() {
        return this.targetLocations;
    }

    Set<DistancedLocation> getVisitedLocations() {
        return this.visitedLocations;
    }

    private void maybeAddLocationToQueue(Queue<DistancedLocation> queue, Set<DistancedLocation> visitedDistancedLocations, DistancedLocation oldDistancedLocation, int x, int y, int z) {
        Location oldLocation = oldDistancedLocation.getLocation();
        Location location = new Location(this.world, oldLocation.getBlockX() + x, oldLocation.getBlockY() + y, oldLocation.getBlockZ() + z);
        DistancedLocation distancedLocation = new DistancedLocation(location, oldDistancedLocation.getDistance() + 1);

        if (this.allowedLocations != null && !this.allowedLocations.contains(location)) {
            return;
        }

        if (distancedLocation.getDistance() > this.radius) {
            return;
        }

        if (queue.contains(distancedLocation) || visitedDistancedLocations.contains(distancedLocation)) {
            return;
        }

        Material blockMaterial = this.world.getBlockAt(location).getType();
        if (blockMaterial.isOccluding() || this.isGlassBlock(blockMaterial) || blockMaterial == Material.FARMLAND) {
            return;
        }

        queue.add(distancedLocation);
    }

    private boolean isGlassBlock(Material blockMaterial) {
        return blockMaterial == Material.GLASS || blockMaterial == Material.WHITE_STAINED_GLASS
                || blockMaterial == Material.ORANGE_STAINED_GLASS || blockMaterial == Material.MAGENTA_STAINED_GLASS
                || blockMaterial == Material.LIGHT_BLUE_STAINED_GLASS || blockMaterial == Material.YELLOW_STAINED_GLASS
                || blockMaterial == Material.LIME_STAINED_GLASS || blockMaterial == Material.PINK_STAINED_GLASS
                || blockMaterial == Material.GRAY_STAINED_GLASS || blockMaterial == Material.LIGHT_GRAY_STAINED_GLASS
                || blockMaterial == Material.CYAN_STAINED_GLASS || blockMaterial == Material.PURPLE_STAINED_GLASS
                || blockMaterial == Material.BLUE_STAINED_GLASS || blockMaterial == Material.BROWN_STAINED_GLASS
                || blockMaterial == Material.GREEN_STAINED_GLASS || blockMaterial == Material.RED_STAINED_GLASS
                || blockMaterial == Material.BLACK_STAINED_GLASS;
    }

}
