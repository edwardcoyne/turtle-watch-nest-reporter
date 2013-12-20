package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditFragmentInfo extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS = ClickHandler.toMap(
      new HandleSetInfoDate());
  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.edit_fragment_info, container, false);
  }

  @Override
  public void updateSection(Report report) {
    if (isDetached() || getActivity() == null) {
      // Will be updated when attached.
      return;
    }

    setText(R.id.buttonDateFound, DateUtil.getFormattedDate(report.getTimestampFoundMs()));
    setText(R.id.labelIncubationDate,
        DateUtil.getFormattedDate(DateUtil.plusDays(report.getTimestampFoundMs(), 55)));

    setText(R.id.fieldObservers, report.getObservers());

    setChecked(R.id.fieldNestVerified, report.getActivity().getNestVerified());
    setChecked(R.id.fieldNestNotVerified, report.getActivity().getNestNotVerified());
    setChecked(R.id.fieldNestRelocated, report.getActivity().getNestRelocated());
    setChecked(R.id.fieldFalseCrawl, report.getActivity().getFalseCrawl());
    setChecked(R.id.fieldAbandonedBodyPits, report.getActivity().getAbandonedBodyPits());
    setChecked(R.id.fieldAbandonedEggCavities, report.getActivity().getAbandonedEggCavities());
  }

  private static class HandleSetInfoDate extends ClickHandler
      implements DatePickerDialog.OnDateSetListener {
    private DataUpdateHandler updateHandler;

    protected HandleSetInfoDate() {
      super(R.id.buttonDateFound);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      this.updateHandler = updateHandler;
      CurrentDatePicker.showOnView(view, this);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      Log.d(EditFragmentInfo.class.getSimpleName(),
          "Set date to " + year + "/" + month + "/" + day);
      displayResult(updateHandler.updateDateFound(year, month, day));
    }
  }
}
