package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.commands.AsyncStoneCommand;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.settings.GeneratorsConfig;
import me.lemurxd.asyncstone.listeners.GeneratorBreakEvent;
import me.lemurxd.asyncstone.listeners.GeneratorPlaceEvent;
import me.lemurxd.asyncstone.listeners.GeneratorProtectionListener;
import me.lemurxd.asyncstone.listeners.WorldLoadListener;
import me.lemurxd.asyncstone.listeners.gui.GeneratorInteractEvent;
import me.lemurxd.asyncstone.listeners.gui.GeneratorMenuListener;
import me.lemurxd.asyncstone.utils.Config;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import me.lemurxd.asyncstone.utils.RecipeManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AsyncStone extends JavaPlugin {

    private static AsyncStone instance;
    private StoneCacheManager cacheManager;
    private RecipeManager recipeManager;
    private GeneratorsConfig generatorsConfig;
    private DatabaseManager databaseManager;
    private BlockGenerationEngine generationEngine;
    private AsyncSaveTask saveTask;
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().warning("Economy setup failed! Money upgrade costs will not work.");
        }

        this.cacheManager = new StoneCacheManager();
        this.recipeManager = new RecipeManager(this);
        this.generatorsConfig = new GeneratorsConfig(this);
        this.databaseManager = new DatabaseManager(new File(getDataFolder(), "data.db"), getLogger());
        this.generationEngine = new BlockGenerationEngine(this);

        loadConfiguration();
        databaseManager.connect();

        WorldLoadListener worldLoader = new WorldLoadListener(cacheManager, databaseManager);
        getServer().getWorlds().forEach(worldLoader::load);

        recipeManager.reloadRecipes();

        registerListeners(worldLoader);
        registerCommands();

        saveTask = new AsyncSaveTask(cacheManager, databaseManager);
        saveTask.runTaskTimerAsynchronously(this, 6000L, 6000L);

        getLogger().info("AsyncStone on!");
    }

    @Override
    public void onDisable() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask.run();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("AsyncStone off.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    private void loadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            Config.load(configFile);
        } catch (Exception e) {
            getComponentLogger().error("Cannot load main config", e);
        }

        generatorsConfig.load();
    }

    public GeneratorsConfig getGeneratorsConfig() {
        return generatorsConfig;
    }

    public void softReload() {
        loadConfiguration();
        recipeManager.reloadRecipes();
        getLogger().info("Configuration and recipes reloaded (soft reload)!");
    }

    private void registerListeners(WorldLoadListener worldLoader) {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldLoader, this);
        pm.registerEvents(new GeneratorPlaceEvent(cacheManager, generationEngine), this);
        pm.registerEvents(new GeneratorBreakEvent(this, cacheManager, generationEngine), this);
        pm.registerEvents(new GeneratorInteractEvent(cacheManager), this);
        pm.registerEvents(new GeneratorMenuListener(this, cacheManager), this);
        pm.registerEvents(new GeneratorProtectionListener(cacheManager, generationEngine), this);
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
