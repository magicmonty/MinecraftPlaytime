package de.pagansoft.playtime;

import de.pagansoft.playtime.Commands.PlaytimeCommand;
import de.pagansoft.playtime.EventHandlers.PlayerListener;
import de.pagansoft.playtime.Utils.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@SuppressWarnings("unused")
public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        PlayerCache.setup(this);
        DefaultPlaytime.setup(this);
        Messages.setup(this);
        Data.setup(this);

        Objects.requireNonNull(this.getCommand("playtime")).setExecutor(new PlaytimeCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        Scheduler.start(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Data.saveConfig();
        PlayerCache.saveConfig();
    }
}
