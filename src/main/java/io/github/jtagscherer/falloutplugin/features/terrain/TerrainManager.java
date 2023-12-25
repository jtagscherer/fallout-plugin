package io.github.jtagscherer.falloutplugin.features.terrain;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

public class TerrainManager {

    private Plugin plugin;
    private Integer taskId;

    private int chunkRadius;
    private List<String> modifiedChunks;
    private AtomicBoolean updateInProgress;

    private String modifiedChunksFileName = "modified-chunks.txt";

    public TerrainManager(Plugin plugin) {
        this.plugin = plugin;
        this.chunkRadius = 8;
        this.updateInProgress = new AtomicBoolean(false);

        try {
            this.modifiedChunks = this.loadModifiedChunkFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::update, 0L, 20L * 5);
    }

    public void stop() {
        if (this.taskId != null) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }

        this.taskId = null;
    }

    private void update() {
        if (!this.updateInProgress.get()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::updateAllNearbyChunks);
        }
    }

    private void updateAllNearbyChunks() {
        this.updateInProgress.set(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            Location location = player.getLocation();

            for (int x = -1 * this.chunkRadius; x < this.chunkRadius; x++) {
                for (int z = -1 * this.chunkRadius; z < this.chunkRadius; z++) {
                    this.updateChunkIfNecessary(world.getChunkAt(location.clone().add(x * 16, 0, z * 16)));
                }
            }
        }

        try {
            this.storeModifiedChunkFile(this.modifiedChunks);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.updateInProgress.set(false);
    }

    private void updateChunkIfNecessary(Chunk chunk) {
        String chunkKey = this.createChunkKey(chunk);

        if (this.modifiedChunks.contains(chunkKey)) {
            return;
        }

        this.modifiedChunks.add(chunkKey);

        Block block;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    block = chunk.getBlock(x, y, z);
                    if (block.getType().isAir()) {
                        continue;
                    }

                    this.trimGrassBlocks(block);
                    this.trimFlowerBlocks(block);
                    this.trimLeafBlocks(block);
                }
            }
        }
    }

    private void trimFlowerBlocks(Block block) {
        Material material = block.getType();
        boolean isFlowerBlock = (material == Material.DANDELION || material == Material.POPPY
                || material == Material.BLUE_ORCHID || material == Material.ALLIUM
                || material == Material.AZURE_BLUET || material == Material.RED_TULIP
                || material == Material.PINK_TULIP || material == Material.WHITE_TULIP
                || material == Material.ORANGE_TULIP || material == Material.OXEYE_DAISY
                || material == Material.CORNFLOWER || material == Material.LILY_OF_THE_VALLEY
                || material == Material.SUNFLOWER || material == Material.LILAC
                || material == Material.ROSE_BUSH || material == Material.PEONY);

        if (isFlowerBlock && Math.random() > 0.05) {
            this.setBlockSynchronously(block, Material.AIR);
        }
    }

    private void trimGrassBlocks(Block block) {
        Material material = block.getType();

        if (material == Material.GRASS || material == Material.TALL_GRASS) {
            this.setBlockSynchronously(block, Material.AIR);
        }

        if (material == Material.GRASS_BLOCK && Math.random() > 0.08) {
            this.setBlockSynchronously(block, Material.COARSE_DIRT);
        }
    }

    private void trimLeafBlocks(Block block) {
        Material material = block.getType();
        boolean isLeafBlock = (material == Material.ACACIA_LEAVES || material == Material.BIRCH_LEAVES
                || material == Material.DARK_OAK_LEAVES || material == Material.JUNGLE_LEAVES
                || material == Material.OAK_LEAVES || material == Material.SPRUCE_LEAVES);

        if (isLeafBlock && Math.random() > 0.05) {
            this.setBlockSynchronously(block, Material.AIR);
        }
    }

    private void setBlockSynchronously(Block block, Material material) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            block.setType(material);
        });
    }

    private String createChunkKey(Chunk chunk) {
        return String.format("%d/%d", chunk.getX(), chunk.getZ());
    }

    private void storeModifiedChunkFile(List<String> modifiedChunks) throws IOException {
        Path path = new File(this.plugin.getDataFolder(), this.modifiedChunksFileName).toPath();

        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        Files.write(path, modifiedChunks, StandardCharsets.UTF_8);
    }

    private List<String> loadModifiedChunkFile() throws IOException {
        Path path = new File(this.plugin.getDataFolder(), this.modifiedChunksFileName).toPath();

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return new ArrayList<>();
        }

        List<String> modifiedChunks = new ArrayList<>();

        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            stream.forEach(modifiedChunks::add);
        }

        return modifiedChunks;
    }

}
