package io.github.jtagscherer.falloutplugin;

import io.github.jtagscherer.falloutplugin.exceptions.InvalidCommandException;
import io.github.jtagscherer.falloutplugin.features.exposure.ExposureDamageManager;
import io.github.jtagscherer.falloutplugin.features.graceperiod.GracePeriodManager;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FalloutPlugin extends JavaPlugin {

    private GracePeriodManager gracePeriodManager;

    public FalloutPlugin() {
        this.gracePeriodManager = new GracePeriodManager(this);
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

            this.handleCommand(player, args);
            return true;
        }

        return false;
    }

    private void validateCommand(Player sender, Command command, String[] args) throws InvalidCommandException {
        if (!sender.isOp()) {
            throw new InvalidCommandException("You need to be an operator to control this plugin.");
        }

        if (args.length < 1 || !(args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))) {
            throw new InvalidCommandException(String.format("Usage: %s", command.getUsage()));
        }
    }

    private void handleCommand(Player sender, String[] args) {
        World world = sender.getWorld();

        if (args[0].equalsIgnoreCase("start")) {
            world.setTime(6000);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            int duration;
            try {
                duration = Integer.valueOf(args[1]);
            } catch (Exception e) {
                getLogger().warning("Could not start fallout: " + e.getMessage());
                sender.sendMessage("Usage: /fallout start graceperiod (in seconds)");
                return;
            }

            this.gracePeriodManager.start(duration);
        } else if (args[0].equalsIgnoreCase("stop")) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

            this.gracePeriodManager.stop();
        }
    }

}