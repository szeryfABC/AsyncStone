package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.commands.AsyncStoneCommand;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.settings.GeneratorsConfig;
import me.lemurxd.asyncstone.listeners.ChunkLoadListener;
import me.lemurxd.asyncstone.listeners.GeneratorBreakEvent;
import me.lemurxd.asyncstone.listeners.GeneratorPlaceEvent;
import me.lemurxd.asyncstone.listeners.GeneratorProtectionListener;
import me.lemurxd.asyncstone.listeners.gui.GeneratorInteractEvent;
import me.lemurxd.asyncstone.listeners.gui.GeneratorMenuListener;
import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.Config;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import me.lemurxd.asyncstone.utils.RecipeManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;

public class AsyncStone extends JavaPlugin {

    private static AsyncStone instance;
    private StoneCacheManager cacheManager;
    private RecipeManager recipeManager;
    private GeneratorsConfig generatorsConfig;
    private DatabaseManager databaseManager;
    private BlockGenerationEngine  generationEngine;
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        new AsyncSaveTask(cacheManager, databaseManager).runTaskTimerAsynchronously(this, 6000L, 6000L);

        this.cacheManager = new StoneCacheManager();
        this.recipeManager = new RecipeManager(this);
        this.generatorsConfig = new GeneratorsConfig(this);
        this.databaseManager = new DatabaseManager(this);
        this.generationEngine = new BlockGenerationEngine(this);

        loadConfiguration();

        recipeManager.reloadRecipes();

        registerListeners();
        registerCommands();

        getLogger().info("AsyncStone on!");
    }

    @Override
    public void onDisable() {

        for (ChunkKey key : cacheManager.getDirtyChunks()) {
            Collection<StoneGenerator> data = cacheManager.getGeneratorsInChunk(key);
            databaseManager.saveChunkAsync(key.uuid(), key.x(), key.z(), data);
        }

        databaseManager.close();
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
        Config.load(configFile);

        generatorsConfig.load();
    }

    public GeneratorsConfig getGeneratorsConfig() {
        return generatorsConfig;
    }

    public void softReload() {
        loadConfiguration();
        recipeManager.reloadRecipes();
        getLogger().info("Przeladowano konfiguracje i receptury (Soft Reload)!");
    }


    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GeneratorPlaceEvent(instance, cacheManager, generationEngine), this);
        getServer().getPluginManager().registerEvents(new GeneratorBreakEvent(instance, cacheManager, generationEngine), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(cacheManager, databaseManager), this);
        getServer().getPluginManager().registerEvents(new GeneratorInteractEvent(cacheManager), this);
        getServer().getPluginManager().registerEvents(new GeneratorMenuListener(instance, cacheManager), this);
        getServer().getPluginManager().registerEvents(new GeneratorProtectionListener(cacheManager), this);
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