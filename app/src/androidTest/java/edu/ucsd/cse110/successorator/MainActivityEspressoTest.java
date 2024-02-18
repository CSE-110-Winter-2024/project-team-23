package edu.ucsd.cse110.successorator;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void NewGoalButtonWorks() {
        onView(withId(R.id.add_goal_menu)).perform(click());
        onView(withId(R.id.goal_input)).check(matches(isDisplayed()));
    }

    @Test
    public void CanAddNewGoal() {
        onView(withId(R.id.add_goal_menu)).perform(click());
        onView(withId(R.id.goal_input)).perform(typeText("meow"));
        onView(withText("Checkmark")).perform(click());
        onView(withText("meow")).check(matches(isDisplayed()));
    }
}
