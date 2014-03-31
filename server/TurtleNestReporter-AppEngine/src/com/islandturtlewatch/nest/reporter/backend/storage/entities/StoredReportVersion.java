package com.islandturtlewatch.nest.reporter.backend.storage.entities;

import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Builder;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Parent;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;

/**
 * Leaf node entity, represents one version of a report.
 */
@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredReportVersion {
  @Id
  long version;

  @Getter
  @Load
  @Parent
  @NonNull
  Ref<StoredReport> storedReport;

  @Transient @Ignore
  @Getter
  private Report report;

  byte[] reportBytes;

  @OnSave
  void persistReport() {
    reportBytes = report.toByteArray();
  }

  @OnLoad
  void restoreReport() {
    Preconditions.checkNotNull(reportBytes, "Trying to init from a null version.");
    try {
      report = Report.parseFrom(reportBytes);
    } catch (InvalidProtocolBufferException e) {
      Throwables.propagate(e);
    }
  }

  public ReportWrapper toReportWrapper() {
    ReportWrapper.Builder builder = ReportWrapper.newBuilder();
    builder.setReport(this.report);
    ReportRef.Builder refBuilder = builder.getRefBuilder();
    refBuilder.setReportId(this.storedReport.get().getReportId());
    refBuilder.setOwnerId(this.storedReport.get().getUser().getId());
    refBuilder.setVersion(this.version);
    return builder.build();
  }
}
