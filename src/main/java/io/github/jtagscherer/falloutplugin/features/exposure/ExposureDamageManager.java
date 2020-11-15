package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ExposureDamageManager implements Listener {

    private static final int TEST_RADIUS = 20;
    private static final int EXPOSURE_RADIUS = 10;

    private Plugin plugin;
    private Integer updateTaskId, particleLocationTaskId, particleTaskId;
    private double baseDamage;

    private ReentrantLock locationLock = new ReentrantLock();
    private ReentrantLock particleLocationLock = new ReentrantLock();
    private Map<String, Set<DistancedLocation>> leakLocations = new HashMap<>();
    private Map<String, Set<Location>> searchedLocations = new HashMap<>();
    private Map<String, List<DistancedLocation>> particleLocations = new HashMap<>();

    public ExposureDamageManager(Plugin plugin) {
        this.plugin = plugin;
        this.baseDamage = 2.5;
    }

    public void start() {
        this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkExposureForAllPlayers, 0L, 20L);
        this.particleLocationTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::findParticleLocationsForLeaks, 0L, 20L * 5);
        this.particleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::spawnRadiationParticles, 0L, 20L);
    }

    public void stop() {
        if (this.updateTaskId != null) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
        }

        if (this.particleLocationTaskId != null) {
            Bukkit.getScheduler().cancelTask(this.particleLocationTaskId);
        }

        if (this.particleTaskId != null) {
            Bukkit.getScheduler().cancelTask(this.particleTaskId);
        }

        this.updateTaskId = null;
        this.particleTaskId = null;
        this.particleLocationTaskId = null;
    }

    private void checkExposureForAllPlayers() {
        this.locationLock.lock();

        this.leakLocations.clear();
        this.searchedLocations.clear();

        ExposureTester exposureTester = new ExposureTester(TEST_RADIUS);

        for (Player player : Bukkit.getOnlinePlayers()) {
            exposureTester.searchLeakLocationsForPlayer(player);

            String worldName = player.getWorld().getName();
            if (!this.leakLocations.containsKey(worldName)) {
                this.leakLocations.put(worldName, new HashSet<>());
            }
            if (!this.searchedLocations.containsKey(worldName)) {
                this.searchedLocations.put(worldName, new HashSet<>());
            }

            this.leakLocations.get(worldName).addAll(exposureTester.getLeakLocations());
            this.searchedLocations.get(worldName).addAll(exposureTester.getSearchedLocations());

            for (DistancedLocation leakLocation : exposureTester.getLeakLocations()) {
                if (leakLocation.getDistance() < EXPOSURE_RADIUS) {
                    this.damagePlayer(player);
                    break;
                }
            }

            exposureTester.clear();
        }

        this.locationLock.unlock();
    }

    private void damagePlayer(Player player) {
        if (player.getHealth() > 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 10, 1);
            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
        }

        player.damage(this.calculateDamage(player));
    }

    private double calculateDamage(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();

        if (helmet == null || !helmet.containsEnchantment(Enchantment.PROTECTION_FIRE)) {
            return this.baseDamage;
        }

        double damageReduction = helmet.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) / 2.0;

        Damageable meta = (Damageable) helmet.getItemMeta();
        if (meta != null) {
            if (meta.getDamage() > helmet.getType().getMaxDurability()) {
                player.getInventory().setHelmet(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 3, 1);
            } else {
                meta.setDamage(meta.getDamage() + 5);
                helmet.setItemMeta((ItemMeta) meta);
            }
        }

        return Math.max(0.5, this.baseDamage - damageReduction);
    }

    private void findParticleLocationsForLeaks() {
        this.locationLock.lock();
        this.particleLocationLock.lock();

        this.particleLocations.clear();

        for (String worldName : this.leakLocations.keySet()) {
            for (DistancedLocation leakLocation : this.leakLocations.get(worldName)) {
                this.findParticleLocationsForLeak(leakLocation, this.searchedLocations.get(worldName), Bukkit.getWorld(worldName));
            }
        }

        this.locationLock.unlock();
        this.particleLocationLock.unlock();
    }

    private void findParticleLocationsForLeak(DistancedLocation leakLocation, Set<Location> searchedLocations, World world) {
        BreadthFirstSearch leakParticleSearch = new BreadthFirstSearch(EXPOSURE_RADIUS, world, leakLocation.getLocation())
                .withStopSearchingWhenEncounteringTarget(false)
                .withAllowedLocations(searchedLocations)
                .search((l, w) -> true);

        if (!this.particleLocations.containsKey(world.getName())) {
            this.particleLocations.put(world.getName(), new ArrayList<>());
        }

        this.particleLocations.get(world.getName()).addAll(leakParticleSearch.getTargetLocations());
    }

    private void spawnRadiationParticles() {
        this.particleLocationLock.lock();

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            for (String worldName : this.particleLocations.keySet()) {
                World world = Bukkit.getWorld(worldName);
                List<DistancedLocation> particleLocationsInWorld = this.particleLocations.get(worldName);

                for (int i = 0; i < Math.min(particleLocationsInWorld.size() / 2, 1000); i++) {
                    DistancedLocation particleLocation = particleLocationsInWorld
                            .get((int) (Math.random() * particleLocationsInWorld.size()));

                    if (particleLocation.getLocation() != null) {
                        world.spawnParticle(Particle.TOWN_AURA, particleLocation.getLocation(), (EXPOSURE_RADIUS - particleLocation.getDistance()) * 5, 1, 1, 1);
                    }

                    try {
                        Thread.sleep((long) (Math.random() * 20));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.particleLocationLock.unlock();
    }

}
