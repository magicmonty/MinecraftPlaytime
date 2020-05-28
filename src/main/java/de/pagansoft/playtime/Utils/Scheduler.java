package de.pagansoft.playtime.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Scheduler {

    public static void start(JavaPlugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                PlayerData data = Data.getPlayerData(player);

                if (data == null && !PlayerCache.hasPermission(player, PlaytimePermissions.INFINITE)) {
                    data = new PlayerData(DefaultPlaytime.getDefaultPlaytime(player));
                    Data.update(player.getUniqueId(), data);
                }

                if (data == null) { continue; }

                if (PlayerCache.hasPermission(player, PlaytimePermissions.INFINITE)) {
                    Data.reset(player);
                    return;
                }

                countdown(player, FixPlaytimeBasedOnPermissions(player, data));
            }

        }, 0L, 20L);
    }

    private static PlayerData FixPlaytimeBasedOnPermissions(Player player, PlayerData data) {
        Playtime configuredPlaytime = data.getConfiguredPlaytime();
        Playtime defaultPlaytimeForPlayer = DefaultPlaytime.getDefaultPlaytime(player);

        if (configuredPlaytime.equals(defaultPlaytimeForPlayer)) {
            return data;
        }

        Playtime defaultPlaytime = DefaultPlaytime.getDefaultPlaytime();
        Playtime premiumPlaytime = DefaultPlaytime.getPremiumPlaytime();
        if (configuredPlaytime.equals(defaultPlaytime) && PlayerCache.hasPermission(player, PlaytimePermissions.PREMIUM)) {
            return updateWithNewConfiguredTime(player, data, premiumPlaytime);
        }

        if (configuredPlaytime.equals(premiumPlaytime) && !PlayerCache.hasPermission(player, PlaytimePermissions.PREMIUM)) {
            return updateWithNewConfiguredTime(player, data, defaultPlaytime);
        }

        return data;
    }

    private static PlayerData updateWithNewConfiguredTime(Player player, PlayerData data, Playtime newTime) {
        PlayerData newData = new PlayerData(newTime, data.getTimePlayed(), data.getLoginTime());
        Data.update(player.getUniqueId(), newData);
        return newData;
    }

    private  static void countdown(Player player, PlayerData data) {
        Playtime time = data.timeLeft();
        long secondsLeft = time.getTotalSeconds();

        if (secondsLeft == 300 || secondsLeft == 600) {
            Helpers.sendTitle(player, Messages.playtimeEndsTitle(), Messages.playtimeEndsMinutes(time), 10, 70, 20);
        }

        if (secondsLeft == 60 || secondsLeft == 30 || secondsLeft <= 15) {
            Helpers.sendTitle(player, Messages.playtimeEndsTitle(), Messages.playtimeEndsSeconds(time), secondsLeft > 15 ? 10 : 0, secondsLeft > 15 ? 70 : 100, -1);
        }

        if (time.isZeroOrBelow()) {
            player.kickPlayer(Messages.kickMessage());
        }

    }
}
