package io.github.jtagscherer.falloutplugin.features.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EffectsManager {

    private Plugin plugin;
    private Integer taskId;

    private int effectRadius;

    public EffectsManager(Plugin plugin) {
        this.plugin = plugin;
        this.effectRadius = 16;
    }

    public void start() {
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::playEffectsForAllPlayers, 0L, 10L);
    }

    public void stop() {
        if (this.taskId != null) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }

        this.taskId = null;
    }

    private void playEffectsForAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::spawnOverworldParticlesForPlayer);
    }

    private void spawnOverworldParticlesForPlayer(Player player) {
        World world = player.getWorld();
        Location playerLocation = player.getLocation();

        for (int i = 0; i < 128; i++) {
            Location location = playerLocation.clone().add(this.calculateRandomDisplacement(), this.calculateRandomDisplacement(), this.calculateRandomDisplacement());

            if (this.isSkyVisibleFromLocation(world, location)) {
                world.spawnParticle(Particle.ASH, location, 1);
            }
        }
    }

    private double calculateRandomDisplacement() {
        return (Math.random() - 0.5) * this.effectRadius;
    }

    private boolean isSkyVisibleFromLocation(World world, Location location) {
        return location.getBlockY() > world.getHighestBlockYAt(location);
    }

}
