package com.islandturtlewatch.nest.reporter.backend.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.StoredReport;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.StoredReportVersion;
import com.islandturtlewatch.nest.reporter.backend.storage.entities.User;

public class ReportStore {
  public void init() {
    ObjectifyService.register(User.class);
    ObjectifyService.register(StoredReport.class);
    ObjectifyService.register(StoredReportVersion.class);
  }

  public boolean addUser(long userId) {
    User user = User.builder()
        .setId(userId)
        .build();
    backend().save().entity(user).now();
    return true;
  }

  public ReportWrapper addReport(long userId, Report report) {
    User user = loadUser(userId);

    StoredReport storedReport = StoredReport.builder()
        .setReportId(user.getHighestReportId() + 1)
        .setUser(user.getKey())
        .setLatestVersion(1) // start at 1 as we are adding it now.
        .build()
        .updateFromReport(report);

    StoredReportVersion reportVersion = StoredReportVersion.builder()
        .setStoredReport(Ref.create(storedReport))
        .setReport(report)
        .setVersion(storedReport.getLatestVersion())
        .build();

    backend().save().entities(storedReport, reportVersion).now();

    user.setHighestReportId(storedReport.getReportId());

    return reportVersion.toReportWrapper();
  }

  public ReportWrapper updateReport(ReportWrapper wrapper) {
    User user = loadUser(wrapper.getOwnerId());
    StoredReport report = loadReport(user, wrapper.getReportId());

    if (report.getLatestVersion() != wrapper.getVersion()) {
      // TODO(edcoyne): plug in conflict handling here.
      throw new UnsupportedOperationException();
    }
    long newVersion = report.getLatestVersion() + 1;

    StoredReportVersion reportVersion = StoredReportVersion.builder()
        .setStoredReport(Ref.create(report))
        .setVersion(newVersion)
        .setReport(wrapper.getReport())
        .build();

    report.setLatestVersion(newVersion);

    backend().save().entities(reportVersion, report).now();
    return reportVersion.toReportWrapper();
  }

  public boolean hasUser(long userId) {
    return tryLoadUser(userId).isPresent();
  }

  public ReportWrapper getReportLatestVersion(long userId, long reportId) {
    User user = loadUser(userId);
    StoredReport report = loadReport(user, reportId);
    StoredReportVersion reportVersion = loadReportVersion(report, report.getLatestVersion());
    return reportVersion.toReportWrapper();
  }

  public ImmutableList<ReportWrapper> getLatestReportsForUser(long userId) {
    User user = loadUser(userId);
    List<StoredReport> reports = backend().load().type(StoredReport.class).ancestor(user).list();
    List<ReportWrapper> versions = new ArrayList<>();
    for (StoredReport report : reports) {
      versions.add(loadReportVersion(report, report.getLatestVersion()).toReportWrapper());
    }
    return ImmutableList.copyOf(versions);
  }

  private User loadUser(long userId) {
    Optional<User> userOpt = tryLoadUser(userId);
    Preconditions.checkArgument(userOpt.isPresent(), "Missing user: " + userId);
    return userOpt.get();
  }

  private Optional<User> tryLoadUser(long userId) {
    return Optional.fromNullable(
        backend().load().type(User.class).id(userId).now());
  }

  private StoredReport loadReport(User user, long reportId) {
    StoredReport report = backend().load().type(StoredReport.class).parent(user).id(reportId).now();
    Preconditions.checkNotNull(report,
        "Missing report, user:" + user.getId() + "report: " + reportId);
    return report;
  }

  private StoredReportVersion loadReportVersion(StoredReport report, long version) {
    StoredReportVersion reportVersion =
        backend().load().type(StoredReportVersion.class).parent(report).id(version).now();
    Preconditions.checkNotNull(report,
        "Missing report, report: " + report.getReportId() + " version:" + version);
    return reportVersion;
  }

  private static Objectify backend() {
    return ObjectifyService.ofy();
  }
}
