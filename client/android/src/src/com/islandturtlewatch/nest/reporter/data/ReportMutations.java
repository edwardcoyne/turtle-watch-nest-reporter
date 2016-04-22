package com.islandturtlewatch.nest.reporter.data;

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
import com.islandturtlewatch.nest.data.ReportProto.Relocation;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
import com.islandturtlewatch.nest.data.ReportProto.Report.Species;

public class ReportMutations {
  private ReportMutations() {
  } // namespace really.


  public static class NestNumberMutation implements ReportMutation {
    private final Optional<Integer> number;

    public NestNumberMutation(Optional<Integer> number) {
      this.number = number;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setNestNumber(number.or(0));
      return updatedReport.build();
    }
  }


  public static class FalseCrawlNumberMutation implements ReportMutation {
    private final Optional<Integer> number;

    public FalseCrawlNumberMutation(Optional<Integer> number) {
      this.number = number;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setFalseCrawlNumber(number.or(0));
      return updatedReport.build();
    }
  }


  public static class DateFoundMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public DateFoundMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      if (maybeDate.isPresent()) {
        return oldReport.toBuilder()
                .setTimestampFoundMs(maybeDate.get().getTimestampMs())
                .build();
      } else {
        return oldReport.toBuilder()
                .clearTimestampFoundMs()
                .build();
      }
    }

  }


  public static class ObserversMutation implements ReportMutation {
    private final String observers;

    public ObserversMutation(String observers) {
      this.observers = observers;
    }

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


  public static class NestStatusMutation
          implements ReportMutation, ReportMutation.RequiresReportsModel {
    private final NestStatus status;

    public NestStatusMutation(NestStatus status) {
      this.status = status;
    }

    private ReportsModel model;

    @Override
    public void setModel(ReportsModel model) {
      this.model = model;
    }

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

  public static class PossibleFalseCrawlMutation implements ReportMutation {
    private final boolean isTrue;

    public PossibleFalseCrawlMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setPossibleFalseCrawl(isTrue);
      return updatedReport.build();
    }
  }

  public static class AbandonedBodyPitsMutation implements ReportMutation {
    private final boolean isTrue;

    public AbandonedBodyPitsMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setAbandonedBodyPits(isTrue);
      return updatedReport.build();
    }
  }

  public static class AbandonedEggCavitiesMutation implements ReportMutation {
    private final boolean isTrue;

    public AbandonedEggCavitiesMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setAbandonedEggCavities(isTrue);
      return updatedReport.build();
    }
  }

  public static class NoDiggingMutation implements ReportMutation {
    private final boolean isTrue;

    public NoDiggingMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setNoDigging(isTrue);
      return updatedReport.build();
    }
  }

  public static class StreetAddressMutation implements ReportMutation {
    private final String address;

    public StreetAddressMutation(String address) {
      this.address = address;
    }

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



  public static class PropEventsRecordedMutation implements ReportMutation {
    private final NestCondition.ProportionEventsRecorded proportion;
    public PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded proportion) {
      this.proportion = proportion;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPropEventsRecorded(proportion);
      return updatedReport.build();
    }
  }

  public static class CityMutation implements ReportMutation {
    private final City city;

    public CityMutation(City city) {
      this.city = city;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setCity(city);
      return updatedReport.build();
    }
  }


  public static class DetailsMutation implements ReportMutation {
    private final String details;

    public DetailsMutation(String details) {
      this.details = details;
    }

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


  public static class ApexToBarrierFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    public ApexToBarrierFtMutation(Optional<Integer> ft) {
      this.ft = ft;
    }

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


  public static class ApexToBarrierInMutation implements ReportMutation {
    private final Optional<Integer> in;

    public ApexToBarrierInMutation(Optional<Integer> in) {
      this.in = in;
    }

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


  public static class WaterToApexFtMutation implements ReportMutation {
    private final Optional<Integer> ft;

    public WaterToApexFtMutation(Optional<Integer> ft) {
      this.ft = ft;
    }

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


  public static class WaterToApexInMutation implements ReportMutation {
    private final Optional<Integer> in;

    public WaterToApexInMutation(Optional<Integer> in) {
      this.in = in;
    }

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


  public static class PlacementMutation implements ReportMutation {
    private final Placement placement;

    public PlacementMutation(Placement placement) {
      this.placement = placement;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setPlacement(placement);
      return updatedReport.build();
    }
  }

  public static class SeawardOfArmoringStructuresMutation implements ReportMutation {
    private final boolean isTrue;

    public SeawardOfArmoringStructuresMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }
    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setNestSeawardOfArmoringStructure(isTrue);
      return updatedReport.build();
    }
  }

  public static class Within3FeetofStructureMutation implements ReportMutation {
    private final boolean isTrue;

    public Within3FeetofStructureMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }
    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.setWithin3FeetOfStructure(isTrue);
      return updatedReport.build();
    }
  }

  public static class TypeOfStructureMutation implements ReportMutation {
    private final String structureType;

    public TypeOfStructureMutation(String details) {
      this.structureType = details;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      if (structureType.isEmpty()) {
        updatedReport.clearTypeOfStructure();
      } else {
        updatedReport.setTypeOfStructure(structureType);
      }
      return updatedReport.build();
    }
  }

  public static class ObstructionsSeawallRocksMutation implements ReportMutation {
    private final boolean isTrue;

    public ObstructionsSeawallRocksMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
              .setSeawallRocks(isTrue);
      return updatedReport.build();
    }
  }


  public static class ObstructionsFurnitureMutation implements ReportMutation {
    private final boolean isTrue;

    public ObstructionsFurnitureMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
              .setFurniture(isTrue);
      return updatedReport.build();
    }
  }

  public static class EscarpmentOver18InchesMutation implements ReportMutation {
    private final boolean isTrue;

    public EscarpmentOver18InchesMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setEscarpmentOver18Inches(isTrue);
      return updatedReport.build();
    }
  }

  public static class WithinReplacementArea implements ReportMutation {
    private final boolean isTrue;

    public WithinReplacementArea(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().setInCortezGroinReplacementArea(isTrue);
      return updatedReport.build();
    }
  }

  public static class ObstructionsEscarpmentMutation implements ReportMutation {
    private final boolean isTrue;

    public ObstructionsEscarpmentMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getLocationBuilder().getObstructionsBuilder()
              .setEscarpment(isTrue);
      return updatedReport.build();
    }
  }


  public static class ObstructionsOtherMutation implements ReportMutation {
    private final Optional<String> other;

    public ObstructionsOtherMutation(Optional<String> other) {
      this.other = other;
    }

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


  public static class ProtectionTypeMutation implements ReportMutation {
    private final ProtectionEvent.Type type;

    public ProtectionTypeMutation(ProtectionEvent.Type type) {
      this.type = type;
    }

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


  public static class WhyProtectedMutation implements ReportMutation {
    private final ProtectionEvent.Reason reason;

    public WhyProtectedMutation(ProtectionEvent.Reason reason) {
      this.reason = reason;
    }

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


  public static class RelocatedMutation implements ReportMutation {
    private final boolean isTrue;

    public RelocatedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
              .setWasRelocated(isTrue);
      return updatedReport.build();
    }
  }


  public static class RelocatedReasonMutation implements ReportMutation {
    private final Relocation.Reason reason;

    public RelocatedReasonMutation(Relocation.Reason reason) {
      this.reason = reason;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
              .setReason(reason);
      return updatedReport.build();
    }
  }

  public static class NewAddressMutation implements ReportMutation {
    private final String address;

    public NewAddressMutation(String address) {
      this.address = address;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getRelocationBuilder()
              .setNewAddress(address);
      return updatedReport.build();
    }
  }


  public static class EggsRelocatedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public EggsRelocatedMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class EggsDestroyedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public EggsDestroyedMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class DateRelocatedMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public DateRelocatedMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Relocation.Builder relocationBuilder =
              updatedReport.getInterventionBuilder().getRelocationBuilder();
      if (maybeDate.isPresent()) {
        relocationBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        relocationBuilder.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class WasAdoptedMutation implements ReportMutation {
    private final boolean isTrue;

    public WasAdoptedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().setAdopted(isTrue);
      return updatedReport.build();
    }
  }

  public static class DateProtectedMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public DateProtectedMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      ProtectionEvent.Builder eventBuilder =
              updatedReport.getInterventionBuilder().getProtectionEventBuilder();
      if (maybeDate.isPresent()) {
        eventBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        eventBuilder.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class HatchDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public HatchDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
      if (maybeDate.isPresent()) {
        conditionBuilder.setHatchTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        conditionBuilder.clearHatchTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class AdditionalHatchDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public AdditionalHatchDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
      if (maybeDate.isPresent()) {
        conditionBuilder.setAdditionalHatchTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        conditionBuilder.clearAdditionalHatchTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class DisorentationMutation implements ReportMutation {
    private final boolean isTrue;

    public DisorentationMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setDisorientation(isTrue);
      return updatedReport.build();
    }
  }


  public static class WasExcavatedMutation implements ReportMutation {
    private final boolean isTrue;

    public WasExcavatedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setExcavated(isTrue);
      return updatedReport.build();
    }
  }


  public static class ExcavationFailureMutation implements ReportMutation {
    private final ExcavationFailureReason reason;

    public ExcavationFailureMutation(ExcavationFailureReason reason) {
      this.reason = reason;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setFailureReason(reason);
      return updatedReport.build();
    }
  }

  public static class AdopteeNameMutation implements ReportMutation {
    private final String adoptee;
    public AdopteeNameMutation(String adoptee) {
      this.adoptee = adoptee;
    }
    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().setAdoptee(adoptee);
      return updatedReport.build();
    }
  }

  public static class ExcavationFailureOtherMutation implements ReportMutation {
    private final String reason;

    public ExcavationFailureOtherMutation(String reason) {
      this.reason = reason;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getInterventionBuilder().getExcavationBuilder().setFailureOther(reason);
      return updatedReport.build();
    }
  }


  public static class ExcavationDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public ExcavationDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      Excavation.Builder excavationBuilder =
              updatedReport.getInterventionBuilder().getExcavationBuilder();
      if (maybeDate.isPresent()) {
        excavationBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        excavationBuilder.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class ExcavationDeadInNestMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public ExcavationDeadInNestMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class ExcavationLiveInNestMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public ExcavationLiveInNestMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class ExcavationHatchedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public ExcavationHatchedMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class ExcavationDeadPippedMutation implements ReportMutation {
    private final Optional<Integer> hatchlings;

    public ExcavationDeadPippedMutation(Optional<Integer> hatchlings) {
      this.hatchlings = hatchlings;
    }

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


  public static class ExcavationLivePippedMutation implements ReportMutation {
    private final Optional<Integer> hatchlings;

    public ExcavationLivePippedMutation(Optional<Integer> hatchlings) {
      this.hatchlings = hatchlings;
    }

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


  public static class ExcavationWholeUnhatchedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public ExcavationWholeUnhatchedMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class ExcavationEggsDestroyedMutation implements ReportMutation {
    private final Optional<Integer> eggs;

    public ExcavationEggsDestroyedMutation(Optional<Integer> eggs) {
      this.eggs = eggs;
    }

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


  public static class VandalizedDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public VandalizedDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
      if (maybeDate.isPresent()) {
        conditionBuilder.setVandalizedTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        conditionBuilder.clearVandalizedTimestampMs();
      }
      return updatedReport.build();
    }
  }


  public static class WasVandalizedMutation implements ReportMutation {
    private final boolean isTrue;

    public WasVandalizedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setVandalized(isTrue);
      return updatedReport.build();
    }
  }

  public static class PredatorSpinnerMutation implements ReportMutation {
    private final Integer ordinal;
    private final String predator;

    public PredatorSpinnerMutation(Integer ordinal, String predator) {
      this.ordinal = ordinal;
      this.predator = predator;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
              ? condition.addPreditationBuilder() : condition.getPreditationBuilder(ordinal);
      preditation.setPredatorSpinnerText(predator);

      return updatedReport.build();
    }
  }


  public static class VandalismTypeMutation implements ReportMutation {
    private final Optional<NestCondition.VandalismType> vandalismType;

    public VandalismTypeMutation(Optional<NestCondition.VandalismType> vandalismType) {
      this.vandalismType = vandalismType;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder builder = updatedReport
              .getConditionBuilder();

      if (vandalismType.isPresent()) {
        builder.setVandalismType(vandalismType.get());
      } else {
        builder.clearVandalismType();
      }

      return updatedReport.build();
    }
  }

  public static class GhostCrabDamageAtMost10EggsMutation implements ReportMutation {
    private final boolean isTrue;

    public GhostCrabDamageAtMost10EggsMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setGhostDamage10OrLess(isTrue);
      return updatedReport.build();
    }
  }

  public static class WasNestDugIntoMutation implements ReportMutation {
    private final boolean isTrue;

    public WasNestDugIntoMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setNestDugInto(isTrue);
      return updatedReport.build();
    }
  }

  public static class PoachedDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public PoachedDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
      if (maybeDate.isPresent()) {
        conditionBuilder.setPoachedTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        conditionBuilder.clearPoachedTimestampMs();
      }
      return updatedReport.build();
    }
  }

  public static class WasPostHatchWashout implements ReportMutation {
    private final boolean isTrue;

    public WasPostHatchWashout(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPostHatchWashout(isTrue);
      return updatedReport.build();
    }
  }

  public static class WasPoachedMutation implements ReportMutation {
    private final boolean isTrue;

    public WasPoachedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPoached(isTrue);
      return updatedReport.build();
    }
  }

  public static class PoachedEggsRemovedMutation implements ReportMutation {
    private final boolean isTrue;

    public PoachedEggsRemovedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setPoachedEggsRemoved(isTrue);
      return updatedReport.build();
    }
  }

  public static class RootsInvadedMutation implements ReportMutation {
    private final boolean isTrue;

    public RootsInvadedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setRootsInvadedEggshells(isTrue);
      return updatedReport.build();
    }
  }

  public static class NestInundatedDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public NestInundatedDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
      if (maybeDate.isPresent()) {
        conditionBuilder.setNestInundatedTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        conditionBuilder.clearNestInundatedTimestampMs();
      }
      return updatedReport.build();
    }
  }
    public static class NestInundatedMutation implements ReportMutation {
      private final boolean isTrue;

      public NestInundatedMutation(boolean isTrue) {
        this.isTrue = isTrue;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getConditionBuilder().setNestInundated(isTrue);
        return updatedReport.build();
      }
    }

  public static class NestDepredatedMutation implements ReportMutation {
    private final boolean isTrue;
    public NestDepredatedMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply (Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setNestDepredated(isTrue);
      return updatedReport.build();
    }
  }

  public static class EggsDamagedByAnotherTurtleMutation implements ReportMutation {
    private final boolean isTrue;
    public EggsDamagedByAnotherTurtleMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }

    @Override
    public Report apply(Report oldreport) {
      Report.Builder updatedReport = oldreport.toBuilder();
      updatedReport.getConditionBuilder().setEggsDamagedByAnotherTurtle(isTrue);
      return updatedReport.build();
    }
  }

    public static class EggsScatteredDateMutation implements ReportMutation {
      private final Optional<Date> maybeDate;

      public EggsScatteredDateMutation(Optional<Date> maybeDate) {
        this.maybeDate = maybeDate;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        NestCondition.Builder conditionBuilder = updatedReport.getConditionBuilder();
        if (maybeDate.isPresent()) {
          conditionBuilder.setEggsScatteredByAnotherTimestampMs(maybeDate.get().getTimestampMs());
        } else {
          conditionBuilder.clearEggsScatteredByAnotherTimestampMs();
        }
        return updatedReport.build();
      }
    }


    public static class EggsScatteredMutation implements ReportMutation {
      private final boolean isTrue;

      public EggsScatteredMutation(boolean isTrue) {
        this.isTrue = isTrue;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getConditionBuilder().setEggsScatteredByAnother(isTrue);
        return updatedReport.build();
      }
    }


    public static class NotesMutation implements ReportMutation {
      private final String notes;

      public NotesMutation(String notes) {
        this.notes = notes;
      }

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


    public static class PartialWashoutDateMutation implements ReportMutation {
      private final Optional<Date> maybeDate;

      public PartialWashoutDateMutation(Optional<Date> maybeDate) {
        this.maybeDate = maybeDate;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        WashEvent.Builder pWashOutBuilder = updatedReport.getConditionBuilder().getPartialWashoutBuilder();
        if (maybeDate.isPresent()) {
          pWashOutBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
        } else {
          pWashOutBuilder.clearTimestampMs();
        }
        return updatedReport.build();
      }
    }

    public static class PartialWashoutStormNameMutation implements ReportMutation {
      private final String stormName;

      public PartialWashoutStormNameMutation(String stormName) {
        this.stormName = stormName;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getConditionBuilder().getPartialWashoutBuilder().setStormName(stormName);
        return updatedReport.build();
      }
    }

  public static class OtherImpactDateMutation implements ReportMutation {
    private final Optional<Date> maybeDate;

    public OtherImpactDateMutation(Optional<Date> maybeDate) {
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.StormImpact.Builder stormImpactBuilder = updatedReport.getConditionBuilder().getStormImpactBuilder();
      if (maybeDate.isPresent()) {
        stormImpactBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        stormImpactBuilder.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }
  public static class OtherImpactStormNameMutation implements ReportMutation {
    private final String stormName;

    public OtherImpactStormNameMutation(String stormName) {
      this.stormName = stormName;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().getStormImpactBuilder().setStormName(stormName);
      return updatedReport.build();
    }
  }
  public static class OtherImpactDetailsMutation implements ReportMutation {
    private final String details;

    public OtherImpactDetailsMutation(String details) {
      this.details = details;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().getStormImpactBuilder().setOtherImpact(details);
      return updatedReport.build();
    }
  }




  public static class WashoutDateMutation implements ReportMutation {
      private final Optional<Date> maybeDate;

      public WashoutDateMutation(Optional<Date> maybeDate) {
        this.maybeDate = maybeDate;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        WashEvent.Builder washOutBuilder = updatedReport.getConditionBuilder().getWashOutBuilder();
        if (maybeDate.isPresent()) {
          washOutBuilder.setTimestampMs(maybeDate.get().getTimestampMs());
        } else {
          washOutBuilder.clearTimestampMs();
        }
        return updatedReport.build();
      }
    }


    public static class WashoutStormNameMutation implements ReportMutation {
      private final String stormName;

      public WashoutStormNameMutation(String stormName) {
        this.stormName = stormName;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getConditionBuilder().getWashOutBuilder()
                .setStormName(stormName);
        return updatedReport.build();
      }
    }


  public static class AccretionDateMutation implements ReportMutation {
    private final Integer ordinal;
    private final Optional<Date> maybeDate;

    public AccretionDateMutation(Integer ordinal, Optional<Date> maybeDate) {
      this.ordinal = ordinal;
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder  accretion = (condition.getAccretionCount() <= ordinal) ? condition
              .addAccretionBuilder() : condition.getAccretionBuilder(ordinal);
      if (maybeDate.isPresent()) {
        accretion.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        accretion.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }
  public static class AccretionStormNameMutation implements ReportMutation {
    private final Integer ordinal;
    private final String name;

    public AccretionStormNameMutation(Integer ordinal, String name) {
      this.ordinal = ordinal;
      this.name = name;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder accretion = (condition.getAccretionCount() <= ordinal) ? condition
              .addAccretionBuilder() : condition.getAccretionBuilder(ordinal);
      accretion.setStormName(name);

      return updatedReport.build();
    }
  }

  public static class DeleteAccretionMutation implements ReportMutation {
    private final Integer ordinal;

    public DeleteAccretionMutation(Integer ordinal) {
      this.ordinal = ordinal;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      condition.removeAccretion(ordinal);

      return updatedReport.build();
    }
  }


    public static class WashoverDateMutation implements ReportMutation {
      private final Integer ordinal;
      private final Optional<Date> maybeDate;

      public WashoverDateMutation(Integer ordinal, Optional<Date> maybeDate) {
        this.ordinal = ordinal;
        this.maybeDate = maybeDate;
      }

      @Override
      public Report apply(Report oldReport) {
        Preconditions.checkNotNull(ordinal);
        Report.Builder updatedReport = oldReport.toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal) ? condition
                .addWashOverBuilder() : condition.getWashOverBuilder(ordinal);
        if (maybeDate.isPresent()) {
          washOver.setTimestampMs(maybeDate.get().getTimestampMs());
        } else {
          washOver.clearTimestampMs();
        }
        return updatedReport.build();
      }
    }


    public static class WashoverStormNameMutation implements ReportMutation {
      private final Integer ordinal;
      private final String name;

      public WashoverStormNameMutation(Integer ordinal, String name) {
        this.ordinal = ordinal;
        this.name = name;
      }

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


    public static class DeleteWashOverMutation implements ReportMutation {
      private final Integer ordinal;

      public DeleteWashOverMutation(Integer ordinal) {
        this.ordinal = ordinal;
      }

      @Override
      public Report apply(Report oldReport) {
        Preconditions.checkNotNull(ordinal);
        Report.Builder updatedReport = oldReport.toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        condition.removeWashOver(ordinal);

        return updatedReport.build();
      }
    }

  public static class InundatedEventDateMutation implements ReportMutation {
    private final Integer ordinal;
    private final Optional<Date> maybeDate;

    public InundatedEventDateMutation(Integer ordinal, Optional<Date> maybeDate) {
      this.ordinal = ordinal;
      this.maybeDate = maybeDate;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder inundatedEvent = (condition.getInundatedEventCount() <= ordinal) ?
              condition.addInundatedEventBuilder() : condition.getInundatedEventBuilder(ordinal);
      if (maybeDate.isPresent()) {
        inundatedEvent.setTimestampMs(maybeDate.get().getTimestampMs());
      } else {
        inundatedEvent.clearTimestampMs();
      }
      return updatedReport.build();
    }
  }

  public static class InundatedEventStormNameMutation implements ReportMutation {
    private final Integer ordinal;
    private final String name;

    public InundatedEventStormNameMutation(Integer ordinal, String name) {
      this.ordinal = ordinal;
      this.name = name;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      WashEvent.Builder inundatedEvent = (condition.getInundatedEventCount() <= ordinal) ?
              condition.addInundatedEventBuilder() : condition.getInundatedEventBuilder(ordinal);
      inundatedEvent.setStormName(name);

      return updatedReport.build();
    }
  }

  public static class DeleteInundatedEventMutation implements ReportMutation {
    private final Integer ordinal;

    public DeleteInundatedEventMutation(Integer ordinal) {
      this.ordinal = ordinal;
    }

    @Override
    public Report apply(Report oldReport) {
      Preconditions.checkNotNull(ordinal);
      Report.Builder updatedReport = oldReport.toBuilder();
      NestCondition.Builder condition = updatedReport.getConditionBuilder();

      condition.removeInundatedEvent(ordinal);
      return updatedReport.build();
    }
  }

    public static class PredationDateMutation implements ReportMutation {
      private final Integer ordinal;
      Optional<Date> maybeDate;

      public PredationDateMutation(Integer ordinal, Optional<Date> maybeDate) {
        this.ordinal = ordinal;
        this.maybeDate = maybeDate;
      }

      @Override
      public Report apply(Report oldReport) {
        Preconditions.checkNotNull(ordinal);
        Report.Builder updatedReport = oldReport.toBuilder();
        NestCondition.Builder condition = updatedReport.getConditionBuilder();

        PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
                ? condition.addPreditationBuilder() : condition.getPreditationBuilder(ordinal);
        if (maybeDate.isPresent()) {
          preditation.setTimestampMs(maybeDate.get().getTimestampMs());
        } else {
          preditation.clearTimestampMs();
        }

        return updatedReport.build();
      }
    }


    public static class PredationNumEggsMutation implements ReportMutation {
      private final Integer ordinal;
      private final Optional<Integer> numEggs;

      public PredationNumEggsMutation(Integer ordinal, Optional<Integer> numEggs) {
        this.ordinal = ordinal;
        this.numEggs = numEggs;
      }

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


    public static class PredationPredatorMutation implements ReportMutation {
      private final Integer ordinal;
      private final String predator;

      public PredationPredatorMutation(Integer ordinal, String predator) {
        this.ordinal = ordinal;
        this.predator = predator;
      }

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

  public static class ActivelyRecordPredationMutation implements ReportMutation {
    private final boolean isTrue;


    public ActivelyRecordPredationMutation( boolean isTrue) {
      this.isTrue = isTrue;
    }
      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
       updatedReport.getConditionBuilder().setActivelyRecordEvents(isTrue);
        return updatedReport.build();
      }
    }


  public static class DeletePredationMutation implements ReportMutation {
      private final Integer ordinal;

      public DeletePredationMutation(Integer ordinal) {
        this.ordinal = ordinal;
      }

      @Override
      public Report apply(Report oldReport) {
        Preconditions.checkNotNull(ordinal);
        Report.Builder updatedReport = oldReport.toBuilder();

        return updatedReport.build();
      }
    }


    public static class GpsMutation implements ReportMutation {
      private final GpsCoordinates coordinates;

      public GpsMutation(GpsCoordinates coordinates) {
        this.coordinates = coordinates;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getLocationBuilder().setCoordinates(coordinates);
        return updatedReport.build();
      }
    }

    public static class NewGpsMutation implements ReportMutation {
      private final GpsCoordinates coordinates;

      public NewGpsMutation(GpsCoordinates coordinates) {
        this.coordinates = coordinates;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.getInterventionBuilder().getRelocationBuilder().setCoordinates(coordinates);
        return updatedReport.build();
      }
    }


    public static class AddPhotoMutation implements ReportMutation {
      private final String fileName;

      public AddPhotoMutation(String fileName) {
        this.fileName = fileName;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.addImageBuilder().setFileName(fileName);
        return updatedReport.build();
      }
    }


    public static class DeletePhotoMutation implements ReportMutation {
      private final String fileName;

      public DeletePhotoMutation(String fileName) {
        this.fileName = fileName;
      }

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


    public static class UpdatePhotoMutation
            implements ReportMutation, ReportMutation.RequiresReportsModel {
      // Don't need this now but leaving it for future use.
      @SuppressWarnings("unused")
      private final String fileName;

      public UpdatePhotoMutation(String fileName) {
        this.fileName = fileName;
      }

      private ReportsModel model;

      @Override
      public void setModel(ReportsModel model) {
        this.model = model;
      }

      @Override
      public Report apply(Report oldReport) {
        // Will implicitly get the update by checking filesystem timestamps.
        model.updateImages(model.getActiveReport());
        return oldReport;
      }
    }


    public static class SpeciesMutation implements ReportMutation {
      private final Species species;

      public SpeciesMutation(Species species) {
        this.species = species;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.setSpecies(species);
        return updatedReport.build();
      }
    }
  public static class ControlMethodDescriptionMutation implements ReportMutation {
    private final String other;

    public ControlMethodDescriptionMutation(String other) {
      this.other = other;
    }

    @Override
    public Report apply(Report oldReport) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().setDescribeControlMethods(other);
      return updatedReport.build();
    }
  }

    public static class SpeciesOtherMutation implements ReportMutation {
      private final String other;

      public SpeciesOtherMutation(String other) {
        this.other = other;
      }

      @Override
      public Report apply(Report oldReport) {
        Report.Builder updatedReport = oldReport.toBuilder();
        updatedReport.setSpeciesOther(other);
        return updatedReport.build();
      }
    }
  }

