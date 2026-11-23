package me.lemurxd.asyncstone.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Config {
    MAIN_PREFIX("main.variables_prefix", "&aAsyncStone &8| ", true),

    GUI_TITLE("gui.title", "&8Generator Menu", true),
    GUI_SIZE("gui.size", 27),

    GUI_UPGRADE_SLOT("gui.upgrade.slot", 11),
    GUI_UPGRADE_MATERIAL("gui.upgrade.material", "GOLD_INGOT"),
    GUI_UPGRADE_NAME("gui.upgrade.name", "&e&lUpgrade Generator", true),
    GUI_UPGRADE_LORE("gui.upgrade.lore", Arrays.asList(
            "&7Click here to upgrade this",
            "&7generator to the next level!",
            "",
            "&7Cost: &f<cost>"
    ), true),

    GUI_MAX_SLOT("gui.max_level.slot", 11),
    GUI_MAX_MATERIAL("gui.max_level.material", "BARRIER"),
    GUI_MAX_NAME("gui.max_level.name", "&c&lMax Level", true),
    GUI_MAX_LORE("gui.max_level.lore", Arrays.asList(
            "&7This generator has reached",
            "&7its maximum level."
    ), true),

    GUI_PICKUP_SLOT("gui.pickup.slot", 15),
    GUI_PICKUP_MATERIAL("gui.pickup.material", "BEDROCK"),
    GUI_PICKUP_NAME("gui.pickup.name", "&c&lPick Up", true),
    GUI_PICKUP_LORE("gui.pickup.lore", Arrays.asList(
            "&7Click here to safely",
            "&7pick up this generator."
    ), true),

    GUI_COST_FREE("gui.cost.free", "Free", true),
    GUI_COST_MONEY("gui.cost.money", "$<amount>", true),
    GUI_COST_ITEM("gui.cost.item", "<amount>x <item>", true),

    MSG_INFO("messages.info", Arrays.asList(
            "&8&m----------------------------------------",
            "&e&lAsyncStone &7v<version>",
            "&7Author: &elemurxd_",
            "&7GitHub: &bhttps://github.com/lemurxd_",
            "&8&m----------------------------------------"
    ), true),
    MSG_HELP("messages.help", Arrays.asList(
            "&8&m----------------------------------------",
            "&e&lAsyncStone Admin Help:",
            "&8» &e/asyncstone give <player> <id> [amount] &7- Gives a generator to a player",
            "&8» &e/asyncstone reload &7- Reloads the configuration",
            "&8» &e/asyncstone help &7- Shows this menu",
            "&8&m----------------------------------------"
    ), true),
    MSG_NO_PERMISSION("messages.no_permission", "<prefix>&cYou don't have permission to do that!", true),
    MSG_UNKNOWN_COMMAND("messages.unknown_command", "<prefix>&cUnknown command. Type &e/asyncstone help &cto see the list.", true),
    MSG_RELOADED("messages.reloaded", "<prefix>&aConfiguration and recipes have been reloaded!", true),
    MSG_GIVE_USAGE("messages.give.usage", "<prefix>&cUsage: /asyncstone give <player> <generator_id> [amount]", true),
    MSG_GIVE_PLAYER_OFFLINE("messages.give.player_offline", "<prefix>&cPlayer &e<player> &cis not online!", true),
    MSG_GIVE_UNKNOWN_GENERATOR("messages.give.unknown_generator", "<prefix>&cGenerator with ID '&e<id>&c' does not exist in generators.yml!", true),
    MSG_GIVE_INVALID_AMOUNT("messages.give.invalid_amount", "<prefix>&cAmount must be a number!", true),
    MSG_GIVE_SENT("messages.give.sent", "<prefix>&aGave &e<amount>x &agenerator (&e<id>&a) to &e<player>&a.", true),
    MSG_GIVE_RECEIVED("messages.give.received", "<prefix>&aYou received &e<amount>x &agenerator!", true),

    MSG_GENERATOR_PLACED("messages.generator.placed", "<prefix>&aGenerator placed successfully!", true),
    MSG_GENERATOR_DESTROYED("messages.generator.destroyed", "<prefix>&cGenerator destroyed!", true),
    MSG_GENERATOR_PICKED_UP("messages.generator.picked_up", "<prefix>&aGenerator picked up successfully!", true),
    MSG_GENERATOR_GONE("messages.generator.gone", "<prefix>&cThis generator no longer exists.", true),

    MSG_UPGRADE_MAX_LEVEL("messages.upgrade.max_level", "<prefix>&cThis generator is already at max level!", true),
    MSG_UPGRADE_CANNOT_AFFORD("messages.upgrade.cannot_afford", "<prefix>&cYou can't afford this upgrade!", true),
    MSG_UPGRADE_UNAVAILABLE("messages.upgrade.unavailable", "<prefix>&cThis upgrade is not available right now. Please tell an administrator.", true),
    MSG_UPGRADE_SUCCESS("messages.upgrade.success", "<prefix>&aGenerator upgraded to: &e<name>", true);

    private final String path;
    private List<String> texts;
    private String text;
    private int number;
    private boolean bool;
    private boolean colored = false;
    private Class<?> type;

    public static final boolean HAS_ADVENTURE;
    static {
        boolean has = false;
        try {
            Class.forName("net.kyori.adventure.text.Component");
            has = true;
        } catch (ClassNotFoundException ignored) {}
        HAS_ADVENTURE = has;
    }

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
        this.texts = Collections.singletonList(this.text);
        this.number = text.length();
        this.bool = !text.isEmpty();
    }

    public void setValue(List<String> texts) {
        this.type = String[].class;
        this.text = String.join(", ", texts);
        this.texts = Collections.unmodifiableList(texts);
        this.number = texts.size();
        this.bool = !texts.isEmpty();
    }

    public void setValue(int number) {
        this.type = Integer.class;
        this.text = Integer.toString(number);
        this.number = number;
        this.bool = number > 0;
    }

    public void setValue(boolean bool) {
        this.type = Boolean.class;
        this.text = Boolean.toString(bool);
        this.number = bool ? 1 : 0;
        this.bool = bool;
    }

    private String getRawString() {
        if (this == MAIN_PREFIX) return this.text;
        return this.text.replace("<prefix>", MAIN_PREFIX.text);
    }

    public String getLegacyString() {
        String raw = getRawString();
        return this.colored ? ChatColor.translateAlternateColorCodes('&', raw) : raw;
    }

    public List<String> getLegacyStringList() {
        return this.texts.stream()
                .map(s -> s.replace("<prefix>", MAIN_PREFIX.text))
                .map(s -> this.colored ? ChatColor.translateAlternateColorCodes('&', s) : s)
                .collect(Collectors.toList());
    }

    public net.kyori.adventure.text.Component getComponent() {
        if (!HAS_ADVENTURE) return null;
        return AdventureHandler.parse(getRawString());
    }

    public List<net.kyori.adventure.text.Component> getComponentList() {
        if (!HAS_ADVENTURE) return null;
        return this.texts.stream()
                .map(s -> s.replace("<prefix>", MAIN_PREFIX.text))
                .map(s -> this.colored ? AdventureHandler.parse(s) : net.kyori.adventure.text.Component.text(s))
                .collect(Collectors.toList());
    }


    public int getInt() { return this.number; }
    public boolean getBoolean() { return this.bool; }
    public File getFile() { return new File(this.text); }
    public boolean isColored() { return this.colored; }
    public String getPath() { return this.path; }
    public Class<?> getType() { return this.type; }

    @Override
    public String toString() { return this.text; }


    public static void load(File file) {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if (!file.exists()) file.createNewFile();
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            if (loadConfig(yml) > 0) yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File file) {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if (!file.exists()) file.createNewFile();
            saveConfig().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static YamlConfiguration saveConfig() {
        YamlConfiguration config = new YamlConfiguration();

        for (Config val : Config.values()) {
            config.set(val.getPath(), val.rawValue());
        }
        return config;
    }

    private Object rawValue() {
        if (type.equals(String[].class)) return texts;
        if (type.equals(Integer.class)) return number;
        if (type.equals(Boolean.class)) return bool;
        return text;
    }

    private static int loadConfig(ConfigurationSection config) {
        int modify = 0;
        for (Config val : Config.values()) {
            if (!config.contains(val.getPath())) {
                modify++;
                // write the default, so admins can see and edit every message in config.yml
                config.set(val.getPath(), val.rawValue());
            }

            if (val.getType().equals(String.class)) {
                val.setValue(config.getString(val.getPath(), val.text));
            } else if (val.getType().equals(String[].class)) {
                if (config.contains(val.getPath())) {
                    val.setValue(config.getStringList(val.getPath()));
                }
            } else if (val.getType().equals(Integer.class)) {
                val.setValue(config.getInt(val.getPath(), val.getInt()));
            } else if (val.getType().equals(Boolean.class)) {
                val.setValue(config.getBoolean(val.getPath(), val.getBoolean()));
            }
        }
        return modify;
    }

    private static class AdventureHandler {
        public static net.kyori.adventure.text.Component parse(String text) {
            if (text.contains("<") && text.contains(">")) {
                try {
                    return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(text);
                } catch (Throwable t) {
                }
            }
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }
}