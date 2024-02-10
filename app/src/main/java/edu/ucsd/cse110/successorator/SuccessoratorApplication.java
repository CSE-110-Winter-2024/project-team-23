package edu.ucsd.cse110.successorator;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.room.Room;


import java.util.Calendar;

import edu.ucsd.cse110.successorator.data.db.RoomGoalRepository;
import edu.ucsd.cse110.successorator.data.db.SuccessoratorDatabase;
import edu.ucsd.cse110.successorator.lib.domain.GoalRepository;
import edu.ucsd.cse110.successorator.lib.util.MutableSubject;
import edu.ucsd.cse110.successorator.lib.util.SimpleSubject;

public class SuccessoratorApplication extends Application {
    private GoalRepository goalRepository;
    private MutableSubject<Long> dateOffset;
    // Ticks every second; useful for propagating time updates to the app
    private MutableSubject<Long> dateTicker;

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

        Long dateOffset = 0L;
        this.dateOffset = new SimpleSubject<Long>();
        this.dateOffset.setValue(dateOffset);

        this.dateTicker = new SimpleSubject<Long>();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable updateDateTask = new Runnable() {
            @Override
            public void run() {
                dateTicker.setValue(System.currentTimeMillis());
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(updateDateTask);
    }

    public GoalRepository getGoalRepository() {
        return goalRepository;
    }

    public MutableSubject<Long> getDateOffset() {
        return dateOffset;
    }

    public MutableSubject<Long> getDateTicker() {
        return dateTicker;
    }
}
