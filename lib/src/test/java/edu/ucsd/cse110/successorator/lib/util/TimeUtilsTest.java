package edu.ucsd.cse110.successorator.lib.util;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Calendar;

public class TimeUtilsTest {

    // Test generated with GPT4, but checked for correctness by me
    @Test
    public void shouldShowCompletedGoal() {
        // Test 1: Goal Created After 2AM Today and Current Time is After 2AM
        var now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 4);
        var goal = (Calendar) now.clone();
        goal.set(Calendar.HOUR_OF_DAY, 3);
        var yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        var today = (Calendar) now.clone();
        var tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        assertTrue(TimeUtils.shouldShowCompleteGoal(goal, now, yesterday));
        assertTrue(TimeUtils.shouldShowCompleteGoal(goal, now, today));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, tomorrow));

        // Test 2: Goal completed Before 2AM Today and Current Time is After 2AM
        goal.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, yesterday));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, today));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, tomorrow));

        // Adjust now to represent a time before 2AM - For tests 3, 4
        now.set(Calendar.HOUR_OF_DAY, 1);

        // Test 3: Goal completed After 2AM Yesterday and Current Time is Before 2AM
        goal.add(Calendar.DAY_OF_YEAR, -1); // Setting goal to yesterday
        goal.set(Calendar.HOUR_OF_DAY, 3);
        assertTrue(TimeUtils.shouldShowCompleteGoal(goal, now, yesterday));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, today));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, tomorrow));

        // Test 4: Goal completed Before 2AM Yesterday and Current Time is Before 2AM
        goal.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, yesterday));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, today));
        assertFalse(TimeUtils.shouldShowCompleteGoal(goal, now, tomorrow));
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

    @Test
    public void shouldShowIncompleteGoal() {
        // Test 1: Goal Created After 2AM Today and Current Time is After 2AM
        var now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 4);
        var today = (Calendar) now.clone();
        var tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        var yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        assertTrue(TimeUtils.shouldShowIncompleteGoal(yesterday, now));
        assertTrue(TimeUtils.shouldShowIncompleteGoal(today, now));
        assertFalse(TimeUtils.shouldShowIncompleteGoal(tomorrow, now));

        // Test 2: Goal Created Before 2AM Today and Current Time is After 2AM
        yesterday.set(Calendar.HOUR_OF_DAY, 1);
        today.set(Calendar.HOUR_OF_DAY, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 1);
        assertTrue(TimeUtils.shouldShowIncompleteGoal(yesterday, now));
        assertTrue(TimeUtils.shouldShowIncompleteGoal(today, now));
        assertTrue(TimeUtils.shouldShowIncompleteGoal(tomorrow, now));

        // Adjust now to represent a time before 2AM - For tests 3, 4
        now.set(Calendar.HOUR_OF_DAY, 1);

        // Test 3: Goal Created before 2AM and Current Time is Before 2AM
        assertTrue(TimeUtils.shouldShowIncompleteGoal(yesterday, now));
        assertTrue(TimeUtils.shouldShowIncompleteGoal(today, now));
        assertFalse(TimeUtils.shouldShowIncompleteGoal(tomorrow, now));

        // Test 4: Goal Created after 2AM and Current Time is Before 2AM
        yesterday.set(Calendar.HOUR_OF_DAY, 3);
        today.set(Calendar.HOUR_OF_DAY, 3);
        tomorrow.set(Calendar.HOUR_OF_DAY, 3);
        assertTrue(TimeUtils.shouldShowIncompleteGoal(yesterday, now));
        assertFalse(TimeUtils.shouldShowIncompleteGoal(today, now));
        assertFalse(TimeUtils.shouldShowIncompleteGoal(tomorrow, now));
    }
}