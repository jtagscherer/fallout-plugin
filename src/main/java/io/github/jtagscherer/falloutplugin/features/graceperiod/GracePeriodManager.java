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

    private Integer updateTaskId;

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
        this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateProgress, 0L, 20L);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::playPanicSounds);
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
            Bukkit.getOnlinePlayers().forEach(this::playCountdownSounds);
        }

        if (this.secondsRemaining <= 0) {
            this.removeBossBar();
            this.startExposure();
            this.effectsManager.start();
        }
    }

    private void removeBossBar() {
        Bukkit.getOnlinePlayers().forEach(this.bossBar::removePlayer);

        if (this.updateTaskId != null) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
        }
    }

    private void sendTitleToPlayer(Player player) {
        player.sendTitle("Missile Strike Imminent", "Seek Shelter Immediately.", 20, 20 * 5, 20);
    }

    private void startExposure() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::playNukeSounds);
        this.exposureDamageManager.start();
    }

    private void playPanicSounds() {
        Long soundStart = System.currentTimeMillis();
        double soundRatio;
        float soundVolume;

        Long finalCountdownStart = null;
        double finalCountdownSoundLength = 6.0;
        double countdownRatio;

        while (this.secondsRemaining > 0) {
            soundRatio = (System.currentTimeMillis() - soundStart) / 2000.0;
            soundVolume = (float) Math.min(soundRatio, 5);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.PLAYERS, soundVolume, (float) (Math.sin(soundRatio) / 2.0 + 0.5));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, soundVolume, (float) (Math.sin(soundRatio + 10) / 2.0 + 0.5));

                if (this.secondsRemaining < finalCountdownSoundLength - 1) {
                    if (finalCountdownStart == null) {
                        finalCountdownStart = System.currentTimeMillis();
                    }

                    countdownRatio = (System.currentTimeMillis() - finalCountdownStart) / 1000.0;
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, (float) (countdownRatio / 4.0), (float) ((finalCountdownSoundLength - countdownRatio) / finalCountdownSoundLength));
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void playCountdownSounds(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.PLAYERS, 10, 1);
    }

    private void playNukeSounds() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 15, 1);
            player.playSound(player.getLocation(), Sound.AMBIENT_BASALT_DELTAS_LOOP, SoundCategory.PLAYERS, 3, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 5, 0.1f);
            player.spawnParticle(Particle.ASH, player.getLocation(), 100000, 5, 5, 5);
        }

        for (int i = 0; i < 16; i++) {
            try {
                Thread.sleep((long) (Math.random() * 200) + 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.PLAYERS, (float) (Math.random() * 20 + 2), (float) (Math.random()));
            }
        }
    }

}
