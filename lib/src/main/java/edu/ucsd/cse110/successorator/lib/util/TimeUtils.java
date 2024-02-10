package edu.ucsd.cse110.successorator.lib.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {
    // Somewhat arbitrary value for the start time; our code will never run around 0
    // So we shouldn't worry about edge cases relating to it
    public final static long DAY_LENGTH = 1000L * 60 * 60 * 24;
    public final static long HOUR_LENGTH = 1000L * 60 * 60;
    public final static long MINUTE_LENGTH = 1000L * 60;
    public final static TimeZone GMT = TimeZone.getTimeZone("GMT");
    public final static long START_TIME = getStartTime();

    /**
     * Some weirdness here: I think it should show goals created in the future, but I'm not sure.
     */
    public static boolean shouldShowGoal(Calendar goalLocalized, Calendar nowLocalized) {
        if (nowLocalized.get(Calendar.HOUR_OF_DAY) < 2) {
            // Accept every goal created after 2am yesterday
            var yesterday2am = (Calendar) nowLocalized.clone();
            yesterday2am.add(Calendar.DAY_OF_YEAR, -1);
            yesterday2am.set(Calendar.HOUR_OF_DAY, 2);
            return goalLocalized.after(yesterday2am);
        }
        else {
            // Accept every goal created after 2am today
            var today2am = (Calendar) nowLocalized.clone();
            today2am.set(Calendar.HOUR_OF_DAY, 2);
            return goalLocalized.after(today2am);
        }
    }

    public static Calendar localize(Long currentTime, Calendar dateConverter) {
        if (currentTime == null) return null;
        Date date = new Date(currentTime);
        Calendar localized = (Calendar) dateConverter.clone();
        localized.setTime(date);
        return localized;
    }

    public static long getStartTime() {
        // Time chosen arbitrarily
        int year = 2024;
        int month = Calendar.FEBRUARY;
        int day  = 7;
        int hour = 16;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.setTimeZone(GMT);

        return calendar.getTimeInMillis();
    }
}
