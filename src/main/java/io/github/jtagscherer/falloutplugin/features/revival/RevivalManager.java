package io.github.jtagscherer.falloutplugin.features.revival;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class RevivalManager implements Listener {

    private Plugin plugin;
    private boolean isRunning = false;
    private Map<String, Location> deadPlayers = new HashMap<>();

    public RevivalManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.isRunning = true;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        this.isRunning = false;
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent damageEvent) {
        if (!this.isRunning) return;

        if (damageEvent.getEntity() instanceof Player) {
            Player player = (Player) damageEvent.getEntity();

            if (player.getHealth() - damageEvent.getFinalDamage() <= 0) {
                damageEvent.setCancelled(true);

                if (!this.isPlayerDead(player)) {
                    Block block = player.getLocation().getBlock();
                    block.setType(Material.PLAYER_HEAD);

                    BlockState blockState = block.getState();
                    if (blockState instanceof Skull) {
                        Skull skull = (Skull) blockState;
                        skull.setOwningPlayer(player);
                        skull.update();
                    }

                    block.setMetadata("player", new FixedMetadataValue(this.plugin, player.getName()));

                    player.setGameMode(GameMode.SPECTATOR);

                    player.sendTitle("You died.", "Other players can revive you.", 7, 20 * 10, 7);
                    this.sendMessageToAllPlayers(String.format("%s has died due to %s!", player.getDisplayName(), damageEvent.getCause()));

                    this.deadPlayers.put(player.getName(), block.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onDeadPlayerBlockInteraction(BlockBreakEvent event) {
        if (!this.isRunning) return;

        Block block = event.getBlock();

        if (block.getType() != Material.PLAYER_HEAD) {
            return;
        }

        BlockState blockState = block.getState();
        if (!(blockState instanceof Skull)) {
            return;
        }

        Skull skull = (Skull) blockState;
        OfflinePlayer offlinePlayer = skull.getOwningPlayer();

        if (offlinePlayer == null) {
            return;
        }

        Player player = offlinePlayer.getPlayer();

        if (player == null) {
            return;
        }

        if (this.isPlayerDead(player)) {
            this.revivePlayer(player, block.getLocation());
            this.sendMessageToAllPlayers(String.format("%s has been revived by %s!", player.getDisplayName(), event.getPlayer().getDisplayName()));
            event.setCancelled(true);
            block.setType(Material.AIR);
        }
    }

    public void revivePlayerByName(String name) {
        Player player = Bukkit.getPlayer(name);

        if (player == null) {
            return;
        }

        if (!this.isPlayerDead(player)) {
            player.sendMessage("You are not dead!");
            return;
        }

        this.revivePlayer(player, this.deadPlayers.get(name));
        this.sendMessageToAllPlayers(String.format("%s has revived themself!", name));

        Block block = player.getWorld().getBlockAt(this.deadPlayers.get(name));
        block.setType(Material.AIR);
    }

    private void revivePlayer(Player player, Location location) {
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(location);
        player.resetTitle();
        player.setHealth(6);
        this.deadPlayers.remove(player.getName());
    }

    private boolean isPlayerDead(Player player) {
        return this.deadPlayers.containsKey(player.getName());
    }

    private void sendMessageToAllPlayers(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }
}
