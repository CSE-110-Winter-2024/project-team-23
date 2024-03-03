package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Goal {
    private final @Nullable Integer id;
    private final @NonNull String content;
    private final int sortOrder;
    private final boolean completed;
    private final boolean pending;
    private final boolean recurring;
    private final @NonNull Long completionDate;
    private final @NonNull Long startDate;
    private final RecurrenceType recurrenceType;
    private final Context context;


    //Constructors
    public Goal(@Nullable Integer id, @NonNull String content, int sortOrder,
                boolean completed, long completionDate, boolean pending, boolean recurring, RecurrenceType recurrenceType, Context goalContext, Long startDate) {
        this.id = id;
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completionDate = completionDate;
        this.pending = pending;
        this.recurring = recurring;
        this.recurrenceType = recurrenceType;
        this.context = goalContext;
        this.startDate = startDate;
    }

    public Goal markIncomplete() {
        // Keeping old completion date is fine; it will be overwritten
        // We assume method won't be called if goal is already incomplete
        return new Goal(id, content, sortOrder, false, completionDate, pending, recurring, recurrenceType, context, startDate);
    }

    public Goal markComplete(@NonNull Long completionDate) {
        return new Goal(id, content, sortOrder, true, completionDate, pending, recurring, recurrenceType, context, startDate);
    }

    public Goal withSortOrder(int sortOrder) {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurring, recurrenceType, context, startDate);
    }



    //Accessor Methods
    public Integer id() {
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

    public @NonNull Long completionDate() {
        return completionDate;
    }

    public boolean pending() {
        return pending;
    }

    public boolean recurring() {
        return recurring;
    }

    public RecurrenceType recurrenceType() {
        return recurrenceType;
    }

    public Context context() {
        return context;
    }

    public Long startDate() {
        return startDate;
    }
}
