package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Goal {
    private final @Nullable Integer sortOrder;
    private final @NonNull String content;
    private boolean completed;

    //Constructors
    public Goal (@Nullable Integer sortOrder, @NonNull String content) {
        this.sortOrder = sortOrder;
        this.content = content;
        this.completed = false;
    }

    //Functionality
    public void toggleCompleted() {
        this.completed = !completed;
    }


    //Accessor Methods
    public @Nullable Integer id() {
        return sortOrder;
    }

    public @NonNull String content() {
        return content;
    }

    public boolean completed() {
        return completed;
    }
}
