package edu.ucsd.cse110.successorator.lib.domain;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Goal {
    private final @Nullable Integer sortOrder;
    private final @NotNull String content;
    private boolean completed;

    //Constructors
    public Goal (Integer sortOrder, String content) {
        this.sortOrder = sortOrder;
        this.content = content;
        this.completed = false;
    }

    //Functionality
    public void swapCompleted() {
        this.completed = !(completed);
    }


    //Accessor Methods
    public @androidx.annotation.Nullable Integer id() {
        return sortOrder;
    }

    public @NonNull String content() {
        return content;
    }

    public boolean completed() {
        return completed;
    }



}
