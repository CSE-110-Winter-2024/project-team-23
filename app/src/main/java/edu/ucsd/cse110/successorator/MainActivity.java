package edu.ucsd.cse110.successorator;



import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;
import edu.ucsd.cse110.successorator.databinding.TutorialTextBinding;
import edu.ucsd.cse110.successorator.lib.domain.AppMode;
import edu.ucsd.cse110.successorator.ui.CreateGoalDialogFragment;
import edu.ucsd.cse110.successorator.ui.GoalListAdapter;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private ActivityMainBinding view;
    private GoalListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inflate View
        this.view = ActivityMainBinding.inflate(getLayoutInflater(), null, false);

        //Initialize viewModel
        var modelFactory = ViewModelProvider.Factory.from(MainViewModel.initializer);
        var modelProvider = new ViewModelProvider(this, modelFactory);
        this.mainViewModel = modelProvider.get(MainViewModel.class);

        //Initialize list adapter
        this.listAdapter = new GoalListAdapter(this.getApplicationContext(), List.of(), mainViewModel);
        mainViewModel.getGoalsToDisplay().observe(goals -> {
            if (goals == null) return;
            listAdapter.clear();
            listAdapter.addAll(new ArrayList<>(goals));
            listAdapter.notifyDataSetChanged();
        });

        // Set tutorial text
        TutorialTextBinding tutorialTextBinding =
                TutorialTextBinding.inflate(getLayoutInflater(), view.getRoot(), true);
        this.view.goalList.setEmptyView(tutorialTextBinding.getRoot());

        //Create list display.
        this.view.goalList.setAdapter(listAdapter);

        view.goalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // Reference for clicking on List View:
            // https://anna-scott.medium.com/clickable-listview-items-with-clickable-buttons-e52fa6030d36
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mainViewModel.pressGoal(Math.toIntExact(id));
            }
        });

        setContentView(view.getRoot());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_right_bar, menu);

        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_media_ff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mainViewModel.getCurrentTitleString().observe(str -> {
            if (str == null) return;
            getSupportActionBar().setTitle(str);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        var itemId = item.getItemId();

        //Home screen menu items
        if (itemId == R.id.add_goal_menu) {
            //probably refactor into its own method later
            var dialogFragment = CreateGoalDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "CreateGoalDialogFragment");
        }
        else if (itemId == R.id.change_view_menu) {
            View anchor = this.findViewById(R.id.change_view_menu);
            PopupMenu viewMenu = new PopupMenu(this, anchor);
            viewMenu.getMenuInflater().inflate(R.menu.view_popup, viewMenu.getMenu());
            viewMenu.setOnMenuItemClickListener(i -> onMenuItemClick(i));
            viewMenu.show();
        } else if (itemId == android.R.id.home) {
            this.mainViewModel.advance24Hours();
        }
        return super.onOptionsItemSelected(item);
    }

    //Necessary for testing.
    public GoalListAdapter getListAdapter() {
        return listAdapter;
    }

    public boolean onMenuItemClick(MenuItem item){
        var itemId = item.getItemId();
        boolean clicked = true;
        if (itemId == R.id.today_popup) {
            this.mainViewModel.activateTodayView();
        } else if (itemId == R.id.tomorrow_popup){
            this.mainViewModel.activateTomorrowView();
        } else if (itemId == R.id.pending_popup){
            this.mainViewModel.activatePendingView();
        } else if (itemId == R.id.recurring_popup){
            this.mainViewModel.activateRecurringView();
        } else {
            clicked = false;
        }
        return clicked;
    }
}
