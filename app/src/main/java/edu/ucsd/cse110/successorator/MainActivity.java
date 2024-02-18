package edu.ucsd.cse110.successorator;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;
import edu.ucsd.cse110.successorator.databinding.TutorialTextBinding;
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
        mainViewModel.getIncompleteGoals().observe(goals -> {
            if (goals == null) return;
            listAdapter.clear();
            listAdapter.addAll(new ArrayList<>(goals));
            var complete = mainViewModel.getCompleteGoalsToDisplay().getValue();
            if (complete == null) {
                return;
            }
            listAdapter.addAll(complete);
            listAdapter.notifyDataSetChanged();
        });
        mainViewModel.getCompleteGoalsToDisplay().observe(goals -> {
            if (goals == null) return;
            var incomplete = mainViewModel.getIncompleteGoals().getValue();
            if (incomplete == null) return;
            listAdapter.clear();
            listAdapter.addAll((incomplete));
            listAdapter.addAll((goals));

            listAdapter.notifyDataSetChanged();
        });

        // Set tutorial text
        TutorialTextBinding tutorialTextBinding =
                TutorialTextBinding.inflate(getLayoutInflater(), view.getRoot(), true);
        this.view.goalList.setEmptyView(tutorialTextBinding.getRoot());

        //Create list display.
        this.view.goalList.setAdapter(listAdapter);


//        ListView listView = (ListView) findViewById(R.id.goal_list);
//        listView.setEmptyView(findViewById(R.id.empty));

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
        getMenuInflater().inflate(R.menu.add_bar, menu);

        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_media_ff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mainViewModel.getCurrentDateString().observe(str -> {
            if (str == null) return;
            getSupportActionBar().setTitle(str);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        var itemId = item.getItemId();

        if (itemId == R.id.add_goal_menu) {
            //probably refactor into its own method later
            var dialogFragment = CreateGoalDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "CreateGoalDialogFragment");
        } else if (itemId == android.R.id.home) {
            this.mainViewModel.advance24Hours();
        }
        return super.onOptionsItemSelected(item);
    }

    //Necessary for testing.
    public GoalListAdapter getListAdapter() {
        return listAdapter;
    }
}
