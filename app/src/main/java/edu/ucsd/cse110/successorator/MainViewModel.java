package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import edu.ucsd.cse110.successorator.lib.domain.DateOffset;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;

public class MainViewModel extends ViewModel {
    private final GoalRepository goalRepository;
    private final MutableSubject<DateOffset> dateOffset;

    public static final ViewModelInitializer<MainViewModel> initializer =
            new ViewModelInitializer<>(
                    MainViewModel.class,
                    creationExtras -> {
                        var app =
                                (SuccessoratorApplication) creationExtras.get(APPLICATION_KEY);
                        assert app != null;
                        return new MainViewModel(app.getGoalRepository(), app.getDateOffset());
                    }
            );

    public MainViewModel(GoalRepository goalRepository, MutableSubject<DateOffset> offset) {
        this.goalRepository = goalRepository;
        this.dateOffset = offset;
    }

    public Subject<DateOffset> getDateOffset() {
        return dateOffset;
    }

    public void advance24Hours() {
        DateOffset offset = dateOffset.getValue();
        if (offset == null) {
            return;
        }

        offset = offset.add(24 * 60 * 60);
        dateOffset.setValue(offset);
    }
}
