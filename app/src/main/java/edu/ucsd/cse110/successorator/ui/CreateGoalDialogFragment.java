package edu.ucsd.cse110.successorator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.ucsd.cse110.successorator.R;
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreateGoalBinding;


public class CreateGoalDialogFragment extends DialogFragment {
    private FragmentDialogCreateGoalBinding view;
    public CreateGoalDialogFragment() {
        // Required empty public constructor
    }

    public static CreateGoalDialogFragment newInstance() {
        var fragment = new CreateGoalDialogFragment();
        //In case args needed in future.
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        this.view = FragmentDialogCreateGoalBinding.inflate(getLayoutInflater());
        return new AlertDialog.Builder(getActivity())
                .setView(view.getRoot())
                .create();
    }
}