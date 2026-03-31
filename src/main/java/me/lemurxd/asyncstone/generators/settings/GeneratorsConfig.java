package me.lemurxd.asyncstone.generators.settings;

import me.lemurxd.asyncstone.AsyncStone;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorsConfig {

    private final AsyncStone plugin;
    private File file;
    private YamlConfiguration config;

    private final Map<String, GeneratorSettings> settingsCache = new HashMap<>();

    public GeneratorsConfig(AsyncStone plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), "generators.yml");

        if (!file.exists()) {
            plugin.saveResource("generators.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        parseConfig();
    }

    private void parseConfig() {
        settingsCache.clear();

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            try {
                Material material = Material.matchMaterial(section.getString("id", "END_STONE").toUpperCase());
                String name = section.getString("name", "Brak nazwy");
                String typeStr = section.getString("type", "above").toUpperCase();
                GeneratorSettings.Type type = GeneratorSettings.Type.valueOf(typeStr);
                int cooldown = section.getInt("cooldown", 40);

                List<GeneratorSettings.DropEntry> drops = new ArrayList<>();
                List<String> blockList = section.getStringList("blocks");
                for (String blockStr : blockList) {
                    String[] split = blockStr.split(":");
                    if (split.length == 2) {
                        Material dropMat = Material.matchMaterial(split[0].toUpperCase());
                        double chance = Double.parseDouble(split[1]);
                        if (dropMat != null) drops.add(new GeneratorSettings.DropEntry(dropMat, chance));
                    }
                }

                GeneratorSettings.UpgradeData upgradeData = null;
                if (section.contains("upgrade")) {
                    String nextGen = section.getString("upgrade.gen");
                    List<String> costList = section.getStringList("upgrade.cost");
                    List<GeneratorSettings.UpgradeCost> costs = new ArrayList<>();

                    for (String costStr : costList) {
                        String[] split = costStr.split(":");
                        if (split[0].equalsIgnoreCase("money") && split.length == 2) {
                            costs.add(new GeneratorSettings.UpgradeCost("money", null, Integer.parseInt(split[1])));
                        }
                        else if (split[0].equalsIgnoreCase("item") && split.length == 3) {
                            costs.add(new GeneratorSettings.UpgradeCost("item", split[1].toUpperCase(), Integer.parseInt(split[2])));
                        }
                    }
                    upgradeData = new GeneratorSettings.UpgradeData(nextGen, costs);
                }

                GeneratorSettings settings = new GeneratorSettings(key, material, name, type, cooldown, drops, upgradeData);
                settingsCache.put(key, settings);

            } catch (Exception e) {
                plugin.getLogger().warning("Blad parsowania stoniarki w sekcji: " + key + ". Sprawdz plik generators.yml!");
                e.printStackTrace();
            }
        }
    }

    public GeneratorSettings getSettings(String generatorId) {
        return settingsCache.get(generatorId);
    }
}
