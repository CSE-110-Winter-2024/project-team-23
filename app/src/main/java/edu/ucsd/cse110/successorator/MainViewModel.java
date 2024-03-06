package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import static edu.ucsd.cse110.successorator.lib.domain.AppMode.PENDING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TODAY;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TOMORROW;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.lib.domain.AppMode;
import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;
import edu.ucsd.cse110.successorator.lib.domain.filtering.IGoalFilter;
import edu.ucsd.cse110.successorator.lib.domain.filtering.PendingGoalFilter;
import edu.ucsd.cse110.successorator.lib.domain.filtering.RecurringGoalFilter;
import edu.ucsd.cse110.successorator.lib.domain.filtering.TodayGoalFilter;
import edu.ucsd.cse110.successorator.lib.domain.filtering.TomorrowGoalFilter;
import edu.ucsd.cse110.successorator.lib.domain.sorting.DefaultGoalSorter;
import edu.ucsd.cse110.successorator.lib.domain.sorting.IGoalSorter;
import edu.ucsd.cse110.successorator.lib.domain.sorting.PendingGoalSorter;
import edu.ucsd.cse110.successorator.lib.domain.sorting.RecurringGoalSorter;
import edu.ucsd.cse110.successorator.lib.util.GoalUtils;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;
import edu.ucsd.cse110.successorator.lib.util.Subject;
import edu.ucsd.cse110.successorator.lib.util.TimeUtils;

public class MainViewModel extends ViewModel {
    private final GoalRepository goalRepository;
    private final MutableSubject<List<Goal>> orderedGoals;
    private final MutableSubject<List<Goal>> goalsContextFiltered;
    private final MutableSubject<List<Goal>> goalsToDisplay;
    private final MutableSubject<IGoalSorter> goalSorter;
    private final MutableSubject<IGoalFilter> goalFilter;
    private final MutableSubject<Long> dateOffset;
    private final MutableSubject<Long> currentRealDate;
    private final MutableSubject<Long> currentDate;
    private final MutableSubject<Calendar> currentDateLocalized;
    private final MutableSubject<String> currentTitleString;
    private final MutableSubject<Context> currentContext;
    private final MutableSubject<AppMode> currentMode;
    private final Calendar dateConverter;

    public static final ViewModelInitializer<MainViewModel> initializer = new ViewModelInitializer<>(MainViewModel.class, creationExtras -> {
        var app = (SuccessoratorApplication) creationExtras.get(APPLICATION_KEY);
        assert app != null;
        return new MainViewModel(app.getGoalRepository(), app.getDateOffset(), app.getCurrentRealTime(), Calendar.getInstance());
    });

    /**
     * @param goalRepository  storage for goals
     * @param offset          offset for the date as configured by the user1
     * @param currentRealDate how the app knows what time it is
     * @param dateConverter   This is specifically used to convert Dates to normal representations (e.g. for display, so we know when 2am is). This is because time zones are hard. During testing we'll pass in a Calendar with a fixed time zone, because we don't want tests flaking depending on where the actions runner is located. We can't use a mock calendar class here becuase we need Calendar to do time zone handling for us; implementing time zones ourselves is inadvisable.
     */
    public MainViewModel(GoalRepository goalRepository, MutableSubject<Long> offset, MutableSubject<Long> currentRealDate, Calendar dateConverter) {
        // How dates work in this app:
        // dateTicker is a subject that emits the current time every second
        // this is how the app actually knows what time it is
        // dateOffset is a subject that emits the offset from the current time
        // this is the offset that should be user-configurable
        // from these two we derive currentDate, which is the time that the app should use for all its calculations
        // currentDateLocalized is the localized version of currentDate, either using the timezone of the device or GMT during tests
        // currentDateString is the string representation of currentDateLocalized, this should be modified for US2
        this.goalRepository = goalRepository;
        this.dateOffset = offset;
        this.currentRealDate = currentRealDate;
        this.dateConverter = dateConverter;

        this.currentDate = new SimpleSubject<>();
        this.currentDateLocalized = new SimpleSubject<>();
        this.currentTitleString = new SimpleSubject<>();

        this.currentMode = new SimpleSubject<>();
        this.currentMode.setValue(AppMode.TODAY);

        this.currentContext = new SimpleSubject<>();
        this.currentContext.setValue(Context.NONE);

        this.dateOffset.observe(offsetValue -> {
            if (offsetValue == null) return;
            Long currentTime = this.currentRealDate.getValue();
            if (currentTime == null) return;
            this.currentDate.setValue(currentTime + offsetValue);
        });
        this.currentRealDate.observe(date -> {
            if (date == null) return;
            Long dateOffset = this.dateOffset.getValue();
            if (dateOffset == null) return;
            this.currentDate.setValue(date + dateOffset);
        });
        this.currentDate.observe(currentTime -> {
            if (currentTime == null) return;
            this.currentDateLocalized.setValue(TimeUtils.localize(currentTime, dateConverter));
        });
        this.currentDateLocalized.observe(localized -> {
            if (localized == null) return;
            var appMode = this.currentMode.getValue();
            if (appMode == null) return;
            //can freely change display date
            this.currentTitleString.setValue(TimeUtils.generateTitleString(dateConverter, localized, appMode));
        });
        this.currentMode.observe(appMode -> {
            if (appMode == null) return;
            var localized = this.currentDateLocalized.getValue();
            if (localized == null) return;
            this.currentTitleString.setValue(TimeUtils.generateTitleString(dateConverter, localized, appMode));
        });

        this.orderedGoals = new SimpleSubject<>();
        this.goalsContextFiltered = new SimpleSubject<>();
        this.goalsToDisplay = new SimpleSubject<>();

        this.goalSorter = new SimpleSubject<>();
        this.goalFilter = new SimpleSubject<>();

        this.goalSorter.setValue(new DefaultGoalSorter());
        this.goalFilter.setValue(new TodayGoalFilter());

        this.goalRepository.findAll().observe(goals -> {
            if (goals == null) return;
            var context = this.currentContext.getValue();
            if (context == null) return;
            // Order the goals
            var goalsToDisplay = goals.stream().filter(goal -> GoalUtils.shouldShowContext(goal, context)).collect(Collectors.toList());

            this.goalsContextFiltered.setValue(goalsToDisplay);
        });

        this.currentContext.observe(context -> {
            if (context == null) return;
            var goals = this.orderedGoals.getValue();
            if (goals == null) return;
            // Filter the goals on context
            var goalsToDisplay = goals.stream().filter(goal -> GoalUtils.shouldShowContext(goal, context)).collect(Collectors.toList());

            this.goalsContextFiltered.setValue(goalsToDisplay);
        });

        this.goalsContextFiltered.observe(goals -> {
            if (goals == null) return;
            var sorter = this.goalSorter.getValue();
            if (sorter == null) return;
            var sortedGoals = goals.stream().sorted(sorter).collect(Collectors.toList());
            this.orderedGoals.setValue(sortedGoals);
        });

        this.goalSorter.observe(sorter -> {
            if (sorter == null) return;
            var goals = this.goalsContextFiltered.getValue();
            if (goals == null) return;
            var sortedGoals = goals.stream().sorted(sorter).collect(Collectors.toList());
            this.orderedGoals.setValue(sortedGoals);
        });

        this.orderedGoals.observe(goals -> {
            if (goals == null) return;
            var filter = this.goalFilter.getValue();
            if (filter == null) return;
            var nowLocalized = currentDateLocalized.getValue();
            var filteredGoals = goals.stream().filter(goal -> filter.shouldShow(goal, nowLocalized)).collect(Collectors.toList());
            this.goalsToDisplay.setValue(filteredGoals);
        });

        this.goalFilter.observe(filter -> {
            if (filter == null) return;
            var goals = this.orderedGoals.getValue();
            if (goals == null) return;
            var nowLocalized = currentDateLocalized.getValue();
            if (nowLocalized == null) return;
            var filteredGoals = goals.stream().filter(goal -> filter.shouldShow(goal, nowLocalized)).collect(Collectors.toList());
            this.goalsToDisplay.setValue(filteredGoals);
        });

        this.currentDateLocalized.observe(nowLocalized -> {
            if (nowLocalized == null) return;
            var goals = this.orderedGoals.getValue();
            if (goals == null) return;
            var filter = this.goalFilter.getValue();
            if (filter == null) return;
            var filteredGoals = goals.stream().filter(goal -> filter.shouldShow(goal, nowLocalized)).collect(Collectors.toList());
            this.goalsToDisplay.setValue(filteredGoals);
        });
    }



    public void advance24Hours() {
        Long offset = dateOffset.getValue();
        if (offset == null) {
            return;
        }

        offset += 24 * 60 * 60 * 1000;
        dateOffset.setValue(offset);
    }

    public Subject<String> getCurrentTitleString() {
        return currentTitleString;
    }

    public void pressGoal(int goalId) {
        var goal = goalRepository.findGoal(goalId);
        if (goal == null) {
            return;
        }
        if (goal.completed()) {
            var newGoal = goal.markIncomplete();
            goalRepository.update(newGoal);
        } else {

            var currentDate = this.currentDate.getValue();
            if (currentDate == null) return;
            var newGoal = goal.markComplete(currentDate);
            goalRepository.update(newGoal);
        }
    }

    // Open question whether this method should take a Context or a String
    // TODO: test these methods in the PRs where they actually start being used
    public void activateFocusMode(Context context) {
        this.currentContext.setValue(context);
    }

    public void deactivateFocusMode() {
        this.currentContext.setValue(Context.NONE);
    }

    public void activateTodayView() {
        this.goalSorter.setValue(new DefaultGoalSorter());
        this.goalFilter.setValue(new TodayGoalFilter());
        this.currentMode.setValue(TODAY);
    }

    public void activateTomorrowView() {
        this.goalSorter.setValue(new DefaultGoalSorter());
        this.goalFilter.setValue(new TomorrowGoalFilter());
        this.currentMode.setValue(TOMORROW);
    }

    public void activatePendingView() {
        this.goalSorter.setValue(new PendingGoalSorter());
        this.goalFilter.setValue(new PendingGoalFilter());
        this.currentMode.setValue(PENDING);
    }

    public void activateRecurringView() {
        this.goalSorter.setValue(new RecurringGoalSorter());
        this.goalFilter.setValue(new RecurringGoalFilter());
        this.currentMode.setValue(AppMode.RECURRING);
    }

    public Subject<List<Goal>> getGoalsToDisplay() {
        return goalsToDisplay;
    }


    public void addGoal(String contents) {
        // We could use a proper value for the completion date, but we don't really care about it
        // At the same time, I don't want to deal with nulls, so I'll just use the current time
        var currentTime = this.currentDate.getValue();
        if (currentTime == null) return;
        var newGoal = new Goal(null, contents, 0, false, currentTime, false, false, RecurrenceType.NONE, Context.HOME, currentTime);
        goalRepository.append(newGoal);
    }
}