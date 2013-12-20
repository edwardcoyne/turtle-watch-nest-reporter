package com.islandturtlewatch.nest.reporter;

import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditView;

public class EditPresenter {
	@SuppressWarnings("unused")
	private final ReportsModel model;
	private final EditView view;

	public EditPresenter(ReportsModel model, EditView activity) {
		this.model = model;
		this.view = activity;
	}

	private class DataUpdateHandler {
		public void updateDateFound(int year, int month, int day) {}
	}

	private class DataUpdateResult {
		public boolean success() {return false;}
		public String errorMessage() {return "";}
	}

}
