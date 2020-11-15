package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class ExposureTester {

    private int testRadius;

    private Set<DistancedLocation> leakLocations = new HashSet<>();
    private Set<Location> searchedLocations = new HashSet<>();

    ExposureTester(int testRadius) {
        this.testRadius = testRadius;
    }

    void searchLeakLocationsForPlayer(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        Location playerLocation = new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());

        BreadthFirstSearch leakSearch = new BreadthFirstSearch(this.testRadius, world, playerLocation)
                .withStopSearchingWhenEncounteringTarget(true)
                .search(this::isSkyVisibleFromLocation);

        this.leakLocations = leakSearch.getTargetLocations();
        this.searchedLocations = leakSearch.getVisitedLocations()
                .stream()
                .map(DistancedLocation::getLocation)
                .collect(Collectors.toSet());
    }

    Set<DistancedLocation> getLeakLocations() {
        return this.leakLocations;
    }

    Set<Location> getSearchedLocations() {
        return this.searchedLocations;
    }

    void clear() {
        this.leakLocations.clear();
        this.searchedLocations.clear();
    }

    private boolean isSkyVisibleFromLocation(DistancedLocation location, World world) {
        return location.getLocation().getBlockY() > world.getHighestBlockYAt(location.getLocation());
    }

}
