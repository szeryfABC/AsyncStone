package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.commands.AsyncStoneCommand;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.listeners.GeneratorPlaceEvent;
import me.lemurxd.asyncstone.utils.Config;
import me.lemurxd.asyncstone.utils.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AsyncStone extends JavaPlugin {

    private static AsyncStone instance;
    private StoneCacheManager cacheManager;
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        instance = this;

        cacheManager = new StoneCacheManager();
        recipeManager = new RecipeManager(this);

        loadConfiguration();

        recipeManager.reloadRecipes();

        registerListeners();
        registerCommands();

        getLogger().info("AsyncStone on!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AsyncStone off.");
    }


    private void loadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");
        Config.load(configFile);
    }

    public void softReload() {
        loadConfiguration();
        recipeManager.reloadRecipes();
        getLogger().info("Przeladowano konfiguracje i receptury (Soft Reload)!");
    }


    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GeneratorPlaceEvent(cacheManager), this);
    }

    private void registerCommands() {
        AsyncStoneCommand commandClass = new AsyncStoneCommand(this);
        getCommand("asyncstone").setExecutor(commandClass);
        getCommand("asyncstone").setTabCompleter(commandClass);
    }


    public static AsyncStone getInstance() {
        return instance;
    }
}