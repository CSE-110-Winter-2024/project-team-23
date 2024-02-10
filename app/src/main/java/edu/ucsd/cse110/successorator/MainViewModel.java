package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.util.Calendar;
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
    private final MutableSubject<Calendar> currentDateLocalized;

    public static final ViewModelInitializer<MainViewModel> initializer = new ViewModelInitializer<>(MainViewModel.class, creationExtras -> {
        var app = (SuccessoratorApplication) creationExtras.get(APPLICATION_KEY);
        assert app != null;
        return new MainViewModel(app.getGoalRepository(), app.getDateOffset(), app.getDateTicker(), Calendar.getInstance());
    });

    /**
     * @param goalRepository storage for goals
     * @param offset         offset for the date as configured by the user1
     * @param dateTicker     used to communicate time updates to the app
     * @param dateConverter  This is specifically used to convert Dates to normal representations (e.g. for display, so we know when 2am is). This is because time zones are hard. During testing we'll pass in a Calendar with a fixed time zone, because we don't want tests flaking depending on where the actions runner is located. We can't use a mock calendar class here becuase we need Calendar to do time zone handling for us; implementing time zones ourselves is inadvisable.
     */
    public MainViewModel(GoalRepository goalRepository, MutableSubject<DateOffset> offset, MutableSubject<Object> dateTicker, Calendar dateConverter) {
        this.goalRepository = goalRepository;
        this.dateOffset = offset;
        this.dateTicker = dateTicker;

        this.currentDate = new SimpleSubject<>();
        this.currentDate.setValue(dateOffset.getValue().now());
        this.dateOffset.observe(newOffset -> currentDate.setValue(newOffset.now()));
        this.dateTicker.observe(tick -> currentDate.setValue(dateOffset.getValue().now()));
        this.currentDateLocalized = new SimpleSubject<>();
        this.currentDate.observe(date -> {
            if (date == null) return;
            currentDateLocalized.setValue(localize(date, dateConverter));
        });

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
            var nowLocalized = localize(dateOffset.getValue().now(), dateConverter);
            var nowDay = nowLocalized.get(Calendar.DAY_OF_YEAR);
            var nowHour = nowLocalized.get(Calendar.HOUR_OF_DAY);
            // Filter the goals
            var completeGoalsToDisplay = completedGoals.stream().filter(goal -> {
                // Display goals that were completed after 2am today if it's after 2am
                // Otherwise, display goals completed after 2am yesterday
                var goalLocalized = localize(goal.completionDate(), dateConverter);
                var goalDay = goalLocalized.get(Calendar.DAY_OF_YEAR);
                var goalHour = goalLocalized.get(Calendar.HOUR_OF_DAY);
                return shouldShowGoal(nowDay, nowHour, goalDay, goalHour);
            }).collect(Collectors.toList());

            this.completeGoalsToDisplay.setValue(completeGoalsToDisplay);
        });
    }

    /**
     * Some weirdness here: I think it should show goals created in the future, but I'm not sure.
     */
    private static boolean shouldShowGoal(int nowDay, int nowHour, int goalDay, int goalHour) {
        if (nowHour >= 2) {
            return goalDay == nowDay && goalHour >= 2 || goalDay >= nowDay + 1;
        } else {
            return (goalDay == nowDay - 1 && goalHour >= 2) || goalDay >= nowDay;
        }
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

    public Subject<Calendar> getCurrentDateLocalized() {
        return currentDateLocalized;
    }

    public void pressGoal(int goalId) {
        var goalSubject = this.goalRepository.find(goalId);
        var goal = goalSubject.getValue();
        if (goal == null) return;
        if (goal.completed()) {
            goalRepository.update(goal.markIncomplete());
        } else {
            var offset = dateOffset.getValue();
            if (offset == null) return;
            // Alternatively can use currentTime, but I view that more of a way to propagate events
            // as opposed to a good single source of truth for time, since the Handler could get
            // paused or something, and I want the app to not fully fail in that case
            goalRepository.update(goal.markComplete(offset.now()));
        }
    }

    public Subject<List<Goal>> getIncompleteGoals() {
        return incompleteGoals;
    }

    public Subject<List<Goal>> getCompleteGoalsToDisplay() {
        return completeGoalsToDisplay;
    }

    public void addGoal(String contents) {
        // We could use a proper value for the completion date, but we don't really care about it
        // At the same time, I don't want to deal with nulls, so I'll just use the current time
        var newGoal = new Goal(null, contents, 0, false, new java.util.Date());
        goalRepository.append(newGoal);
    }

    private static Calendar localize(Date date, Calendar dateConverter) {
        if (date == null) return null;
        Calendar localized = (Calendar) dateConverter.clone();
        localized.setTime(date);
        return localized;
    }
}
