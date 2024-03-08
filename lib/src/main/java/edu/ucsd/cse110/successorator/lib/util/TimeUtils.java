package edu.ucsd.cse110.successorator.lib.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;

public class TimeUtils {
    // Somewhat arbitrary value for the start time; our code will never run around 0
    // So we shouldn't worry about edge cases relating to it
    public final static long DAY_LENGTH = 1000L * 60 * 60 * 24;
    public final static long HOUR_LENGTH = 1000L * 60 * 60;
    public final static long MINUTE_LENGTH = 1000L * 60;
    public final static TimeZone GMT = TimeZone.getTimeZone("GMT");
    public final static long START_TIME = getStartTime();

    public static boolean shouldShowCompleteGoal(Calendar completionTime, Calendar nowLocalized, Calendar startTime) {
        var today2am = (Calendar) nowLocalized.clone();
        today2am.set(Calendar.HOUR_OF_DAY, 2);
        var yesterday2am = (Calendar) today2am.clone();
        yesterday2am.add(Calendar.DAY_OF_YEAR, -1);
        var tomorrow2am = (Calendar) today2am.clone();
        tomorrow2am.add(Calendar.DAY_OF_YEAR, 1);
        if (nowLocalized.get(Calendar.HOUR_OF_DAY) < 2) {
            // Accept every goal created after 2am yesterday, but not after 2am today
            return completionTime.after(yesterday2am) && startTime.before(today2am);
        } else {
            // Accept every goal created after 2am today, but before 2am tomorrow
            return completionTime.after(today2am) && startTime.before(tomorrow2am);
        }
    }

    public static boolean shouldShowIncompleteGoal(Calendar startTime, Calendar nowLocalized) {
        var today2am = (Calendar) nowLocalized.clone();
        today2am.set(Calendar.HOUR_OF_DAY, 2);
        var tomorrow2am = (Calendar) today2am.clone();
        tomorrow2am.add(Calendar.DAY_OF_YEAR, 1);
        if (nowLocalized.get(Calendar.HOUR_OF_DAY) < 2) {
            return startTime.before(today2am);
        } else {
            return startTime.before(tomorrow2am);
        }
    }

    // TODO: implement this and test when you write task 4.1
    public static boolean shouldShowIncompleteGoalTomorrow(Calendar nowLocalized, Calendar startTime) {
        var today2am = (Calendar) nowLocalized.clone();
        today2am.set(Calendar.HOUR_OF_DAY, 2);
        var yesterday2am = (Calendar) today2am.clone();
        yesterday2am.add(Calendar.DAY_OF_YEAR, -1);
        var tomorrow2am = (Calendar) today2am.clone();
        tomorrow2am.add(Calendar.DAY_OF_YEAR, 1);
        if (nowLocalized.get(Calendar.HOUR_OF_DAY) < 2) {
            // Accept every goal created after 2am yesterday, but not after 2am today
            return startTime.after(yesterday2am) && startTime.before(today2am);
        } else {
            // Accept every goal created after 2am today, but before 2am tomorrow
            return startTime.after(today2am) && startTime.before(tomorrow2am);
        }
    }

    // Assumed start is after 2am normalization
    public static Calendar nthRecurrence(Calendar start, RecurrenceType recurrenceType, int n) {
        Calendar next = (Calendar) start.clone();
        switch (recurrenceType) {
            case DAILY:
                next.add(Calendar.DAY_OF_YEAR, n);
                break;
            case WEEKLY:
                next.add(Calendar.WEEK_OF_YEAR, n);
                // Repeat on same weekday
                break;
            case MONTHLY:
                // Repeat on nth weekday of month
                // i.e 3rd tuesday of month
                // For these next 2 we try to avoid coercing smaller fields
                var weekDay = next.get(Calendar.DAY_OF_WEEK);
                var weekNum = next.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                next.set(Calendar.DAY_OF_MONTH, 1);
                next.add(Calendar.MONTH, n);
                while (next.get(Calendar.DAY_OF_WEEK) != weekDay) {
                    next.add(Calendar.DAY_OF_MONTH, 1);
                }
                next.add(Calendar.WEEK_OF_MONTH, weekNum - 1);
                break;
            case YEARLY:
                // Repeat on same day and month
                var month = next.get(Calendar.MONTH);
                var day = next.get(Calendar.DAY_OF_MONTH);
                next.set(Calendar.DAY_OF_MONTH, 1);
                next.add(Calendar.YEAR, n);
                next.set(Calendar.MONTH, month);
                next.add(Calendar.DAY_OF_MONTH, day - 1);
                break;
        }
        // Sometimes nonzero ms count causes boundary cases
        return TimeUtils.twoAMNormalized(next);
    }

    public static int nextGoalRecurrenceIndex(Calendar nowLocalized, Calendar startTime, RecurrenceType recurrenceType) {
        // Binary search for recurrence k where k-1 is before or equal to today, and k is after today
        int low = 0;
        int high = 1000;
        while (low < high) {
            int mid = (low + high) / 2;
            Calendar midRecurrence = nthRecurrence(startTime, recurrenceType, mid);
        if (midRecurrence.before(nowLocalized) || (midRecurrence.get(Calendar.DAY_OF_YEAR) == nowLocalized.get(Calendar.DAY_OF_YEAR) && midRecurrence.get(Calendar.YEAR) == nowLocalized.get(Calendar.YEAR))) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    // These two are trivial wrappers over nthRecurrence and nextGoalRecurrenceIndex
    // So they won't get dedicated tests
    // Expects nowLocalized to be 2am localized
    public static Calendar nextGoalRecurrence(Calendar nowLocalized, Calendar startTime, RecurrenceType recurrenceType) {
        // Binary search for recurrence k where k-1 is before or equal to today, and k is after today
        return nthRecurrence(startTime, recurrenceType, nextGoalRecurrenceIndex(nowLocalized, startTime, recurrenceType));
    }

    // Expects nowLocalized to be 2am localized
    public static Calendar previousGoalRecurrence(Calendar nowLocalized, Calendar startTime, RecurrenceType recurrenceType) {
        // Binary search for recurrence k where k-1 is before or equal to today, and k is after today
        return nthRecurrence(startTime, recurrenceType, nextGoalRecurrenceIndex(nowLocalized, startTime, recurrenceType) - 1);
    }


    // Account for 2am wrapping and set time to 12pm
    public static Calendar twoAMNormalized(Calendar dateConverter) {
        Calendar normalized = (Calendar) dateConverter.clone();
        if (normalized.get(Calendar.HOUR_OF_DAY) < 2) {
            normalized.add(Calendar.DAY_OF_YEAR, -1);
        }
        normalized.set(Calendar.HOUR_OF_DAY, 12);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized;
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
        int day = 7;
        int hour = 16;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.setTimeZone(GMT);

        return calendar.getTimeInMillis();
    }
}
