package de.pagansoft.playtime.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class PlayerData {
    public PlayerData(Playtime configured) {
        this(configured, new Playtime(0));
    }

    public PlayerData(Playtime configured, Playtime timePlayed) {
        this(configured, timePlayed, Instant.now());
    }

    public PlayerData(Playtime configured, Playtime timePlayed, Instant lastCheck) {
        this.configuredPlaytime = configured;
        this.timePlayed = timePlayed;
        this.lastCheck = lastCheck;
    }

    private final Playtime configuredPlaytime;
    private final Playtime timePlayed;
    private final Instant lastCheck;

    public Playtime getConfiguredPlaytime() { return this.configuredPlaytime; }
    public Playtime getTimePlayed() { return this.timePlayed; }
    public Instant getLoginTime() { return this.lastCheck; }

    public Playtime timeLeft() {

        return configuredPlaytime.minus(timePlayed).minus(new Playtime(Duration.between(lastCheck, Instant.now())));
    }

    public PlayerData reset() {

        return new PlayerData(this.configuredPlaytime);
    }

    public PlayerData updateLoginTime() {
        return new PlayerData(configuredPlaytime, timePlayed, Instant.now());
    }

    public PlayerData updateTimePlayed() {
        return shouldReset(Date.from(this.lastCheck))
                ? reset()
                : new PlayerData(
                        this.configuredPlaytime,
                        this.timePlayed.plus(new Playtime(Duration.between(this.lastCheck, Instant.now()))),
                        Instant.now());
    }

    public static boolean shouldReset(Date lastCheckDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastCheckDate);
        int lastCheckDay = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(Date.from(Instant.now()));
        int dayToday = cal.get(Calendar.DAY_OF_MONTH);
        return dayToday > lastCheckDay;
    }

}
