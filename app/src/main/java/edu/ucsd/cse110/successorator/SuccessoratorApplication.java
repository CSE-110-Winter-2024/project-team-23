package edu.ucsd.cse110.successorator;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.room.Room;

import edu.ucsd.cse110.successorator.data.db.RoomGoalRepository;
import edu.ucsd.cse110.successorator.data.db.SuccessoratorDatabase;
import edu.ucsd.cse110.successorator.lib.domain.DateOffset;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;

public class SuccessoratorApplication extends Application {
    private GoalRepository goalRepository;
    private MutableSubject<DateOffset> dateOffset;
    // Ticks every second; useful for propagating time updates to the app
    private MutableSubject<Object> dateTicker;

    @Override
    public void onCreate() {
        super.onCreate();

        var database = Room.databaseBuilder(
                getApplicationContext(),
                SuccessoratorDatabase.class,
                "successorator-database"
        )
                .allowMainThreadQueries()
                .build();

        this.goalRepository = new RoomGoalRepository(database.goalDao());

        var dateOffset = new DateOffset(0);
        this.dateOffset = new SimpleSubject<>();
        this.dateOffset.setValue(dateOffset);

        this.dateTicker = new SimpleSubject<>();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable updateDateTask = new Runnable() {
            @Override
            public void run() {
                dateTicker.setValue(null);
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(updateDateTask);
    }

    public GoalRepository getGoalRepository() {
        return goalRepository;
    }

    public MutableSubject<DateOffset> getDateOffset() {
        return dateOffset;
    }

    public MutableSubject<Object> getDateTicker() {
        return dateTicker;
    }
}
