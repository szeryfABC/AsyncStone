package me.lemurxd.asyncstone;

import org.bukkit.plugin.java.JavaPlugin;

public class AsyncStone extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("AsyncStone on!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AsyncStone off.");
    }
}
