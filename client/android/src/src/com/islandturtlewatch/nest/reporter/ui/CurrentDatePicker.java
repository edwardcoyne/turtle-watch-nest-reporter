package com.islandturtlewatch.nest.reporter.ui;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.google.common.base.Optional;

public class CurrentDatePicker extends DialogFragment {
  private OnDateSetListener listener;
  private Optional<Integer> year = Optional.absent();
  private Optional<Integer> month = Optional.absent();
  private Optional<Integer> day = Optional.absent();

  /**
   * This class shouldn't be constructed, use the static method
   * .showOnView().
   */
  @Deprecated
  public CurrentDatePicker() {
    super();
  }

  public void setDate(int year, int month, int day) {
    this.year = Optional.of(year);
    this.month = Optional.of(month);
    this.day = Optional.of(day);
  }

  public void setListener(OnDateSetListener listener) {
    this.listener = listener;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Calendar calendar = Calendar.getInstance();
    int year = this.year.or(calendar.get(Calendar.YEAR));
    int month = this.month.or(calendar.get(Calendar.MONTH));
    int day = this.day.or(calendar.get(Calendar.DAY_OF_MONTH));

    return new DatePickerDialog(getActivity(), listener, year, month, day);
  }

  public static void showOnView(View view, OnDateSetListener listener) {
    CurrentDatePicker fragment = new CurrentDatePicker();
    fragment.setListener(listener);
    Activity activity = (Activity) view.getContext();
    fragment.show(activity.getFragmentManager(), "foundDatePicker");
  }

  public static void showOnView(View view, OnDateSetListener listener,
      int year, int month, int day) {
    CurrentDatePicker fragment = new CurrentDatePicker();
    fragment.setDate(year, month, day);
    fragment.setListener(listener);
    Activity activity = (Activity) view.getContext();
    fragment.show(activity.getFragmentManager(), "foundDatePicker");
  }
}
