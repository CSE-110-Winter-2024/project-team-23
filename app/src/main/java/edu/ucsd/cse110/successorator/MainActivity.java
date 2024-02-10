package edu.ucsd.cse110.successorator;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;
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
            listAdapter.notifyDataSetChanged();
        });
        //Create list display.
        this.view.goalList.setAdapter(listAdapter);

        view.goalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                System.out.println("Position:" + position);
                System.out.println("ID: " + id);
                mainViewModel.pressGoal(Math.toIntExact(id));

            }
        });

        setContentView(view.getRoot());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        var itemId = item.getItemId();

        if(itemId == R.id.add_goal_menu){
            //probably refactor into its own method later
            var dialogFragment = CreateGoalDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "CreateGoalDialogFragment");
        }
        return super.onOptionsItemSelected(item);
    }
}
