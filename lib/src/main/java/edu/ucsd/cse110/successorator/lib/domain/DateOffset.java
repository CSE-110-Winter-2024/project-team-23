package edu.ucsd.cse110.successorator.lib.domain;

import java.util.Calendar;
import java.util.Date;

/**
 * Stores the offset from the current date, as specified by the user pressing a button
 */
public class DateOffset {
    private final long seconds;
    private final Calendar calendar;

    public DateOffset(long offset, Calendar calendar) {
        this.seconds = offset;
        this.calendar = calendar;
    }

    public long getSeconds() {
        return seconds;
    }

    public DateOffset add(long seconds) {
        return new DateOffset(this.seconds + seconds, this.calendar);
    }

    public DateOffset addDay() {
        return add(24 * 60 * 60);
    }

    public Date now() {
        // Used below link to determine idiomatic way to get current time
        // https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android
        // Used below link to add to time
        // https://stackoverflow.com/questions/3581258/adding-n-hours-to-a-date-in-java
        var millis = calendar.getTimeInMillis();
        return new Date(millis + seconds * 1000);
    }
}
