package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import edu.ucsd.cse110.successorator.lib.domain.DateOffset;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.domain.MockCalendar;
import edu.ucsd.cse110.successorator.lib.domain.MockGoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;

public class MainViewModelTest {
    GoalRepository goalRepository;
    MutableSubject<DateOffset> dateOffset;
    MutableSubject<Object> dateTicker;
    MockCalendar mockCalendar;
    Calendar localizedCalendar;
    MainViewModel mainViewModel;

    @Before
    public void setUp() {
        goalRepository = MockGoalRepository.createWithDefaultGoals();
        mockCalendar = new MockCalendar(MockGoalRepository.START_TIME);
        dateOffset = new SimpleSubject<>();
        dateOffset.setValue(new DateOffset(0, mockCalendar));
        dateTicker = new SimpleSubject<>();
        dateTicker.setValue(null);
        localizedCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
    }

    // https://www.softwaretestinghelp.com/junit-test-fixture-with-examples/
    // Recommended we set to null to avoid memory leaking
    @After
    public void tearDown() throws Exception {
        goalRepository = null;
        dateOffset = null;
        dateTicker = null;
        mockCalendar = null;
        localizedCalendar = null;
        mainViewModel = null;
    }

    @Test
    public void pressGoal() {
        // We test both pressing complete and incomplete goals
        // Including pressing a button twice, and making sure there are no side effects
        mainViewModel.pressGoal(1);
        // TODO: use mocked calendar to test complete date setting
        // Verify that goal IDs 2, 3 are in the incomplete goals list and ids 1, 4, 5, 6 are not
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(0).id() == 2);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(1).id() == 3);
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 1));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 4));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 5));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 6));
        // Now re-press 1 and 4
        mainViewModel.pressGoal(1);
        mainViewModel.pressGoal(4);
        // Now verify expected state change occured
        // Verify that goal IDs 1, 2, 3, 4 are in the incomplete goals list and ids 5, 6 are not
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(0).id() == 1);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(1).id() == 2);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(2).id() == 3);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(3).id() == 4);
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 5));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 6));
    }

    @Test
    public void getIncompleteGoals() {
        // We test both pressing complete and incomplete goals
        // First verify expected state (that first 3 goals are incomplete)
        assertNotNull(mainViewModel.getIncompleteGoals().getValue());
        assertEquals(3, mainViewModel.getIncompleteGoals().getValue().size());
        // Verify that goal IDs 1, 2, 3 are in the incomplete goals list and ids 4, 5, 6 are not
        // Also tests ordering
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(0).id() == 1);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(1).id() == 2);
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(2).id() == 3);
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 4));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 5));
        assertFalse(mainViewModel.getIncompleteGoals().getValue().stream().anyMatch(goal -> goal.id() == 6));
    }

    @Test
    public void addGoal() {
        // Add a new goal
        mainViewModel.addGoal("Goal 7");
        // Verify that the new goal is in the incomplete goals list
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(3).id() == 7);
    }
}