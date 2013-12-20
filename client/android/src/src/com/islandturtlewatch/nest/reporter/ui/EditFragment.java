package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.support.v4.app.Fragment;
import android.view.View;

import com.google.common.collect.Maps;
import com.islandturtlewatch.nest.data.ReportProto.Report;

public class EditFragment extends Fragment {
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

	public static abstract class ClickHandler {
		private final int resourceId;
		protected ClickHandler(int resourceId) {
	    this.resourceId = resourceId;
    }

		int getResourceId() {
			return resourceId;
		}

		public abstract void handleClick(View view);
	}
}
