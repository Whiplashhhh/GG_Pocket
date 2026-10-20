package fr.gravelinesgrimp.pocket.platform.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/** Calendar rules of the club (days, ages, months) follow the time in Gravelines. */
public final class ClubTime {

    public static final ZoneId ZONE = ZoneId.of("Europe/Paris");

    private ClubTime() {}

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock.withZone(ZONE));
    }
}
