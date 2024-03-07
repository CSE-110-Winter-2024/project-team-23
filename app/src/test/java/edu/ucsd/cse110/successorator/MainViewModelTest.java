package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.AppMode;
import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.domain.MockGoalRepository;
import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

/**
 * Pretty much all of these are integration tests because I've refactored units out of MainViewModel
 * and business logic is fairly coupled with all of these elements
 */
public class MainViewModelTest {
    MockGoalRepository goalRepository;
    MutableSubject<Long> dateOffset;
    MutableSubject<Long> dateTicker;
    Calendar localizedCalendar;
    MainViewModel mainViewModel;

    @Before
    public void setUp() {
        goalRepository = MockGoalRepository.createWithDefaultGoals();
        dateOffset = new SimpleSubject<>();
        dateOffset.setValue(0L);
        dateTicker = new SimpleSubject<>();
        dateTicker.setValue(TimeUtils.START_TIME);
        localizedCalendar = Calendar.getInstance(TimeUtils.GMT);
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
    }

    // https://www.softwaretestinghelp.com/junit-test-fixture-with-examples/
    // Recommended we set to null to avoid memory leaking
    @After
    public void tearDown() {
        goalRepository = null;
        dateOffset = null;
        dateTicker = null;
        localizedCalendar = null;
        mainViewModel = null;
    }

    public void assertIncompleteCount(int expected) {
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var incompleteGoals = displayGoals.stream().filter(goal -> !goal.completed()).count();
        assertEquals(expected, incompleteGoals);
    }

    public void assertCompleteCount(int expected) {
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var completeGoals = displayGoals.stream().filter(Goal::completed).count();
        assertEquals(expected, completeGoals);
    }

    public void assertPresence(int id, boolean expected) {
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var present = displayGoals.stream().anyMatch(goal -> goal.id() == id);
        assertEquals(expected, present);
    }

    public void assertPresenceInComplete(int id, boolean expected) {
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var present = displayGoals.stream().filter(Goal::completed).anyMatch(goal -> goal.id() == id);
        assertEquals(expected, present);
    }

    public void assertPresenceInIncomplete(int id, boolean expected) {
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var present = displayGoals.stream().filter(goal -> !goal.completed()).anyMatch(goal -> goal.id() == id);
        assertEquals(expected, present);
    }

    @Test
    public void pressGoalToday() {
        // We test both pressing complete and incomplete goals
        // Including pressing a button twice, and making sure there are no side effects
        mainViewModel.pressGoal(1);

        // Verify that goal ID 1 is visible and has the correct date
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var goal = displayGoals.stream().filter(g -> g.id() == 1).findFirst().orElse(null);
        assertNotNull(goal);
        assertEquals((Long) TimeUtils.START_TIME, goal.completionDate());
        assertTrue(goal.completed());

        // Verify that goal IDs 2, 3 are in the incomplete goals list and ids 1, 4, 5, 6 are not
        assertIncompleteCount(2);
        assertPresenceInIncomplete(2, true);
        assertPresenceInIncomplete(3, true);
        assertPresenceInIncomplete(1, false);
        for (int id = 4; id <= 8; id++) {
            assertPresenceInIncomplete(id, false);
        }
        // Now re-press 1 and 5
        mainViewModel.pressGoal(1);
        mainViewModel.pressGoal(5);
        // Now verify expected state change occured
        // Verify that goal IDs 1, 2, 3, 5 are in the incomplete goals list and others are not
        assertIncompleteCount(4);
        assertPresenceInIncomplete(1, true);
        assertPresenceInIncomplete(2, true);
        assertPresenceInIncomplete(3, true);
        assertPresenceInIncomplete(5, true);
        for (int id = 6; id <= 8; id++) {
            assertPresenceInIncomplete(id, false);
        }
    }


    @Test
    public void addGoal() {
        // Add a new goal
        mainViewModel.addGoal("Goal 9");
        // Verify that the new goal is displayed
        assertPresence(9, true);
        assertPresenceInIncomplete(9, true);
        // Verify that the new goal has correct dates
        var displayGoals = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(displayGoals);
        var goal = displayGoals.stream().filter(g -> g.id() == 9).findFirst().orElse(null);
        assertNotNull(goal);
        assertEquals((Long) TimeUtils.START_TIME, goal.completionDate());
        assertEquals((Long) TimeUtils.START_TIME, goal.startDate());
        assertFalse(goal.completed());
        assertEquals("Goal 9", goal.content());

    }

    @Test
    public void advance24HoursGoalVisibility() {
        // Verify that all goals except 4 and 8 are visible
        for (int id = 1; id <= 8; id++) {
            assertPresence(id, id != 4 && id != 8);
        }
        // Now, advance real time (not mock time) by 24 hours
        mainViewModel.advance24Hours();
        // verify that all completed goals except 8 are not visible
        for (int id = 4; id <= 8; id++) {
            assertPresence(id, id == 8);
        }
        assertIncompleteCount(3);
        mainViewModel.advance24Hours();
        // Verify that no goals are visible
        assertIncompleteCount(3);
        assertCompleteCount(0);
    }

    @Test
    public void testCalendarStreings() {
        // Verify that the current date string is correct
        // Test the weekday and m/d strings as well, but the nth weekday gets its own test
        var currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Wed 2/7", currentDateString);
        var currentWeekdayString = mainViewModel.getCurrentWeekday().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Wed", currentWeekdayString);
        var currentDateString2 = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("2/7", currentDateString2);
        // advance 9 hours and verify no change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9);
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Wed 2/7", currentDateString);
        currentWeekdayString = mainViewModel.getCurrentWeekday().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Wed", currentWeekdayString);
        currentDateString2 = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("2/7", currentDateString2);
        // Advance another 2 and verify change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 11);
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Thu 2/8", currentDateString);
        currentWeekdayString = mainViewModel.getCurrentWeekday().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Thu", currentWeekdayString);
        currentDateString2 = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("2/8", currentDateString2);
        // Now test different app modes
        mainViewModel.activatePendingView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Pending", currentDateString);
        mainViewModel.activateTomorrowView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Tomorrow, Fri 2/9", currentDateString);
        currentWeekdayString = mainViewModel.getCurrentWeekday().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Fri", currentWeekdayString);
        currentDateString2 = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("2/9", currentDateString2);
        mainViewModel.activateRecurringView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Recurring", currentDateString);
        mainViewModel.activateTodayView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Thu 2/8", currentDateString);
        currentWeekdayString = mainViewModel.getCurrentWeekday().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Thu", currentWeekdayString);
        currentDateString2 = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("2/8", currentDateString2);

    }

    @Test
    public void getGoalsToDisplay() {
        // Verify that all goals except 4 and 8 are visible
        for (int id = 1; id <= 8; id++) {
            assertPresence(id, id != 4 && id != 8);
        }
        // Now, advance real time (not mock time) by 24 hours
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH);
        // verify that all complete goals except 8 are not visible
        for (int id = 4; id <= 8; id++) {
            assertPresence(id, id == 8);
        }
        assertIncompleteCount(3);
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 2);
        // Verify that no goals are visible
        assertIncompleteCount(3);
        assertCompleteCount(0);
    }

    @Test
    public void rolloverTime2AM() {
        // We assume the app is open the whole time now
        var US7GoalRepository = MockGoalRepository.createWithUS7TestGoals();
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Since our standard start time is 4pm, move to 10:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 6 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        assertPresence(1, true);
        assertPresence(2, true);
        assertPresence(3, true);
        // Move to 11:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 7 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        assertPresence(1, true);
        assertPresence(2, true);
        assertPresence(3, true);
        // Move to 1:30 next day
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        assertPresence(1, true);
        assertPresence(2, true);
        assertPresence(3, true);
        // Wait another hour
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 10 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that no goals are visible
        assertIncompleteCount(0);
        assertCompleteCount(0);
    }

    @Test
    public void US8Scenario1() {
        mainViewModel.pressGoal(6);
        assertIncompleteCount(4);
        assertCompleteCount(2);
    }

    @Test
    public void US6Scenario1() {
        mainViewModel.pressGoal(1);

        assertIncompleteCount(2);
        assertCompleteCount(4);
    }

    @Test
    public void Iteration1Integration1() {
        //
        mainViewModel.addGoal("meow");
        assertIncompleteCount(4);
        assertPresenceInIncomplete(9, true);
        var goalsToDisplay = mainViewModel.getGoalsToDisplay().getValue();
        assertNotNull(goalsToDisplay);
        assertEquals(7, goalsToDisplay.size());
        var goal = goalsToDisplay.stream().filter(g -> g.id() == 9).findFirst().orElse(null);
        assertEquals("meow", goal.content());
        mainViewModel.pressGoal(9);
        mainViewModel.pressGoal(1);
        mainViewModel.pressGoal(2);
        mainViewModel.pressGoal(3);
        assertIncompleteCount(0);
        assertCompleteCount(7);
        mainViewModel.advance24Hours();
        assertCompleteCount(1);
    }

    @Test
    public void Iteration1Integration2() {
        mainViewModel.pressGoal(1);
        mainViewModel.pressGoal(1);
        assertIncompleteCount(3);
        assertPresence(1, true);
        assertCompleteCount(3);
    }

    @Test
    public void addRecurringGoal() {
        // Add a goal in the past
        assertFalse(mainViewModel.addRecurringGoal("Recurring Goal", 2020, 2, 7, RecurrenceType.DAILY, Context.HOME));

        // Use empty repository
        for (int i = 0; i < 3; i++) {
            goalRepository = MockGoalRepository.createWithEmptyGoals();

            dateTicker.setValue(TimeUtils.START_TIME);

            mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

            RecurrenceType recurrenceType = RecurrenceType.values()[i + 1];
            // i = 0: daily, i = 1: weekly, i = 2: monthly, i = 3: yearly

            assertTrue(mainViewModel.addRecurringGoal("Recurring Goal", 2024, 1, 7, recurrenceType, Context.HOME));

            var goals = goalRepository.goals;
            assertEquals(3, goals.size());

            assertTrue(mainViewModel.addRecurringGoal("Recurring Goal", 2024, 1, 8, recurrenceType, Context.HOME));

            goals = goalRepository.goals;
            assertEquals(5, goals.size());

            assertTrue(mainViewModel.addRecurringGoal("Recurring Goal", 2024, 2, 7, recurrenceType, Context.HOME));
            assertEquals(7, goals.size());

            assertTrue(mainViewModel.addRecurringGoal("Recurring Goal", 2025, 1, 7, recurrenceType, Context.HOME));
            assertEquals(9, goals.size());
        }
        // Use daily, weekly, monthly, and yearly recurrence types

        // Add recurring goal today and make sure two get generated

        // Add recurring goal tomorrow and make sure only one gets generated

        // Add recurring goal some arbitrary day in the future and make only one gets generated
    }

    @Test
    public void addRecurringGoalDateless() {
        // Use empty repository
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // This test will be trivial since it uses pretty much the same method as previous
        // Just test daily case like above, on today and tomorrow views
        mainViewModel.addRecurringGoalDateless("Recurring Goal", RecurrenceType.DAILY, Context.HOME);
        var goals = goalRepository.goals;
        assertEquals(3, goals.size());

        mainViewModel.activateTomorrowView();
        mainViewModel.addRecurringGoalDateless("Recurring Goal", RecurrenceType.DAILY, Context.HOME);
        goals = goalRepository.goals;
        assertEquals(5, goals.size());

    }

    @Test
    public void handleRecurringGoalGeneration() {
        // Setup custom goal repository
        goalRepository = MockGoalRepository.createWithRecurringTestGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // Run once and since the state is setup properly, no new goals should get generated
        // On creation implicitly runs once
        var goals = goalRepository.goals;
        assertEquals(3, goals.size());

        // Then test time traveling to the future, and consequently generating prev and next goals
        // Note that this doesn't call the method directly, just verifies that the observables call it
        // AND it runs correctly

        // Time travel to tomorrow, and verify that ONE goal is generated
        // BUT, one goal gets deleted by the list cleaning logic
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH);
        goals = goalRepository.goals;
        assertEquals(3, goals.size());

        // Time travel by 2 days, and verify that TWO goals are generated
        // BUT, two goals get deleted by the list cleaning logic
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 2);

        goals = goalRepository.goals;
        assertEquals(3, goals.size());
    }

    @Test
    public void verifyRecurringGoalMerging() {
        // Setup empty goal repository
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // Add a recurring goal
        mainViewModel.addRecurringGoal("Recurring Goal", 2024, 1, 8, RecurrenceType.WEEKLY, Context.HOME);

        // Verify it got added
        var goals = goalRepository.goals;
        assertEquals(2, goals.size());

        // If we time travel to feb 14, 1. we should have 3 goals in db 2. no merge should occur (yet)
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 7);

        goals = goalRepository.goals;
        assertEquals(3, goals.size());

        // Test fine grained by showing goal in today and tomorrow
        mainViewModel.activateTodayView();
        assertIncompleteCount(1);
        mainViewModel.activateTomorrowView();
        assertIncompleteCount(1);

        // If we time travel to feb 15, still 3 goals but this is because a merge will have occured
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 8);

        goals = goalRepository.goals;
        assertEquals(3, goals.size());

        // Test fine grained by showing goal in today and tomorrow
        mainViewModel.activateTodayView();
        assertIncompleteCount(1);
        mainViewModel.activateTomorrowView();
        assertIncompleteCount(0);
    }

    @Test
    public void deleteRecurringGoal() {
        // Setup custom goal repository
        goalRepository = MockGoalRepository.createWithRecurringTestGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // Run once: recurring goal should be gone, but not the "generated" goals
        mainViewModel.deleteRecurringGoal(1);
        // Manually check repository to see if it's gone
        var goals = goalRepository.goals;
        assertEquals(2, goals.size());
        var goal = goals.get(0).getValue();
        assertNotNull(goal);
        assertEquals((Integer) 2, goal.id());
        goal = goals.get(1).getValue();
        assertNotNull(goal);
        assertEquals((Integer) 3, goal.id());

    }

    @Test
    public void pressRecurringGoal() {
        // Setup custom goal repository
        goalRepository = MockGoalRepository.createWithRecurringTestGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // Verify pressing tomorrow goal fails
        mainViewModel.activateTomorrowView();
        assertFalse(mainViewModel.pressGoal(3));
        assertIncompleteCount(1);
        assertCompleteCount(0);

        // Verify pressing today goal passes
        mainViewModel.activateTodayView();
        assertTrue(mainViewModel.pressGoal(2));
        assertIncompleteCount(0);
        assertCompleteCount(1);

        // Verify pressing tomorrow goal passes
        mainViewModel.activateTomorrowView();
        assertTrue(mainViewModel.pressGoal(3));
        assertIncompleteCount(0);
        assertCompleteCount(1);

        // Verify pressing today goal fails
        mainViewModel.activateTodayView();
        assertFalse(mainViewModel.pressGoal(2));
        assertIncompleteCount(0);
        assertCompleteCount(1);
    }
}
