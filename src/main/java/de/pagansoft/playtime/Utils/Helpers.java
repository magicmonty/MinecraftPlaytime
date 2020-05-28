package de.pagansoft.playtime.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Helpers {
    public static boolean isPluginAvailable(String pluginName) {
        PluginManager pm = Bukkit.getPluginManager();
        Plugin plugin = pm.getPlugin(pluginName.trim());
        return ((plugin != null) && (plugin.isEnabled()));
    }

    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (isPluginAvailable("TitleAPI")) {
            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        } else {
            player.sendMessage(subTitle);
        }
    }
}
