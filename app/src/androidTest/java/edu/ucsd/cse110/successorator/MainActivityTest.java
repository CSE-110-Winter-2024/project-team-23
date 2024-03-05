package edu.ucsd.cse110.successorator;

import static android.system.Os.close;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import static junit.framework.TestCase.assertEquals;

import static edu.ucsd.cse110.successorator.lib.domain.AppMode.PENDING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.RECURRING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TOMORROW;

import android.content.res.Resources;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Transformations;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.MockGoalRepository;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;
import edu.ucsd.cse110.successorator.ui.GoalListAdapter;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Test
    public void listDisplaysDatabase() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            var goalRepository = MockGoalRepository.createWithDefaultGoals();
            var dateOffset = new SimpleSubject<Long>();
            dateOffset.setValue(0L);
            var dateTicker = new SimpleSubject<Long>();
            dateTicker.setValue(TimeUtils.START_TIME);
            var localizedCalendar = Calendar.getInstance(TimeUtils.GMT);
            var mainViewModel = new MainViewModel(goalRepository, dateOffset, dateTicker, localizedCalendar);

            // Observe the scenario's lifecycle to wait until the activity is created.
            scenario.onActivity(activity -> {
                var adapter = activity.getListAdapter();
                adapter.clear();
                var subject = mainViewModel.getGoalsToDisplay();
                var goalList = subject.getValue();
                adapter.clear();
                adapter.addAll(goalList);

                //3 incomplete goals.
                for(int i = 0; i < 3; i++) {
                    var expected = String.format("Goal %d", i + 1);
                    var goal = adapter.getItem(i);
                    var actual = goal.content();
                    assertEquals(expected, actual);
                }
            });

            // Simulate moving to the started state (above will then be called).
            scenario.moveToState(Lifecycle.State.STARTED);
        }
    }

    @Test
    public void titleUpdates() {
        try (var scenario = ActivityScenario.launch(MainActivity.class)) {
            // Observe the scenario's lifecycle to wait until the activity is created.
            scenario.onActivity(activity -> {
                MainViewModel mainViewModel = activity.getMainViewModel();
                var expected = "Today, " + mainViewModel.getCurrentDateString().getValue();
                var actual = activity.getSupportActionBar().getTitle();
                assertEquals(expected, actual);
                activity.setAppMode(TOMORROW);
                activity.updateTitle();
                expected = "Tomorrow, " + mainViewModel.getTomorrowDateString();
                actual = activity.getSupportActionBar().getTitle();
                assertEquals(expected, actual);
                activity.setAppMode(PENDING);
                activity.updateTitle();
                expected = "Pending";
                actual = activity.getSupportActionBar().getTitle();
                assertEquals(expected, actual);
                activity.setAppMode(RECURRING);
                activity.updateTitle();
                expected = "Recurring";
                actual = activity.getSupportActionBar().getTitle();
                assertEquals(expected, actual);
            });

            // Simulate moving to the started state (above will then be called).
            scenario.moveToState(Lifecycle.State.STARTED);
        }
    }
}