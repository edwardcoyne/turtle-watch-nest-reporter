package com.islandturtlewatch.nest.reporter.backend.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
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
    Preconditions.checkArgument(!hasUser(userId), "Trying to insert duplicate user: " + userId);
    User user = User.builder()
        .setId(userId)
        .build();
    backend().save().entity(user).now();
    return true;
  }

  public boolean hasUser(long userId) {
    return tryLoadUser(userId).isPresent();
  }

  public ReportWrapper addReport(final long userId, final Report report) {
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        return doAddReport(userId, report);
      }});
  }

  public ReportWrapper updateReport(final ReportWrapper wrapper) {
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        return doUpdateReport(wrapper);
      }});
  }

  public ReportWrapper getReportLatestVersion(long userId, final long reportId) {
    final User user = loadUser(userId);
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        StoredReport report = loadReport(user, reportId);
        StoredReportVersion reportVersion = loadReportVersion(report, report.getLatestVersion());
        return reportVersion.toReportWrapper();
      }});
  }

  public ImmutableList<ReportWrapper> getLatestReportsForUser(final long userId) {
    return backend().transact(new Work<ImmutableList<ReportWrapper>>(){
      @Override
      public ImmutableList<ReportWrapper> run() {
        return doGetLatestReportsForUser(userId);
      }});
  }

  private ReportWrapper doAddReport(long userId, Report report) {
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
    backend().save().entity(user);

    return reportVersion.toReportWrapper();
  }

  private ReportWrapper doUpdateReport(ReportWrapper wrapper) {
    ReportRef ref = wrapper.getRef();
    User user = loadUser(ref.getOwnerId());
    StoredReport report = loadReport(user, ref.getReportId());

    if (report.getLatestVersion() != ref.getVersion()) {
      // TODO(edcoyne): plug in conflict handling here.
      throw new UnsupportedOperationException(
          "Attempting to update with old version, We don't support conflict resultion yet...");
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

  private ImmutableList<ReportWrapper> doGetLatestReportsForUser(long userId) {
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
    // Docs suggest never caching the result of this.
    return ObjectifyService.ofy();
  }
}
