package me.lemurxd.asyncstone.utils;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Config {
    MAIN_PREFIX("main.variables_prefix", "&aAsyncStone &f| ", true),

    GENERATOR_ITEM_MATERIAL("generator.item.material", "END_STONE"),
    GENERATOR_ITEM_NAME("generator.item.name", "&e&lStoniarka", true),
    GENERATOR_ITEM_LORE("generator.item.lore", Arrays.asList(
            "&7Postaw ten blok, aby",
            "&7rozpocząć generowanie kamienia."
    ), true),
    GENERATOR_RECIPE_SHAPE("generator.recipe.shape", Arrays.asList(
            "SSS",
            "SRS",
            "SSS"
    )),
    GENERATOR_RECIPE_INGREDIENTS("generator.recipe.ingredients", Arrays.asList(
            "S:STONE",
            "R:REDSTONE"
    ))

    ;



    private final String path;
    private List<String> texts;
    private String text;
    private Component component;
    private List<Component> components;
    private int number;
    private boolean bool;
    private boolean colored = false;
    private Class<?> type;


    private Config(String path, String text) {
        this(path, text, false);
    }
    private Config(String path, String text, boolean colored) {
        this.path = path;
        this.colored = colored;
        setValue(text);
    }
    private Config(String path, List<String> texts) {
        this(path, texts, false);
    }
    private Config(String path, List<String> texts, boolean colored) {
        this.path = path;
        this.colored = colored;
        setValue(texts);
    }
    private Config(String path, int number) {
        this.path = path;
        setValue(number);
    }
    private Config(String path, boolean bool) {
        this.path = path;
        setValue(bool);
    }


    public void setValue(String text) {
        this.type = String.class;
        this.text = text;
        this.component = this.colored? parseComponent(this.text) : null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = this.colored? Collections.unmodifiableList(Arrays.asList(parseComponent(this.text))) : null;
        this.number = text.length();
        this.bool = !text.isEmpty();
    }
    public void setValue(List<String> texts) {
        this.type = String[].class;
        this.text = String.join(", ", texts);
        this.component = this.colored? parseComponent(this.text) : null;
        this.texts = Collections.unmodifiableList(texts);
        this.components = this.colored? Collections.unmodifiableList(this.texts.stream().map(Config::parseComponent).collect(Collectors.toList())) : null;
        this.number = texts.size();
        this.bool = !texts.isEmpty();
    }
    public void setValue(int number) {
        this.type = Integer.class;
        this.text = Integer.toString(number);
        this.component = null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = null;
        this.number = number;
        this.bool = number > 0;
    }
    public void setValue(boolean bool) {
        this.type = Boolean.class;
        this.text = Boolean.toString(bool);
        this.component = null;
        this.texts = Collections.unmodifiableList(Arrays.asList(this.text));
        this.components = null;
        this.number = bool? 1 : 0;
        this.bool = bool;
    }

    public static Component parseComponent(@NotNull String text) {
        try {
            return (Component) GsonComponentSerializer.gson().deserialize(text);
        } catch (Exception e) {
            return (Component) LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(text);
        }
    }


    public String getString() {
        if (this == MAIN_PREFIX) {
            return this.text;
        }

        return this.text.replace("<prefix>", MAIN_PREFIX.text);
    }
    @Override
    public String toString() {
        return this.text;
    }
    public List<String> getStringList() {
        return this.texts;
    }
    public int getInt() {
        return this.number;
    }
    public boolean getBoolean() {
        return this.bool;
    }
    public File getFile() {
        return new File(this.text);
    }
    public Component getComponent() {
        return this.component;
    }
    public boolean isColored() {
        return this.colored;
    }
    public String getPath() {
        return this.path;
    }
    public Class<?> getType() {
        return this.type;
    }

    public static void load(File file) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if(!file.exists()) file.createNewFile();
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            if(loadConfig(yml) > 0) yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void save(File file) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if(!file.exists()) file.createNewFile();
            saveConfig().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static YamlConfiguration saveConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Config val : Config.values()) {
            if (val.getType().equals(String.class)) {
                if (val.isColored()) config.set(val.getPath(), val.getString().replace('§', '&'));
                else config.set(val.getPath(), val.getString());
            } else if (val.getType().equals(String[].class)) {
                if (val.isColored()) config.set(val.getPath(), val.getStringList().stream().map(s -> s.replace('§', '&')).collect(Collectors.toList()));
                else config.set(val.getPath(), val.getStringList());
            } else if (val.getType().equals(Integer.class)) config.set(val.getPath(), val.getInt());
            else if (val.getType().equals(Boolean.class)) config.set(val.getPath(), val.getBoolean());
        }
        return config;
    }
    private static int loadConfig(ConfigurationSection config) {
        int modify = 0;
        for (Config val : Config.values()) {
            if(!config.contains(val.getPath())) modify++;
            if (val.getType().equals(String.class)) {
                if (val.isColored())val.setValue(getColoredStringOrSetDefault(config, val.getPath(), val.getString()));
                else val.setValue(getStringOrSetDefault(config, val.getPath(), val.getString()));
            } else if (val.getType().equals(String[].class)) {
                if (val.isColored())val.setValue(getColoredStringListOrSetDefault(config, val.getPath(), val.getStringList()));
                else val.setValue(getStringListOrSetDefault(config, val.getPath(), val.getStringList()));
            } else if (val.getType().equals(Integer.class)) val.setValue(getIntOrSetDefault(config, val.getPath(), val.getInt()));
            else if (val.getType().equals(Boolean.class)) val.setValue(getBooleanOrSetDefault(config, val.getPath(), val.getBoolean()));
        }
        return modify;
    }



    private static int getIntOrSetDefault(ConfigurationSection config, String path, int def) {
        if (config.contains(path)) return config.getInt(path);
        config.set(path, def);
        return def;
    }
    private static boolean getBooleanOrSetDefault(ConfigurationSection config, String path, boolean def) {
        if (config.contains(path)) return config.getBoolean(path);
        config.set(path, def);
        return def;
    }
    private static String getStringOrSetDefault(ConfigurationSection config, String path, String def) {
        if (config.contains(path)) return config.getString(path);
        config.set(path, def);
        return def;
    }
    private static String getColoredStringOrSetDefault(ConfigurationSection config, String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', getStringOrSetDefault(config, path, def.replace('§', '&')));
    }
    private static ArrayList<String> getStringListOrSetDefault(ConfigurationSection config, String path, List<String> def) {
        if(config.contains(path)) return new ArrayList<>(config.getStringList(path));
        config.set(path, def);
        return new ArrayList<>(def);
    }
    private static ArrayList<String> getColoredStringListOrSetDefault(ConfigurationSection config, String path, List<String> def) {
        ArrayList<String> list = getStringListOrSetDefault(config, path, def.stream().map(str -> str.replace('§', '&')).collect(Collectors.toCollection(ArrayList::new)));
        return list.stream().map(str -> ChatColor.translateAlternateColorCodes('&', str))
                .collect(Collectors.toCollection(ArrayList::new));
    }



}