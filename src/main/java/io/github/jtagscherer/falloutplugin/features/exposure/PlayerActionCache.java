package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerActionCache implements Listener {

    private Map<String, Location> lastPlayerLocations;
    private List<Location> blockChangeLocations;

    public PlayerActionCache() {
        this.lastPlayerLocations = new HashMap<>();
        this.blockChangeLocations = new ArrayList<>();
    }

    public boolean hasPlayerChanged(Player player) {
        if (this.hasLocationChanged(player)) {
            return true;
        }

        if (this.hasBlockChanged(player)) {
            return true;
        }

        return false;
    }

    public void clearIntervalCaches() {
        this.blockChangeLocations.clear();
    }

    private boolean hasLocationChanged(Player player) {
        String playerName = player.getName();

        if (!this.lastPlayerLocations.containsKey(playerName)
                || this.lastPlayerLocations.get(playerName).distance(player.getLocation()) > ExposureDamageManager.EXPOSURE_RADIUS) {
            this.lastPlayerLocations.put(playerName, player.getLocation());
            return true;
        }

        return false;
    }

    private boolean hasBlockChanged(Player player) {
        for (Location location : this.blockChangeLocations) {
            if (location.distance(player.getLocation()) < ExposureDamageManager.TEST_RADIUS) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        this.blockChangeLocations.add(event.getBlock().getLocation());
    }

}
