package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.collect.Maps;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateResult;

public class EditFragment extends Fragment {
	private static final String TAG = EditFragment.class.getSimpleName();

	private final Map<Integer, ClickHandler> clickHandlers = Maps.newHashMap();

	/**
	 * Will be called to update the display contents based on information in report.
	 */
	public void updateDisplay(Report report) {

	}

	public Map<Integer, ClickHandler> getClickHandlers() {
		return clickHandlers;
	}

	protected void addClickHandler(ClickHandler handler) {
		clickHandlers.put(handler.getResourceId(), handler);
	}

	protected EditText getEditTextById(int id) {
		return (EditText) getActivity().findViewById(id);
	}

	protected Button getButtonById(int id) {
		return (Button) getActivity().findViewById(id);
	}

	protected TextView getTextViewById(int id) {
		return (TextView) getActivity().findViewById(id);
	}

	public static abstract class ClickHandler {
		private final int resourceId;
		protected ClickHandler(int resourceId) {
	    this.resourceId = resourceId;
    }

		int getResourceId() {
			return resourceId;
		}

		protected void displayResult(DataUpdateResult result) {
			if (result.isSuccess()) {
				Log.d(TAG, "Update successful");
			} else {
				Log.e(TAG, "Update failed: " + ((result.hasErrorMessage()) ? result.getErrorMessage() : ""));
				//TODO (edcoyne): add dialog with error message for user.
			}
		}

		public abstract void handleClick(View view, DataUpdateHandler updateHandler);
	}
}
