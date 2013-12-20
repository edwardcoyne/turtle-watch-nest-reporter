package com.islandturtlewatch.nest.reporter.ui;

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
  public EditFragmentInfo() {
	  super();
	  addClickHandler(new HandleSetInfoDate());
  }

	@Override
  public View onCreateView(LayoutInflater inflater,
  		ViewGroup container,
      Bundle savedInstanceState) {
      return inflater.inflate(R.layout.edit_fragment_info, container, false);
  }

  @Override
  public void updateDisplay(Report report) {
  	getButtonById(R.id.buttonDateFound).setText(
  			DateUtil.getFormattedDate(report.getTimestampFoundMs()));
  	getTextViewById(R.id.labelIncubationDate).setText(
  			DateUtil.getFormattedDate(DateUtil.plusDays(report.getTimestampFoundMs(), 55)));
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
