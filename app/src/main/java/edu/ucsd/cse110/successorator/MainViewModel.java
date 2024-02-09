package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.util.Date;

import edu.ucsd.cse110.successorator.lib.domain.DateOffset;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;

public class MainViewModel extends ViewModel {
    private final GoalRepository goalRepository;
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
}
