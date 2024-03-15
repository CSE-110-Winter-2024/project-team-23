package edu.ucsd.cse110.successorator.lib.domain.filtering;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

public class TomorrowGoalFilterTest {

    @Test
    public void shouldShow() {
        // Verify returns false for pending and recurring
        Goal testGoal = new Goal(1, "Goal 1", 1, false, 0, true, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME, null, null, null, false);
        Calendar localizedCalendar = Calendar.getInstance(TimeUtils.GMT);
        Calendar startTime = TimeUtils.localize(TimeUtils.START_TIME, localizedCalendar);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));
        testGoal = new Goal(1, "Goal 1", 1, false, 0, false, true, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME, null, null, null, false);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));

        // Verify returns false for completed and yesterday
        testGoal = new Goal(1, "Goal 1", 1, true, TimeUtils.START_TIME - TimeUtils.DAY_LENGTH, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME - TimeUtils.DAY_LENGTH, null, null, null, false);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));

        // Verify returns false for completed and today
        testGoal = new Goal(1, "Goal 1", 1, true, TimeUtils.START_TIME, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME, null, null, null, false);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));

        // Verify returns false for incomplete, today or yesterday
        testGoal = new Goal(1, "Goal 1", 1, false, 0, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME, null, null, null, false);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));
        testGoal = new Goal(1, "Goal 1", 1, false, 0, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME - TimeUtils.DAY_LENGTH, null, null, null, false);
        assertFalse(new TomorrowGoalFilter().shouldShow(testGoal, startTime));

        // Verify returns true for incomplete or complete and tomorrow
        testGoal = new Goal(1, "Goal 1", 1, false, 0, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME + TimeUtils.DAY_LENGTH, null, null, null, false);
        assertTrue(new TomorrowGoalFilter().shouldShow(testGoal, startTime));
        testGoal = new Goal(1, "Goal 1", 1, true, TimeUtils.START_TIME + TimeUtils.DAY_LENGTH, false, false, RecurrenceType.NONE, Context.HOME, TimeUtils.START_TIME + TimeUtils.DAY_LENGTH, null, null, null, false);
        assertTrue(new TomorrowGoalFilter().shouldShow(testGoal, startTime));
    }
}