package edu.ucsd.cse110.successorator.lib.domain;

import static org.junit.Assert.*;

import java.util.Calendar;

// File generated with copilot
public class DateOffsetTest {
    private long[] times = {0, 10, 60 * 60 * 24 * 365};

    @org.junit.Test
    public void getSeconds() {
        DateOffset dateOffset = new DateOffset(0, Calendar.getInstance());
        assertEquals(0, dateOffset.getSeconds());
        dateOffset = new DateOffset(1, Calendar.getInstance());
        assertEquals(1, dateOffset.getSeconds());
        dateOffset = new DateOffset(Integer.MAX_VALUE, Calendar.getInstance());
        assertEquals(Integer.MAX_VALUE, dateOffset.getSeconds());
    }

    @org.junit.Test
    public void add() {
        DateOffset dateOffset = new DateOffset(0, Calendar.getInstance());
        assertEquals(0, dateOffset.add(0).getSeconds());
        assertEquals(1, dateOffset.add(1).getSeconds());
        assertEquals(Integer.MAX_VALUE, dateOffset.add(Integer.MAX_VALUE).getSeconds());
        dateOffset = new DateOffset(1, Calendar.getInstance());
        assertEquals(1, dateOffset.add(0).getSeconds());
        assertEquals(2, dateOffset.add(1).getSeconds());
        assertEquals(Integer.MAX_VALUE, dateOffset.add(Integer.MAX_VALUE - 1).getSeconds());
        // Technically redundany, but it costs nothing to add
        dateOffset = new DateOffset(Integer.MAX_VALUE, Calendar.getInstance());
        assertEquals(Integer.MAX_VALUE, dateOffset.add(0).getSeconds());
    }

    @org.junit.Test
    public void nowIntegration() {
        // Check that the time is within 1 second of the desired time
        // We check 0, 10 (since 1 might be within our error margin), and 1 year
        // We don't use int max because i32 and 2030s don't mix very well

        for (var time : times) {
            DateOffset dateOffset = new DateOffset(time, Calendar.getInstance());
            long currentTime = System.currentTimeMillis();
            long offsetTime = dateOffset.now().getTime();
            assertTrue(Math.abs(currentTime + time * 1000L - offsetTime) < 1000);
        }
    }

    @org.junit.Test
    public void now() {
        // New test: use mocked time
        // Table based tests are low cost and give us better coverage
        long[] startTimes = {times[0], times[1], times[2], System.currentTimeMillis() / 1000};
        for (var startTime : times) {
            for (var time : times) {
                var mockCalendar = new MockCalendar(0);
                DateOffset dateOffset = new DateOffset(time, mockCalendar);
                assertEquals(time * 1000, dateOffset.now().getTime());
                mockCalendar.advanceTime(startTime * 1000);
                assertEquals(startTime * 1000 + time * 1000, dateOffset.now().getTime());
            }
        }

    }
}