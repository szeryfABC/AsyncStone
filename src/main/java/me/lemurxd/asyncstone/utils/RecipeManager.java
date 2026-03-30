package me.lemurxd.asyncstone.utils;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

public class RecipeManager {
    private final AsyncStone plugin;
    private final NamespacedKey recipeKey;

    public RecipeManager(AsyncStone plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "stone_generator_recipe");
    }

    public void reloadRecipes() {
        plugin.getServer().removeRecipe(recipeKey);

        try {
            ShapedRecipe recipe = new ShapedRecipe(recipeKey, StoneGeneratorItem.create());

            List<String> shapeList = Config.GENERATOR_RECIPE_SHAPE.getStringList();
            recipe.shape(shapeList.toArray(new String[0]));

            for (String ingredientStr : Config.GENERATOR_RECIPE_INGREDIENTS.getStringList()) {
                String[] split = ingredientStr.split(":");

                if (split.length == 2) {
                    char symbol = split[0].charAt(0);
                    Material material = Material.matchMaterial(split[1].toUpperCase());

                    if (material != null) {
                        recipe.setIngredient(symbol, material);
                    } else {
                        plugin.getLogger().warning("UWAGA! Niepoprawny material w config.yml (generator.recipe.ingredients): " + split[1]);
                    }
                } else {
                    plugin.getLogger().warning("UWAGA! Zly format w config.yml: " + ingredientStr + " (Prawidlowy format to np. S:STONE)");
                }
            }

            plugin.getServer().addRecipe(recipe);

        } catch (Exception e) {
            plugin.getLogger().severe("Wystapil blad podczas ladowania receptury stoniarki! Sprawdz config.yml.");
            e.printStackTrace();
        }
    }
}
