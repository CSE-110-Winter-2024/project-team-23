package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.domain.MockGoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

/**
 * Pretty much all of these are integration tests because I've refactored units out of MainViewModel
 * and business logic is fairly coupled with all of these elements
 */
public class MainViewModelTest {
    GoalRepository goalRepository;
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

    // Pretty much tests US2 scenarios 1 and 2 as well (just differnt time increments).
    @Test
    public void getCurrentDateString() {
        // Verify that the current date string is correct
        var currentDateString = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Wednesday, 07 February", currentDateString);
        // advance 9 hours and verify no change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9);
        currentDateString = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Wednesday, 07 February", currentDateString);
        // Advance another 2 and verify change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 11);
        currentDateString = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Thursday, 08 February", currentDateString);
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
}
