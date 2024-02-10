package edu.ucsd.cse110.successorator.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import edu.ucsd.cse110.successorator.lib.domain.Goal;

@Entity(tableName = "goals")
public class GoalEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Integer id = null;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "sortOrder")
    public int sortOrder;

    @ColumnInfo(name = "completed")
    public boolean completed;

    @ColumnInfo(name = "completionDate")
    public long completionDate;

    public GoalEntity(@NonNull String content, int sortOrder,
                      boolean completed, long  completionDate) {
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completionDate = completionDate;
    }

    public static GoalEntity fromGoal(Goal goal) {
        GoalEntity goalEntity = new GoalEntity(goal.content(), goal.sortOrder(),
                goal.completed(), goal.completionDate().getTime());
        goalEntity.id = goal.id();
        return goalEntity;
    }

    public Goal toGoal() {
        return new Goal(id, content, sortOrder, completed, new Date(completionDate));
    }
}
