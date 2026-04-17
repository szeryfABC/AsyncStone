package me.lemurxd.asyncstone.generators.settings;

import org.bukkit.Material;

import java.util.List;

public class GeneratorSettings {

    public enum Type {
        ABOVE,
        REPLACE
    }

    public record DropEntry(Material material, double chance) {}

    public record UpgradeCost(String type, String value, int amount) {}

    public record UpgradeData(String nextGeneratorId, List<UpgradeCost> costs) {}

    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final Type type;
    private final int cooldown;
    private final List<DropEntry> drops;
    private final UpgradeData upgradeData;
    private final List<String> recipeShape;
    private final List<String> recipeIngredients;

    public GeneratorSettings(String id, Material material, String name, List<String> lore, Type type, int cooldown, List<DropEntry> drops, UpgradeData upgradeData, List<String> recipeShape, List<String> recipeIngredients) {
        this.id = id;
        this.material = material;
        this.name = name;

        this.lore = lore != null ? List.copyOf(lore) : List.of();

        this.type = type;
        this.cooldown = cooldown;
        this.drops = List.copyOf(drops);
        this.upgradeData = upgradeData;

        this.recipeShape = recipeShape != null ? List.copyOf(recipeShape) : null;
        this.recipeIngredients = recipeIngredients != null ? List.copyOf(recipeIngredients) : null;
    }

    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public Type getType() { return type; }
    public int getCooldown() { return cooldown; }
    public List<DropEntry> getDrops() { return drops; }
    public UpgradeData getUpgradeData() { return upgradeData; }
    public List<String> getRecipeShape() { return recipeShape; }
    public List<String> getRecipeIngredients() { return recipeIngredients; }
}
