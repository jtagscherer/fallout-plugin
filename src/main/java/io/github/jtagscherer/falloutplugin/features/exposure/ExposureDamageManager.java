package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExposureDamageManager implements Listener {

    private static final int EFFECT_DURATION = 200;

    private Plugin plugin;
    private Integer taskId;

    public ExposureDamageManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            public void run() {
                checkExposureForAllPlayers();
            }
        }, 0L, 20L);
    }

    public void stop() {
        if (this.taskId != null) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }

        this.taskId = null;
    }

    private void checkExposureForAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::dealDamageUponExposure);
    }

    private void dealDamageUponExposure(Player player) {
        byte skyLight = player.getLocation().getBlock().getLightFromSky();

        if (skyLight > 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 10, 1);
            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, EFFECT_DURATION, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, EFFECT_DURATION, 0));

            player.damage(2);
        }
    }
}
