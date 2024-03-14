package edu.ucsd.cse110.successorator;


import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
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
import edu.ucsd.cse110.successorator.ui.CreateRecurringGoalDialogFragment;
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

        //Registration for press and hold menu
        registerForContextMenu(this.view.goalList);


        view.goalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // Reference for clicking on List View:
            // https://anna-scott.medium.com/clickable-listview-items-with-clickable-buttons-e52fa6030d36
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Pending logic so that click does nothing
                if (mainViewModel.getCurrentMode().getValue() == AppMode.PENDING) {

                } else {
                    mainViewModel.pressGoal(Math.toIntExact(id));
                }
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
            if (mainViewModel.getCurrentMode().getValue() == AppMode.RECURRING) {
                var dialogFragment = CreateRecurringGoalDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), "CreateRecurringGoalDialogFragment");
            } else {
                var dialogFragment = CreateGoalDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), "CreateGoalDialogFragment");
            }
        } else if (itemId == R.id.change_view_menu) {
            View anchor = this.findViewById(R.id.change_view_menu);
            PopupMenu viewMenu = new PopupMenu(this, anchor);
            viewMenu.getMenuInflater().inflate(R.menu.view_popup, viewMenu.getMenu());
            viewMenu.setOnMenuItemClickListener(this::onViewMenuItemClick);
            viewMenu.show();
        } else if (itemId == android.R.id.home) {
            //View anchor = this.findViewById(android.R.id.home);
            View anchor = this.findViewById(R.id.change_view_menu);
            PopupMenu focusMenu = new PopupMenu(this, anchor);
            focusMenu.setGravity(Gravity.BOTTOM);
            focusMenu.getMenuInflater().inflate(R.menu.focus_popup, focusMenu.getMenu());
            focusMenu.setOnMenuItemClickListener(this::onFocusMenuItemClick);
            focusMenu.show();
            //TODO: Reimplement this somewhere else.
            //this.mainViewModel.advance24Hours();
        }
        return super.onOptionsItemSelected(item);
    }

    //Necessary for testing.
    public GoalListAdapter getListAdapter() {
        return listAdapter;
    }

    public boolean onViewMenuItemClick(MenuItem item) {
        var itemId = item.getItemId();
        boolean clicked = true;
        if (itemId == R.id.today_popup) {
            this.mainViewModel.activateTodayView();
        } else if (itemId == R.id.tomorrow_popup) {
            this.mainViewModel.activateTomorrowView();
        } else if (itemId == R.id.pending_popup) {
            this.mainViewModel.activatePendingView();
        } else if (itemId == R.id.recurring_popup) {
            this.mainViewModel.activateRecurringView();
        } else {
            clicked = false;
        }
        return clicked;
    }

    public boolean onFocusMenuItemClick(MenuItem item) {
        var itemId = item.getItemId();
        boolean clicked = true;
        if (itemId == R.id.home_popup) {
            this.mainViewModel.setFocusHome();
        } else if (itemId == R.id.work_popup) {
            this.mainViewModel.setFocusWork();
        } else if (itemId == R.id.school_popup) {
            this.mainViewModel.setFocusSchool();
        } else if (itemId == R.id.errands_popup) {
            this.mainViewModel.setFocusErrands();
        } else if (itemId == R.id.cancel_popup) {
            this.mainViewModel.deactivateFocusMode();
        } else {
            clicked = false;
        }
        return clicked;
    }

    // Using this and Android documentation as reference for context menu behavior
    // https://stackoverflow.com/questions/9114912/what-is-context-menu-method-registerforcontextmenu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Today");
        menu.add(Menu.NONE, 1, Menu.NONE, "Tomorrow");
        menu.add(Menu.NONE, 1, Menu.NONE, "Finish");
        menu.add(Menu.NONE, 1, Menu.NONE, "Delete");
    }

    //This should handle the logic, examples online uses switch cases
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }


}
