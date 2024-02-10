package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.util.Date;

import edu.ucsd.cse110.successorator.lib.domain.DateOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;

public class MainViewModel extends ViewModel {
    private final GoalRepository goalRepository;
    private final MutableSubject<List<Goal>> orderedGoals;
    private final MutableSubject<List<Goal>> completedGoals;
    private final MutableSubject<List<Goal>> completeGoalsToDisplay;
    private final MutableSubject<List<Goal>> incompleteGoals;
    private final MutableSubject<DateOffset> dateOffset;
    private final MutableSubject<Object> dateTicker;
    private final MutableSubject<Date> currentDate;

    public static final ViewModelInitializer<MainViewModel> initializer =
            new ViewModelInitializer<>(
                    MainViewModel.class,
                    creationExtras -> {
                        var app =
                                (SuccessoratorApplication) creationExtras.get(APPLICATION_KEY);
                        assert app != null;
                        return new MainViewModel(app.getGoalRepository(), app.getDateOffset(), app.getDateTicker());
                    }
            );

    public MainViewModel(GoalRepository goalRepository, MutableSubject<DateOffset> offset, MutableSubject<Object> dateTicker) {
        this.goalRepository = goalRepository;
        this.dateOffset = offset;
        this.dateTicker = dateTicker;

        this.currentDate = new SimpleSubject<>();
        this.currentDate.setValue(dateOffset.getValue().now());
        this.dateOffset.observe(newOffset -> currentDate.setValue(newOffset.now()));
        this.dateTicker.observe(tick -> currentDate.setValue(dateOffset.getValue().now()));

        this.orderedGoals = new SimpleSubject<>();
        this.completedGoals = new SimpleSubject<>();
        this.incompleteGoals = new SimpleSubject<>();
        this.completeGoalsToDisplay = new SimpleSubject<>();

        this.goalRepository.findAll().observe(goals -> {
            if (goals == null) return;
            // Order the goals
            var orderedGoals = goals.stream().sorted(Comparator.comparingInt(Goal::sortOrder)).collect(Collectors.toList());

            this.orderedGoals.setValue(orderedGoals);
        });

        this.orderedGoals.observe(goals -> {
            if (goals == null) return;
            // Filter the goals
            var completedGoals = goals.stream().filter(Goal::completed).collect(Collectors.toList());
            var incompleteGoals = goals.stream().filter(goal -> !goal.completed()).collect(Collectors.toList());

            this.completedGoals.setValue(completedGoals);
            this.incompleteGoals.setValue(incompleteGoals);
        });

        this.completedGoals.observe(completedGoals -> {
            if (completedGoals == null) return;
            // Filter the goals
            // We use a trivial filter, but anticipate using Date in the future
            var completeGoalsToDisplay = completedGoals.stream().filter(goal -> true).collect(Collectors.toList());

            this.completeGoalsToDisplay.setValue(completeGoalsToDisplay);
        });
    }

    public void advance24Hours() {
        DateOffset offset = dateOffset.getValue();
        if (offset == null) {
            return;
        }

        offset = offset.addDay();
        dateOffset.setValue(offset);
    }

    public Subject<Date> getCurrentDate() {
        return currentDate;
    }

    public void pressGoal(int goalId) {
        var goalSubject = this.goalRepository.find(goalId);
        var goal = goalSubject.getValue();
        if (goal == null) return;
        if (goal.completed()) {
            goalRepository.update(goal.markIncomplete());
        } else {
            // TODO: use mock date. Can't test date handling (completedGoalsToDisplay) until that's there
            goalRepository.update(goal.markComplete(new java.util.Date()));
        }
    }

    public Subject<List<Goal>> getIncompleteGoals() {
        return incompleteGoals;
    }

    // TODO: test this; depends on mock date
    public Subject<List<Goal>> getCompleteGoalsToDisplay() {
        return completeGoalsToDisplay;
    }

    public void addGoal(String contents) {
        var newGoal = new Goal(null, contents, 0, false, new java.util.Date());
        goalRepository.append(newGoal);
    }
}
