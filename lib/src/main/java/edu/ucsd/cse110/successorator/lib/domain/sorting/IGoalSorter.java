package edu.ucsd.cse110.successorator.lib.domain.sorting;

import java.util.Comparator;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

public interface IGoalSorter extends Comparator<Goal> {
    public int compare(Goal a, Goal b);
}
