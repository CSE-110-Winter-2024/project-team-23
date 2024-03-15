package edu.ucsd.cse110.successorator.ui;


import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.ListItemGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;

public class GoalListAdapter extends ArrayAdapter<Goal> {
    private MainViewModel mainViewModel;

    public GoalListAdapter(@NonNull android.content.Context context, @NonNull List<Goal> goals, MainViewModel mainViewModel) {
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
        binding.goalText.setText(mainViewModel.getGoalContent(goal));
        if (goal.completed()) {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.background.setBackgroundColor(Color.LTGRAY);
        } else {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            binding.background.setBackgroundColor(Color.WHITE);
        }

        /*
        Logic for color of the list context icon
         */
        if (goal.context() == Context.HOME) {
            binding.imageView.setColorFilter(Color.parseColor("#faf04d"));
        }
        if (goal.context() == Context.WORK) {
            binding.imageView.setColorFilter(Color.parseColor("#31c7f8"));
        }
        if (goal.context() == Context.SCHOOL) {
            binding.imageView.setColorFilter(Color.parseColor("#c95bf9"));
        }
        if (goal.context() == Context.ERRANDS) {
            binding.imageView.setColorFilter(Color.parseColor("#a2cb84"));
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
