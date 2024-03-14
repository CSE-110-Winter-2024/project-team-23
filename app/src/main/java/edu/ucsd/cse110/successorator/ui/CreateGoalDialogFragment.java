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

import edu.ucsd.cse110.successorator.MainViewModel;
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreateGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.AppMode;
import edu.ucsd.cse110.successorator.lib.domain.Context;


public class CreateGoalDialogFragment extends DialogFragment {
    private FragmentDialogCreateGoalBinding view;
    //Not this most flexible name but the least ambiguous.
    private MainViewModel mainViewModel;

    private Context context;

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

        // Hook up button labels to the view model
        mainViewModel.getWeeklyButtonString().observe(text -> view.WeeklyRecurringGoalButton.setText(text));
        mainViewModel.getMonthlyButtonString().observe(text -> view.MonthlyRecurringGoalButton.setText(text));
        mainViewModel.getYearlyButtonString().observe(text -> view.YearlyRecurringGoalButton.setText(text));


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
            if(actionId == EditorInfo.IME_ACTION_DONE){
                String content = view.goalInput.getText().toString();

                if (context == null) {
                    return false;
                }

                //If on pending view logic to make pending goal
                if (mainViewModel.getCurrentMode().getValue() == AppMode.PENDING) {
                    mainViewModel.addPendingGoal(content, context);
                } else {
                    mainViewModel.addGoal(content, context);
                }

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
