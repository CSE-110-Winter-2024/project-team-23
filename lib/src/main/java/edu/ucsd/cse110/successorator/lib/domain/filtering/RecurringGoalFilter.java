package edu.ucsd.cse110.successorator.lib.domain.filtering;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

// TODO: actually implement this and unit test it when you write task 6.1
public class RecurringGoalFilter implements IGoalFilter {
    @Override
    public boolean shouldShow(Goal goal, Calendar localizedTime) {
        return goal.recurring();
    }
}
