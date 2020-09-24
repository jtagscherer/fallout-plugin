package io.github.jtagscherer.falloutplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class FalloutPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("Enabled Fallout Plugin.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled Fallout Plugin.");
    }

}
