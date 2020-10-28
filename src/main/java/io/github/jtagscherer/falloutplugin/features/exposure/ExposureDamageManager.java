package io.github.jtagscherer.falloutplugin.features.exposure;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class ExposureDamageManager implements Listener {

    private Plugin plugin;
    private Integer taskId;
    private ExposureTester exposureTester;
    private double baseDamage;

    public ExposureDamageManager(Plugin plugin) {
        this.plugin = plugin;
        this.exposureTester = new ExposureTester(10);
        this.baseDamage = 2.5;
    }

    public void start() {
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkExposureForAllPlayers, 0L, 20L);
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
        if (this.exposureTester.isPlayerExposed(player)) {
            if (player.getHealth() > 2) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 10, 1);
                player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
            }

            player.damage(2);
        }
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

}
