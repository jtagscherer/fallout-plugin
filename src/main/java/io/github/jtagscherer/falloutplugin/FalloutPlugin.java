package io.github.jtagscherer.falloutplugin;

import io.github.jtagscherer.falloutplugin.exceptions.InvalidCommandException;
import io.github.jtagscherer.falloutplugin.features.exposure.ExposureDamageManager;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FalloutPlugin extends JavaPlugin {

    private ExposureDamageManager exposureDamageManager;

    public FalloutPlugin() {
        this.exposureDamageManager = new ExposureDamageManager(this);
    }

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
        World world = sender.getWorld();

        if (argument.equalsIgnoreCase("start")) {
            world.setTime(6000);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            this.exposureDamageManager.start();
        } else if (argument.equalsIgnoreCase("stop")) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

            this.exposureDamageManager.stop();
        }

        /*sender.playSound(sender.getLocation(), Sound.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 10, 1);
        sender.playSound(sender.getLocation(), Sound.AMBIENT_BASALT_DELTAS_LOOP, SoundCategory.PLAYERS, 3, 1);
        sender.playSound(sender.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 5, 0.1f);
        sender.spawnParticle(Particle.ASH, sender.getLocation(), 100000, 5, 5, 5);*/
    }

}
