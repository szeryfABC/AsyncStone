package me.lemurxd.asyncstone.generators;

import me.lemurxd.asyncstone.AsyncStone;
import me.lemurxd.asyncstone.generators.settings.GeneratorSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class BlockGenerationEngine {

    private final AsyncStone plugin;

    public BlockGenerationEngine(AsyncStone plugin) {
        this.plugin = plugin;
    }

    public void triggerGeneration(StoneGenerator generator) {
        GeneratorSettings settings = plugin.getGeneratorsConfig().getSettings(generator.getId());

        if (settings == null) return;

        Location targetLoc = generator.getLocation().clone();
        if (settings.getType() == GeneratorSettings.Type.ABOVE) {
            targetLoc.add(0, 1, 0);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            Block targetBlock = targetLoc.getBlock();

            if (targetBlock.getType().isSolid()) {
                return;
            }

            Material toSpawn = getRandomDrop(settings.getDrops());

            if (toSpawn != null) {
                targetBlock.setType(toSpawn, false);
            }

        }, settings.getCooldown());
    }

    private Material getRandomDrop(List<GeneratorSettings.DropEntry> drops) {
        if (drops.isEmpty()) return Material.STONE;

        double totalWeight = 0.0;
        for (GeneratorSettings.DropEntry entry : drops) {
            totalWeight += entry.chance();
        }

        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;

        for (GeneratorSettings.DropEntry entry : drops) {
            currentWeight += entry.chance();
            if (random <= currentWeight) {
                return entry.material();
            }
        }
        return drops.get(0).material();
    }
}
