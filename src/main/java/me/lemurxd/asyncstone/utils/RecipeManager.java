package me.lemurxd.asyncstone.utils;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.StoneGeneratorItem;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeManager {

    private final AsyncStone plugin;
    private final List<ItemStack> registeredResults = new ArrayList<>();

    public RecipeManager(AsyncStone plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void reloadRecipes() {

        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe != null && recipe.getResult() != null) {
                ItemStack result = recipe.getResult();

                for (ItemStack registeredItem : registeredResults) {
                    if (result.isSimilar(registeredItem)) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        registeredResults.clear();

        for (String generatorId : plugin.getGeneratorsConfig().getAllIds()) {
            GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generatorId);

            if (settings.getRecipeShape() == null || settings.getRecipeShape().isEmpty() ||
                    settings.getRecipeIngredients() == null || settings.getRecipeIngredients().isEmpty()) {
                continue;
            }

            try {
                ItemStack resultItem = StoneGeneratorItem.create(generatorId);
                if (resultItem == null) continue;

                ShapedRecipe recipe = new ShapedRecipe(resultItem);

                List<String> shapeList = settings.getRecipeShape();
                recipe.shape(shapeList.toArray(new String[0]));

                for (String ingredientStr : settings.getRecipeIngredients()) {
                    String[] split = ingredientStr.split(":");

                    if (split.length == 2) {
                        char symbol = split[0].charAt(0);
                        Material material = Material.matchMaterial(split[1].toUpperCase());

                        if (material != null) {
                            recipe.setIngredient(symbol, material);
                        } else {
                            plugin.getLogger().warning("UWAGA! Niepoprawny material w configu (receptura dla " + generatorId + "): " + split[1]);
                        }
                    } else {
                        plugin.getLogger().warning("UWAGA! Zly format w configu (receptura dla " + generatorId + "): " + ingredientStr + " (Prawidlowy format to np. S:STONE)");
                    }
                }

                plugin.getServer().addRecipe(recipe);
                registeredResults.add(resultItem);

            } catch (Exception e) {
                plugin.getLogger().severe("Wystapil blad podczas ladowania receptury dla stoniarki: " + generatorId);
                e.printStackTrace();
            }
        }
    }
}
