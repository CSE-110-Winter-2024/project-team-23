package edu.ucsd.cse110.successorator.lib.domain.sorting;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

// TODO: actually implement this and unit test it when you write task 7.2
public class PendingGoalSorter implements IGoalSorter {
    @Override
    public int compare(Goal a, Goal b) {
        if (a.context().ordinal() > b.context().ordinal()) {
            return 1;
        } else if (a.context().ordinal() < b.context().ordinal()) {
            return -1;
        } else {
            if (a.sortOrder() > b.sortOrder()) {
                return 1;
            } else if (a.sortOrder() < b.sortOrder()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
