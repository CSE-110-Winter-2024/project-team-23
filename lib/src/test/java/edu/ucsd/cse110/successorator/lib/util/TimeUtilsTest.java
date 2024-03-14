package edu.ucsd.cse110.successorator.lib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static edu.ucsd.cse110.successorator.lib.util.TimeUtils.nthDayofWeek;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;

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
    public void nthRecurrence() {
        // Sanity test
        var now = Calendar.getInstance();
        // Only accepts 2am normalized
        now = TimeUtils.twoAMNormalized(now);
        assertEquals(TimeUtils.nthRecurrence(now, RecurrenceType.DAILY, 0), now);
        assertEquals(TimeUtils.nthRecurrence(now, RecurrenceType.WEEKLY, 0), now);
        assertEquals(TimeUtils.nthRecurrence(now, RecurrenceType.MONTHLY, 0), now);
        assertEquals(TimeUtils.nthRecurrence(now, RecurrenceType.YEARLY, 0), now);


        int year = 2024;
        int month = Calendar.FEBRUARY;
        int day = 7;
        int hour = 12;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.setTimeZone(TimeUtils.GMT);

        // Yearly recurrence happy path
        // Test n=1
        // No need to test more than that
        var recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.YEARLY, 1);
        assertEquals(recurrence.get(Calendar.YEAR), year + 1);


        // Yearly recurrence feb 29
        calendar = Calendar.getInstance();
        calendar.set(year, month, 29, hour, 0, 0);
        calendar.setTimeZone(TimeUtils.GMT);

        // Test n=1
        recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.YEARLY, 1);
        assertEquals(recurrence.get(Calendar.YEAR), year + 1);
        assertEquals(recurrence.get(Calendar.MONTH), Calendar.MARCH);
        assertEquals(recurrence.get(Calendar.DAY_OF_MONTH), 1);

        calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.setTimeZone(TimeUtils.GMT);

        // Monthly recurrence happy path
        // Test n=1, 10, 100
        for (int i = 1; i <= 100; i *= 10) {
            var testTime = (Calendar) calendar.clone();
            for (int j = 0; j < i; j++) {
                testTime.add(Calendar.MONTH, 1);
            }
            recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.MONTHLY, i);
            assertEquals(recurrence.get(Calendar.MONTH), testTime.get(Calendar.MONTH));
            assertEquals(recurrence.get(Calendar.YEAR), testTime.get(Calendar.YEAR));
        }

        // Monthly recurrence 5th day of week not present in next month
        // March 29 2024 is a Friday (5th friday)
        // There is no 5th Friday in April so it should fall on May 3
        // But there is a 5th Friday in May so it should fall on May 31
        calendar = Calendar.getInstance();
        calendar.set(year, Calendar.MARCH, 29, hour, 0, 0);
        calendar.setTimeZone(TimeUtils.GMT);

        recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.MONTHLY, 1);
        assertEquals(recurrence.get(Calendar.MONTH), Calendar.MAY);
        assertEquals(recurrence.get(Calendar.DAY_OF_MONTH), 3);

        recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.MONTHLY, 2);
        assertEquals(recurrence.get(Calendar.MONTH), Calendar.MAY);
        assertEquals(recurrence.get(Calendar.DAY_OF_MONTH), 31);


        calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.setTimeZone(TimeUtils.GMT);

        // Weekly recurrence happy path (there is no sad path)
        // Test n=1, 10, 100, 1000
        // n is chosen so it covers >2 years, which is why it's 1000 here but 100 for months
        for (int i = 1; i <= 1000; i *= 10) {
            var testTime = (Calendar) calendar.clone();
            for (int j = 0; j < i; j++) {
                testTime.add(Calendar.WEEK_OF_YEAR, 1);
            }
            recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.WEEKLY, i);
            assertEquals(recurrence.get(Calendar.WEEK_OF_YEAR), testTime.get(Calendar.WEEK_OF_YEAR));
        }

        // Daily recurrence happy path (there is no sad path)
        // Test n=1, 10, 100, 1000
        for (int i = 1; i <= 1000; i *= 10) {
            var testTime = (Calendar) calendar.clone();
            for (int j = 0; j < i; j++) {
                testTime.add(Calendar.DAY_OF_YEAR, 1);
            }
            recurrence = TimeUtils.nthRecurrence(calendar, RecurrenceType.DAILY, i);
            assertEquals(recurrence.get(Calendar.DAY_OF_YEAR), testTime.get(Calendar.DAY_OF_YEAR));
        }
    }

    @Test
    public void nextGoalRecurrenceIndex() {
        // Test 1: Recurrence is Daily
        var now = Calendar.getInstance();
        now = TimeUtils.twoAMNormalized(now);
        var start = (Calendar) now.clone();

        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.DAILY), 1);
        start.add(Calendar.DAY_OF_YEAR, -1);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.DAILY), 2);
        start.add(Calendar.DAY_OF_YEAR, -500);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.DAILY), 502);

        // Test 2: Recurrence is Weekly
        start = (Calendar) now.clone();

        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.WEEKLY), 1);
        start.add(Calendar.WEEK_OF_YEAR, -1);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.WEEKLY), 2);
        start.add(Calendar.DAY_OF_YEAR, -1);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.WEEKLY), 2);

        // For next 2 tests we test differently for edge cases

        // Test 3: Recurrence is Monthly
        // 5th friday of december 2023 is 12/29/2023
        // Test with that as a start date
        // Then, test number of recurrences for the day before, during, and after the march and may dates from the nthRecurrence test
        start = Calendar.getInstance();
        start.setTimeZone(TimeUtils.GMT);
        start.set(2023, Calendar.DECEMBER, 29, 12, 0, 0);
        now.setTimeZone(TimeUtils.GMT);
        now = TimeUtils.twoAMNormalized(now);
        now.set(2024, Calendar.MARCH, 28, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 3);
        now.set(2024, Calendar.MARCH, 29, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 4);
        now.set(2024, Calendar.MARCH, 30, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 4);
        now.set(2024, Calendar.MAY, 2, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 4);
        now.set(2024, Calendar.MAY, 3, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 5);
        now.set(2024, Calendar.MAY, 4, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 5);
        now.set(2024, Calendar.MAY, 30, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 5);
        now.set(2024, Calendar.MAY, 31, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 6);
        now.set(2024, Calendar.JUNE, 1, 12, 0, 0);
        assertEquals(TimeUtils.nextGoalRecurrenceIndex(now, start, RecurrenceType.MONTHLY), 6);


        // Test 4: Recurrence is Yearly
    }

    @Test
    public void twoAMNormalized() {
        // Test 1: Date is before 2AM
        var now = Calendar.getInstance();
        var test = (Calendar) now.clone();
        test.set(Calendar.HOUR_OF_DAY, 1);
        var normalized = TimeUtils.twoAMNormalized(test);
        assertEquals(normalized.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(normalized.get(Calendar.MINUTE), 0);
        assertEquals(normalized.get(Calendar.SECOND), 0);
        assertEquals(normalized.get(Calendar.MILLISECOND), 0);
        assertEquals(normalized.get(Calendar.DAY_OF_YEAR), test.get(Calendar.DAY_OF_YEAR) - 1);
        // Test 2: Date is after 2AM
        test.set(Calendar.HOUR_OF_DAY, 3);
        normalized = TimeUtils.twoAMNormalized(test);
        assertEquals(normalized.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(normalized.get(Calendar.MINUTE), 0);
        assertEquals(normalized.get(Calendar.SECOND), 0);
        assertEquals(normalized.get(Calendar.MILLISECOND), 0);
        assertEquals(normalized.get(Calendar.DAY_OF_YEAR), test.get(Calendar.DAY_OF_YEAR));

    }

    // Test Generated with GPT4, Check for correctness
    @Test
    public void testNthDayOfWeekForVariousDates() {
        Calendar calendar = Calendar.getInstance(Locale.US);

        // Test for the first Monday of a month
        calendar.set(2024, Calendar.MARCH, 4); // Assuming March 4, 2024 is the first Monday
        assertEquals("1st Monday", nthDayofWeek(calendar));

        // Test for the second Tuesday of a month
        calendar.set(2024, Calendar.APRIL, 9); // Assuming April 9, 2024 is the second Tuesday
        assertEquals("2nd Tuesday", nthDayofWeek(calendar));

        // Test for the third Wednesday of a month
        calendar.set(2024, Calendar.MAY, 15); // Assuming May 15, 2024 is the third Wednesday
        assertEquals("3rd Wednesday", nthDayofWeek(calendar));

        // Test for the fourth Thursday of a month
        calendar.set(2024, Calendar.JUNE, 27); // Assuming June 27, 2024 is the fourth Thursday
        assertEquals("4th Thursday", nthDayofWeek(calendar));

        // Test for the fifth Friday of a month
        calendar.set(2024, Calendar.AUGUST, 30); // Assuming August 30, 2024 is the fifth Friday
        assertEquals("5th Friday", nthDayofWeek(calendar));

        // Test for the last Saturday of a month (could be 4th or 5th)
        calendar.set(2024, Calendar.FEBRUARY, 24); // Assuming February 24, 2024 is the last Saturday
        assertEquals("4th Saturday", nthDayofWeek(calendar));
    }
}