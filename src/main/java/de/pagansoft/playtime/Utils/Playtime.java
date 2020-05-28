package de.pagansoft.playtime.Utils;

import java.time.Duration;
import java.util.Objects;

public class Playtime {

    public Playtime(long totalSeconds)  {
        this(Duration.ofSeconds(totalSeconds));
    }

    public Playtime(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            this.duration = Duration.ofNanos(0);
        }
        else {
            this.duration = duration;
        }
    }

    public boolean isZeroOrBelow() {
        return duration.isZero() || duration.isNegative();
    }

    private final Duration duration;

    public long getHours() {
        return this.duration.toHours();
    }

    public long getMinutes() {
        return this.duration.minusMinutes(getHours() * 60).toMinutes();
    }

    public long getSeconds() {
        return this.duration.minusSeconds(getHours() * 3600 + getMinutes() * 60).getSeconds();
    }

    public long getTotalSeconds() {
        return this.duration.getSeconds();
    }

    public Playtime plus(Playtime other) {
        return new Playtime(this.duration.plus(other.duration));
    }

    public Playtime minus(Playtime other) {
        return new Playtime(this.duration.minus(other.duration));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playtime playtime = (Playtime) o;
        return duration.equals(playtime.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration);
    }
}
