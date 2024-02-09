package edu.ucsd.cse110.successorator.lib.domain;

import static org.junit.Assert.*;

// File generated with copilot
public class DateOffsetTest {

    @org.junit.Test
    public void getSeconds() {
        DateOffset dateOffset = new DateOffset(0);
        assertEquals(0, dateOffset.getSeconds());
        dateOffset = new DateOffset(1);
        assertEquals(1, dateOffset.getSeconds());
        dateOffset = new DateOffset(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, dateOffset.getSeconds());
    }

    @org.junit.Test
    public void add() {
        DateOffset dateOffset = new DateOffset(0);
        assertEquals(0, dateOffset.add(0).getSeconds());
        assertEquals(1, dateOffset.add(1).getSeconds());
        assertEquals(Integer.MAX_VALUE, dateOffset.add(Integer.MAX_VALUE).getSeconds());
        dateOffset = new DateOffset(1);
        assertEquals(1, dateOffset.add(0).getSeconds());
        assertEquals(2, dateOffset.add(1).getSeconds());
        assertEquals(Integer.MAX_VALUE, dateOffset.add(Integer.MAX_VALUE - 1).getSeconds());
        // Technically redundany, but it costs nothing to add
        dateOffset = new DateOffset(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, dateOffset.add(0).getSeconds());
    }

    @org.junit.Test
    public void now() {
        // Check that the time is within 1 second of the desired time
        // We check 0, 10 (since 1 might be within our error margin), and 1 year
        // We don't use int max because i32 and 2030s don't mix very well
        int[] times = {0, 10, 60 * 60 * 24 * 365};
        for (int time : times) {
            DateOffset dateOffset = new DateOffset(time);
            long currentTime = System.currentTimeMillis();
            long offsetTime = dateOffset.now().getTime();
            assertTrue(Math.abs(currentTime + time * 1000L - offsetTime) < 1000);
        }
    }
}