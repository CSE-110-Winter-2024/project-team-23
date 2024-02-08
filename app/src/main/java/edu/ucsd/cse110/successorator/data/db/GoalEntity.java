package edu.ucsd.cse110.successorator.data.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    public boolean completed = false;

    public GoalEntity(@NonNull String content, @NonNull int sortOrder,
                      boolean completed) {
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
    }
}
