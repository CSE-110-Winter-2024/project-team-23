package edu.ucsd.cse110.successorator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreateGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.Context;


public class CreateGoalDialogFragment extends DialogFragment {
    private FragmentDialogCreateGoalBinding view;
    //Not this most flexible name but the least ambiguous.
    private MainViewModel mainViewModel;

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

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var modelOwner = requireActivity();
        var modelFactory = ViewModelProvider.Factory.from(MainViewModel.initializer);
        var modelProvider = new ViewModelProvider(modelOwner, modelFactory);
        this.mainViewModel = modelProvider.get(MainViewModel.class);
    }

    void contextButtonSelected(Context c) {
        switch (c) {
            case HOME:

                break;
            case WORK:
                break;
            case SCHOOL:
                break;
            case ERRANDS:
                break;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        this.view = FragmentDialogCreateGoalBinding.inflate(getLayoutInflater());

        //Create listener for enter key.
        //Interface containing method called anytime enter key is pressed.
        //https://youtu.be/DivBp_9ZeK0?si=8Laea7bnST0mfmtm
        TextView.OnEditorActionListener editListener = (v, actionId, event) -> {
            //actionId determined in corresponding xml file.
            //Unnecessary now but futureproofs for multiple textboxes with the same listener.
            if(actionId == EditorInfo.IME_ACTION_DONE){
                String content = view.goalInput.getText().toString();
                mainViewModel.addGoal(content);
                //Lambda functions allow for usage of this. in interface declaration.
                //Interestingly, without it dismiss() appears to call the correct function regardless.
                this.dismiss();
            }
            return false;
        };
        //Give goal input textbox an enter listener.
        this.view.goalInput.setOnEditorActionListener(editListener);

        return new AlertDialog.Builder(getActivity())
                .setView(view.getRoot())
                .create();
    }
}