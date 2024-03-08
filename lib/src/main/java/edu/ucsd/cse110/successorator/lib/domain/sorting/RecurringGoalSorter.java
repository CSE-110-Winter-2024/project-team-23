package edu.ucsd.cse110.successorator.lib.domain.sorting;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

// TODO: actually implement this and unit test it when you write task 6.1
public class RecurringGoalSorter implements IGoalSorter {
    @Override
    public int compare(Goal a, Goal b) {
        // Compare start dates
        var startA = a.startDate();
        var startB = b.startDate();
        return startA.compareTo(startB);
    }
}
