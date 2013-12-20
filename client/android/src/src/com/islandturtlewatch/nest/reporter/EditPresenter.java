package com.islandturtlewatch.nest.reporter;

import android.os.Bundle;

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

	public void persistToBundle(Bundle outState) {
	  this.model.persistToBundle(outState);
	}

	public void restoreFromBundle(Bundle inState) {
	  this.model.restoreFromBundle(inState);
	}

	public Report getCurrentReport() {
	  return model.getActiveReport();
	}

	private void updateView() {
		Report report = model.getActiveReport();
		view.updateDisplay(report);
	}

	private void writeChangesAndUpdate(Report udpatedReport) {
	  model.setActiveReport(udpatedReport);
	  updateView();
	}

	public class DataUpdateHandler {
		public DataUpdateResult updateDateFound(int year, int month, int day) {
			Report updatedReport = model.getActiveReport().toBuilder()
			    .setTimestampFoundMs(DateUtil.getTimestampInMs(year, month, day))
			    .build();

			writeChangesAndUpdate(updatedReport);
			return DataUpdateResult.success();
		}

		public DataUpdateResult updateObservers(String observers) {
          Report updatedReport = model.getActiveReport().toBuilder()
              .setObservers(observers)
              .build();

          writeChangesAndUpdate(updatedReport);
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateNestVerified(boolean value) {
		  Report.Builder updatedReport = model.getActiveReport().toBuilder();
		  updatedReport.getActivityBuilder().setNestVerified(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
		}

		public DataUpdateResult updateNestNotVerified(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getActivityBuilder().setNestNotVerified(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateNestRelocated(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getActivityBuilder().setNestRelocated(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateFalseCrawl(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getActivityBuilder().setFalseCrawl(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateAbandonedBodyPits(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getActivityBuilder().setAbandonedBodyPits(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateAbandonedEggCavities(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getActivityBuilder().setAbandonedEggCavities(value);
          writeChangesAndUpdate(updatedReport.build());
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
