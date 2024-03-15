package edu.ucsd.cse110.successorator.lib.domain.sorting;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

// TODO: actually implement this and unit test it when you write task 2.1
public class DefaultGoalSorter implements IGoalSorter {
    public int compare(Goal a, Goal b) {
        // Completed goals are sorted relative to sort order and go on the bottom
        // Incomplete goals are sorted relative to Context, then sort order, and are on the top
        if (a.completed() != b.completed()) {
            return Boolean.compare(a.completed(), b.completed());
        } else if (a.context().ordinal() != b.context().ordinal()) {
            return Integer.compare(a.context().ordinal(), b.context().ordinal());
        } else {
            return Integer.compare(a.sortOrder(), b.sortOrder());
        }
    }
}
