package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class Goal {
    private final @Nullable Integer id;
    private final @NonNull String content;
    private final int sortOrder;
    private boolean completed;
    private Date completionDate;

    //Constructors
    public Goal(@Nullable Integer id, @NonNull String content, int sortOrder,
                boolean completed, Date completionDate) {
        this.id = id;
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
    }

    public Goal markIncomplete() {
        return new Goal(id, content, sortOrder, !completed, null);
    }

    public Goal markComplete(Date completionDate) {
        return new Goal(id, content, sortOrder, true, completionDate);
    }

    public Goal withSortOrder(int sortOrder) {
        return new Goal(id, content, sortOrder, completed, null);
    }


    //Accessor Methods
    public @Nullable Integer id() {
        return id;
    }

    public @NonNull String content() {
        return content;
    }

    public int sortOrder() {
        return sortOrder;
    }

    public boolean completed() {
        return completed;
    }
}
