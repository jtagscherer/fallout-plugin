package io.github.jtagscherer.falloutplugin;

import io.github.jtagscherer.falloutplugin.exceptions.InvalidCommandException;
import io.github.jtagscherer.falloutplugin.features.graceperiod.GracePeriodManager;
import io.github.jtagscherer.falloutplugin.features.revival.RevivalManager;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FalloutPlugin extends JavaPlugin {

    private GracePeriodManager gracePeriodManager;
    private RevivalManager revivalManager;

    public FalloutPlugin() {
        this.gracePeriodManager = new GracePeriodManager(this);
        this.revivalManager = new RevivalManager(this);
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
        if (args.length < 1 || !(args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("reviveme"))) {
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
            this.revivalManager.start();
        } else if (args[0].equalsIgnoreCase("stop")) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

            this.gracePeriodManager.stop();
            this.revivalManager.stop();
        } else if (args[0].equalsIgnoreCase("reviveme")) {
            this.revivalManager.revivePlayer(sender);
        }
    }

}
