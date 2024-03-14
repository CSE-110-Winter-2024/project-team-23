package edu.ucsd.cse110.successorator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreatePendingGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.Context;


public class CreatePendingGoalDialogFragment extends DialogFragment {
    private FragmentDialogCreatePendingGoalBinding view;
    //Not this most flexible name but the least ambiguous.
    private MainViewModel mainViewModel;

    private Context context;

    public CreatePendingGoalDialogFragment() {
        // Required empty public constructor
    }

    public static CreatePendingGoalDialogFragment newInstance() {
        var fragment = new CreatePendingGoalDialogFragment();
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
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        this.view = FragmentDialogCreatePendingGoalBinding.inflate(getLayoutInflater());


        // Create listener for context buttons
        this.view.contextRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == this.view.homeButton.getId()) {
                this.context = Context.HOME;
            } else if (checkedId == this.view.workButton.getId()) {
                this.context = Context.WORK;
            } else if (checkedId == this.view.schoolButton.getId()) {
                this.context = Context.SCHOOL;
            } else if (checkedId == this.view.errandsButton.getId()) {
                this.context = Context.ERRANDS;
            }
        });

        //Create listener for enter key.
        //Interface containing method called anytime enter key is pressed.
        //https://youtu.be/DivBp_9ZeK0?si=8Laea7bnST0mfmtm
        TextView.OnEditorActionListener editListener = (v, actionId, event) -> {
            //actionId determined in corresponding xml file.
            //Unnecessary now but futureproofs for multiple textboxes with the same listener.
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String content = view.goalInput.getText().toString();

                if (context == null) {
                    return false;
                }

                //Add pending goal to the view model.
                mainViewModel.addPendingGoal(content, context);

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
