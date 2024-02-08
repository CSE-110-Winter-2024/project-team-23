package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Goal {
    private final @Nullable Integer id;
    private final @NonNull String content;
    private final int sortOrder;
    private boolean completed;

    //Constructors
    public Goal(@Nullable Integer id, @NonNull String content, int sortOrder,
                boolean completed) {
        this.id = id;
        this.content = content;
        this.sortOrder = sortOrder;
        this.completed = completed;
    }

    //Functionality
    public void toggleCompleted() {
        this.completed = !completed;
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
