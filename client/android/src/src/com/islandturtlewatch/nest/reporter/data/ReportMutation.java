package com.islandturtlewatch.nest.reporter.data;

import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.data.ReportProto.Report;

public abstract class ReportMutation {
  public Report apply(Report oldReport) {
    Report.Builder updatedReport = oldReport.toBuilder();
    updatedReport.setLocation(applyLocation(updatedReport.getLocation().toBuilder()));
    updatedReport.setIntervention(applyIntervention(updatedReport.getIntervention().toBuilder()));
    updatedReport.setCondition(applyCondition(updatedReport.getCondition().toBuilder()));
    return updatedReport.build();
  }

  public ReportProto.NestLocation.Builder applyLocation(ReportProto.NestLocation.Builder location) {
    location.setObstructions(applyObstructions(location.getObstructions().toBuilder()));
    return location;
  }

  public ReportProto.NestLocation.NestObstructions.Builder applyObstructions(
          ReportProto.NestLocation.NestObstructions.Builder obstructions) {
    return obstructions;
  }

  public ReportProto.Intervention.Builder applyIntervention(
          ReportProto.Intervention.Builder intervention) {
    intervention.setProtectionEvent(
            applyProtectionEvent(intervention.getProtectionEvent().toBuilder()));
    intervention.setProtectionChangedEvent(
            applyProtectionChangedEvent(intervention.getProtectionChangedEvent().toBuilder()));
    intervention.setRelocation(applyRelocation(intervention.getRelocation().toBuilder()));
    intervention.setExcavation(applyExcavation(intervention.getExcavation().toBuilder()));
    return intervention;
  }

  public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionEvent(
          ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
    return protectionEvent;
  }

  public ReportProto.Intervention.ProtectionEvent.Builder applyProtectionChangedEvent(
          ReportProto.Intervention.ProtectionEvent.Builder protectionEvent) {
    return protectionEvent;
  }

  public ReportProto.Excavation.Builder applyExcavation(
          ReportProto.Excavation.Builder excavation) {
    return excavation;
  }

  public ReportProto.Relocation.Builder applyRelocation(
          ReportProto.Relocation.Builder relocation) {
    return relocation;
  }

  public ReportProto.NestCondition.Builder applyCondition(
          ReportProto.NestCondition.Builder condition) {
    condition.setStormImpact(applyStormImpact(condition.getStormImpact().toBuilder()));
    condition.setPartialWashout(applyPartialWashout(condition.getPartialWashout().toBuilder()));
    condition.setWashOut(applyWashout(condition.getWashOut().toBuilder()));
    return condition;
  }

  public ReportProto.NestCondition.StormImpact.Builder applyStormImpact(
          ReportProto.NestCondition.StormImpact.Builder impact) {
    return impact;
  }

  public ReportProto.NestCondition.WashEvent.Builder applyPartialWashout(
          ReportProto.NestCondition.WashEvent.Builder washout) {
    return washout;
  }

  public ReportProto.NestCondition.WashEvent.Builder applyWashout(
          ReportProto.NestCondition.WashEvent.Builder washout) {
    return washout;
  }

  public interface RequiresReportsModel {
    //this is pointless
    public void setModel(ReportsModel model);
  }
}
