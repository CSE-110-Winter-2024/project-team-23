package edu.ucsd.cse110.successorator.lib.domain.filtering;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

// TODO: depends on task 6.1/6.4 stuff, so not testing rn
public class TodayGoalFilter implements IGoalFilter {
    @Override
    public boolean shouldShow(Goal goal, Calendar now) {
        // If pending do not show
        if(goal.pending()) return false;
        // Since now is localized we can use that instead of dateConverter
        var completionTime = TimeUtils.localize(goal.completionDate(), now);
        var startTime = TimeUtils.localize(goal.startDate(), now);
        if(goal.recurring()) {
            return TimeUtils.shouldShowRecurring(goal.recurrenceType(), completionTime, startTime, now);
        }
        if (goal.completed()) {
            return TimeUtils.shouldShowCompleteGoal(completionTime, now, startTime);
        }
        return TimeUtils.shouldShowIncompleteGoal(startTime, now);
    }
}
