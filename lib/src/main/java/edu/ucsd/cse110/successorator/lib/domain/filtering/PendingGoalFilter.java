package edu.ucsd.cse110.successorator.lib.domain.filtering;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

// TODO: actually implement this and unit test it when you write task 7.2
public class PendingGoalFilter implements IGoalFilter {
    @Override
    public boolean shouldShow(Goal goal, Calendar localizedTime) {
        return goal.pending();
    }
}
