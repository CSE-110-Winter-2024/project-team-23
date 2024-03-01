package edu.ucsd.cse110.successorator.lib.domain.filtering;

import java.util.Calendar;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

public interface IGoalFilter {
    public boolean shouldShow(Goal goal, Calendar localizedTime);
}
