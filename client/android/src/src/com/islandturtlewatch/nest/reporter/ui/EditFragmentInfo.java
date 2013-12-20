package com.islandturtlewatch.nest.reporter.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.islandturtlewatch.nest.reporter.R;

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

  private static class HandleSetInfoDate extends ClickHandler
  		implements DatePickerDialog.OnDateSetListener {
		protected HandleSetInfoDate() {
      super(R.id.buttonDateFound);
    }

		@Override
    public void handleClick(View view) {
			CurrentDatePicker.showOnView(view, this);
    }

		@Override
    public void onDateSet(DatePicker view,
    		int year,
    		int monthOfYear,
        int dayOfMonth) {
			Log.d(EditFragmentInfo.class.getSimpleName(),
					"Set date to " + year + "/" + monthOfYear + "/" + dayOfMonth);

    }
  }
}
