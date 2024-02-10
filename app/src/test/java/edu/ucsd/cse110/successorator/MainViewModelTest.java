package edu.ucsd.cse110.successorator;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucsd.cse110.successorator.lib.domain.MockGoalRepository;

public class MainViewModelTest {

    @Test
    public void pressGoal() {
        // We test both pressing complete and incomplete goals
        // Including pressing a button twice, and making sure there are no side effects
        MainViewModel mainViewModel = new MainViewModel(MockGoalRepository.createWithDefaultGoals());

        mainViewModel.pressGoal(1);
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
        MainViewModel mainViewModel = new MainViewModel(MockGoalRepository.createWithDefaultGoals());
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
        MainViewModel mainViewModel = new MainViewModel(MockGoalRepository.createWithDefaultGoals());
        // Add a new goal
        mainViewModel.addGoal("Goal 7");
        // Verify that the new goal is in the incomplete goals list
        assertTrue(mainViewModel.getIncompleteGoals().getValue().get(3).id() == 7);
    }
}