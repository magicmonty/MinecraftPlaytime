package de.pagansoft.playtime.Commands;

import de.pagansoft.playtime.Utils.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.InvalidParameterException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaytimeCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public PlaytimeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return getPlaytime(sender);
        }

        if (args[0].equals("reload")) {
            return handleReload(sender);
        }

        if (args.length == 1) {
            return getUserPlaytime(sender, args[0], PlayerCache.getPlayerId(args[0]));
        }

        if ((sender instanceof Player) && sender.hasPermission(PlaytimePermissions.ADMIN)) {
            sender.sendMessage(Messages.noPermission());
            return false;
        }

        UUID playerId = PlayerCache.getPlayerId(args[0]);
        if (playerId == null) {
            sender.sendMessage(Messages.playerIsOffline(args[0]));
            return false;
        }

        return handleAdminCommand(sender, command, playerId, args);
    }

    private boolean handleAdminCommand(CommandSender sender, Command command, UUID playerId, String[] args) {
        try {
            if (PlayerCache.hasPermission(playerId, PlaytimePermissions.INFINITE)) {
                sender.sendMessage(Messages.cannotChange());
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe(String.format("Could not get permission for player %s: %s", playerId.toString(), e.getMessage()));
            return false;
        }

        switch (args[1]) {
            case "add": return handleAdd(sender, command, playerId, args);
            case "remove": return handleRemove(sender, command, playerId, args);
            case "reset": return handleReset(sender, command, playerId, args);
            default:
                sender.sendMessage(Messages.unknownCommand());
                return false;
        }
    }

    private boolean handleReset(CommandSender sender, Command command, UUID playerId, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(command.getDescription());
            return false;
        }

        Data.reset(playerId);
        return getUserPlaytime(sender, args[0], playerId);
    }

    private PlayerData getPlayerData(UUID playerId) {
        PlayerData playerData = Data.getPlayerData(playerId);
        return playerData == null
                ? new PlayerData(DefaultPlaytime.getDefaultPlaytime(playerId))
                : playerData;
    }

    private boolean handleRemove(CommandSender sender, Command command, UUID playerId, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(command.getDescription());
            return false;
        }

        try {
            long seconds = getSeconds(args[2]);
            PlayerData playerData = getPlayerData(playerId);
            Playtime configuredPlayTime = playerData
                    .getConfiguredPlaytime()
                    .minus(new Playtime(seconds));

            Playtime timePlayed = playerData.getTimePlayed();

            Data.update(playerId, new PlayerData(configuredPlayTime, timePlayed));

            return getUserPlaytime(sender, args[0], playerId);
        } catch (InvalidParameterException e) {
            sender.sendMessage(e.getMessage());
            return false;
        }
    }

    private static final Pattern timePattern = Pattern.compile("^(?<value>\\d+)(?<type>[hm])");
    private long getSeconds(String timeValue) throws InvalidParameterException {
        Matcher m = timePattern.matcher(timeValue);
        if (!m.find()) {
            throw new InvalidParameterException(Messages.noTimeValue());
        }

        boolean isHours = m.group("type").equalsIgnoreCase("h");
        long value = Long.parseLong(m.group("value"));

        return isHours ? value * 3600 : value * 60;
    }

    private boolean handleAdd(CommandSender sender, Command command, UUID playerId, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(command.getDescription());
            return false;
        }

        try {
            long seconds = getSeconds(args[2]);

            PlayerData playerData = getPlayerData(playerId);
            Playtime configuredPlayTime = playerData
                    .getConfiguredPlaytime()
                    .plus(new Playtime(seconds));

            Playtime timePlayed = playerData.getTimePlayed();

            Data.update(playerId, new PlayerData(configuredPlayTime, timePlayed));

            return getUserPlaytime(sender, args[0], playerId);
        } catch (InvalidParameterException e) {
            sender.sendMessage(e.getMessage());
            return false;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(PlaytimePermissions.ADMIN)) {
            sender.sendMessage(Messages.noPermission());
            return false;
        }

        Data.reloadConfig();
        Messages.reloadConfig();
        PlayerCache.reloadConfig();
        DefaultPlaytime.reloadConfig();
        return true;
    }

    private boolean getUserPlaytime(CommandSender sender, String playerName, UUID playerId) {
        if (!PlayerCache.hasPermission(sender, PlaytimePermissions.OTHERS)) {
            sender.sendMessage(Messages.noPermission());
            return false;
        }

        try {
            if (PlayerCache.hasPermission(playerId, PlaytimePermissions.INFINITE)) {
                sender.sendMessage(Messages.infinitePlaytime(playerName));
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe(String.format("Could not get permission for player %s: %s", playerId.toString(), e.getMessage()));
        }

        PlayerData playerData = Data.getPlayerData(playerId);
        if (playerData == null) {
            playerData = new PlayerData(DefaultPlaytime.getDefaultPlaytime(playerId));
        }

        sender.sendMessage(Messages.timeLeft(playerName, playerData.timeLeft()));
        return true;
    }

    private boolean getPlaytime(CommandSender sender) {
        if (sender.getName().equalsIgnoreCase("CONSOLE") ||  !(sender instanceof Player)) {
            plugin.getLogger().severe("Could not get playtime for console user!");
            return false;
        }

        if (PlayerCache.hasPermission(sender, PlaytimePermissions.INFINITE)) {
            sender.sendMessage(Messages.infinitePlaytime());
            return true;
        }

        PlayerData playerData = Data.getPlayerData(sender);
        if (playerData == null) {
            playerData = new PlayerData(DefaultPlaytime.getDefaultPlaytime(sender));
            Data.update(((Player)sender).getUniqueId(), playerData);
        }

        sender.sendMessage(Messages.timeLeft(playerData.timeLeft()));
        if (playerData.timeLeft().isZeroOrBelow()) {
            ((Player) sender).kickPlayer(Messages.kickMessage());
        }

        return true;
    }
}
