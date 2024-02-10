package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

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

    @Test
    public void pressGoal() {
        // We test both pressing complete and incomplete goals
        // Including pressing a button twice, and making sure there are no side effects
        mainViewModel.pressGoal(1);

        var incompleteGoals = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(incompleteGoals);
        // Verify that goal ID 1 is visible and has the correct date
        var completeGoals = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoals);
        assertEquals((Integer) 1, completeGoals.get(0).id());
        assertEquals(TimeUtils.START_TIME, completeGoals.get(0).completionDate());

        // Verify that goal IDs 2, 3 are in the incomplete goals list and ids 1, 4, 5, 6 are not
        incompleteGoals = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(incompleteGoals);
        assertEquals(2, (int) incompleteGoals.get(0).id());
        assertEquals(3, (int) incompleteGoals.get(1).id());
        assertFalse(incompleteGoals.stream().anyMatch(goal -> goal.id() == 1));
        for (int id = 4; id <= 8; id++) {
            int finalId = id;
            assertFalse(incompleteGoals.stream().anyMatch(goal -> goal.id() == finalId));
        }
        // Now re-press 1 and 4
        mainViewModel.pressGoal(1);
        mainViewModel.pressGoal(4);
        incompleteGoals = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(incompleteGoals);
        // Now verify expected state change occured
        // Verify that goal IDs 1, 2, 3, 4 are in the incomplete goals list and ids 5, 6 are not
        assertEquals(1, (int) incompleteGoals.get(0).id());
        assertEquals(2, (int) incompleteGoals.get(1).id());
        assertEquals(3, (int) incompleteGoals.get(2).id());
        assertEquals(4, (int) incompleteGoals.get(3).id());
        for (int id = 5; id <= 8; id++) {
            int finalId = id;
            assertFalse(incompleteGoals.stream().anyMatch(goal -> goal.id() == finalId));
        }
    }

    @Test
    public void getIncompleteGoals() {
        // We test both pressing complete and incomplete goals
        // First verify expected state (that first 3 goals are incomplete)
        var incompleteGoals = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(incompleteGoals);
        assertEquals(3, incompleteGoals.size());
        // Verify that goal IDs 1, 2, 3 are in the incomplete goals list and other ids are not
        // Also tests ordering
        assertEquals(1, (int) incompleteGoals.get(0).id());
        assertEquals(2, (int) incompleteGoals.get(1).id());
        assertEquals(3, (int) incompleteGoals.get(2).id());
        for (int id = 4; id <= 8; id++) {
            int finalId = id;
            assertFalse(incompleteGoals.stream().anyMatch(goal -> goal.id() == finalId));
        }
    }

    @Test
    public void addGoal() {
        // Add a new goal
        mainViewModel.addGoal("Goal 9");
        // Verify that the new goal is in the incomplete goals list
        var incompleteGoals = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(incompleteGoals);
        assertEquals(9, (int) incompleteGoals.get(3).id());
    }

    @Test
    public void advance24HoursGoalVisibility() {
        // Note that this is a copy of the below method but changing the time via UI methods
        // Verify that goal 4 is not visible but 5, 6, 7, 8 are
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(4, completeGoalsToDisplay.size());
        for (int id = 5; id <= 8; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 5).id());
        }
        assertFalse(completeGoalsToDisplay.stream().anyMatch(goal -> goal.id() == 4));
        // Now, advance real time (not mock time) by 24 hours
        mainViewModel.advance24Hours();
        // Verify that goals 4, 5, 6, 7 are not visible and goal 8 is
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        for (int id = 4; id <= 7; id++) {
            int finalId = id;
            assertFalse(completeGoalsToDisplay.stream().anyMatch(goal -> goal.id() == finalId));
        }
        assertEquals(8, (int) completeGoalsToDisplay.get(0).id());
        mainViewModel.advance24Hours();
        // Verify that no goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(0, completeGoalsToDisplay.size());
    }


    @Test
    public void getCurrentDateString() {
        // Verify that the current date string is correct
        var currentDateString = mainViewModel.getCurrentDateString().getValue();
        assertNotNull(currentDateString);
        assertEquals("2024-02-07 16:00:00 GMT", currentDateString);
    }

    @Test
    public void getCompleteGoalsToDisplay() {
        // Verify that goal 4 is not visible but 5, 6, 7, 8 are
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(4, completeGoalsToDisplay.size());
        for (int id = 5; id <= 8; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 5).id());
        }
        assertFalse(completeGoalsToDisplay.stream().anyMatch(goal -> goal.id() == 4));
        // Now, advance real time (not mock time) by 24 hours
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH);
        // Verify that goals 4, 5, 6, 7 are not visible and goal 8 is
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        for (int id = 4; id <= 7; id++) {
            int finalId = id;
            assertFalse(completeGoalsToDisplay.stream().anyMatch(goal -> goal.id() == finalId));
        }
        assertEquals(8, (int) completeGoalsToDisplay.get(0).id());
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 2);
        // Verify that no goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(0, completeGoalsToDisplay.size());
    }
}