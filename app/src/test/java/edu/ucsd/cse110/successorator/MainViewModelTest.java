package edu.ucsd.cse110.successorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
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
        mainViewModel.addGoal("Goal 9", Context.HOME);
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
    public void testGetGoalContent() {
        goalRepository = MockGoalRepository.createWithAllRecurrenceTypeTestGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.activateTodayView();
        assertEquals("Goal 1", mainViewModel.getGoalContent(goalRepository.goals.get(0).getValue()));
        assertEquals("Goal 2", mainViewModel.getGoalContent(goalRepository.goals.get(1).getValue()));
        assertEquals("Goal 3", mainViewModel.getGoalContent(goalRepository.goals.get(2).getValue()));
        assertEquals("Goal 4", mainViewModel.getGoalContent(goalRepository.goals.get(3).getValue()));
        assertEquals("Goal 5", mainViewModel.getGoalContent(goalRepository.goals.get(4).getValue()));

        mainViewModel.activateRecurringView();
        assertEquals("Goal 1", mainViewModel.getGoalContent(goalRepository.goals.get(0).getValue()));
        assertEquals("Goal 2, Daily", mainViewModel.getGoalContent(goalRepository.goals.get(1).getValue()));
        assertEquals("Goal 3, Weekly on Wednesday", mainViewModel.getGoalContent(goalRepository.goals.get(2).getValue()));
        assertEquals("Goal 4, Monthly on 1st Wednesday", mainViewModel.getGoalContent(goalRepository.goals.get(3).getValue()));
        assertEquals("Goal 5, Yearly on 2/7", mainViewModel.getGoalContent(goalRepository.goals.get(4).getValue()));
    }

    @Test
    public void testCalendarStreings() {
        // Verify that the current date string is correct
        // Test the weekday and m/d strings as well
        var currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Wed 2/7", currentDateString);
        var currentWeekdayString = mainViewModel.getWeeklyButtonString().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Weekly on Wed", currentWeekdayString);
        var currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 1st Wed", currentMonthDayString);
        var currentDateString2 = mainViewModel.getYearlyButtonString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("Yearly on 2/7", currentDateString2);
        // advance 9 hours and verify no change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9);
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Wed 2/7", currentDateString);
        currentWeekdayString = mainViewModel.getWeeklyButtonString().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Weekly on Wed", currentWeekdayString);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 1st Wed", currentMonthDayString);
        currentDateString2 = mainViewModel.getYearlyButtonString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("Yearly on 2/7", currentDateString2);
        // Advance another 2 and verify change
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 11);
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Thu 2/8", currentDateString);
        currentWeekdayString = mainViewModel.getWeeklyButtonString().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Weekly on Thu", currentWeekdayString);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 2nd Thu", currentMonthDayString);
        currentDateString2 = mainViewModel.getYearlyButtonString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("Yearly on 2/8", currentDateString2);
        // Now test different app modes
        mainViewModel.activatePendingView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Pending", currentDateString);
        mainViewModel.activateTomorrowView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Tomorrow, Fri 2/9", currentDateString);
        currentWeekdayString = mainViewModel.getWeeklyButtonString().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Weekly on Fri", currentWeekdayString);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 2nd Fri", currentMonthDayString);
        currentDateString2 = mainViewModel.getYearlyButtonString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("Yearly on 2/9", currentDateString2);
        mainViewModel.activateRecurringView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Recurring", currentDateString);
        mainViewModel.activateTodayView();
        currentDateString = mainViewModel.getCurrentTitleString().getValue();
        assertNotNull(currentDateString);
        assertEquals("Today, Thu 2/8", currentDateString);
        currentWeekdayString = mainViewModel.getWeeklyButtonString().getValue();
        assertNotNull(currentWeekdayString);
        assertEquals("Weekly on Thu", currentWeekdayString);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 2nd Thu", currentMonthDayString);
        currentDateString2 = mainViewModel.getYearlyButtonString().getValue();
        assertNotNull(currentDateString2);
        assertEquals("Yearly on 2/8", currentDateString2);
        // Special case for 3rd, 4th, 5th
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 8);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 3rd Thu", currentMonthDayString);
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 15);
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 4th Thu", currentMonthDayString);
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 22);
        // Take advantage of leap year to make this test easy
        currentMonthDayString = mainViewModel.getMonthlyButtonString().getValue();
        assertNotNull(currentMonthDayString);
        assertEquals("Monthly on 5th Thu", currentMonthDayString);
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
        mainViewModel.addGoal("meow", Context.HOME);
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
    public void addGoalWithContext() {
        mainViewModel.addGoal("foo", Context.HOME);
        var goalsToDisplay = mainViewModel.getGoalsToDisplay().getValue();
        var goal = goalsToDisplay.stream().filter(g -> g.content().equals("foo")).findFirst().orElse(null);
        assertNotNull(goal);
        assertEquals(Context.HOME, goal.context());
    }

    @Test
    public void addGoalWithWithContextInRepo() {
        mainViewModel.addGoal("foo", Context.HOME);
        var goalsToDisplay = mainViewModel.getGoalsToDisplay().getValue();
        var goal = goalsToDisplay.stream().filter(g -> g.content().equals("foo")).findFirst().orElse(null);
        assertNotNull(goal);
        var repoGoal = goalRepository.findGoal(goal.id());
        assertNotNull(repoGoal);
        assertEquals(Context.HOME, repoGoal.context());
    }

    //Based off of the ones written on the master doc
    //Test 1: Creating various goals (UI testing)
    @Test
    public void ScenarioBasedSystemTests1() {

        //Starts with cleared database
        goalRepository = null;
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        //Steps 1 to 7
        //Add one time goal with Home context
        mainViewModel.addGoal("Go to party tonight", Context.HOME);
        mainViewModel.addRecurringGoalDateless("Call mom", RecurrenceType.WEEKLY, Context.HOME);
        assertPresence(1, true);
        assertCompleteCount(0);
        assertIncompleteCount(2);

        //Steps 8 to 14
        mainViewModel.activateTomorrowView();
        //Add one time goal with Home context
        mainViewModel.addGoal("Install game update", Context.HOME);
        mainViewModel.addRecurringGoalDateless("Pay bills", RecurrenceType.MONTHLY, Context.HOME);
        assertCompleteCount(0);
        assertIncompleteCount(2);

        //Steps 15 to 24
        mainViewModel.activatePendingView();
        //WIP does this really treat as pending? No
        mainViewModel.addGoal("Research job market", Context.HOME);
        assertCompleteCount(0);
        assertIncompleteCount(0);
        mainViewModel.activateRecurringView();
        mainViewModel.addRecurringGoal("Visit Goal", 2025, 1, 20, RecurrenceType.YEARLY, Context.HOME);
        assertCompleteCount(0);
        assertIncompleteCount(3);

    }

    //Test 2: Time handling: Clearing finished goals and keeping recurring goals
    @Test
    public void ScenarioBasedSystemTests2() {
        //Starts with cleared database
        goalRepository = null;
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        //Step 1
        //Sets up time to be March 7th, 2024
        dateTicker.setValue(TimeUtils.getStartTime() + TimeUtils.DAY_LENGTH * 28);
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        //Steps 2 to 5
        mainViewModel.activateTodayView();
        mainViewModel.addRecurringGoalDateless("10km run", RecurrenceType.MONTHLY, Context.HOME);
        mainViewModel.activateTomorrowView();
        mainViewModel.addRecurringGoalDateless("    ", RecurrenceType.MONTHLY, Context.HOME);
        mainViewModel.activateTodayView();
        mainViewModel.addRecurringGoalDateless("push buttons on keyboard", RecurrenceType.DAILY, Context.HOME);
        mainViewModel.activatePendingView();
        mainViewModel.addGoal("@everyone", Context.HOME);
        assertCompleteCount(0);
        assertIncompleteCount(0);

        //Step 6
        mainViewModel.activateTodayView();
        var displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "10km run") {
                assertTrue(mainViewModel.pressGoal(displayedGoals.get(i).id()));
            }
        }
        mainViewModel.activateTomorrowView();
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "    ") {
                assertTrue(mainViewModel.pressGoal(displayedGoals.get(i).id()));
                break;
            }
        }

        //Step 7, trying to complete a goal that shouldn't be
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "push buttons on keyboard") {
                assertFalse(mainViewModel.pressGoal(displayedGoals.get(i).id()));
                break;
            }
        }

        //Step 8
        mainViewModel.activateTodayView();
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "push buttons on keyboard") {
                assertTrue(mainViewModel.pressGoal(displayedGoals.get(i).id()));
                break;
            }
        }
        mainViewModel.activateTomorrowView();
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "push buttons on keyboard") {
                assertTrue(mainViewModel.pressGoal(displayedGoals.get(i).id()));
                break;
            }
        }

        //Step 9 WIP pending behavior

        //Step 10 to 12
        mainViewModel.advance24Hours();
        mainViewModel.activateTodayView();
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();

        //For later after pending goal behaviors are done
        //assertEquals(1, displayedGoals.size());
        //assertTrue(displayedGoals.get(0).content()=="push buttons on keyboard");

        mainViewModel.activateTomorrowView();
        displayedGoals = mainViewModel.getGoalsToDisplay().getValue();
        for (int i = 0; i < displayedGoals.size(); i++) {
            if (displayedGoals.get(i).content() == "push buttons on keyboard") {
                assertFalse(displayedGoals.get(i).completed());
                break;
            }
        }


    }

    //Test 3: Correctly display, filter and order goals
    @Test
    public void ScenarioBasedSystemTests3() {
        //Starts with cleared database
        goalRepository = null;
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        //Steps 1 to 8

        mainViewModel.addGoal("work", Context.WORK);
        mainViewModel.addGoal("home", Context.HOME);
        mainViewModel.addGoal("errands", Context.ERRANDS);
        mainViewModel.addGoal("school", Context.SCHOOL);
        assertCompleteCount(0);
        assertIncompleteCount(4);


        //WIP logic to filter by context

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
    public void deleteRecurringGoalGeneratedGoals() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        // Tests the specific cases where: first goal of recurring goal is visible tomorrow
        // so make sure that first goal still exists
        // first goal of recurring goal is NOT visible today or tomorrow, so it should go away
        // recurring goal has an instance visible today, and one not visible tomorrow
        // Start date is feb 7 2024, so spawn a daily recurring on feb 8 2024
        mainViewModel.addRecurringGoal("Recurring Goal", 2024, 1, 8, RecurrenceType.DAILY, Context.HOME);
        var goals = goalRepository.goals;
        assertEquals(2, goals.size());

        // Delete the goal
        mainViewModel.deleteRecurringGoal(1);
        goals = goalRepository.goals;
        assertEquals(1, goals.size());

        // Reset, then do daily recurring goal on feb 9
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Recurring Goal", 2024, 1, 9, RecurrenceType.DAILY, Context.HOME);
        goals = goalRepository.goals;
        assertEquals(2, goals.size());
        mainViewModel.deleteRecurringGoal(1);
        goals = goalRepository.goals;
        assertEquals(0, goals.size());

        // Reset, then do weekly recurring goal today
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoalDateless("Recurring Goal", RecurrenceType.WEEKLY, Context.HOME);
        goals = goalRepository.goals;
        assertEquals(3, goals.size()); // next instance should be generated in this case
        mainViewModel.deleteRecurringGoal(1);
        goals = goalRepository.goals;
        assertEquals(1, goals.size());
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

    //Focus Mode testing
    @Test
    public void MS2_US2() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addGoal("Errand1", Context.ERRANDS);
        mainViewModel.addGoal("Errand2", Context.ERRANDS);
        mainViewModel.addGoal("School", Context.SCHOOL);
        mainViewModel.addGoal("Work", Context.WORK);
        mainViewModel.addGoal("Home1", Context.HOME);
        mainViewModel.addGoal("Home2", Context.HOME);
        mainViewModel.activateFocusMode(Context.ERRANDS);
        assertIncompleteCount(2);
        mainViewModel.addGoal("Errand3", Context.ERRANDS);
        assertIncompleteCount(3);
        mainViewModel.activateFocusMode(Context.WORK);
        assertIncompleteCount(1);
        mainViewModel.activateFocusMode(Context.SCHOOL);
        assertIncompleteCount(1);
        mainViewModel.activateFocusMode(Context.ERRANDS);
    }

    //Focus sorting testing.
    @Test
    public void MS2_US3() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addGoal("Errand", Context.ERRANDS);
        mainViewModel.addGoal("Work", Context.WORK);
        mainViewModel.addGoal("School", Context.SCHOOL);
        mainViewModel.addGoal("Home", Context.HOME);
        //Alternative: Create expected list and compare elements
        List<Goal> output = mainViewModel.getGoalsToDisplay().getValue();
        assertEquals(output.get(0).context(), Context.HOME);
        assertEquals(output.get(1).context(), Context.WORK);
        assertEquals(output.get(2).context(), Context.SCHOOL);
        assertEquals(output.get(3).context(), Context.ERRANDS);
    }

    @Test
    public void MS2_US4Scenario1() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.activateTomorrowView();

        mainViewModel.addGoal("ASDF", Context.HOME);

        assertIncompleteCount(1);

        mainViewModel.activateTodayView();

        assertIncompleteCount(0);

        mainViewModel.activateRecurringView();

        assertIncompleteCount(0);

        mainViewModel.activatePendingView();

        assertIncompleteCount(0);
    }

    @Test
    public void MS2_US4Scenario2() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.activateTomorrowView();

        mainViewModel.addGoal("ASDF", Context.HOME);
        mainViewModel.pressGoal(1);

        assertIncompleteCount(0);
        assertCompleteCount(1);

        mainViewModel.activateTodayView();

        assertIncompleteCount(0);
        assertCompleteCount(0);

        // Advance real time by 24 hours
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH);

        mainViewModel.activateTomorrowView();

        assertIncompleteCount(0);
        assertCompleteCount(0);

        mainViewModel.activateTodayView();

        assertIncompleteCount(0);
        assertCompleteCount(1);
    }

    @Test
    public void MS2_US4Scenario3() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.addRecurringGoalDateless("ASDF", RecurrenceType.WEEKLY, Context.HOME);

        // Complete recurring goal instance today
        // Generated goal has ID 2; we can hardcode here because of the way we generate goals
        mainViewModel.pressGoal(2);

        // Advance the real time by 5 days; next recurring goal instance should be 2 days after
        // So not visible in today or tomorrow view

        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 5);

        assertCompleteCount(0);
        assertIncompleteCount(0);

        mainViewModel.activateTomorrowView();

        assertCompleteCount(0);
        assertIncompleteCount(0);

        // Advance the real time by another day
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 6);

        mainViewModel.activateTomorrowView();

        assertCompleteCount(0);
        assertIncompleteCount(1);
    }


    @Test
    public void MS2_US6Scenario1() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2024, 1, 7, RecurrenceType.DAILY, Context.HOME);
        mainViewModel.addRecurringGoal("Weekly Goal", 2024, 1, 7, RecurrenceType.WEEKLY, Context.HOME);

        // checking if the goals show up on the recurring tab
        mainViewModel.activateRecurringView();
        assertIncompleteCount(2);

        mainViewModel.activateTodayView();

        //advance a week and check the today, tomorrow, and recurring tabs
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 7);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(2);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(1);

        mainViewModel.activateTodayView();
        assertIncompleteCount(2);


    }

    @Test
    public void MS2_US6Scenario2() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Weekly Goal", 2024, 1, 7, RecurrenceType.WEEKLY, Context.HOME);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);

        // advance to 3/4/24 (First Wednesday of the Month)
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 28);

        mainViewModel.activateTodayView();
        assertIncompleteCount(1);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);
    }

    @Test
    public void MS2_US6Scenario3() {
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Yearly Goal", 2024, 1, 7, RecurrenceType.YEARLY, Context.HOME);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);

        // advance a year (takes the leap year into account)
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH * 366);

        mainViewModel.activateTodayView();
        assertIncompleteCount(1);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);
    }

    @Test
    public void MS2_US6Scenario5() {
        // create a goal for 2 days from now
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2024, 1, 9, RecurrenceType.DAILY, Context.HOME);

        mainViewModel.activateTodayView();
        assertIncompleteCount(0);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(0);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);

        //advance a day and check tmr view
        dateTicker.setValue(TimeUtils.START_TIME + TimeUtils.DAY_LENGTH);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(1);
    }

    @Test
    public void MS2_US6Scenario6() {
        // Delete Recurring Goal

        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2024, 1, 9, RecurrenceType.DAILY, Context.HOME);
        MutableSubject<Goal> goal =
                goalRepository.goals.stream().filter(g -> g.getValue().content().equals("Daily Goal")).findFirst().orElse(null);
        assertNotNull(goal);
        assertEquals(goal.getValue().content(), "Daily Goal");

        mainViewModel.activateTodayView();
        assertIncompleteCount(0);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(0);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);

        mainViewModel.deleteRecurringGoal(goal.getValue().id());


        mainViewModel.activateTodayView();
        assertIncompleteCount(0);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(0);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(0);
    }

    @Test
    public void MS2_US6Scenario7() {
        // Don't duplicate incomplete goals
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2024, 1, 9, RecurrenceType.DAILY, Context.HOME);
        MutableSubject<Goal> goal =
                goalRepository.goals.stream().filter(g -> g.getValue().content().equals("Daily Goal")).findFirst().orElse(null);
        assertNotNull(goal);
        assertEquals(goal.getValue().content(), "Daily Goal");

        mainViewModel.advance24Hours();
        mainViewModel.advance24Hours();
        mainViewModel.advance24Hours();


        mainViewModel.activateTodayView();
        assertIncompleteCount(1);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(1);

        mainViewModel.activateRecurringView();
        assertIncompleteCount(1);
    }


    @Test
    public void MS2_US6Scenario8() {
        // Reject Past Dates

        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2023, 1, 9, RecurrenceType.DAILY, Context.HOME);
        MutableSubject<Goal> goal =
                goalRepository.goals.stream().filter(g -> g.getValue().content().equals("Daily Goal")).findFirst().orElse(null);
        assertNull(goal);
    }


    @Test
    public void MS2_US6Scenario9() {
        // Leap year repeating goal

        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Yearly Goal", 2024, 1, 29,
                RecurrenceType.YEARLY, Context.HOME);


        for (int i = 0; i < 22; i++) {
            mainViewModel.advance24Hours();
        }


        Goal goal =
                mainViewModel.getGoalsToDisplay().getValue().stream().filter(g -> g.content().equals(
                        "Yearly Goal")).findFirst().orElse(null);
        assertNotNull(goal);


        mainViewModel.activateTodayView();
        mainViewModel.pressGoal(goal.id());
        assertIncompleteCount(0);

        for (int i = 0; i < 365; i++) {
            mainViewModel.advance24Hours();
        }

        assertIncompleteCount(0);


        mainViewModel.advance24Hours();
        assertIncompleteCount(1);
    }


    @Test
    public void MS2_US6Scenario10() {
        // multiple instances of day recurring
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);
        mainViewModel.addRecurringGoal("Daily Goal", 2024, 1, 9, RecurrenceType.DAILY, Context.HOME);

        for (int i = 0; i < 22; i++) {
            mainViewModel.advance24Hours();
        }

        mainViewModel.activateTodayView();
        assertIncompleteCount(1);

        mainViewModel.activateTomorrowView();
        assertIncompleteCount(1);

        Goal goal =
                mainViewModel.getGoalsToDisplay().getValue().stream().filter(g -> g.content().equals(
                        "Daily Goal")).findFirst().orElse(null);
        assertNotNull(goal);

        mainViewModel.pressGoal(goal.id());
        assertIncompleteCount(1);

        mainViewModel.activateTodayView();
        goal =
                mainViewModel.getGoalsToDisplay().getValue().stream().filter(g -> g.content().equals(
                        "Daily Goal")).findFirst().orElse(null);
        assertNotNull(goal);
        mainViewModel.pressGoal(goal.id());
        assertIncompleteCount(0);

        mainViewModel.activateTomorrowView();
        goal =
                mainViewModel.getGoalsToDisplay().getValue().stream().filter(g -> g.content().equals(
                        "Daily Goal")).findFirst().orElse(null);
        assertNotNull(goal);
        mainViewModel.pressGoal(goal.id());
        assertIncompleteCount(0);

    }

    @Test
    public void MS2_US8Scenario1() {
        //Title: Moving goal to today
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.addPendingGoal("Build car", Context.ERRANDS);
        mainViewModel.activatePendingView();

        //Tap and do nothing
        //Tap and hold logic
        //Select today, move pending goal to today logic
        //assertIncompleteCount(1); //assert goal is not completed
        //assert the goal's pending is now false
        mainViewModel.activateTodayView();
        //assert the goal is on Today view

    }

    @Test
    public void MS2_US8Scenario2() {
        //Title:  Moving goal to tomorrow
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.addPendingGoal("Build furniture", Context.HOME);
        mainViewModel.activatePendingView();

        //Tap and do nothing
        //Tap and hold logic
        //Select tomorrow, move pending goal to tomorrow logic
        //assertIncompleteCount(1); //assert goal is not completed
        //assert the goal's pending is now false
        mainViewModel.activateTomorrowView();
        //assert the goal is on tomorrow view

    }

    @Test
    public void MS2_US8Scenario3() {
        //Title:   Finish pending goal
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.addPendingGoal("Build car", Context.ERRANDS);
        mainViewModel.activatePendingView();

        //Tap and do nothing
        //Tap and hold logic
        //Select finish, move pending goal to today logic
        //assertCompleteCount(1); //assert goal is completed
        //assert the goal's pending is now false
        mainViewModel.activateTomorrowView();
        //assert the goal is on today view


    }

    @Test
    public void MS2_US8Scenario4() {
        //Title: Delete pending goal
        goalRepository = MockGoalRepository.createWithEmptyGoals();
        mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

        mainViewModel.addPendingGoal("Build car", Context.ERRANDS);
        mainViewModel.activatePendingView();

        //Tap and do nothing
        //Tap and hold logic
        //Select delete logic
        //assert there are no goals

    }


}