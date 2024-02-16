package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

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
        assertEquals((Long) TimeUtils.START_TIME, completeGoals.get(0).completionDate());

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
        assertEquals("Wednesday07February040000", currentDateString);
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


    @Test
    public void US7Scenario1() {
        // Since our standard start time is 4pm, move to 10:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 6 + TimeUtils.MINUTE_LENGTH * 30);
        var US7GoalRepository = MockGoalRepository.createWithUS7TestGoals();
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Verify that all 3 goals are visible
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Move to 11:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 7 + TimeUtils.MINUTE_LENGTH * 30);
        // simulate closing the app by creating a new mainViewModel
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Verify that all 3 goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Move to 1:30 next day
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9 + TimeUtils.MINUTE_LENGTH * 30);
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Verify that all 3 goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Wait another hour
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 10 + TimeUtils.MINUTE_LENGTH * 30);
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Verify that no goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(0, completeGoalsToDisplay.size());
    }

    @Test
    public void US7Scenario2() {
        // We assume the app is open the whole time now
        var US7GoalRepository = MockGoalRepository.createWithUS7TestGoals();
        mainViewModel = new MainViewModel(US7GoalRepository, dateOffset, dateTicker, localizedCalendar);
        // Since our standard start time is 4pm, move to 10:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 6 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Move to 11:30
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 7 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Move to 1:30 next day
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that all 3 goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        for (int id = 1; id <= 3; id++) {
            assertEquals(id, (int) completeGoalsToDisplay.get(id - 1).id());
        }
        // Wait another hour
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 10 + TimeUtils.MINUTE_LENGTH * 30);
        // Verify that no goals are visible
        completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(0, completeGoalsToDisplay.size());
    }

    @Test
    public void US8Scenario1() {
        mainViewModel.pressGoal(6);
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        var incompleteGoalsToDisplay = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(3, completeGoalsToDisplay.size());
        assertNotNull(incompleteGoalsToDisplay);
        assertEquals(4, incompleteGoalsToDisplay.size());
        assertEquals(1, (int) incompleteGoalsToDisplay.get(0).id());
        assertEquals(2, (int) incompleteGoalsToDisplay.get(1).id());
        assertEquals(3, (int) incompleteGoalsToDisplay.get(2).id());
        assertEquals(6, (int) incompleteGoalsToDisplay.get(3).id());
        assertEquals(5, (int) completeGoalsToDisplay.get(0).id());
        assertEquals(7, (int) completeGoalsToDisplay.get(1).id());
        assertEquals(8, (int) completeGoalsToDisplay.get(2).id());

    }

    @Test
    public void US6Scenario1() {
        mainViewModel.pressGoal(1);
        var completeGoalsToDisplay = mainViewModel.getCompleteGoalsToDisplay().getValue();
        var incompleteGoalsToDisplay = mainViewModel.getIncompleteGoals().getValue();
        assertNotNull(completeGoalsToDisplay);
        assertEquals(5, completeGoalsToDisplay.size());
        assertNotNull(incompleteGoalsToDisplay);
        assertEquals(2, incompleteGoalsToDisplay.size());

        assertEquals(2, (int) incompleteGoalsToDisplay.get(0).id());
        assertEquals(3, (int) incompleteGoalsToDisplay.get(1).id());
        assertEquals(1, (int) completeGoalsToDisplay.get(0).id());
        assertEquals(5, (int) completeGoalsToDisplay.get(1).id());
        assertEquals(6, (int) completeGoalsToDisplay.get(2).id());
        assertEquals(7, (int) completeGoalsToDisplay.get(3).id());
        assertEquals(8, (int) completeGoalsToDisplay.get(4).id());
    }
    
}
