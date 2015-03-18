package com.islandturtlewatch.nest.reporter.backend.storage.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Builder;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Index;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.City;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;

/**
 * Represents a report, the parent of multiple {@link StoredReportVersion}.
 */
@Entity
@Builder(fluent=false)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredReport {
  @Getter
  @Id
  long reportId;

  @Getter
  @Parent
  @NonNull
  Key<User> user;

  @Getter @Setter
  long latestVersion;

  // Results of merge errors.
  @Getter
  List<Long> conflictingBranches;

  // What verson conflict branches were resolved to.
  // This no longer works, objectify dropped support,
  // May have stored values so we cannot change the type until we are sure there is no old data.
  //@Getter
  //Map<Long, Long> conflictingVersionResolutions;

  @Getter
  @Index
  Date dateFound;

  @Getter
  @Index
  Date dateHatched;

  @Getter
  @Index
  City city;

  @Getter @Setter
  @Index
  // deprecated, use state instead.
  // TODO(edcoyne): see if we can remove this in the future.
  boolean active;

  @Getter @Setter
  @Index
  ReportRef.State state;

  public StoredReport updateFromReport(Report report) {
    dateFound = report.hasTimestampFoundMs() ? new Date(report.getTimestampFoundMs()) : null;
    dateHatched = report.getCondition().hasHatchTimestampMs()
        ? new Date(report.getCondition().getHatchTimestampMs()) : null;
    city = report.getLocation().hasCity()
        ? report.getLocation().getCity() : null;
    return this;
  }

  public Key<StoredReport> getKey() {
    return Key.create(this);
  }
}
