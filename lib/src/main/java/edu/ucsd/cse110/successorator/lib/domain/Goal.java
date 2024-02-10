package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class Goal {
    private final @Nullable Integer id;
    private final @NonNull String content;
    private final int sortOrder;
    private final boolean completed;
    private final @NonNull Date completionDate;

    //Constructors
    public Goal(@Nullable Integer id, @NonNull String content, int sortOrder,
                boolean completed, @NonNull Date completionDate) {
        this.id = id;
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completionDate = completionDate;
    }

    public Goal markIncomplete() {
        // Keeping old completion date is fine; it will be overwritten
        // We assume method won't be called if goal is already incomplete
        return new Goal(id, content, sortOrder, !completed, completionDate);
    }

    public Goal markComplete(@NonNull Date completionDate) {
        // We assume method won't be called if goal is already complete
        return new Goal(id, content, sortOrder, true, completionDate);
    }

    public Goal withSortOrder(int sortOrder) {
        return new Goal(id, content, sortOrder, completed, completionDate);
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

    public @NonNull Date completionDate() {
        return completionDate;
    }
}
