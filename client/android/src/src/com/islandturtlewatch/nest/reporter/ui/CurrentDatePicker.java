package com.islandturtlewatch.nest.reporter.ui;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

public class CurrentDatePicker extends DialogFragment {
	private OnDateSetListener listener;

	/**
	 * This class generally shouldn't be constructed, use the static method .showOnView().
	 */
	@Deprecated
	public CurrentDatePicker() {
	  super();
  }

	public void setListener(OnDateSetListener listener) {
		this.listener = listener;
	}

	@Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		return new DatePickerDialog(getActivity(), listener, year, month, day);
  }

	public static void showOnView(View view, OnDateSetListener listener) {
		CurrentDatePicker fragment = new CurrentDatePicker();
		fragment.setListener(listener);
		Activity activity = (Activity) view.getContext();
		fragment.show(activity.getFragmentManager(), "foundDatePicker");
	}
}
