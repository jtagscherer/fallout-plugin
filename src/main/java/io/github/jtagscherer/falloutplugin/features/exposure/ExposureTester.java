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
        if (blockMaterial.isOccluding() || this.isGlassBlock(blockMaterial)) {
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
