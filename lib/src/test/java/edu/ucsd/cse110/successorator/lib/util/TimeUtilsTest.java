package edu.ucsd.cse110.successorator.lib.util;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Calendar;

public class TimeUtilsTest {

    // Test generated with GPT4, but checked for correctness by me
    @Test
    public void shouldShowGoal() {
        // Test 1: Goal Created After 2AM Today and Current Time is After 2AM
        var now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 4);
        var goal = (Calendar) now.clone();
        goal.set(Calendar.HOUR_OF_DAY, 3);
        assertTrue(TimeUtils.shouldShowGoal(goal, now));

        // Test 2: Goal Created Before 2AM Today and Current Time is After 2AM
        goal.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(TimeUtils.shouldShowGoal(goal, now));

        // Adjust now to represent a time before 2AM - For tests 3, 4
        now.set(Calendar.HOUR_OF_DAY, 1);

        // Test 3: Goal Created After 2AM Yesterday and Current Time is Before 2AM
        goal.add(Calendar.DAY_OF_YEAR, -1); // Setting goal to yesterday
        goal.set(Calendar.HOUR_OF_DAY, 3);
        assertTrue(TimeUtils.shouldShowGoal(goal, now));

        // Test 4: Goal Created Before 2AM Yesterday and Current Time is Before 2AM
        goal.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(TimeUtils.shouldShowGoal(goal, now));
    }

    @Test
    public void localize() {
        Calendar dateConverter = Calendar.getInstance(TimeUtils.GMT);
        Long currentTimeMillis = System.currentTimeMillis();
        Calendar localized = TimeUtils.localize(currentTimeMillis, dateConverter);
        assertNotNull(localized);
        assertEquals(localized.getTimeInMillis(), currentTimeMillis.longValue());
        assertEquals(localized.getTimeZone(), TimeUtils.GMT);

        Long startTime = TimeUtils.START_TIME;
        localized = TimeUtils.localize(startTime, dateConverter);
        assertNotNull(localized);
        assertEquals(localized.getTimeInMillis(), startTime.longValue());
        assertEquals(localized.getTimeZone(), TimeUtils.GMT);
        assertEquals(localized.get(Calendar.YEAR), 2024);
        assertEquals(localized.get(Calendar.MONTH), Calendar.FEBRUARY);
        assertEquals(localized.get(Calendar.DAY_OF_MONTH), 7);
        assertEquals(localized.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(localized.get(Calendar.MINUTE), 0);
        assertEquals(localized.get(Calendar.SECOND), 0);
    }
}