package com.islandturtlewatch.nest.reporter.data;

import lombok.Setter;
import lombok.experimental.Builder;

import com.google.api.client.util.Preconditions;
import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Excavation;
import com.islandturtlewatch.nest.data.ReportProto.Excavation.ExcavationFailureReason;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.PreditationEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.WashEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.City;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Placement;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Triangulation;
import com.islandturtlewatch.nest.data.ReportProto.Relocation;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
import com.islandturtlewatch.nest.data.ReportProto.Report.Species;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class ReportMutations {
  private ReportMutations() {} // namespace really.

  @Builder(fluent=false)
  public static class NestNumberMutation implements ReportMutation {
    private final Optional<Integer> number;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setNestNumber(number.or(0));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class FalseCrawlNumberMutation implements ReportMutation {
    private final Optional<Integer> number;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setFalseCrawlNumber(number.or(0));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DateFoundMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      return oldReport.toBuilder()
          .setTimestampFoundMs(DateUtil.getTimestampInMs(year, month, day))
          .build();
    }
  }

  @Builder(fluent=false)
  public static class ObserversMutation implements ReportMutation {
    private final String observers;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      if (observers.isEmpty()) {
        updatedReport.clearObservers();
      } else {
        updatedReport.setObservers(observers);
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class NestStatusMutation
      implements ReportMutation, ReportMutation.RequiresReportsModel {
    private final NestStatus status;

    @Setter
    private ReportsModel model;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();

      if (status == NestStatus.FALSE_CRAWL
          && updatedReport.getStatus() != NestStatus.FALSE_CRAWL) {
        updatedReport.clearNestNumber();
        updatedReport
            .setFalseCrawlNumber(model.getHighestFalseCrawlNumber() + 1);
      } else if (status != NestStatus.FALSE_CRAWL
          && updatedReport.getStatus() == NestStatus.FALSE_CRAWL) {
        updatedReport.clearFalseCrawlNumber();
        updatedReport.setNestNumber(model.getHighestNestNumber() + 1);
      }

      updatedReport.setStatus(status);
      return updatedReport.build();
    }
  }
  @Builder(fluent=false)
  public static class AbandonedBodyPitsMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setAbandonedBodyPits(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class AbandonedEggCavitiesMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setAbandonedEggCavities(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationNorthFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Triangulation.Builder triangulation =
          updatedReport.getLocationBuilder().getTriangulationBuilder();

      if (ft.isPresent()) {
        triangulation.setNorthFt(ft.get());
      } else {
        triangulation.clearNorthFt();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationNorthInMutation implements ReportMutation {
    private final Optional<Integer> in;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Triangulation.Builder triangulation =
          updatedReport.getLocationBuilder().getTriangulationBuilder();

      if (in.isPresent()) {
        triangulation.setNorthIn(in.get());
      } else {
        triangulation.clearNorthIn();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationSouthFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Triangulation.Builder triangulation =
          updatedReport.getLocationBuilder().getTriangulationBuilder();

      if (ft.isPresent()) {
        triangulation.setSouthFt(ft.get());
      } else {
        triangulation.clearSouthFt();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationSouthInMutation implements ReportMutation {
    private final Optional<Integer> in;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Triangulation.Builder triangulation =
          updatedReport.getLocationBuilder().getTriangulationBuilder();

      if (in.isPresent()) {
        triangulation.setSouthIn(in.get());
      } else {
        triangulation.clearSouthIn();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class StreetAddressMutation implements ReportMutation {
    private final String address;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      if (address.isEmpty()) {
        updatedReport.getLocationBuilder().clearStreetAddress();
      } else {
        updatedReport.getLocationBuilder().setStreetAddress(address);
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class CityMutation implements ReportMutation {
    private final City city;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setCity(city);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DetailsMutation implements ReportMutation {
    private final String details;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      if (details.isEmpty()) {
        updatedReport.getLocationBuilder().clearDetails();
      } else {
        updatedReport.getLocationBuilder().setDetails(details);
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ApexToBarrierFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestLocation.Builder location = updatedReport.getLocationBuilder();

      if (ft.isPresent()) {
        location.setApexToBarrierFt(ft.get());
      } else {
        location.clearApexToBarrierFt();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ApexToBarrierInMutation implements ReportMutation {
    private final Optional<Integer> in;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestLocation.Builder location = updatedReport.getLocationBuilder();

      if (in.isPresent()) {
        location.setApexToBarrierIn(in.get());
      } else {
        location.clearApexToBarrierIn();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WaterToApexFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestLocation.Builder location = updatedReport.getLocationBuilder();

      if (ft.isPresent()) {
        location.setWaterToApexFt(ft.get());
      } else {
        location.clearWaterToApexFt();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WaterToApexInMutation implements ReportMutation {
    private final Optional<Integer> in;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestLocation.Builder location = updatedReport.getLocationBuilder();

      if (in.isPresent()) {
        location.setWaterToApexIn(in.get());
      } else {
        location.clearWaterToApexIn();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class PlacementMutation implements ReportMutation {
    private final Placement placement;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setPlacement(placement);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ObstructionsSeawallRocksMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
          .setSeawallRocks(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ObstructionsFurnitureMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
          .setFurniture(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ObstructionsEscarpmentMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
          .setEscarpment(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ObstructionsOtherMutation implements ReportMutation {
    private final Optional<String> other;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      if (other.isPresent()) {
        updatedReport.getLocationBuilder().getObstructionsBuilder()
            .setOther(other.get());
      } else {
        updatedReport.getLocationBuilder().getObstructionsBuilder()
            .clearOther();
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ProtectionTypeMutation implements ReportMutation {
    private final ProtectionEvent.Type type;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      ProtectionEvent.Builder protectionEventBuilder = updatedReport
          .getInterventionBuilder().getProtectionEventBuilder();
      if (protectionEventBuilder.getType() == type) {
        // Hack means it was clicked twice, unset.
        protectionEventBuilder.clearType();
      } else {
        protectionEventBuilder.setType(type);
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WhyProtectedMutation implements ReportMutation {
    private final ProtectionEvent.Reason reason;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      ProtectionEvent.Builder protectionEventBuilder = updatedReport
          .getInterventionBuilder().getProtectionEventBuilder();
      if (protectionEventBuilder.getReason() == reason) {
        // Hack means it was clicked twice, unset.
        protectionEventBuilder.clearReason();
      } else {
        protectionEventBuilder.setReason(reason);
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class RelocatedMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
          .setWasRelocated(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class RelocatedReasonMutation implements ReportMutation {
    private final Relocation.Reason reason;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
          .setReason(reason);
      return updatedReport.build();
    }
  }
  @Builder(fluent=false)
  public static class NewAddressMutation implements ReportMutation {
    private final String address;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
          .setNewAddress(address);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class EggsRelocatedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Relocation.Builder relocationBuilder = updatedReport
          .getInterventionBuilder().getRelocationBuilder();
      if (eggs.isPresent()) {
        relocationBuilder.setEggsRelocated(eggs.get());
      } else {
        relocationBuilder.clearEggsRelocated();
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class EggsDestroyedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Relocation.Builder relocationBuilder = updatedReport
          .getInterventionBuilder().getRelocationBuilder();
      if (eggs.isPresent()) {
        relocationBuilder.setEggsDestroyed(eggs.get());
      } else {
        relocationBuilder.clearEggsDestroyed();
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DateRelocatedMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
          .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DateProtectedMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getProtectionEventBuilder()
          .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class HatchDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setHatchTimestampMs(
          DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class AdditionalHatchDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setAdditionalHatchTimestampMs(
          DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DisorentationMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setDisorientation(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WasExcavatedMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setExcavated(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationFailureMutation implements ReportMutation {
    private final ExcavationFailureReason reason;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setFailureReason(reason);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationFailureOtherMutation implements ReportMutation {
    private final String reason;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setFailureOther(reason);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder()
          .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationDeadInNestMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (eggs.isPresent()) {
        excavationBuilder.setDeadInNest(eggs.get());
      } else {
        excavationBuilder.clearDeadInNest();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationLiveInNestMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (eggs.isPresent()) {
        excavationBuilder.setLiveInNest(eggs.get());
      } else {
        excavationBuilder.clearLiveInNest();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationHatchedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (eggs.isPresent()) {
        excavationBuilder.setHatchedShells(eggs.get());
      } else {
        excavationBuilder.clearHatchedShells();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationDeadPippedMutation implements ReportMutation {
    private final Optional<Integer> hatchlings;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (hatchlings.isPresent()) {
        excavationBuilder.setDeadPipped(hatchlings.get());
      } else {
        excavationBuilder.clearDeadPipped();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationLivePippedMutation implements ReportMutation {
    private final Optional<Integer> hatchlings;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (hatchlings.isPresent()) {
        excavationBuilder.setLivePipped(hatchlings.get());
      } else {
        excavationBuilder.clearLivePipped();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationWholeUnhatchedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (eggs.isPresent()) {
        excavationBuilder.setWholeUnhatched(eggs.get());
      } else {
        excavationBuilder.clearWholeUnhatched();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class ExcavationEggsDestroyedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder = updatedReport
          .getInterventionBuilder().getExcavationBuilder();

      if (eggs.isPresent()) {
        excavationBuilder.setEggsDestroyed(eggs.get());
      } else {
        excavationBuilder.clearEggsDestroyed();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class VandalizedDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setVandalizedTimestampMs(
          DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WasVandalizedMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setVandalized(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class PoachedDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPoachedTimestampMs(
          DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WasPoachedMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPoached(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class RootsInvadedMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setRootsInvadedEggshells(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class EggsScatteredMutation implements ReportMutation {
    private final boolean isTrue;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setEggsScatteredByAnother(isTrue);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class NotesMutation implements ReportMutation {
    private final String notes;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();

      if (notes.isEmpty()) {
        updatedReport.clearAdditionalNotes();
      } else {
        updatedReport.setAdditionalNotes(notes);
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WashoutDateMutation implements ReportMutation {
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().getWashOutBuilder()
          .setTimestampMs(DateUtil.getTimestampInMs(year, month, day));
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WashoutStormNameMutation implements ReportMutation {
    private final String stormName;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().getWashOutBuilder()
          .setStormName(stormName);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WashoverDateMutation implements ReportMutation {
    private final Integer ordinal;
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal) ? condition
          .addWashOverBuilder() : condition.getWashOverBuilder(ordinal);
      washOver.setTimestampMs(DateUtil.getTimestampInMs(year, month, day));

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class WashoverStormNameMutation implements ReportMutation {
    private final Integer ordinal;
    private final String name;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal) ? condition
          .addWashOverBuilder() : condition.getWashOverBuilder(ordinal);
      washOver.setStormName(name);

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DeleteWashOverMutation implements ReportMutation {
    private final Integer ordinal;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      condition.removeWashOver(ordinal);

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class PredationDateMutation implements ReportMutation {
    private final Integer ordinal;
    private final int year;
    private final int month;
    private final int day;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
          ? condition.addPreditationBuilder() : condition.getPreditationBuilder(ordinal);
      preditation.setTimestampMs(DateUtil.getTimestampInMs(year, month, day));

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class PredationNumEggsMutation implements ReportMutation {
    private final Integer ordinal;
    private final Optional<Integer> numEggs;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
          ? condition.addPreditationBuilder() : condition.getPreditationBuilder(ordinal);
      if (numEggs.isPresent()) {
        preditation.setNumberOfEggs(numEggs.get());
      } else {
        preditation.clearNumberOfEggs();
      }

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class PredationPredatorMutation implements ReportMutation {
    private final Integer ordinal;
    private final String predator;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
          ? condition.addPreditationBuilder() : condition.getPreditationBuilder(ordinal);
      preditation.setPredator(predator);

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DeletePredationMutation implements ReportMutation {
    private final Integer ordinal;

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      condition.removePreditation(ordinal);

      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class GpsMutation implements ReportMutation {
    private final GpsCoordinates coordinates;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setCoordinates(coordinates);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationNorthMutation implements ReportMutation {
    private final GpsCoordinates coordinates;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getTriangulationBuilder().setNorth(coordinates);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class TriangulationSouthMutation implements ReportMutation {
    private final GpsCoordinates coordinates;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getTriangulationBuilder().setSouth(coordinates);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class NewGpsMutation implements ReportMutation {
    private final GpsCoordinates coordinates;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder().setCoordinates(coordinates);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class AddPhotoMutation implements ReportMutation {
    private final String fileName;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.addImageBuilder().setFileName(fileName);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class DeletePhotoMutation implements ReportMutation {
    private final String fileName;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      for (int i = 0; i < updatedReport.getImageCount(); ++i) {
        if (updatedReport.getImage(i).getFileName().equals(fileName)) {
          updatedReport.removeImage(i);
          break;
        }
      }
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class UpdatePhotoMutation
      implements ReportMutation, ReportMutation.RequiresReportsModel {
    // Don't need this now but leaving it for future use.
    @SuppressWarnings("unused")
    private final String fileName;

    @Setter
    private ReportsModel model;

    @Override
    public Report apply(Report oldReport) {
      // Will implicitly get the update by checking filesystem timestamps.
      model.updateImages(model.getActiveReport());
      return oldReport;
    }
  }

  @Builder(fluent=false)
  public static class SpeciesMutation implements ReportMutation {
    private final Species species;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setSpecies(species);
      return updatedReport.build();
    }
  }

  @Builder(fluent=false)
  public static class SpeciesOtherMutation implements ReportMutation {
    private final String other;

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setSpeciesOther(other);
      return updatedReport.build();
    }
  }
}
