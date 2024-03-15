package edu.ucsd.cse110.successorator.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;

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

    @ColumnInfo(name = "pending")
    public boolean pending;

    @ColumnInfo(name = "recurring")
    public boolean recurring;

    @ColumnInfo(name = "recurrenceType")
    public RecurrenceType recurrenceType;

    @ColumnInfo(name = "context")
    public Context context;

    @ColumnInfo(name = "startDate")
    public long startDate;

    @ColumnInfo(name = "prevId")
    public Integer prevId;

    @ColumnInfo(name = "nextId")
    public Integer nextId;

    @ColumnInfo(name = "recurrenceId")
    public Integer recurrenceId;

    @ColumnInfo(name = "recurringGenerated")
    public boolean recurringGenerated;

    public GoalEntity(@NonNull String content, int sortOrder,
                      boolean completed, long  completionDate, long startDate, boolean pending, boolean recurring, RecurrenceType recurrenceType, Context context, Integer prevId, Integer nextId, Integer recurrenceId, boolean recurringGenerated) {
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completionDate = completionDate;
        this.pending = pending;
        this.recurring = recurring;
        this.recurrenceType = recurrenceType;
        this.context = context;
        this.startDate = startDate;
        this.prevId = prevId;
        this.nextId = nextId;
        this.recurrenceId = recurrenceId;
        this.recurringGenerated = recurringGenerated;
    }

    public static GoalEntity fromGoal(Goal goal) {
        GoalEntity goalEntity = new GoalEntity(goal.content(), goal.sortOrder(),
                goal.completed(), goal.completionDate(), goal.startDate(), goal.pending(), goal.recurring() , goal.recurrenceType(), goal.context(), goal.prevId(), goal.nextId(), goal.recurrenceId(), goal.recurringGenerated());
        goalEntity.id = goal.id();
        return goalEntity;
    }

    public Goal toGoal() {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurring, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }
}
