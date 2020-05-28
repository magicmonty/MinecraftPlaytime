package de.pagansoft.playtime.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.UUID;

public class DefaultPlaytime {

    private static final String PREMIUM = "premium";
    private static final String DEFAULT = "default";

    private static JavaPlugin plugin;

    public static void setup(JavaPlugin plugin)
    {
        DefaultPlaytime.plugin = plugin;
    }

    public static Playtime getDefaultPlaytime(Player player) {
        if (player == null) { return getPlaytime(DEFAULT); }

        return PlayerCache.hasPermission(player, PlaytimePermissions.PREMIUM)
                ? getPremiumPlaytime()
                : getDefaultPlaytime();
    }

    public static Playtime getDefaultPlaytime(UUID id) {
        try {
            return PlayerCache.hasPermission(id, PlaytimePermissions.PREMIUM)
                    ? getPremiumPlaytime()
                    : getDefaultPlaytime();
        } catch (Exception e) {
            return getDefaultPlaytime();
        }
    }

    public static Playtime getDefaultPlaytime(CommandSender player) {
        if (player == null) { return getPlaytime(DEFAULT); }

        return PlayerCache.hasPermission(player, PlaytimePermissions.PREMIUM)
                ? getPremiumPlaytime()
                : getDefaultPlaytime();
    }

    public static Playtime getDefaultPlaytime() { return getPlaytime(DEFAULT); }
    public static Playtime getPremiumPlaytime() { return getPlaytime(PREMIUM); }

    private static Playtime getPlaytime(String section) {
        FileConfiguration config = plugin.getConfig();
        long hours = config.getLong(String.format("playtime.%s.hours", section));
        long minutes = config.getLong(String.format("playtime.%s.minutes", section));

        return new Playtime(Duration.ofHours(hours).plusMinutes(minutes));
    }

    public static void reloadConfig() {
        plugin.reloadConfig();
    }
}
