package edu.ucsd.cse110.successorator.lib.domain;

import java.util.List;

import edu.ucsd.cse110.successorator.lib.util.Subject;

public interface GoalRepository {
    Subject<Goal> find(int id);
    Goal findGoal(int id);

    Subject<List<Goal>> findAll();

    List<Goal> findAllRaw();

    void update(Goal goal);

    int prepend(Goal goal);

    int append(Goal goal);

    void remove(int id);
}
