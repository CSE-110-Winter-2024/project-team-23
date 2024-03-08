package edu.ucsd.cse110.successorator;


import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import static edu.ucsd.cse110.successorator.lib.domain.AppMode.PENDING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TODAY;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TOMORROW;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.HashMap;
import java.util.HashSet;
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
    private final MutableSubject<List<Goal>> goalOutput;
    private final MutableSubject<List<Goal>> goalsToDisplay;
    private final MutableSubject<IGoalSorter> goalSorter;
    private final MutableSubject<IGoalFilter> goalFilter;
    private final MutableSubject<Long> dateOffset;
    private final MutableSubject<Long> currentRealDate;
    private final MutableSubject<Long> currentDate;
    private final MutableSubject<Calendar> currentDateLocalized;
    private final MutableSubject<String> currentTitleString;
    private final MutableSubject<String> currentWeekday;
    private final MutableSubject<String> currentNumberedWeekday;
    private final MutableSubject<String> currentDateString;
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
        this.currentWeekday = new SimpleSubject<>();
        this.currentNumberedWeekday = new SimpleSubject<>();
        this.currentDateString = new SimpleSubject<>();
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
            updateCalendarStrings(appMode, localized);
        });
        this.currentDateLocalized.observe(localized -> {
            if (localized == null) return;
            var goals = this.goalRepository.findAll().getValue();
            if (goals == null) return;
            for (var goal : goals) {
                if (goal.recurring()) {
                    handleRecurringGoalGeneration(goal, localized);
                }
            }
        });
        this.currentMode.observe(appMode -> {
            if (appMode == null) return;
            var localized = this.currentDateLocalized.getValue();
            if (localized == null) return;
            updateCalendarStrings(appMode, localized);
        });

        this.orderedGoals = new SimpleSubject<>();
        this.goalsContextFiltered = new SimpleSubject<>();
        this.goalOutput = new SimpleSubject<>();
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
            this.goalOutput.setValue(filteredGoals);
        });

        this.goalFilter.observe(filter -> {
            if (filter == null) return;
            var goals = this.orderedGoals.getValue();
            if (goals == null) return;
            var nowLocalized = currentDateLocalized.getValue();
            if (nowLocalized == null) return;
            var filteredGoals = goals.stream().filter(goal -> filter.shouldShow(goal, nowLocalized)).collect(Collectors.toList());
            this.goalOutput.setValue(filteredGoals);
        });

        this.currentDateLocalized.observe(nowLocalized -> {
            if (nowLocalized == null) return;
            var goals = this.orderedGoals.getValue();
            if (goals == null) return;
            var filter = this.goalFilter.getValue();
            if (filter == null) return;
            var filteredGoals = goals.stream().filter(goal -> filter.shouldShow(goal, nowLocalized)).collect(Collectors.toList());
            this.goalOutput.setValue(filteredGoals);
        });

        this.goalOutput.observe(goals -> {
            if (goals == null) return;

            // Any goals with the same recurrenceId as another goal being displayed at the same time should be removed
            // We don't need to worry about goal completion status because it's impossible that a complete goal and an incomplete goal with the same recurrenceId are displayed at the same time
            // Because the recurrence generator doesn't generate multiple goals on the same day, and complete goals otherwise would roll over.
            HashMap<Integer, Goal> recurrenceIds = new HashMap<>();
            for (var goal : goals) {
                if (goal.recurringGenerated()) {
                    if (recurrenceIds.containsKey(goal.recurrenceId())) {
                        // Remove goal with earlier start date
                        if (goal.startDate() > recurrenceIds.get(goal.recurrenceId()).startDate()) {
                            recurrenceIds.put(goal.recurrenceId(), goal);
                        }
                        continue;
                    }
                    recurrenceIds.put(goal.recurrenceId(), goal);
                }
            }
            var filteredGoals = goals.stream().filter(goal -> {
                if (goal.recurringGenerated()) {
                    if (recurrenceIds.get(goal.recurrenceId()).id() != goal.id()) {
                        this.goalRepository.remove(goal.id());
                        return false;
                    };
                }
                return true;
            }).collect(Collectors.toList());
            goalsToDisplay.setValue(filteredGoals);
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

    // Returns true or false depending on success
    // Should return false for invalid presses, or invalid goalId
    public boolean pressGoal(int goalId) {
        var goal = goalRepository.findGoal(goalId);
        if (goal == null) {
            return false;
        }
        var appMode = this.currentMode.getValue();
        if (appMode == null) {
            return false;
        }
        if (appMode.equals(PENDING) || appMode.equals(AppMode.RECURRING)) {
            return false;
        }
        if (appMode.equals(TOMORROW) && goal.recurringGenerated()) {
            // If previous goal exists and is incomplete, we should fail
            var prevId = goal.prevId();
            if (prevId != null) {
                var prevGoal = goalRepository.findGoal(prevId);
                if (prevGoal != null && !prevGoal.completed()) {
                    return false;
                }
            }
        }
        else if (appMode.equals(TODAY) && goal.recurringGenerated()) {
            // If next goal exists and is complete, we should fail
            // This case isn't specified explicitly in clarifications, but it follows from the
            // statement that there can only be one "active" recurring instance at a time
            var nextId = goal.nextId();
            if (nextId != null) {
                var nextGoal = goalRepository.findGoal(nextId);
                if (nextGoal != null && nextGoal.completed()) {
                    return false;
                }
            }
        }
        if (goal.completed()) {
            var newGoal = goal.markIncomplete();
            goalRepository.update(newGoal);
        } else {
            var currentDate = this.currentDateLocalized.getValue();
            if (currentDate == null) return false;
            currentDate = (Calendar) currentDate.clone();
            if (appMode.equals(TOMORROW)) {
                currentDate.add(Calendar.DAY_OF_YEAR, 1);
            }
            var newGoal = goal.markComplete(currentDate.getTimeInMillis());
            goalRepository.update(newGoal);
        }
        return true;
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

    // Exporting these strings for UI
    public Subject<String> getCurrentWeekday() {
        return currentWeekday;
    }

    public Subject<String> getCurrentNumberedWeekday() {
        return currentNumberedWeekday;
    }

    public Subject<String> getCurrentDateString() {
        return currentDateString;
    }

    // All testing for this method is accomplished via title strings and above functions
    private void updateCalendarStrings(AppMode appMode, Calendar localized) {
        var displayDate = (Calendar) localized.clone();
        if (displayDate.get(Calendar.HOUR_OF_DAY) < 2) {
            displayDate.add(Calendar.DAY_OF_YEAR, -1);
        }
        if (appMode.equals(TOMORROW)) {
            displayDate.add(Calendar.DAY_OF_YEAR, 1);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("EEE M/d", Locale.US);
        formatter.setCalendar(dateConverter);
        var dateString = formatter.format(displayDate.getTime());
        if (appMode.equals(TODAY)) {
            this.currentTitleString.setValue("Today, " + dateString);
        } else if (appMode.equals(TOMORROW)) {
            this.currentTitleString.setValue("Tomorrow, " + dateString);
        } else if (appMode.equals(PENDING)) {
            this.currentTitleString.setValue("Pending");
        } else {
            this.currentTitleString.setValue("Recurring");
        }

        formatter.applyPattern("EEE");
        var weekday = formatter.format(displayDate.getTime());
        this.currentWeekday.setValue(weekday);

        var weekday_num = displayDate.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        var weekday_string = Integer.toString(weekday_num);
        if (weekday_num == 1) {
            weekday_string += "st";
        } else if (weekday_num == 2) {
            weekday_string += "nd";
        } else if (weekday_num == 3) {
            weekday_string += "rd";
        } else {
            weekday_string += "th";
        }
        this.currentNumberedWeekday.setValue(weekday_string + " " + weekday);

        formatter.applyPattern("M/d");
        dateString = formatter.format(displayDate.getTime());
        this.currentDateString.setValue(dateString);
    }


    public void addGoal(String contents) {
        // We could use a proper value for the completion date, but we don't really care about it
        // At the same time, I don't want to deal with nulls, so I'll just use the current time
        var currentTime = this.currentDate.getValue();
        if (currentTime == null) return;
        var newGoal = new Goal(null, contents, 0, false, currentTime, false, false, RecurrenceType.NONE, Context.HOME, currentTime, null, null, null, false);
        goalRepository.append(newGoal);
    }

    public void addGoal(String contents, Context context) {
        // We could use a proper value for the completion date, but we don't really care about it
        // At the same time, I don't want to deal with nulls, so I'll just use the current time
        var currentTime = this.currentDate.getValue();
        if (currentTime == null) return;
        var newGoal = new Goal(null, contents, 0, false, currentTime, false, false, RecurrenceType.NONE, context, currentTime, null, null, null, false);
        goalRepository.append(newGoal);
    }

    // Returns true if the goal was added, or false if it wasn't (due to the selected date being in the past)
    public boolean addRecurringGoal(String contents, int year, int month, int day, RecurrenceType recurrenceType, Context context) {
        var currentTime = this.currentDateLocalized.getValue();
        if (currentTime == null) return false;
        var selectedDate = (Calendar) currentTime.clone();
        // Set at 12:00 PM to avoid 2am edge cases
        selectedDate.set(year, month, day, 12, 0, 0);
        currentTime = TimeUtils.twoAMNormalized(currentTime);
        if (selectedDate.before(currentTime)) return false;
        var selectedMoment = selectedDate.getTimeInMillis();
        var recurringGoal = new Goal(null, contents, 0, false, selectedMoment, false, true, recurrenceType, context, selectedDate.getTimeInMillis(), null, null, null, false);
        var goalId = goalRepository.append(recurringGoal);
        recurringGoal = goalRepository.findGoal(goalId);
        handleRecurringGoalGeneration(recurringGoal, currentTime);
        return true;
    }

    // Should be called when adding goals from Today or Tomorrow because only the MVM knows what time it is
    // Not valid in Recurring or Pending
    public void addRecurringGoalDateless(String contents, RecurrenceType recurrenceType, Context context) {
        var currentTime = this.currentDateLocalized.getValue();
        if (currentTime == null) return;

        var mode = this.currentMode.getValue();
        if (mode == null) return;
        currentTime = TimeUtils.twoAMNormalized(currentTime);
        var goalCreationTime = (Calendar) currentTime.clone();
        if (mode.equals(TOMORROW)) {
            goalCreationTime.add(Calendar.DAY_OF_YEAR, 1);
            goalCreationTime = TimeUtils.twoAMNormalized(goalCreationTime);
        }
        var recurringGoal = new Goal(null, contents, 0, false, goalCreationTime.getTimeInMillis(), false, true, recurrenceType, context, goalCreationTime.getTimeInMillis(), null, null, null, false);
        var goalId = goalRepository.append(recurringGoal);
        recurringGoal = goalRepository.findGoal(goalId);

        handleRecurringGoalGeneration(recurringGoal, currentTime);
    }

    // Public for testing
    // Only call from a context where it won't recurse into LiveData updates
    public void handleRecurringGoalGeneration(Goal recurringGoal, Calendar currentDate) {
        if (!recurringGoal.recurring() || recurringGoal.recurrenceType().equals(RecurrenceType.NONE)) return;
        // nextId is null, so we need to generate the first goal
        if (recurringGoal.nextId() == null) {
            // Created at start date
            var newGoal = new Goal(null, recurringGoal.content(), 0, false, recurringGoal.startDate(), false, false, recurringGoal.recurrenceType(), recurringGoal.context(), recurringGoal.startDate(), null, null, recurringGoal.id(), true);
            var newId = goalRepository.append(newGoal);
            recurringGoal = recurringGoal.withNextId(newId);
            goalRepository.update(recurringGoal);
        }

        // If the next goal is today or earlier (2am normalized), we need to generate a new goal

        var nextGoal = goalRepository.findGoal(recurringGoal.nextId());
        if (nextGoal == null) return;
        var nextGoalDate = nextGoal.startDate();
        var nextGoalLocalized = TimeUtils.localize(nextGoalDate, dateConverter);
        var nextGoalNormalized = TimeUtils.twoAMNormalized(nextGoalLocalized);
        var nowNormalized = TimeUtils.twoAMNormalized(currentDate);
        var localizedStartDate = TimeUtils.localize(recurringGoal.startDate(), dateConverter);
        var normalizedStartDate = TimeUtils.twoAMNormalized(localizedStartDate);
        if (nextGoalNormalized.before(nowNormalized) || nextGoalNormalized.get(Calendar.DAY_OF_YEAR) == nowNormalized.get(Calendar.DAY_OF_YEAR) && nextGoalNormalized.get(Calendar.YEAR) == nowNormalized.get(Calendar.YEAR)) {
            var startTime = TimeUtils.nextGoalRecurrence(nowNormalized, normalizedStartDate, recurringGoal.recurrenceType()).getTimeInMillis();
            var newGoal = new Goal(null, recurringGoal.content(), 0, false, startTime, false, false, recurringGoal.recurrenceType(), recurringGoal.context(), startTime, nextGoal.id(), null, recurringGoal.id(), true);
            var newId = goalRepository.append(newGoal);
            nextGoal = nextGoal.withPrevId(null);
            nextGoal = nextGoal.withNextId(newId);
            goalRepository.update(nextGoal);
            var prevGoal = recurringGoal.prevId();
            if (prevGoal != null) {
                goalRepository.remove(prevGoal);
            }
            recurringGoal = recurringGoal.withPrevId(nextGoal.id());
            recurringGoal = recurringGoal.withNextId(newId);
            goalRepository.update(recurringGoal);
            nextGoal = goalRepository.findGoal(newId);
        }

        // If the previous goal is before the previous goal recurrence, we nede to generate a new goal
        // This is slightly unintuitive, but it provides an elegant way to handle large timeskips where a goal was completed in Tomorrow
        var prevId = recurringGoal.prevId();
        // If there's no previous goal, we don't need to do anything
        if (prevId == null) return;
        var prevGoal = goalRepository.findGoal(prevId);
        if (prevGoal == null) return;
        var prevGoalDate = prevGoal.startDate();
        var prevGoalLocalized = TimeUtils.localize(prevGoalDate, dateConverter);
        var prevGoalNormalized = TimeUtils.twoAMNormalized(prevGoalLocalized);
        var prevGoalRecurrence = TimeUtils.previousGoalRecurrence(nowNormalized, normalizedStartDate, recurringGoal.recurrenceType());

        // Since we spit out 2am normalized dates its valid to compare them this way
        if (prevGoalNormalized.before(prevGoalRecurrence)) {
            var newGoal = new Goal(null, recurringGoal.content(), 0, false, prevGoalRecurrence.getTimeInMillis(), false, false, recurringGoal.recurrenceType(), recurringGoal.context(), prevGoalRecurrence.getTimeInMillis(), null, nextGoal.id(), recurringGoal.id(), true);
            var newId = goalRepository.append(newGoal);
            nextGoal = nextGoal.withPrevId(newId);
            goalRepository.update(nextGoal);
            recurringGoal = recurringGoal.withPrevId(newId);
            goalRepository.update(recurringGoal);
            goalRepository.remove(prevId);
        }
    }

    public void deleteRecurringGoal(int goalId) {
        // We were explicitly told in clarifications not to delete generated goals
        this.goalRepository.remove(goalId);
    }
}