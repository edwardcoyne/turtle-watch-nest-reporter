package com.islandturtlewatch.nest.reporter;

import android.os.Bundle;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Builder;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Placement;
import com.islandturtlewatch.nest.data.ReportProto.Relocation;
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

		public DataUpdateResult updateStreetAddress(String address) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().setStreetAddress(address);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateSectionNumber(int sectionNumber) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().setSection(sectionNumber);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateDetails(String details) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().setDetails(details);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateApexToBarrierFt(Optional<Integer> apexToBarrierFt) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          Builder location = updatedReport.getLocationBuilder();

          if (apexToBarrierFt.isPresent()) {
            location.setApexToBarrierFt(apexToBarrierFt.get());
          } else {
            location.clearApexToBarrierFt();
          }

          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateApexToBarrierIn(Optional<Integer> apexToBarrierIn) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          Builder location = updatedReport.getLocationBuilder();

          if (apexToBarrierIn.isPresent()) {
            location.setApexToBarrierIn(apexToBarrierIn.get());
          } else {
            location.clearApexToBarrierIn();
          }

          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateWaterToApexFt(Optional<Integer> waterToApexFt) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();

          Builder location = updatedReport.getLocationBuilder();
          if (waterToApexFt.isPresent()) {
            location.setWaterToApexFt(waterToApexFt.get());
          } else {
            location.clearWaterToApexFt();
          }

          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateWaterToApexIn(Optional<Integer> waterToApexIn) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          Builder location = updatedReport.getLocationBuilder();

          if (waterToApexIn.isPresent()) {
            location.setWaterToApexIn(waterToApexIn.get());
          } else {
            location.clearWaterToApexIn();
          }

          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateLocationPlacement(Placement value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().setPlacement(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateObstructionsSeawallRocks(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().getObstructionsBuilder().setSeawallRocks(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
		}

		public DataUpdateResult updateObstructionsFurniture(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().getObstructionsBuilder().setFurniture(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
		}

		public DataUpdateResult updateObstructionsEscarpment(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().getObstructionsBuilder().setEscarpment(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateObstructionsOther(Optional<String> value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          if (value.isPresent()) {
            updatedReport.getLocationBuilder().getObstructionsBuilder().setOther(value.get());
          } else {
            updatedReport.getLocationBuilder().getObstructionsBuilder().clearOther();
          }
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateAdopted(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().setAdopted(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateProtectionType(ProtectionEvent.Type value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getProtectionEventBuilder().setType(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateWhenProtected(ProtectionEvent.Reason value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getProtectionEventBuilder().setReason(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateRelocated(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder().setWasRelocated(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateNewAddress(String value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder().setNewAddress(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateNumberOfEggsRelocated(Optional<Integer> value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          Relocation.Builder relocationBuilder = updatedReport.getInterventionBuilder().getRelocationBuilder();
          if (value.isPresent()) {
            relocationBuilder.setEggsRelocated(value.get());
          } else {
            relocationBuilder.clearEggsRelocated();
          }
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateNumberOfEggsDestroyed(Optional<Integer> value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          Relocation.Builder relocationBuilder =
              updatedReport.getInterventionBuilder().getRelocationBuilder();
          if (value.isPresent()) {
            relocationBuilder.setEggsDestroyed(value.get());
          } else {
            relocationBuilder.clearEggsDestroyed();
          }
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateRelocationReasonHighWater(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder().setReasonHighWater(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateRelocationReasonPredation(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder().setReasonPredation(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateRelocationReasonWashingOut(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder().setReasonWashingOut(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

        public DataUpdateResult updateRelocationReasonConstruction(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder()
              .setReasonConstructionRenourishment(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }
        public DataUpdateResult updateDateProtected(int year, int month, int day) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getProtectionEventBuilder()
              .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
      }
      public DataUpdateResult updateDateRelocated(int year, int month, int day) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getInterventionBuilder().getRelocationBuilder()
              .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
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
