package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Goal {
    private final @Nullable Integer id;
    private final @NonNull String content;
    private final int sortOrder;
    private final boolean completed;
    private final boolean pending;
    private final boolean recurringGenerator;
    private final boolean recurringGenerated;
    private final @NonNull Long completionDate;
    private final @NonNull Long startDate;
    private final RecurrenceType recurrenceType;
    private final Context context;
    // Required to prevent duplication on active tasks
    private final @Nullable Integer prevId;
    private final @Nullable Integer nextId;
    // Recurrence this is referencing
    private final @Nullable Integer recurrenceId;


    //Constructors
    public Goal(@Nullable Integer id, @NonNull String content, int sortOrder,
                boolean completed, long completionDate, boolean pending, boolean recurring, RecurrenceType recurrenceType, Context goalContext, @NonNull Long startDate, @Nullable Integer prevId, @Nullable Integer nextId, @Nullable Integer recurrenceId, boolean reccuringGenerated) {
        this.id = id;
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completionDate = completionDate;
        this.pending = pending;
        this.recurringGenerator = recurring;
        this.recurrenceType = recurrenceType;
        this.context = goalContext;
        this.startDate = startDate;
        this.prevId = prevId;
        this.nextId = nextId;
        this.recurrenceId = recurrenceId;
        this.recurringGenerated = reccuringGenerated;
    }

    public Goal markIncomplete() {
        // Keeping old completion date is fine; it will be overwritten
        // We assume method won't be called if goal is already incomplete
        return new Goal(id, content, sortOrder, false, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }

    public Goal markComplete(@NonNull Long completionDate) {
        return new Goal(id, content, sortOrder, true, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }

    public Goal withSortOrder(int sortOrder) {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }

    public Goal withNextId(@Nullable Integer nextId) {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }

    public Goal withPrevId(@Nullable Integer prevId) {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
    }

    public Goal withRecurrenceId(@Nullable Integer recurrenceId) {
        return new Goal(id, content, sortOrder, completed, completionDate, pending, recurringGenerator, recurrenceType, context, startDate, prevId, nextId, recurrenceId, recurringGenerated);
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
        return recurringGenerator;
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

    public @Nullable Integer prevId() {
        return prevId;
    }

    public @Nullable Integer nextId() {
        return nextId;
    }

    public @Nullable Integer recurrenceId() {
        return recurrenceId;
    }

    public boolean recurringGenerated() {
        return recurringGenerated;
    }
}
