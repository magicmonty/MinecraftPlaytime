package de.pagansoft.playtime.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Data {
    static FileConfiguration fileConfig;
    static File file;
    private static JavaPlugin plugin;
    static final String fileName = "data.yml";

    public static void setup(JavaPlugin plugin) {
        Data.plugin = plugin;
        setupConfig();
    }

    private static void setupConfig() {
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Could not create data.yml.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
            }
        }

        reloadConfig();
    }

    public static void reloadConfig() {
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static PlayerData getPlayerData(CommandSender sender) {
        if (!(sender instanceof Player)) { return null; }
        return getPlayerData(((Player)sender).getUniqueId());
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public static PlayerData getPlayerData(UUID id) {
        if (!fileConfig.contains(id.toString())) { return null; }

        Playtime timePlayed =
                shouldReset(id)
                        ? new Playtime(0)
                        : new Playtime(fileConfig.getLong(String.format("%s.timePlayed", id.toString()), 0));

        long configuredPlaytime =
                fileConfig.getLong(
                        String.format("%s.playTime", id.toString()),
                        DefaultPlaytime.getDefaultPlaytime(id).getTotalSeconds());

        Instant lastCheck =
                Instant.ofEpochSecond(
                        fileConfig.getLong(
                                String.format("%s.lastCheck", id.toString()),
                                Instant.now().getEpochSecond()));

        return new PlayerData(
                new Playtime(configuredPlaytime),
                timePlayed,
                lastCheck);
    }

    private static boolean shouldReset(UUID playerId) {
        Instant lastCheck = Instant.ofEpochSecond(fileConfig.getLong(String.format("%s.lastCheck", playerId.toString()), Instant.now().getEpochSecond()));
        return PlayerData.shouldReset(Date.from(lastCheck));
    }

    public static void reset(Player player) {
        reset(player.getUniqueId());
    }

    public static void reset(UUID playerId) {
        if (fileConfig.contains(playerId.toString())) {
            fileConfig.set(playerId.toString(), null);
        }

        saveConfig();
    }

    public static void update(UUID playerId, PlayerData data) {
        String uuid = playerId.toString();
        fileConfig.set(String.format("%s.timePlayed", uuid), data.getTimePlayed().getTotalSeconds());
        fileConfig.set(String.format("%s.playTime", uuid), data.getConfiguredPlaytime().getTotalSeconds());
        fileConfig.set(String.format("%s.lastCheck", uuid), Instant.now().getEpochSecond());
        saveConfig();
    }

    public static void saveConfig() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(String.format("Could not save data.yml: %s", e.getMessage()));
        }
    }
}
