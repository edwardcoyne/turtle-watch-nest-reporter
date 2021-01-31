package com.islandturtlewatch.nest.reporter.data;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto;
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


    public static class NestNumberMutation extends ReportMutation {
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

    public static class PossibleFalseCrawlNumberMutation extends ReportMutation {
        private final Optional<Integer> number;

        public PossibleFalseCrawlNumberMutation(Optional<Integer> number) {
            this.number = number;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setPossibleFalseCrawlNumber(number.or(0));
            return updatedReport.build();
        }
    }

    public static class FalseCrawlNumberMutation extends ReportMutation {
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


    public static class DateFoundMutation extends ReportMutation {
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


    public static class ObserversMutation extends ReportMutation {
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
            extends ReportMutation implements ReportMutation.RequiresReportsModel {
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

    public static class PossibleFalseCrawlMutation extends ReportMutation implements
            ReportMutation.RequiresReportsModel {
        private final boolean isTrue;
        private ReportsModel model;

        @Override
        public void setModel(ReportsModel model) {
            this.model = model;
        }


        public PossibleFalseCrawlMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setPossibleFalseCrawl(isTrue);
            if (isTrue) {
                updatedReport.setPossibleFalseCrawlNumber(model.getHighestPossibleFalseCrawlNumber() + 1);
            } else {
                updatedReport.clearPossibleFalseCrawlNumber();
            }
            return updatedReport.build();
        }
    }

    public static class AbandonedBodyPitsMutation extends ReportMutation {
        private final boolean isTrue;

        public AbandonedBodyPitsMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setCondition(
                    updatedReport.getCondition().toBuilder().setAbandonedBodyPits(isTrue));
            return updatedReport.build();
        }
    }

    public static class AbandonedEggCavitiesMutation extends ReportMutation {
        private final boolean isTrue;

        public AbandonedEggCavitiesMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setCondition(
                    updatedReport.getCondition().toBuilder().setAbandonedEggCavities(isTrue));
            return updatedReport.build();
        }
    }

    public static class NoDiggingMutation extends ReportMutation {
        private final boolean isTrue;

        public NoDiggingMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setCondition(
                    updatedReport.getCondition().toBuilder().setNoDigging(isTrue));
            return updatedReport.build();
        }
    }

    public static class StreetAddressMutation extends ReportMutation {
        private final String address;

        public StreetAddressMutation(String address) {
            this.address = address;
        }

        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (address.isEmpty()) {
                location.clearStreetAddress();
            } else {
                location.setStreetAddress(address);
            }
            return location;
        }
    }

    public static class PropEventsRecordedMutation extends ReportMutation {
        private final NestCondition.ProportionEventsRecorded proportion;

        public PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded proportion) {
            this.proportion = proportion;
        }

        @Override
        public Report apply(Report oldReport) {
            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setCondition(
                    updatedReport.getCondition().toBuilder().setPropEventsRecorded(proportion));
            return updatedReport.build();
        }
    }

    public static class CityMutation extends ReportMutation {
        private final City city;

        public CityMutation(City city) {
            this.city = city;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            return location.setCity(city);
        }
    }

    public static class DetailsMutation extends ReportMutation {
        private final String details;

        public DetailsMutation(String details) {
            this.details = details;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (details.isEmpty()) {
                location.clearDetails();
            } else {
                location.setDetails(details);
            }
            return location;
        }
    }

    public static class ApexToBarrierFtMutation extends ReportMutation {
        private final Optional<Integer> ft;

        public ApexToBarrierFtMutation(Optional<Integer> ft) {
            this.ft = ft;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (ft.isPresent()) {
                location.setApexToBarrierFt(ft.get());
            } else {
                location.clearApexToBarrierFt();
            }
            return location;
        }
    }

    public static class ApexToBarrierInMutation extends ReportMutation {
        private final Optional<Integer> in;

        public ApexToBarrierInMutation(Optional<Integer> in) {
            this.in = in;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (in.isPresent()) {
                location.setApexToBarrierIn(in.get());
            } else {
                location.clearApexToBarrierIn();
            }
            return location;
        }
    }

    public static class WaterToApexFtMutation extends ReportMutation {
        private final Optional<Integer> ft;

        public WaterToApexFtMutation(Optional<Integer> ft) {
            this.ft = ft;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (ft.isPresent()) {
                location.setWaterToApexFt(ft.get());
            } else {
                location.clearWaterToApexFt();
            }
            return location;
        }
    }

    public static class WaterToApexInMutation extends ReportMutation {
        private final Optional<Integer> in;

        public WaterToApexInMutation(Optional<Integer> in) {
            this.in = in;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            if (in.isPresent()) {
                location.setWaterToApexIn(in.get());
            } else {
                location.clearWaterToApexIn();
            }
            return location;
        }
    }

    public static class PlacementMutation extends ReportMutation {
        private final Placement placement;

        public PlacementMutation(Placement placement) {
            this.placement = placement;
        }

        @Override
        public NestLocation.Builder applyLocation(NestLocation.Builder location) {
            return location.setPlacement(placement);
        }
    }

    public static class SeawardOfArmoringStructuresMutation extends ReportMutation {
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

    public static class Within3FeetofStructureMutation extends ReportMutation {
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

    public static class TypeOfStructureMutation extends ReportMutation {
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

    public static class ObstructionsSeawallRocksMutation extends ReportMutation {
        private final boolean isTrue;

        public ObstructionsSeawallRocksMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.NestObstructions.Builder applyObstructions(
                ReportProto.NestLocation.NestObstructions.Builder obstructions) {
            return obstructions.setSeawallRocks(isTrue);
        }
    }


    public static class ObstructionsFurnitureMutation extends ReportMutation {
        private final boolean isTrue;

        public ObstructionsFurnitureMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.NestObstructions.Builder applyObstructions(
                ReportProto.NestLocation.NestObstructions.Builder obstructions) {
            return obstructions.setFurniture(isTrue);
        }
    }

    public static class EscarpmentOver18InchesMutation extends ReportMutation {
        private final boolean isTrue;

        public EscarpmentOver18InchesMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.Builder applyLocation(
                ReportProto.NestLocation.Builder location) {
            return location.setEscarpmentOver18Inches(isTrue);
        }
    }

    public static class WithinProjectArea extends ReportMutation {
        private final boolean isTrue;

        public WithinProjectArea(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.Builder applyLocation(
                ReportProto.NestLocation.Builder location) {
            return location.setNestWithinProjectArea(isTrue);
        }
    }

    public static class WithinReplacementArea extends ReportMutation {
        private final boolean isTrue;

        public WithinReplacementArea(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.Builder applyLocation(
                ReportProto.NestLocation.Builder location) {
            return location.setInCortezGroinReplacementArea(isTrue);
        }
    }

    public static class ObstructionsEscarpmentMutation extends ReportMutation {
        private final boolean isTrue;

        public ObstructionsEscarpmentMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestLocation.NestObstructions.Builder applyObstructions(
                ReportProto.NestLocation.NestObstructions.Builder obstructions) {
            return obstructions.setEscarpment(isTrue);
        }
    }


    public static class ObstructionsOtherMutation extends ReportMutation {
        private final Optional<String> other;

        public ObstructionsOtherMutation(Optional<String> other) {
            this.other = other;
        }

        @Override
        public ReportProto.NestLocation.NestObstructions.Builder applyObstructions(
                ReportProto.NestLocation.NestObstructions.Builder obstructions) {
            if (other.isPresent()) {
                obstructions.setOther(other.get());
            } else {
                obstructions.clearOther();
            }
            return obstructions;
        }
    }

    public static class ProtectionTypeMutation extends ReportMutation {
        private final ProtectionEvent.Type type;

        public ProtectionTypeMutation(ProtectionEvent.Type type) {
            this.type = type;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (protectionEvent.getType() == type) {
                // Hack means it was clicked twice, unset.
                protectionEvent.clearType();
            } else {
                protectionEvent.setType(type);
            }
            return protectionEvent;
        }
    }

    public static class WhyProtectedMutation extends ReportMutation {
        private final ProtectionEvent.Reason reason;

        public WhyProtectedMutation(ProtectionEvent.Reason reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (protectionEvent.getReason() == reason) {
                // Hack means it was clicked twice, unset.
                protectionEvent.clearReason();
            } else {
                protectionEvent.setReason(reason);
            }
            return protectionEvent;
        }
    }

    public static class ReasonOtherValueMutation extends ReportMutation {
        private final String other;

        public ReasonOtherValueMutation(String other) {
            this.other = other;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            return protectionEvent.setReasonOther(other);
        }
    }

    public static class DateProtectedMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public DateProtectedMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (maybeDate.isPresent()) {
                protectionEvent.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                protectionEvent.clearTimestampMs();
            }
            return protectionEvent;
        }
    }

    //  TODO: refactor me
    public static class ProtectionChangeTypeMutation extends ReportMutation {
        private final ProtectionEvent.Type type;

        public ProtectionChangeTypeMutation(ProtectionEvent.Type type) {
            this.type = type;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionChangedEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (protectionEvent.getType() == type) {
                // Hack means it was clicked twice, unset.
                protectionEvent.clearType();
            } else {
                protectionEvent.setType(type);
            }
            return protectionEvent;
        }
    }


    public static class WhyProtectedChangeMutation extends ReportMutation {
        private final ProtectionEvent.Reason reason;

        public WhyProtectedChangeMutation(ProtectionEvent.Reason reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionChangedEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (protectionEvent.getReason() == reason) {
                // Hack means it was clicked twice, unset.
                protectionEvent.clearReason();
            } else {
                protectionEvent.setReason(reason);
            }
            return protectionEvent;
        }
    }

    public static class ReasonOtherValueChangeMutation extends ReportMutation {
        private final String other;

        public ReasonOtherValueChangeMutation(String other) {
            this.other = other;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionChangedEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            return protectionEvent.setReasonOther(other);
        }
    }

    public static class DateProtectedChangeMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public DateProtectedChangeMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionChangedEvent(
                ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
            if (maybeDate.isPresent()) {
                protectionEvent.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                protectionEvent.clearTimestampMs();
            }
            return protectionEvent;
        }
    }

    public static class ChangeNestProtectionReasonMutation extends ReportMutation {
        private final String reason;

        public ChangeNestProtectionReasonMutation(String reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Intervention.Builder applyIntervention(
                ReportProto.Intervention.Builder intervention) {
            return intervention.setProtectionChangedReason(reason);
        }
    }

//end section

    public static class RelocatedMutation extends ReportMutation {
        private final boolean isTrue;

        public RelocatedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            return relocation.setWasRelocated(isTrue);
        }
    }


    public static class RelocatedReasonMutation extends ReportMutation {
        private final Relocation.Reason reason;

        public RelocatedReasonMutation(Relocation.Reason reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            return relocation.setReason(reason);
        }
    }

    public static class NewAddressMutation extends ReportMutation {
        private final String address;

        public NewAddressMutation(String address) {
            this.address = address;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            return relocation.setNewAddress(address);
        }
    }


    public static class EggsRelocatedMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public EggsRelocatedMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            if (eggs.isPresent()) {
                relocation.setEggsRelocated(eggs.get());
            } else {
                relocation.clearEggsRelocated();
            }
            return relocation;
        }
    }


    public static class EggsDestroyedMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public EggsDestroyedMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            if (eggs.isPresent()) {
                relocation.setEggsDestroyed(eggs.get());
            } else {
                relocation.clearEggsDestroyed();
            }
            return relocation;
        }
    }


    public static class DateRelocatedMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public DateRelocatedMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            if (maybeDate.isPresent()) {
                relocation.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                relocation.clearTimestampMs();
            }
            return relocation;
        }
    }


    public static class WasAdoptedMutation extends ReportMutation {
        private final boolean isTrue;

        public WasAdoptedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.Intervention.Builder applyIntervention(
                ReportProto.Intervention.Builder intervention) {
            return intervention.setAdopted(isTrue);
        }
    }


    public static class HatchDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public HatchDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (maybeDate.isPresent()) {
                condition.setHatchTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                condition.clearHatchTimestampMs();
            }
            return condition;
        }
    }


    public static class AdditionalHatchDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public AdditionalHatchDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (maybeDate.isPresent()) {
                condition.setAdditionalHatchTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                condition.clearAdditionalHatchTimestampMs();
            }
            return condition;
        }
    }


    public static class DisorentationMutation extends ReportMutation {
        private final boolean isTrue;

        public DisorentationMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setDisorientation(isTrue);
        }
    }

    public static class WasExcavatedMutation extends ReportMutation {
        private final boolean isTrue;

        public WasExcavatedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            return excavation.setExcavated(isTrue);
        }
    }

    public static class ExcavationFailureMutation extends ReportMutation {
        private final ExcavationFailureReason reason;

        public ExcavationFailureMutation(ExcavationFailureReason reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            return excavation.setFailureReason(reason);
        }
    }

    public static class AdopteeNameMutation extends ReportMutation {
        private final String adoptee;

        public AdopteeNameMutation(String adoptee) {
            this.adoptee = adoptee;
        }

        @Override
        public ReportProto.Intervention.Builder applyIntervention(
                ReportProto.Intervention.Builder intervention) {
            return intervention.setAdoptee(adoptee);
        }
    }

    public static class ExcavationFailureOtherMutation extends ReportMutation {
        private final String reason;

        public ExcavationFailureOtherMutation(String reason) {
            this.reason = reason;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            return excavation.setFailureOther(reason);
        }
    }

    public static class ExcavationDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public ExcavationDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (maybeDate.isPresent()) {
                excavation.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                excavation.clearTimestampMs();
            }
            return excavation;
        }
    }

    public static class ExcavationDeadInNestMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public ExcavationDeadInNestMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (eggs.isPresent()) {
                excavation.setDeadInNest(eggs.get());
            } else {
                excavation.clearDeadInNest();
            }
            return excavation;
        }
    }

    public static class ExcavationLiveInNestMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public ExcavationLiveInNestMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (eggs.isPresent()) {
                excavation.setLiveInNest(eggs.get());
            } else {
                excavation.clearLiveInNest();
            }
            return excavation;
        }
    }

    public static class ExcavationHatchedMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public ExcavationHatchedMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (eggs.isPresent()) {
                excavation.setHatchedShells(eggs.get());
            } else {
                excavation.clearHatchedShells();
            }
            return excavation;
        }
    }

    public static class ExcavationDeadPippedMutation extends ReportMutation {
        private final Optional<Integer> hatchlings;

        public ExcavationDeadPippedMutation(Optional<Integer> hatchlings) {
            this.hatchlings = hatchlings;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (hatchlings.isPresent()) {
                excavation.setDeadPipped(hatchlings.get());
            } else {
                excavation.clearDeadPipped();
            }
            return excavation;
        }
    }

    public static class ExcavationLivePippedMutation extends ReportMutation {
        private final Optional<Integer> hatchlings;

        public ExcavationLivePippedMutation(Optional<Integer> hatchlings) {
            this.hatchlings = hatchlings;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (hatchlings.isPresent()) {
                excavation.setLivePipped(hatchlings.get());
            } else {
                excavation.clearLivePipped();
            }
            return excavation;
        }
    }

    public static class ExcavationWholeUnhatchedMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public ExcavationWholeUnhatchedMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (eggs.isPresent()) {
                excavation.setWholeUnhatched(eggs.get());
            } else {
                excavation.clearWholeUnhatched();
            }
            return excavation;
        }
    }


    public static class ExcavationEggsDestroyedMutation extends ReportMutation {
        private final Optional<Integer> eggs;

        public ExcavationEggsDestroyedMutation(Optional<Integer> eggs) {
            this.eggs = eggs;
        }

        @Override
        public ReportProto.Excavation.Builder applyExcavation(
                ReportProto.Excavation.Builder excavation) {
            if (eggs.isPresent()) {
                excavation.setEggsDestroyed(eggs.get());
            } else {
                excavation.clearEggsDestroyed();
            }
            return excavation;
        }
    }

    public static class VandalizedDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public VandalizedDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (maybeDate.isPresent()) {
                condition.setVandalizedTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                condition.clearVandalizedTimestampMs();
            }
            return condition;
        }
    }

    public static class WasVandalizedMutation extends ReportMutation {
        private final boolean isTrue;

        public WasVandalizedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setVandalized(isTrue);
        }
    }

    public static class PredatedPriorMutation extends ReportMutation {
        private final Integer ordinal;
        private final PreditationEvent.PredationTimeOption option;

        public PredatedPriorMutation(Integer ordinal, PreditationEvent.PredationTimeOption option) {
            this.ordinal = ordinal;
            this.option = option;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal) ?
                    PreditationEvent.newBuilder() : condition.getPreditation(ordinal).toBuilder();
            preditation.setPredatedPrior(option);
            return condition.setPreditation(ordinal, preditation);
        }
    }

    public static class PredatorSpinnerMutation extends ReportMutation {
        private final Integer ordinal;
        private final String predator;

        public PredatorSpinnerMutation(Integer ordinal, String predator) {
            this.ordinal = ordinal;
            this.predator = predator;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            Preconditions.checkNotNull(ordinal);

            PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal) ?
                    PreditationEvent.newBuilder() : condition.getPreditation(ordinal).toBuilder();

            preditation.setPredatorSpinnerText(predator);

            return condition.setPreditation(ordinal, preditation);
        }
    }

    public static class VandalismTypeMutation extends ReportMutation {
        private final Optional<NestCondition.VandalismType> vandalismType;

        public VandalismTypeMutation(Optional<NestCondition.VandalismType> vandalismType) {
            this.vandalismType = vandalismType;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (vandalismType.isPresent()) {
                condition.setVandalismType(vandalismType.get());
            } else {
                condition.clearVandalismType();
            }
            return condition;
        }
    }

    public static class GhostCrabDamageAtMost10EggsMutation extends ReportMutation {
        private final boolean isTrue;

        public GhostCrabDamageAtMost10EggsMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setGhostDamage10OrLess(isTrue);
        }
    }

    public static class WasStormImpactPriorToHatch extends ReportMutation {
        private final boolean isTrue;

        public WasStormImpactPriorToHatch(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.StormImpact.Builder applyStormImpact(
                ReportProto.NestCondition.StormImpact.Builder impact) {
            return impact.setEventPriorToHatching(isTrue);
        }
    }

    public static class WasNestDugIntoMutation extends ReportMutation {
        private final boolean isTrue;

        public WasNestDugIntoMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setNestDugInto(isTrue);
        }
    }

    public static class PoachedDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public PoachedDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (maybeDate.isPresent()) {
                condition.setPoachedTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                condition.clearPoachedTimestampMs();
            }
            return condition;
        }
    }

    public static class CompleteWashoutTimingMutation extends ReportMutation {
        private final NestCondition.WashoutTimeOption timing;

        public CompleteWashoutTimingMutation(NestCondition.WashoutTimeOption timing) {
            this.timing = timing;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setCompleteWashoutTiming(timing);
        }
    }

    public static class PartialWashoutTimingMutation extends ReportMutation {
        private final NestCondition.WashoutTimeOption timing;

        public PartialWashoutTimingMutation(NestCondition.WashoutTimeOption timing) {
            this.timing = timing;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setPartialWashoutTiming(timing);
        }
    }
/*
  public static class WashoutPriorToHatchingMutation extends ReportMutation {
    private final boolean isTrue;

    public WashoutPriorToHatchingMutation(boolean isTrue) {
      this.isTrue = isTrue;
    }
    @Override
    public ReportProto.NestCondition.Builder applyCondition(
            ReportProto.NestCondition.Builder condition) {
      Report.Builder updatedReport = oldReport.toBuilder();
      updatedReport.getConditionBuilder().getWashOutBuilder().setEventPriorToHatching(isTrue);
      return updatedReport.build();
    }
  }

  public static class WasPostHatchWashout extends ReportMutation {
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
 */

    public static class WasPoachedMutation extends ReportMutation {
        private final boolean isTrue;

        public WasPoachedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setPoached(isTrue);
        }
    }

    public static class PoachedEggsRemovedMutation extends ReportMutation {
        private final boolean isTrue;

        public PoachedEggsRemovedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setPoachedEggsRemoved(isTrue);
        }
    }

    public static class RootsInvadedMutation extends ReportMutation {
        private final boolean isTrue;

        public RootsInvadedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setRootsInvadedEggshells(isTrue);
        }
    }

    public static class NestDepredatedMutation extends ReportMutation {
        private final boolean isTrue;

        public NestDepredatedMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setNestDepredated(isTrue);
        }
    }

    public static class EggsDamagedByAnotherTurtleMutation extends ReportMutation {
        private final boolean isTrue;

        public EggsDamagedByAnotherTurtleMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setEggsDamagedByAnotherTurtle(isTrue);
        }
    }

    public static class EggsScatteredDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public EggsScatteredDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            if (maybeDate.isPresent()) {
                condition.setEggsScatteredByAnotherTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                condition.clearEggsScatteredByAnotherTimestampMs();
            }
            return condition;
        }
    }

    public static class EggsScatteredMutation extends ReportMutation {
        private final boolean isTrue;

        public EggsScatteredMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setEggsScatteredByAnother(isTrue);
        }
    }

    public static class NotesMutation extends ReportMutation {
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

    public static class PartialWashoutDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public PartialWashoutDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.WashEvent.Builder applyPartialWashout(
                ReportProto.NestCondition.WashEvent.Builder washout) {
            if (maybeDate.isPresent()) {
                washout.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                washout.clearTimestampMs();
            }
            return washout;
        }
    }

    public static class PartialWashoutStormNameMutation extends ReportMutation {
        private final String stormName;

        public PartialWashoutStormNameMutation(String stormName) {
            this.stormName = stormName;
        }

        @Override
        public ReportProto.NestCondition.WashEvent.Builder applyPartialWashout(
                ReportProto.NestCondition.WashEvent.Builder washout) {
            return washout.setStormName(stormName);
        }
    }

    public static class PartialWashoutPriorToHatchingMutation extends ReportMutation {
        private final boolean isTrue;

        public PartialWashoutPriorToHatchingMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.WashEvent.Builder applyPartialWashout(
                ReportProto.NestCondition.WashEvent.Builder washout) {
            return washout.setEventPriorToHatching(isTrue);
        }
    }

    public static class OtherImpactDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public OtherImpactDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.StormImpact.Builder applyStormImpact(
                ReportProto.NestCondition.StormImpact.Builder impact) {
            if (maybeDate.isPresent()) {
                impact.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                impact.clearTimestampMs();
            }
            return impact;
        }
    }

    public static class OtherImpactStormNameMutation extends ReportMutation {
        private final String stormName;

        public OtherImpactStormNameMutation(String stormName) {
            this.stormName = stormName;
        }

        @Override
        public ReportProto.NestCondition.StormImpact.Builder applyStormImpact(
                ReportProto.NestCondition.StormImpact.Builder impact) {
            return impact.setStormName(stormName);
        }
    }

    public static class OtherImpactDetailsMutation extends ReportMutation {
        private final String details;

        public OtherImpactDetailsMutation(String details) {
            this.details = details;
        }

        @Override
        public ReportProto.NestCondition.StormImpact.Builder applyStormImpact(
                ReportProto.NestCondition.StormImpact.Builder impact) {
            return impact.setOtherImpact(details);
        }
    }

    public static class WashoutDateMutation extends ReportMutation {
        private final Optional<Date> maybeDate;

        public WashoutDateMutation(Optional<Date> maybeDate) {
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.WashEvent.Builder applyWashout(
                ReportProto.NestCondition.WashEvent.Builder washout) {
            if (maybeDate.isPresent()) {
                washout.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                washout.clearTimestampMs();
            }
            return washout;
        }
    }

    public static class WashoutStormNameMutation extends ReportMutation {
        private final String stormName;

        public WashoutStormNameMutation(String stormName) {
            this.stormName = stormName;
        }

        @Override
        public ReportProto.NestCondition.WashEvent.Builder applyWashout(
                ReportProto.NestCondition.WashEvent.Builder washout) {
            return washout.setStormName(stormName);
        }
    }

    //Accretion Section
    public static class AccretionOccurredPriorToHatchingMutation extends ReportMutation {
        private final Integer ordinal;
        private final boolean isTrue;

        public AccretionOccurredPriorToHatchingMutation(Integer ordinal, boolean isTrue) {
            this.ordinal = ordinal;
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder accretion = (condition.getAccretionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getAccretion(ordinal).toBuilder();
            accretion.setEventPriorToHatching(isTrue);
            return condition.setAccretion(ordinal, accretion);
        }
    }

    public static class AccretionDateMutation extends ReportMutation {
        private final Integer ordinal;
        private final Optional<Date> maybeDate;

        public AccretionDateMutation(Integer ordinal, Optional<Date> maybeDate) {
            this.ordinal = ordinal;
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder accretion = (condition.getAccretionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getAccretion(ordinal).toBuilder();
            if (maybeDate.isPresent()) {
                accretion.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                accretion.clearTimestampMs();
            }
            return condition.setAccretion(ordinal, accretion);
        }
    }

    public static class AccretionStormNameMutation extends ReportMutation {
        private final Integer ordinal;
        private final String name;

        public AccretionStormNameMutation(Integer ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder accretion = (condition.getAccretionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getAccretion(ordinal).toBuilder();
            accretion.setStormName(name);
            return condition.setAccretion(ordinal, accretion);
        }
    }

    public static class DeleteAccretionMutation extends ReportMutation {
        private final Integer ordinal;

        public DeleteAccretionMutation(Integer ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.removeAccretion(ordinal);
        }
    }
//End Accretion section

    //Erosion Section
    public static class ErosionOccurredPriorToHatchingMutation extends ReportMutation {
        private final Integer ordinal;
        private final boolean isTrue;

        public ErosionOccurredPriorToHatchingMutation(Integer ordinal, boolean isTrue) {
            this.ordinal = ordinal;
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder erosion = (condition.getErosionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getErosion(ordinal).toBuilder();
            erosion.setEventPriorToHatching(isTrue);
            return condition.setErosion(ordinal, erosion);
        }
    }

    public static class ErosionDateMutation extends ReportMutation {
        private final Integer ordinal;
        private final Optional<Date> maybeDate;

        public ErosionDateMutation(Integer ordinal, Optional<Date> maybeDate) {
            this.ordinal = ordinal;
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder erosion = (condition.getErosionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getErosion(ordinal).toBuilder();
            if (maybeDate.isPresent()) {
                erosion.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                erosion.clearTimestampMs();
            }
            return condition.setErosion(ordinal, erosion);
        }
    }

    public static class ErosionStormNameMutation extends ReportMutation {
        private final Integer ordinal;
        private final String name;

        public ErosionStormNameMutation(Integer ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder erosion = (condition.getErosionCount() <= ordinal)
                    ? WashEvent.newBuilder() : condition.getErosion(ordinal).toBuilder();
            erosion.setStormName(name);
            return condition.setErosion(ordinal, erosion);
        }
    }

    public static class DeleteErosionMutation extends ReportMutation {
        private final Integer ordinal;

        public DeleteErosionMutation(Integer ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.removeErosion(ordinal);
        }
    }
//End Erosion section

    public static class WashoverDateMutation extends ReportMutation {
        private final Integer ordinal;
        private final Optional<Date> maybeDate;

        public WashoverDateMutation(Integer ordinal, Optional<Date> maybeDate) {
            this.ordinal = ordinal;
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal) ?
                    WashEvent.newBuilder() : condition.getWashOver(ordinal).toBuilder();
            if (maybeDate.isPresent()) {
                washOver.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                washOver.clearTimestampMs();
            }
            return condition.setWashOver(ordinal, washOver);
        }
    }

    public static class WashoverStormNameMutation extends ReportMutation {
        private final Integer ordinal;
        private final String name;

        public WashoverStormNameMutation(Integer ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder washOver = (condition.getWashOverCount() <= ordinal) ?
                    WashEvent.newBuilder() : condition.getWashOver(ordinal).toBuilder();
            washOver.setStormName(name);
            return condition.setWashOver(ordinal, washOver);
        }
    }

    public static class DeleteWashOverMutation extends ReportMutation {
        private final Integer ordinal;

        public DeleteWashOverMutation(Integer ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            Preconditions.checkNotNull(ordinal);
            return condition.removeWashOver(ordinal);
        }
    }

    public static class InundatedEventDateMutation extends ReportMutation {
        private final Integer ordinal;
        private final Optional<Date> maybeDate;

        public InundatedEventDateMutation(Integer ordinal, Optional<Date> maybeDate) {
            this.ordinal = ordinal;
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder inundatedEvent = (condition.getInundatedEventCount() <= ordinal) ?
                    WashEvent.newBuilder() : condition.getInundatedEvent(ordinal).toBuilder();
            if (maybeDate.isPresent()) {
                inundatedEvent.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                inundatedEvent.clearTimestampMs();
            }
            return condition.setInundatedEvent(ordinal, inundatedEvent);
        }
    }

    public static class InundatedEventStormNameMutation extends ReportMutation {
        private final Integer ordinal;
        private final String name;

        public InundatedEventStormNameMutation(Integer ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder inundatedEvent = (condition.getInundatedEventCount() <= ordinal) ?
                    WashEvent.newBuilder() : condition.getInundatedEvent(ordinal).toBuilder();
            inundatedEvent.setStormName(name);
            return condition.setInundatedEvent(ordinal, inundatedEvent);
        }
    }

    public static class InundatedEventOccuredPriorToHatchingMutation extends ReportMutation {
        private final Integer ordinal;
        private final boolean isTrue;

        public InundatedEventOccuredPriorToHatchingMutation(Integer ordinal, boolean isTrue) {
            this.ordinal = ordinal;
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            WashEvent.Builder inundatedEvent = (condition.getInundatedEventCount() <= ordinal) ?
                    WashEvent.newBuilder() : condition.getInundatedEvent(ordinal).toBuilder();
            inundatedEvent.setEventPriorToHatching(isTrue);
            return condition.setInundatedEvent(ordinal, inundatedEvent);
        }
    }

    public static class DeleteInundatedEventMutation extends ReportMutation {
        private final Integer ordinal;

        public DeleteInundatedEventMutation(Integer ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.removeInundatedEvent(ordinal);
        }
    }

    public static class PredationDateMutation extends ReportMutation {
        private final Integer ordinal;
        Optional<Date> maybeDate;

        public PredationDateMutation(Integer ordinal, Optional<Date> maybeDate) {
            this.ordinal = ordinal;
            this.maybeDate = maybeDate;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
                    ? PreditationEvent.newBuilder() : condition.getPreditation(ordinal).toBuilder();
            if (maybeDate.isPresent()) {
                preditation.setTimestampMs(maybeDate.get().getTimestampMs());
            } else {
                preditation.clearTimestampMs();
            }

            return condition.setPreditation(ordinal, preditation);
        }
    }

    public static class PredationNumEggsMutation extends ReportMutation {
        private final Integer ordinal;
        private final Optional<Integer> numEggs;

        public PredationNumEggsMutation(Integer ordinal, Optional<Integer> numEggs) {
            this.ordinal = ordinal;
            this.numEggs = numEggs;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
                    ? PreditationEvent.newBuilder() : condition.getPreditation(ordinal).toBuilder();
            if (numEggs.isPresent()) {
                preditation.setNumberOfEggs(numEggs.get());
            } else {
                preditation.clearNumberOfEggs();
            }

            return condition.setPreditation(ordinal, preditation);
        }
    }

    public static class PredationPredatorMutation extends ReportMutation {
        private final Integer ordinal;
        private final String predator;

        public PredationPredatorMutation(Integer ordinal, String predator) {
            this.ordinal = ordinal;
            this.predator = predator;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            PreditationEvent.Builder preditation = (condition.getPreditationCount() <= ordinal)
                    ? PreditationEvent.newBuilder() : condition.getPreditation(ordinal).toBuilder();
            preditation.setPredator(predator);

            return condition.setPreditation(ordinal, preditation);
        }
    }

    public static class ActivelyRecordPredationMutation extends ReportMutation {
        private final boolean isTrue;

        public ActivelyRecordPredationMutation(boolean isTrue) {
            this.isTrue = isTrue;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setActivelyRecordEvents(isTrue);
        }
    }

    public static class DeletePredationMutation extends ReportMutation {
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

    public static class GpsMutation extends ReportMutation {
        private final GpsCoordinates coordinates;

        public GpsMutation(GpsCoordinates coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public Report apply(Report oldReport) {

            Report.Builder updatedReport = oldReport.toBuilder();
            updatedReport.setLocation(updatedReport.getLocation().toBuilder().setCoordinates(coordinates));
            return updatedReport.build();
        }
    }

    public static class RelocationGpsMutation extends ReportMutation {
        private final GpsCoordinates coordinates;

        public RelocationGpsMutation(GpsCoordinates coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public ReportProto.Relocation.Builder applyRelocation(
                ReportProto.Relocation.Builder relocation) {
            return relocation.setCoordinates(coordinates);
        }
    }

    public static class SpeciesMutation extends ReportMutation {
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

    public static class ControlMethodDescriptionMutation extends ReportMutation {
        private final String other;

        public ControlMethodDescriptionMutation(String other) {
            this.other = other;
        }

        @Override
        public ReportProto.NestCondition.Builder applyCondition(
                ReportProto.NestCondition.Builder condition) {
            return condition.setDescribeControlMethods(other);
        }
    }

    public static class SpeciesOtherMutation extends ReportMutation {
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

