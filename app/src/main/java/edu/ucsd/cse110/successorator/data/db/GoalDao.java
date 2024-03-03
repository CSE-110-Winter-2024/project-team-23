package edu.ucsd.cse110.successorator.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GoalEntity goal);

    @Query("SELECT * FROM goals WHERE id = :id")
    GoalEntity find(int id);

    @Query("SELECT * FROM goals ORDER BY sortOrder")
    List<GoalEntity> findAll();

    @Query("SELECT * FROM goals WHERE id = :id")
    LiveData<GoalEntity> findAsLiveData(int id);

    @Query("SELECT * FROM goals ORDER BY sortOrder")
    LiveData<List<GoalEntity>> findAllAsLiveData();

    @Query("SELECT COUNT(*) FROM goals")
    int count();

    @Query("SELECT MIN(sortOrder) FROM goals")
    int getMinSortOrder();

    @Query("SELECT MAX(sortOrder) FROM goals")
    int getMaxSortOrder();

    @Query("UPDATE goals SET sortOrder = sortOrder + :by WHERE sortOrder >= " +
            ":from AND sortOrder <= :to")
    void shiftSortOrder(int from, int to, int by);

    @Transaction
    default int append(GoalEntity goal) {
        int maxSortOrder = getMaxSortOrder();
        GoalEntity newGoal = new GoalEntity(goal.content, maxSortOrder + 1,
                false, goal.completionDate, goal.startDate, goal.pending, goal.recurring, goal.recurrenceType, goal.context);
        return Math.toIntExact(insert(newGoal));
    }

    @Transaction
    default int prepend(GoalEntity goal) {
        shiftSortOrder(getMinSortOrder(), getMaxSortOrder(), 1);
        GoalEntity newGoal = new GoalEntity(goal.content, getMinSortOrder() - 1,
                false, goal.completionDate, goal.startDate, goal.pending, goal.recurring, goal.recurrenceType, goal.context);
        return Math.toIntExact(insert(newGoal));
    }

    @Query("DELETE FROM goals WHERE id = :id")
    void delete(int id);
}
