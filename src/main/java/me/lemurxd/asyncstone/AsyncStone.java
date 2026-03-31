package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.commands.AsyncStoneCommand;
import me.lemurxd.asyncstone.generators.BlockGenerationEngine;
import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.settings.GeneratorsConfig;
import me.lemurxd.asyncstone.listeners.ChunkLoadListener;
import me.lemurxd.asyncstone.listeners.GeneratorBreakEvent;
import me.lemurxd.asyncstone.listeners.GeneratorPlaceEvent;
import me.lemurxd.asyncstone.records.ChunkKey;
import me.lemurxd.asyncstone.utils.Config;
import me.lemurxd.asyncstone.utils.DatabaseManager;
import me.lemurxd.asyncstone.utils.RecipeManager;
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

    @Override
    public void onEnable() {
        instance = this;

        new AsyncSaveTask(cacheManager, databaseManager).runTaskTimerAsynchronously(this, 200L, 200L);

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