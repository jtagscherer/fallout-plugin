package io.github.jtagscherer.falloutplugin;

import io.github.jtagscherer.falloutplugin.exceptions.InvalidCommandException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class FalloutPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("Enabled Fallout Plugin.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled Fallout Plugin.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fallout") && sender instanceof Player) {
            Player player = (Player) sender;

            try {
                this.validateCommand(player, command, args);
            } catch (InvalidCommandException e) {
                player.sendMessage(e.getMessage());
                return true;
            }

            this.handleCommand(player, args[0]);
            return true;
        }

        return false;
    }

    private void validateCommand(Player sender, Command command, String[] args) throws InvalidCommandException {
        if (!sender.isOp()) {
            throw new InvalidCommandException("You need to be an operator to control this plugin.");
        }

        if (args.length != 1 || !(args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))) {
            throw new InvalidCommandException(String.format("Usage: %s", command.getUsage()));
        }
    }

    private void handleCommand(Player sender, String argument) {
        sender.playSound(sender.getLocation(), Sound.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 10, 1);
        sender.playSound(sender.getLocation(), Sound.AMBIENT_BASALT_DELTAS_LOOP, SoundCategory.PLAYERS, 3, 1);
        sender.playSound(sender.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 5, 0.1f);
        sender.spawnParticle(Particle.ASH, sender.getLocation(), 100000, 5, 5, 5);
        
        /*sender.playEffect(sender.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
        sender.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
        sender.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));*/
    }

}
