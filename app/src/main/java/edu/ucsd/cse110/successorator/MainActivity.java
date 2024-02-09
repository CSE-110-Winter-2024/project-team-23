package edu.ucsd.cse110.successorator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private MainViewModel activityModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var view = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        view.placeholderText.setText(R.string.hello_world);


        var modelFactory = ViewModelProvider.Factory.from(MainViewModel.initializer);
        var modelProvider = new ViewModelProvider(this, modelFactory);
        this.activityModel = modelProvider.get(MainViewModel.class);

        activityModel.getDateOffset().observe(offset -> {
            if (offset == null) return;
            view.dateDebugText.setText(offset.now().toString());
        });

        view.dateAdvanceButton.setOnClickListener(v -> activityModel.advance24Hours());

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable updateDateTask = new Runnable() {
            @Override
            public void run() {
                view.dateDebugText.setText(activityModel.getDateOffset().getValue().now().toString());
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(updateDateTask);

        setContentView(view.getRoot());
    }
}
