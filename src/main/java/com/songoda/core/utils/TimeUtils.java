package com.songoda.core.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    /**
     * @param time In milliseconds
     */
    public static String makeReadable(Long time) {
        if (time == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        long days = TimeUnit.MILLISECONDS.toDays(time);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(time));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        if (days != 0L) {
            sb.append(" ")
                    .append(days)
                    .append("d");
        }

        if (hours != 0L) {
            sb.append(" ")
                    .append(hours)
                    .append("h");
        }

        if (minutes != 0L) {
            sb.append(" ")
                    .append(minutes)
                    .append("m");
        }

        if (seconds != 0L) {
            sb.append(" ")
                    .append(seconds)
                    .append("s");
        }

        return sb.toString().trim();
    }

    private static long convert(long value, char unit) {
        switch (unit) {
            case 'd':
                return value * 1000 * 60 * 60 * 24;
            case 'h':
                return value * 1000 * 60 * 60;
            case 'm':
                return value * 1000 * 60;
            case 's':
                return value * 1000;
            default:
                return 0;
        }
    }
}
