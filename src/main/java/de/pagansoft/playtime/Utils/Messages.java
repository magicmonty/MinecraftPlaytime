package de.pagansoft.playtime.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Messages {
    static FileConfiguration fileConfig;
    static File file;
    static final String fileName = "messages.yml";
    static JavaPlugin plugin;

    public static void setup(JavaPlugin plugin) {
        Messages.plugin = plugin;
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

                InputStream defaultResource = plugin.getResource(fileName);
                if (defaultResource != null) {
                    plugin.getLogger().info("Setting defaults...");
                    Reader defaultConfigStream = new InputStreamReader(defaultResource, StandardCharsets.UTF_8);
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);

                    try {
                        defaultConfig.save(file);
                    } catch (IOException e) {
                        plugin.getLogger().severe(String.format("Error saving default configuration: %s", e.getMessage()));
                    }
                } else {
                    plugin.getLogger().severe("messages.yml is not included as resource");
                }
            } catch (IOException ignored) { }
        }

        reloadConfig();
    }

    public static void reloadConfig() {
        InputStream defaultResource = plugin.getResource(fileName);
        if (defaultResource != null) {
            plugin.getLogger().info("Setting defaults...");
            Reader defaultConfigStream = new InputStreamReader(defaultResource, StandardCharsets.UTF_8);
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);

            try {
                fileConfig = YamlConfiguration.loadConfiguration(file);
                fileConfig.setDefaults(defaultConfig);
                fileConfig.options().copyDefaults(true);
                fileConfig.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe(String.format("Error saving default configuration: %s", e.getMessage()));
            }
        } else {
            plugin.getLogger().severe("messages.yml is not included as resource");
        }

        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    private static final String playerPlaceholder = "{Player}";
    private static final String hoursPlaceholder = "{hours}";
    private static final String minutesPlaceholder = "{minutes}";
    private static final String secondsPlaceholder = "{seconds}";
    private static final String leftTime = "leftTime";
    private static final String leftTimeOther = "leftTimeOther";
    private static final String leftTimeUnlimited = "leftTimeUnlimited";
    private static final String leftTimeUnlimitedOther = "leftTimeUnlimitedOther";
    private static final String noPermission = "noPermission";
    private static final String playerIsOffline = "playerIsOffline";
    private static final String unknownCommand = "unknownCommand";
    private static final String cannotChange = "cannotChange";
    private static final String noTimeValue = "noTimeValue";
    private static final String kickMessage = "kickMessage";
    private static final String playtimeEndsMinutes = "playtimeEndsMinutes";
    private static final String playtimeEndsSeconds = "playtimeEndsSeconds";
    private static final String playtimeEndsTitle = "playtimeEndsTitle";
    private static final String playtimeLeftTitle = "playtimeLeftTitle";

    public static String kickMessage() { return getString(kickMessage); }
    public static String playtimeEndsTitle() { return getString(playtimeEndsTitle); }
    public static String playtimeLeftTitle() { return getString(playtimeLeftTitle); }
    public static String playtimeEndsSeconds(Playtime time) {
        return getString(playtimeEndsSeconds).replace(secondsPlaceholder, Long.toString(time.getTotalSeconds())); }
    public static String playtimeEndsMinutes(Playtime time) {
        return getString(playtimeEndsMinutes).replace(secondsPlaceholder, Long.toString(time.getTotalSeconds() / 60)); }

    public static String noTimeValue() { return getString(noTimeValue); }
    public static String cannotChange() { return getString(cannotChange); }
    public static String unknownCommand() { return getString(unknownCommand); }
    public static String noPermission() { return getString(noPermission); }

    public static String playerIsOffline(String playerName) {
        return getString(playerIsOffline).replace(playerPlaceholder, playerName);
    }

    public static String timeLeft(Playtime time) {
        return replaceTime(getString(leftTime), time);
    }

    public static String timeLeft(String playerName, Playtime time) {
        return replaceTime(getString(leftTimeOther), time)
                .replace(playerPlaceholder, playerName);
    }

    private static String replaceTime(String value, Playtime time) {
        return value
                .replace(hoursPlaceholder, String.format("%d", time.getHours()))
                .replace(minutesPlaceholder, String.format("%d", time.getMinutes()))
                .replace(secondsPlaceholder, String.format("%d", time.getSeconds()));
    }

    public static String infinitePlaytime() { return getString(leftTimeUnlimited); }

    public static String infinitePlaytime(String playerName) {
        return getString(leftTimeUnlimitedOther).replace(playerPlaceholder, playerName);
    }

    private static String getString(String name) {
        String result = fileConfig.getString(name);
        if (result == null || result.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', result);
    }
}
