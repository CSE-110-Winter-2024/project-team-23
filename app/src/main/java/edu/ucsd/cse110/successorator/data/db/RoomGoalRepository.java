package edu.ucsd.cse110.successorator.data.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.Subject;
import edu.ucsd.cse110.successorator.util.LiveDataSubjectAdapter;

public class RoomGoalRepository implements GoalRepository {
    private final GoalDao goalDao;

    public RoomGoalRepository(GoalDao goalDao) {
        this.goalDao = goalDao;
    }

    @Override
    public Subject<Goal> find(int id) {
        LiveData<Goal> goal = Transformations.map(goalDao.findAsLiveData(id),
                GoalEntity::toGoal);
        return new LiveDataSubjectAdapter<>(goal);
    }

    @Override
    public Goal findGoal(int id) {
        return goalDao.find(id).toGoal();
    }

    @Override
    public Subject<List<Goal>> findAll() {
        LiveData<List<Goal>> goals =
                Transformations.map(goalDao.findAllAsLiveData(), entities -> {
                    return entities.stream()
                            .map(GoalEntity::toGoal)
                            .collect(Collectors.toList());
                });
        return new LiveDataSubjectAdapter<>(goals);
    }

    @Override
    public void update(Goal goal) {
        goalDao.insert(GoalEntity.fromGoal(goal));
    }

    @Override
    public int prepend(Goal goal) {
        return goalDao.prepend(GoalEntity.fromGoal(goal));
    }

    @Override
    public int append(Goal goal) {
        return goalDao.append(GoalEntity.fromGoal(goal));
    }

    @Override
    public void remove(int id) {
        goalDao.delete(id);
    }
}
