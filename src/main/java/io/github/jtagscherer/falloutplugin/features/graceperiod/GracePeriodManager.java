package io.github.jtagscherer.falloutplugin.features.graceperiod;

import io.github.jtagscherer.falloutplugin.features.effects.EffectsManager;
import io.github.jtagscherer.falloutplugin.features.exposure.ExposureDamageManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GracePeriodManager {

    private Plugin plugin;
    private ExposureDamageManager exposureDamageManager;
    private EffectsManager effectsManager;
    private BossBar bossBar;
    private Integer taskId;

    private int secondsTotal;
    private int secondsRemaining;

    public GracePeriodManager(Plugin plugin) {
        this.plugin = plugin;
        this.exposureDamageManager = new ExposureDamageManager(this.plugin);
        this.effectsManager = new EffectsManager(this.plugin);
        this.bossBar = Bukkit.createBossBar(StringUtils.EMPTY, BarColor.GREEN, BarStyle.SOLID);
    }

    public void start(int duration) {
        Bukkit.getOnlinePlayers().forEach(this::sendTitleToPlayer);
        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);

        this.secondsTotal = duration;
        this.secondsRemaining = duration;
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateProgress, 0L, 20L);
    }

    public void stop() {
        this.removeBossBar();
        this.exposureDamageManager.stop();
        this.effectsManager.stop();
    }

    private void updateProgress() {
        this.secondsRemaining--;
        this.bossBar.setTitle(String.format("Missile Strike in %d s", this.secondsRemaining));
        this.bossBar.setProgress((1.0 * this.secondsRemaining) / this.secondsTotal);

        if (this.secondsRemaining <= 10) {
            this.bossBar.setColor(BarColor.RED);
        }

        if (this.secondsRemaining <= 0) {
            this.removeBossBar();
            this.startExposure();
            this.effectsManager.start();
        }
    }

    private void removeBossBar() {
        Bukkit.getOnlinePlayers().forEach(this.bossBar::removePlayer);

        if (this.taskId != null) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
    }

    private void sendTitleToPlayer(Player player) {
        player.sendTitle("Missile Strike Imminent", "Seek Shelter Immediately.", 20, 20 * 5, 20);
    }

    private void startExposure() {
        Bukkit.getOnlinePlayers().forEach(this::playNukeSounds);
        this.exposureDamageManager.start();
    }

    private void playNukeSounds(Player player) {
        player.playSound(player.getLocation(), Sound.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 15, 1);
        player.playSound(player.getLocation(), Sound.AMBIENT_BASALT_DELTAS_LOOP, SoundCategory.PLAYERS, 3, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 5, 0.1f);
        player.spawnParticle(Particle.ASH, player.getLocation(), 100000, 5, 5, 5);
    }

}
