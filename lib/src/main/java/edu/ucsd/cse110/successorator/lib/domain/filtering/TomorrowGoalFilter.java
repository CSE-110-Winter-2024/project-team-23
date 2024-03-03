package edu.ucsd.cse110.successorator.lib.domain.filtering;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

// TODO: actually implement this and unit test it when you write task 4.1/6.5
public class TomorrowGoalFilter implements IGoalFilter {
    @Override
    public boolean shouldShow(Goal goal, Calendar now) {
        // If pending do not show
        if(goal.pending()) return false;
        // Since now is localized we can use that instead of dateConverter
        var completionTime = TimeUtils.localize(goal.completionDate(), now);
        var startTime = TimeUtils.localize(goal.startDate(), now);
        // Now is "today" not tomorrow. If we assume the goal got completed today, and
        // shouldShowRecurring says it should show up tomorrow, then it should show up
        var tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if(goal.recurring()) {
            return TimeUtils.shouldShowRecurring(goal.recurrenceType(), now, startTime, tomorrow);
        }
        // Since we assume the goal got completed "today", tomorrow we don't show it if the start
        // time is before "tomorrow"

        // That being said, if a goal is completed we know the completion time checks kill it
        if (goal.completed()) {
            return TimeUtils.shouldShowCompleteGoal(completionTime, tomorrow, startTime);
        }

        return TimeUtils.shouldShowIncompleteGoalTomorrow(startTime, tomorrow);
    }
}
