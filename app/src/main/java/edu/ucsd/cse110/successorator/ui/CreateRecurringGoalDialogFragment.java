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
import edu.ucsd.cse110.successorator.databinding.FragmentDialogCreateRecurringGoalBinding;
import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.RecurrenceType;


public class CreateRecurringGoalDialogFragment extends DialogFragment {
    private FragmentDialogCreateRecurringGoalBinding view;
    //Not this most flexible name but the least ambiguous.
    private MainViewModel mainViewModel;
    private RecurrenceType recurrenceType;
    public CreateRecurringGoalDialogFragment() {
        // Required empty public constructor
    }

    public static CreateRecurringGoalDialogFragment newInstance() {
        var fragment = new CreateRecurringGoalDialogFragment();
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
        this.view = FragmentDialogCreateRecurringGoalBinding.inflate(getLayoutInflater());

        // Create listener for context buttons
        this.view.recurrenceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == this.view.dailyRecurringGoalButton.getId()) {
                this.recurrenceType = RecurrenceType.DAILY;
            } else if (checkedId == this.view.weeklyRecurringGoalButton.getId()) {
                this.recurrenceType = RecurrenceType.WEEKLY;
            } else if (checkedId == this.view.monthlyRecurringGoalButton.getId()) {
                this.recurrenceType = RecurrenceType.MONTHLY;
            } else if (checkedId == this.view.yearlyRecurringGoalButton.getId()) {
                this.recurrenceType = RecurrenceType.YEARLY;
            }
        });

        //Create listener for enter key.
        //Interface containing method called anytime enter key is pressed.
        //https://youtu.be/DivBp_9ZeK0?si=8Laea7bnST0mfmtm
        TextView.OnEditorActionListener editListener = (v, actionId, event) -> {
            //actionId determined in corresponding xml file.
            //Unnecessary now but futureproofs for multiple textboxes with the same listener.

            String[] parts = view.recurringDatePicker.getText().toString().split("/");
            if (parts.length == 3) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    String content = view.goalInput.getText().toString();
                    //Context is defaulted to HOME, Needs US3 to be implemented.
                    mainViewModel.addRecurringGoal(content,Integer.parseInt(parts[2]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), recurrenceType, Context.HOME);

                    //Lambda functions allow for usage of this. in interface declaration.
                    //Interestingly, without it dismiss() appears to call the correct function regardless.
                    this.dismiss();
                }
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