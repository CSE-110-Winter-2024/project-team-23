package edu.ucsd.cse110.successorator.lib.util;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.AppMode;

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

    @Test
    public void generateTitleString() {
        var localizedCalendar = Calendar.getInstance(TimeUtils.GMT);
        var now = TimeUtils.localize(TimeUtils.START_TIME, localizedCalendar);
        // Verify that the current date string is correct
        var currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.TODAY);
        assertEquals("Today, Wed 2/7", currentDateString);
        // advance 9 hours and verify no change
        now.add(Calendar.HOUR_OF_DAY, 9);
        currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.TODAY);
        assertEquals("Today, Wed 2/7", currentDateString);
        // Advance another 2 and verify change
        now.add(Calendar.HOUR_OF_DAY, 2);
        currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.TODAY);
        assertEquals("Today, Thu 2/8", currentDateString);
        // Now test different app modes
        currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.PENDING);
        assertEquals("Pending", currentDateString);
        currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.TOMORROW);
        assertEquals("Tomorrow, Fri 2/9", currentDateString);
        currentDateString = TimeUtils.generateTitleString(localizedCalendar, now, AppMode.RECURRING);
        assertEquals("Recurring", currentDateString);
    }
}