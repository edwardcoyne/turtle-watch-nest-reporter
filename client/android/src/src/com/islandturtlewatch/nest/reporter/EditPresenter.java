package com.islandturtlewatch.nest.reporter;

import android.os.Bundle;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Excavation;
import com.islandturtlewatch.nest.data.ReportProto.Excavation.ExcavationFailureReason;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.PreditationEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.WashEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Builder;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.City;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Placement;
import com.islandturtlewatch.nest.data.ReportProto.Relocation;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
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

	public void updateView() {
	  Report report = model.getActiveReport();
	  view.updateDisplay(report);
	}

	private void writeChangesAndUpdate(Report udpatedReport) {
	  model.setActiveReport(udpatedReport);
	  updateView();
	}

	// TODO(edcoyne): break this into a separate class and add tests
	public class DataUpdateHandler {

      public DataUpdateResult updateNestNumber(Optional<Integer> nestNumber) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        if (nestNumber.isPresent()) {
          updatedReport.setNestNumber(nestNumber.get());
        } else {
          updatedReport.clearNestNumber();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }

		public DataUpdateResult updateDateFound(int year, int month, int day) {
			Report updatedReport = model.getActiveReport().toBuilder()
			    .setTimestampFoundMs(DateUtil.getTimestampInMs(year, month, day))
			    .build();

			writeChangesAndUpdate(updatedReport);
			return DataUpdateResult.success();
		}

		public DataUpdateResult updateObservers(String observers) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          if (observers.isEmpty()) {
            updatedReport.clearObservers();
          } else {
            updatedReport.setObservers(observers);
          }
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateNestStatus(NestStatus status) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.setStatus(status);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateAbandonedBodyPits(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getConditionBuilder().setAbandonedBodyPits(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateAbandonedEggCavities(boolean value) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getConditionBuilder().setAbandonedEggCavities(value);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateStreetAddress(String address) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          if (address.isEmpty()) {
            updatedReport.getLocationBuilder().clearStreetAddress();
          } else {
            updatedReport.getLocationBuilder().setStreetAddress(address);
          }
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateCity(City city) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          updatedReport.getLocationBuilder().setCity(city);
          writeChangesAndUpdate(updatedReport.build());
          return DataUpdateResult.success();
        }

		public DataUpdateResult updateDetails(String details) {
          Report.Builder updatedReport = model.getActiveReport().toBuilder();
          if (details.isEmpty()) {
            updatedReport.getLocationBuilder().clearDetails();
          } else {
            updatedReport.getLocationBuilder().setDetails(details);
          }
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
          Relocation.Builder relocationBuilder =
              updatedReport.getInterventionBuilder().getRelocationBuilder();
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
      public DataUpdateResult updateHatchDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder()
            .setHatchTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateAdditionalHatchDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder()
            .setAdditionalHatchTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateDisorentation(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().setDisorientation(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateExcavated(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getInterventionBuilder().getExcavationBuilder()
            .setExcavated(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateExcavationFailure(ExcavationFailureReason reason) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getInterventionBuilder().getExcavationBuilder()
            .setFailureReason(reason);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateExcavationFailureOther(String value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getInterventionBuilder().getExcavationBuilder()
            .setFailureOther(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateExcavationDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getInterventionBuilder().getExcavationBuilder()
            .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateDeadInNest(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setDeadInNest(value.get());
        } else {
          excavationBuilder.clearDeadInNest();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateLiveInNest(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setLiveInNest(value.get());
        } else {
          excavationBuilder.clearLiveInNest();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
  	public DataUpdateResult updateHatchedShells(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setHatchedShells(value.get());
        } else {
          excavationBuilder.clearHatchedShells();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
  	public DataUpdateResult updateDeadPipped(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setDeadPipped(value.get());
        } else {
          excavationBuilder.clearDeadPipped();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
  	public DataUpdateResult updateLivePipped(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setLivePipped(value.get());
        } else {
          excavationBuilder.clearLivePipped();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
  	public DataUpdateResult updateWholeUnhatched(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setWholeUnhatched(value.get());
        } else {
          excavationBuilder.clearWholeUnhatched();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
  	  public DataUpdateResult updateEggsDestroyed(Optional<Integer> value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        Excavation.Builder excavationBuilder =
            updatedReport.getInterventionBuilder().getExcavationBuilder();
        if (value.isPresent()) {
          excavationBuilder.setEggsDestroyed(value.get());
        } else {
          excavationBuilder.clearEggsDestroyed();
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateVandalizedDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder()
            .setVandalizedTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updatePoachedDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder()
            .setPoachedTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateVandalized(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().setVandalized(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updatePoached(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().setPoached(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateRootsInvaded(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().setRootsInvadedEggshells(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateEggsScattered(boolean value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().setEggsScatteredByAnother(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateNotes(String value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        if (value.isEmpty()) {
          updatedReport.clearAdditionalNotes();
        } else {
          updatedReport.setAdditionalNotes(value);
        }
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateWashoutDate(int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder()
            .getWashOutBuilder().setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateWashoutStorm(String value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getConditionBuilder().getWashOutBuilder().setStormName(value);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateWashOverDate(int ordinal, int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal)
            ? condition.addWashOverBuilder()
                : condition.getWashOverBuilder(ordinal);
        washOver.setTimestampMs(DateUtil.getTimestampInMs(year, month, day));

        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateWashOverStorm(int ordinal, String value) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal)
            ? condition.addWashOverBuilder()
                : condition.getWashOverBuilder(ordinal);
        washOver.setStormName(value);

        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult deleteWashOver(int ordinal) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();
        condition.removeWashOver(ordinal);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updatePreditationDate(int ordinal, int year, int month, int day) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
            ? condition.addPreditationBuilder()
                : condition.getPreditationBuilder(ordinal);
        preditation.setTimestampMs(DateUtil.getTimestampInMs(year, month, day));

        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updatePreditationNumEggs(int ordinal, Optional<Integer> numEggs) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
            ? condition.addPreditationBuilder()
                : condition.getPreditationBuilder(ordinal);
        if (numEggs.isPresent()) {
          preditation.setNumberOfEggs(numEggs.get());
        } else {
          preditation.clearNumberOfEggs();
        }

        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updatePreditationPredator(int ordinal, String predator) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
            ? condition.addPreditationBuilder()
                : condition.getPreditationBuilder(ordinal);
        preditation.setPredator(predator);

        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult deletePreditation(int ordinal) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();
        condition.removePreditation(ordinal);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }

      public DataUpdateResult updateNestGps(GpsCoordinates coordinates) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getLocationBuilder().setCoordinates(coordinates);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateTriangulationNorth(GpsCoordinates coordinates) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getLocationBuilder().getTriangulationBuilder().setNorth(coordinates);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateTriangulationSouth(GpsCoordinates coordinates) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getLocationBuilder().getTriangulationBuilder().setSouth(coordinates);
        writeChangesAndUpdate(updatedReport.build());
        return DataUpdateResult.success();
      }
      public DataUpdateResult updateNewGps(GpsCoordinates coordinates) {
        Report.Builder updatedReport = model.getActiveReport().toBuilder();
        updatedReport.getInterventionBuilder().getRelocationBuilder().setCoordinates(coordinates);
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
