package io.github.jtagscherer.falloutplugin.features.revival;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

public class RevivalManager implements Listener {
    Plugin plugin;
    boolean isRunning = false;

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
                    p.setCustomName("[DEAD]");
                    p.sendTitle("You died.", "Other players can revive you.", 7, 20 * 300, 7);
                    p.setSwimming(true);
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(String.format("%s has died! Revive them by right-clicking.", p.getDisplayName()));
                    });
                }
            }
        }
    }

    @EventHandler
    public void onRightClickDeadPlayer(PlayerInteractEntityEvent interactEntityName) {
        if (interactEntityName.getRightClicked() instanceof Player) {
            Player p = ((Player) interactEntityName.getRightClicked());
            if (this.isPlayerDead(p)) {
                this.revivePlayer(p);
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(String.format("%s has been revived by %s!", p.getDisplayName(), interactEntityName.getPlayer().getDisplayName()));
                });
            }
        }
    }

    @EventHandler
    public void onDeadPlayerMove(PlayerMoveEvent moveEvent) {
        if (!this.isRunning) return;

        if ("[DEAD]".equals(moveEvent.getPlayer().getCustomName())) {
            moveEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeadPlayerBlockInteraction(PlayerInteractEvent interactEntityEvent) {
        if (!this.isRunning) return;

        if (this.isPlayerDead(interactEntityEvent.getPlayer())) {
            interactEntityEvent.setCancelled(true);
        }
    }

    public void revivePlayer(Player player) {
        player.setCustomName(null);
        player.resetTitle();
        player.setHealth(6);
    }

    private boolean isPlayerDead(Player player) {
        return "[DEAD]".equals(player.getCustomName());
    }
}
