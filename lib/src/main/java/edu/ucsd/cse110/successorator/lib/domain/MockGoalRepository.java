package edu.ucsd.cse110.successorator.lib.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

public class MockGoalRepository implements GoalRepository {
    private final List<MutableSubject<Goal>> goals;
    private final MutableSubject<List<Goal>> goalSubject;

    public final static List<Goal> DEFAULT_GOALS = List.of(
            new Goal(1, "Goal 1", 1, false, 0),
            new Goal(2, "Goal 2", 2, false, 0),
            new Goal(3, "Goal 3", 3, false, 0),
            new Goal(4, "Goal 4; should not be visible now and in 24", 4, true, TimeUtils.START_TIME - TimeUtils.HOUR_LENGTH * 15),
            new Goal(5, "Goal 5; should be visible now but not in 24", 5, true, TimeUtils.START_TIME - TimeUtils.HOUR_LENGTH * 13),
            new Goal(6, "Goal 6; should be visible now but not in 24", 6, true, TimeUtils.START_TIME),
            new Goal(7, "Goal 7; should be visible now but not in 24", 7, true, TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 9),
            new Goal(8, "Goal 8; should be visible now and in 24", 8, true, TimeUtils.START_TIME + TimeUtils.HOUR_LENGTH * 11)
    );

    public static MockGoalRepository createWithDefaultGoals() {
        return new MockGoalRepository(DEFAULT_GOALS);
    }

    public MockGoalRepository(List<Goal> goals) {
    this.goals = new ArrayList<>();
        for (Goal goal : goals) {
            var subject = new SimpleSubject<Goal>();
            subject.setValue(goal);
            this.goals.add(subject);
        }
        goalSubject = new SimpleSubject<>();
        goalSubject.setValue(goals);
    }

    @Override
    public Subject<Goal> find(int id) {
        for (var goal : goals) {
            if (goal.getValue().id() == id) {
                return goal;
            }
        }
        // We assume that the goal exists, but if it doesn't we return a subject with a null value.
        // This subject will NOT get updated when the goal is updated.
        return new SimpleSubject<>();
    }

    @Override
    public Subject<List<Goal>> findAll() {
        return goalSubject;
    }

    @Override
    public void update(Goal goal) {
        for (var subject : goals) {
            if (subject.getValue().id() == goal.id()) {
                subject.setValue(goal);
                goalSubject.setValue(goals.stream().map(Subject::getValue).collect(Collectors.toList()));
                return;
            }
        }
    }

    @Override
    public void prepend(Goal goal) {
        var subject = new SimpleSubject<Goal>();
        // Get min sort order and a not taken ID
        int minSortOrder = goals.stream().map(Subject::getValue).mapToInt(Goal::sortOrder).min().orElse(0);
        int id = goals.stream().map(Subject::getValue).mapToInt(Goal::id).max().orElse(0) + 1;
        goal = new Goal(id, goal.content(), minSortOrder - 1, goal.completed(), goal.completionDate());
        subject.setValue(goal);
        goals.add(subject);
        goalSubject.setValue(goals.stream().map(Subject::getValue).collect(Collectors.toList()));
    }

    @Override
    public void append(Goal goal) {
        var subject = new SimpleSubject<Goal>();
        // Get max sort order and a not taken ID
        int maxSortOrder = goals.stream().map(Subject::getValue).mapToInt(Goal::sortOrder).max().orElse(0);
        int id = goals.stream().map(Subject::getValue).mapToInt(Goal::id).max().orElse(0) + 1;
        goal = new Goal(id, goal.content(), maxSortOrder + 1, goal.completed(), goal.completionDate());
        subject.setValue(goal);
        goals.add(subject);
        goalSubject.setValue(goals.stream().map(Subject::getValue).collect(Collectors.toList()));
    }

    @Override
    public void remove(int id) {
        goals.removeIf(subject -> subject.getValue().id() == id);
        goalSubject.setValue(goals.stream().map(Subject::getValue).collect(Collectors.toList()));
    }
}
