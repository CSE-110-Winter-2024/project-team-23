package edu.ucsd.cse110.successorator.ui;

import android.content.Context;
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

        System.out.println("aaaa");
        System.out.println(binding.goalText.getPaintFlags());

        binding.goalText.setText(goal.content());
        binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        System.out.println(binding.goalText.getPaintFlags());
        if (goal.completed()) {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            binding.goalText.setPaintFlags(binding.goalText.getPaintFlags() & (~16));
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
