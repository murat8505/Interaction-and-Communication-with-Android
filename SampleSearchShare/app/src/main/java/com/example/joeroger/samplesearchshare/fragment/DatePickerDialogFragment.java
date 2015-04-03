package com.example.joeroger.samplesearchshare.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_DATE = DatePickerDialogFragment.class.getName() + ".date";
    private static final String ARG_REQUEST_ID = DatePickerDialogFragment.class.getName() + ".requestId";

    private DatePickerDialogFragmentListener listener;
    private int requestId;
    private Date date;

    public interface DatePickerDialogFragmentListener {
        public void onDateSet(int requestId, @NonNull Date date);
    }

    public static DatePickerDialogFragment newInstance(int requestId, Date date) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_ID, requestId);
        args.putLong(ARG_DATE, date.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    public DatePickerDialogFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // In the app this dialog is never called by an activity, but
        // here is an example of how to dynamically handle either a parent fragment
        // or an activity for our listener
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (DatePickerDialogFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() +
                    " must implement DatePickerFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            requestId = args.getInt(ARG_REQUEST_ID);
            date = new Date(args.getLong(ARG_DATE));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Initialize our date picker dialog with the last birthday of the user...
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        // No birthdays allowed in the future...
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // One note. The DatePickerDialog is managing the saved state for us. This is why
        // this fragment isn't trying to do that. It is nice when that happens, but you
        // should always verify expected behavior.
        return dialog;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
        listener.onDateSet(requestId, cal.getTime());
    }
}
