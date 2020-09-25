package io.github.jtagscherer.falloutplugin.features.revival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevivalManager implements Listener {
    Plugin plugin;
    boolean isRunning = false;
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
            Player p = (Player) damageEvent.getEntity();

            if (p.getHealth() - damageEvent.getFinalDamage() <= 0) {
                damageEvent.setCancelled(true);

                if (!this.isPlayerDead(p)) {
                    Block block = p.getLocation().getBlock();
                    block.setType(Material.PLAYER_HEAD);

                    BlockState blockState = block.getState();
                    if (blockState instanceof Skull) {
                        Skull skull = (Skull) blockState;
                        skull.setOwningPlayer(p);
                        skull.update();
                    }

                    block.setMetadata("player", new FixedMetadataValue(this.plugin, p.getName()));

                    p.setGameMode(GameMode.SPECTATOR);

                    p.sendTitle("You died.", "Other players can revive you.", 7, 20 * 10, 7);
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(String.format("%s has died!", p.getDisplayName()));
                    });

                    this.deadPlayers.put(p.getName(), block.getLocation());
                }
            }
        }
    }

    /*@EventHandler
    public void onRightClickPlayerHead(PlayerInteractEntityEvent interactEntityName) {
        if (interactEntityName.getRightClicked() instanceof Block) {
            Block block = (Block) interactEntityName.getRightClicked();

            if (block.getType() != Material.PLAYER_HEAD || !block.hasMetadata("player")) {
                return;
            }

            String playerName = block.getMetadata("player").get(0).asString();
            Player p = Bukkit.getPlayer(playerName);
            if (this.isPlayerDead(p)) {
                this.revivePlayer(block, p);
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(String.format("%s has been revived by %s!", p.getDisplayName(), interactEntityName.getPlayer().getDisplayName()));
                });
            }
        }
    }*/

    /*@EventHandler
    public void onDeadPlayerMove(PlayerMoveEvent moveEvent) {
        if (!this.isRunning) return;

        if (this.isPlayerDead(moveEvent.getPlayer())) {
            moveEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeadPlayerBlockInteraction(PlayerInteractEvent interactEntityEvent) {
        if (!this.isRunning) return;

        if (this.isPlayerDead(interactEntityEvent.getPlayer())) {
            interactEntityEvent.setCancelled(true);
        }
    }*/

    @EventHandler
    public void onDeadPlayerBlockInteraction(BlockBreakEvent event) {
        if (!this.isRunning) return;

        Block block = event.getBlock();

        if (block.getType() != Material.PLAYER_HEAD) {
            return;
        }

        // String playerName = block.getMetadata("player").get(0).asString();
        BlockState blockState = block.getState();
        if (!(blockState instanceof Skull)) {
            return;
        }

        Skull skull = (Skull) blockState;
        String playerName = skull.getOwningPlayer().getName();
        Player p = Bukkit.getPlayer(playerName);

        if (this.isPlayerDead(p)) {
            this.revivePlayer(p, block.getLocation());
            this.deadPlayers.remove(p.getName());
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(String.format("%s has been revived by %s!", p.getDisplayName(), event.getPlayer().getDisplayName()));
            });
            event.setCancelled(true);
            block.setType(Material.AIR);
        }
    }

    public void revivePlayerByName(String name) {
        if (!this.deadPlayers.containsKey(name)) {
            Bukkit.getPlayer(name).sendMessage("You are not dead!");
            return;
        }

        Player player = Bukkit.getPlayer(name);
        this.revivePlayer(player, this.deadPlayers.get(name));

        Block block = player.getWorld().getBlockAt(this.deadPlayers.get(name));
        if (block != null) {
            block.setType(Material.AIR);
        }

        this.deadPlayers.remove(player.getName());
    }

    public void revivePlayer(Player player, Location location) {
        // player.setCustomName(StringUtils.EMPTY);
        player.sendMessage("Teleporting you!");
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(location);
        player.resetTitle();
        player.setHealth(6);
    }

    private boolean isPlayerDead(Player player) {
        return this.deadPlayers.containsKey(player.getName());
    }
}
