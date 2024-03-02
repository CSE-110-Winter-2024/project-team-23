package edu.ucsd.cse110.successorator;



import static edu.ucsd.cse110.successorator.lib.domain.AppMode.PENDING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.RECURRING;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TODAY;
import static edu.ucsd.cse110.successorator.lib.domain.AppMode.TOMORROW;

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
    private String dateString;
    private AppMode appMode = TODAY;

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

        //Initialize Title
        dateString = mainViewModel.getCurrentDateString().getValue();
        updateTitle();

        setContentView(view.getRoot());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_right_bar, menu);

        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_media_ff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mainViewModel.getCurrentDateString().observe(str -> {
            if (str == null) return;
            dateString = str;
            updateTitle();
            //getSupportActionBar().setTitle(str);
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
    public void updateTitle(){
        if(appMode.equals(TODAY)){
            getSupportActionBar().setTitle("Today " + dateString);
        } else if(appMode.equals(TOMORROW)){
            getSupportActionBar().setTitle("Tomorrow " + dateString);
        } else if(appMode.equals(PENDING)){
            getSupportActionBar().setTitle("Pending");
        } else {
            getSupportActionBar().setTitle("Recurring");
        }
    }
}
