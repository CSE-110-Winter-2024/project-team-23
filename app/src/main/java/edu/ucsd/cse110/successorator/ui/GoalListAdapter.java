package edu.ucsd.cse110.successorator.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.ListItemGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.Goal;

public class GoalListAdapter extends ArrayAdapter<Goal> {
    private MainViewModel mainViewModel;
    public GoalListAdapter(@NonNull Context context, @NonNull List<Goal> goals, MainViewModel mainViewModel) {
        super(context, 0, new ArrayList<>(goals));
        this.mainViewModel = mainViewModel;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        var goal = getItem(position);
        assert goal != null;

        ListItemGoalBinding binding;
        if (convertView != null) {
            binding = ListItemGoalBinding.bind(convertView);
        } else {
            var layoutInflater = LayoutInflater.from(getContext());
            binding = ListItemGoalBinding.inflate(layoutInflater, parent, false);
        }

        /*
        Strike through reference:
        https://stackoverflow.com/questions/3881553/is-there-an-easy-way-to-strike-through-text-in-an-app-widget/6739637
         */
        binding.goalText.setText(goal.content());
        if (goal.completed()) {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.background.setBackgroundColor(Color.LTGRAY);
        }
        else {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            binding.background.setBackgroundColor(Color.WHITE);
        }



        return binding.getRoot();
    }

    //Optional fluff
    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        var goal = getItem(position);
        assert goal != null;

        var id = goal.id();
        assert id != null;

        return id;
    }
}
