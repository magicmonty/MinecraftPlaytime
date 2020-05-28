package de.pagansoft.playtime.Utils;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.mojang.Mojang;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerCache {
    private static Mojang api;
    private//  static Permission perms;
    static FileConfiguration fileConfig;
    static File file;
    static final String fileName = "players.yml";
    static Permission perms;
    private static JavaPlugin plugin;

    public static void setup(JavaPlugin plugin) {
        PlayerCache.plugin = plugin;
        PlayerCache.api = new Mojang().connect();
        setupConfig();
        setupPermissions();
    }

    private static void setupConfig() {
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {}
        }

        reloadConfig();
    }

    private static void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
        }
    }

    public static UUID getPlayerId(String name) {
        if (name.equalsIgnoreCase("CONSOLE")) { return null; }

        if (fileConfig.contains(name)) {
            try {
                String playerIdString = fileConfig.getString(name);
                if (playerIdString != null && !playerIdString.isEmpty()) {
                    return UUID.fromString(playerIdString);
                }
            } catch (Exception ignored) {
                fileConfig.set(name, null);
            }
        }

        if (api.getStatus(Mojang.ServiceType.API_MOJANG_COM) != Mojang.ServiceStatus.GREEN) { return null; }

        String playerIdAsString = api.getUUIDOfUsername(name);
        if (playerIdAsString == null || playerIdAsString.isEmpty()) { return null; }

        UUID playerId;
        try {
            playerIdAsString = playerIdAsString.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5");

            playerId = UUID.fromString(playerIdAsString);
        } catch (Exception e) {
            plugin.getLogger().severe(String.format("Could not convert UUID %s into uuid: %s", playerIdAsString, e.getMessage()));
            return null;
        }

        if (!fileConfig.contains(name)) {
            fileConfig.set(name, playerId.toString());
            saveConfig();
        }

        return playerId;
    }

    public static boolean hasPermission(UUID playerId, String permission) throws ExecutionException, InterruptedException {
        if (playerId == null || permission == null || permission.isEmpty()) {
            return false;
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return perms != null
                    ? CompletableFuture.supplyAsync(() -> perms.playerHas(player, permission)).get()
                    : player.hasPermission(permission);
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        if (offlinePlayer instanceof Permissible) {
            try {
                return ((Permissible)offlinePlayer).hasPermission(permission);
            } catch (Exception ignored) { }
        }

        Player offline = offlinePlayer.getPlayer();
        if (offline == null && perms != null) {
            return CompletableFuture.supplyAsync(() -> perms.playerHas("world", offlinePlayer, permission)).get();
        }

        return offline != null && offline.hasPermission(permission);
    }

    public static boolean hasPermission(Player player, String permission) {
        if (player == null || permission == null || permission.isEmpty()) {
            return false;
        }

        return perms != null
                ? perms.playerHas(player, permission)
                : player.hasPermission(permission);
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender == null || permission == null || permission.isEmpty()) {
            return false;
        }

        if (!(sender instanceof Player)) { return true; }

        return hasPermission((Player) sender, permission);
    }

    public static void reloadConfig() {
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static void saveConfig() {
        plugin.getLogger().info("Saving player cache");
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(String.format("Could not save player cache: %s", e.getMessage()));
        }
    }
}
