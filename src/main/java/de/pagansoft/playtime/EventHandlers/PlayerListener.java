package de.pagansoft.playtime.EventHandlers;

import de.pagansoft.playtime.Utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        try {
            if (PlayerCache.hasPermission(playerId, PlaytimePermissions.INFINITE)) {
                Helpers.sendTitle(player, Messages.playtimeLeftTitle(), Messages.infinitePlaytime(), -1, -1, -1);
                return;
            }
        }  catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            return;
        }

        PlayerData data = getPlayerData(playerId);
        Data.update(playerId, data.updateLoginTime());

        if (data.timeLeft().isZeroOrBelow()) {
            player.kickPlayer(Messages.kickMessage());
        } else {
            Helpers.sendTitle(player, Messages.playtimeLeftTitle(), Messages.timeLeft(data.timeLeft()), -1, -1, -1);
        }
    }

    private PlayerData getPlayerData(UUID player) {
        PlayerData data = Data.getPlayerData(player);
        if (data == null) {
            data = new PlayerData(DefaultPlaytime.getDefaultPlaytime(player));
            Data.update(player, data);
        }
        return data;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = getPlayerData(event.getPlayer().getUniqueId());
        Data.update(player.getUniqueId(), data.updateTimePlayed());
    }
}
