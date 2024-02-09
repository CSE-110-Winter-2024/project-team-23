package edu.ucsd.cse110.successorator.lib.domain;

import java.util.List;

import edu.ucsd.cse110.successorator.lib.util.Subject;

public interface GoalRepository {
    Subject<Goal> find(int id);

    Subject<List<Goal>> findAll();

    void update(Goal goal);

    void prepend(Goal goal);

    void append(Goal goal);

    void remove(int id);
}
