package edu.ucsd.cse110.successorator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.util.Date;

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreateGoalBinding;


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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        this.view = FragmentDialogCreateGoalBinding.inflate(getLayoutInflater());

        //Create listener for enter key.
        //https://youtu.be/DivBp_9ZeK0?si=8Laea7bnST0mfmtm <- GOATED
        //https://guides.codepath.com/android/Basic-Event-Listeners#edittext-common-listeners
        //https://developer.android.com/reference/android/widget/TextView.OnEditorActionListener official documentation frankly incredibly unhelpful
        TextView.OnEditorActionListener enterListener = (v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                String content = view.goalInput.getText().toString();
                mainViewModel.addGoal(content);
                //Lambda functions allow for usage of this. in interface declaration.
                //Interestingly, without it dismiss() appears to call the correct function regardless.
                this.dismiss();
            }
            return false;
        };
        this.view.goalInput.setOnEditorActionListener(enterListener);

        return new AlertDialog.Builder(getActivity())
                .setView(view.getRoot())
                .create();
    }
}