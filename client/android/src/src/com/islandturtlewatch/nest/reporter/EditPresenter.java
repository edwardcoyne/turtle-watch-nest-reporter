package com.islandturtlewatch.nest.reporter;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditPresenter {
	private final ReportsModel model;
	private final EditView view;
	private final DataUpdateHandler updateHandler;

	public EditPresenter(ReportsModel model, EditView activity) {
		this.model = model;
		this.view = activity;
		this.updateHandler = new DataUpdateHandler();
	}

	public DataUpdateHandler getUpdateHandler() {
		return this.updateHandler;
	}

	private void updateView() {
		Report report = model.getActiveReport();
		view.updateDisplay(report);
	}

	public class DataUpdateHandler {
		public DataUpdateResult updateDateFound(int year, int month, int day) {
			Report.Builder activeReport = model.getActiveReport().toBuilder();
			activeReport.setTimestampFoundMs(DateUtil.getTimestampInMs(year, month, day));

			model.setActiveReport(activeReport.build());
			updateView();

			return DataUpdateResult.success();
		}
	}

	public static class DataUpdateResult {
		private final boolean success;
		private final Optional<String> errorMessage;

		public static DataUpdateResult success() {
			return new DataUpdateResult(true, Optional.<String>absent());
		}

		public static DataUpdateResult failed(String errorMessage) {
			return new DataUpdateResult(false, Optional.of(errorMessage));
		}

		private DataUpdateResult(boolean success, Optional<String> errorMessage) {
	    this.success = success;
	    this.errorMessage = errorMessage;
    }

		public boolean isSuccess() {
			return success;
		}

		public boolean hasErrorMessage() {
			return errorMessage.isPresent();
		}

		public String getErrorMessage() {
			return errorMessage.get();
		}
	}

}
