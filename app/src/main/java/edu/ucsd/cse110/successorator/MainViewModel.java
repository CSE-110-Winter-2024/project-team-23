package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import edu.ucsd.cse110.successorator.lib.domain.Goal;

import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;

public class MainViewModel extends ViewModel {
    private final GoalRepository goalRepository;

    public static final ViewModelInitializer<MainViewModel> initializer =
            new ViewModelInitializer<>(
                    MainViewModel.class,
                    creationExtras -> {
                        var app =
                                (SuccessoratorApplication) creationExtras.get(APPLICATION_KEY);
                        assert app != null;
                        return new MainViewModel(app.getGoalRepository());
                    }
            );

    public MainViewModel(GoalRepository goalRepository) {
        this.goalRepository = goalRepository();
    }

    public void tapToComplete(int id) {
        Goal goal = goalRepository.find(id).getValue();
        goal.toggleCompleted();
        goalRepository.update(goal);
    }


}
