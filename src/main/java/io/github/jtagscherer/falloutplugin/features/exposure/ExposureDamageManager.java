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

    static final int TEST_RADIUS = 20;
    static final int EXPOSURE_RADIUS = 10;

    private Plugin plugin;
    private Integer updateTaskId, particleLocationTaskId, particleTaskId;
    private PlayerActionCache playerActionCache;
    private double baseDamage;

    private ReentrantLock locationLock = new ReentrantLock();
    private ReentrantLock particleLocationLock = new ReentrantLock();
    private Map<String, Set<DistancedLocation>> leakLocations = new HashMap<>();
    private Map<String, Set<Location>> searchedLocations = new HashMap<>();
    private Map<String, List<DistancedLocation>> particleLocations = new HashMap<>();
    private List<String> changedPlayers = new ArrayList<>();

    public ExposureDamageManager(Plugin plugin) {
        this.plugin = plugin;
        this.baseDamage = 2.5;
        this.playerActionCache = new PlayerActionCache();
    }

    public void start() {
        this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkExposureForAllPlayers, 0L, 20L);
        this.particleLocationTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::findParticleLocationsForLeaks, 10L, 20L);
        this.particleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::spawnRadiationParticles, 15L, 20L);

        this.plugin.getServer().getPluginManager().registerEvents(this.playerActionCache, plugin);
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

        ExposureTester exposureTester = new ExposureTester(TEST_RADIUS);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                continue;
            }

            if (!this.playerActionCache.hasPlayerChanged(player)) {
                continue;
            }

            String playerName = player.getName();

            if (!this.leakLocations.containsKey(playerName)) {
                this.leakLocations.put(playerName, new HashSet<>());
            }
            if (!this.searchedLocations.containsKey(playerName)) {
                this.searchedLocations.put(playerName, new HashSet<>());
            }

            this.changedPlayers.add(playerName);
            this.leakLocations.get(playerName).clear();
            this.searchedLocations.get(playerName).clear();

            exposureTester.searchLeakLocationsForPlayer(player);

            this.leakLocations.get(playerName).addAll(exposureTester.getLeakLocations());
            this.searchedLocations.get(playerName).addAll(exposureTester.getSearchedLocations());

            for (DistancedLocation leakLocation : exposureTester.getLeakLocations()) {
                if (leakLocation.getDistance() < EXPOSURE_RADIUS) {
                    this.damagePlayer(player);
                    break;
                }
            }

            exposureTester.clear();
        }

        this.playerActionCache.clearIntervalCaches();

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

        return 0;
    }

    private void findParticleLocationsForLeaks() {
        this.locationLock.lock();
        this.particleLocationLock.lock();

        for (String playerName : this.changedPlayers) {
            if (this.particleLocations.containsKey(playerName)) {
                this.particleLocations.get(playerName).clear();
            }

            for (DistancedLocation leakLocation : this.leakLocations.get(playerName)) {
                Player player = Bukkit.getPlayer(playerName);

                if (player == null) {
                    continue;
                }

                this.findParticleLocationsForLeak(leakLocation, this.searchedLocations.get(playerName), player);
            }
        }

        this.changedPlayers.clear();

        this.locationLock.unlock();
        this.particleLocationLock.unlock();
    }

    private void findParticleLocationsForLeak(DistancedLocation leakLocation, Set<Location> searchedLocations, Player player) {
        BreadthFirstSearch leakParticleSearch = new BreadthFirstSearch(EXPOSURE_RADIUS, player.getWorld(), leakLocation.getLocation())
                .withStopSearchingWhenEncounteringTarget(false)
                .withAllowedLocations(searchedLocations)
                .search((l, w) -> true);

        String playerName = player.getName();

        if (!this.particleLocations.containsKey(playerName)) {
            this.particleLocations.put(playerName, new ArrayList<>());
        }

        this.particleLocations.get(playerName).addAll(leakParticleSearch.getTargetLocations());
    }

    private void spawnRadiationParticles() {
        this.particleLocationLock.lock();

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            for (String playerName : this.particleLocations.keySet()) {
                List<DistancedLocation> particleLocationsInWorld = this.particleLocations.get(playerName);
                Player player = Bukkit.getPlayer(playerName);

                if (player == null) {
                    continue;
                }

                World world = player.getWorld();

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
