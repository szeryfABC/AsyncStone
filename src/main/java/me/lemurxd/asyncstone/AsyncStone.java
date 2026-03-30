package me.lemurxd.asyncstone;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.listeners.GeneratorPlaceEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class AsyncStone extends JavaPlugin {

    private static AsyncStone instance;

    private StoneCacheManager cacheManager;

    @Override
    public void onEnable() {
        instance = this;
        cacheManager = new StoneCacheManager();

        registerRecipes();
        registerListeners();

        getLogger().info("AsyncStone on!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AsyncStone off.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GeneratorPlaceEvent(cacheManager), this);
    }

    private void registerRecipes() {
        NamespacedKey recipeKey = new NamespacedKey(this, "stone_generator_recipe");

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, StoneGeneratorItem.create());

        recipe.shape(
                "SSS",
                "SRS",
                "SSS"
        );

        recipe.setIngredient('S', Material.STONE);
        recipe.setIngredient('R', Material.REDSTONE);

        getServer().addRecipe(recipe);
    }

    public static AsyncStone getInstance() {
        return instance;
    }

}
