package com.shinoaki.wows.api.utils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Xun
 * @date 2023/4/10 16:45 星期一
 */
public class DateUtils {
    private DateUtils() {

    }

    private static final Clock CLOCK = Clock.systemDefaultZone();

    /**
     * 毫秒
     *
     * @param time
     * @return
     */
    public static long toTimeMillis(LocalDateTime time) {
        return time.atZone(CLOCK.getZone()).toInstant().toEpochMilli();
    }


    public static long toTimeMillis(Timestamp time) {
        return time.toInstant().toEpochMilli();
    }

    public static long toEpochMilli() {
        return Instant.now(CLOCK).toEpochMilli();
    }

    public static long timeMillis(long timeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - timeMillis;
    }

    public static LocalDateTime ofEpochMilli(long ofEpochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ofEpochMilli), CLOCK.getZone());
    }

    public static LocalDate ofEpochSecond(long ofEpochSecond) {
        return LocalDate.ofInstant(Instant.ofEpochSecond(ofEpochSecond), CLOCK.getZone());
    }

    public static LocalDateTime localDateTime() {
        return LocalDateTime.now(CLOCK);
    }

    public static LocalDate localDate() {
        return LocalDate.now(CLOCK);
    }

    public static Clock getClock() {
        return CLOCK;
    }
}
