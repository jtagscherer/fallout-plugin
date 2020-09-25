package io.github.jtagscherer.falloutplugin.features.revival;

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
                p.setCustomName("[DEAD]");
                p.sendTitle("You died.", "Other players can revive you by getting close to you.", 7, 20*300, 7);
            }
        }
    }

    @EventHandler
    public void onRightClickDeadPlayer(PlayerInteractEntityEvent interactEntityName) {
        if (interactEntityName.getRightClicked() instanceof Player) {
            Player p = ((Player) interactEntityName.getRightClicked());
            if (p.getCustomName().equals("[DEAD]")) {
                p.setCustomName("");
            }
        }
    }

    @EventHandler
    public void onDeadPlayerMove(PlayerMoveEvent moveEvent) {
        if (!this.isRunning) return;

        if (moveEvent.getPlayer().getCustomName().equals("[DEAD]")) {
            moveEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeadPlayerBlockInteraction(PlayerInteractEvent interactEntityEvent) {
        if (!this.isRunning) return;

        if (interactEntityEvent.getPlayer().getCustomName().equals("[DEAD]")) {
            interactEntityEvent.setCancelled(true);
        }
    }
    public void revivePlayer(Player player) {
        player.setCustomName("");
        player.sendMessage("you revived yourself");
        player.resetTitle();
    }
}
