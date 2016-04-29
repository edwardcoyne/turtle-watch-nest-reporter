package com.islandturtlewatch.nest.reporter.backend.storage;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.java.Log;

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

// Needs to be thread safe.
@Log
public class ReportStore {

  public void init() {
    ObjectifyService.register(User.class);
    ObjectifyService.register(StoredReport.class);
    ObjectifyService.register(StoredReportVersion.class);
  }

  public boolean addUser(String userId) {
    Preconditions.checkArgument(!hasUser(userId), "Trying to insert duplicate user: " + userId);
    User user = User.builder()
        .setId(userId)
        .build();
    backend().save().entity(user).now();
    log.info("Added user: " + userId);
    return true;
  }

  public boolean hasUser(String userId) {
    return tryLoadUser(userId).isPresent();
  }

  public ReportWrapper addReport(final String userId, final Report report) {
    log.info("Adding user: " + userId + " report:" + report);
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        return doAddReport(userId, report);
      }});
  }

  public ReportWrapper updateReport(final ReportWrapper wrapper) {
    log.info("Updating ref: " + wrapper.getRef());
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        return doUpdateReport(wrapper);
      }});
  }

  public void deleteReport(final ReportRef ref) {
    log.info("deleteing ref: " + ref);
    backend().transact(new Work<Boolean>(){
      @Override
      public Boolean run() {
        doDeleteReport(ref);
        return true;
      }});
  }

  public ReportWrapper getReportLatestVersion(String userId, final long reportId) {
    final User user = loadOrCreateUser(userId);
    return backend().transact(new Work<ReportWrapper>(){
      @Override
      public ReportWrapper run() {
        StoredReport report = loadReport(user, reportId);
        StoredReportVersion reportVersion = loadReportVersion(report, report.getLatestVersion());
        return reportVersion.toReportWrapper();
      }});
  }

  public ImmutableList<ReportWrapper> getLatestReportsForUser(final String userId) {
    return backend().transact(new Work<ImmutableList<ReportWrapper>>(){
      @Override
      public ImmutableList<ReportWrapper> run() {
        return doGetLatestReportsForUser(userId);
      }});
  }

  public ImmutableList<ReportWrapper> getActiveReports() {
    List<StoredReport> reports =
        backend().load().type(StoredReport.class)
          .filter("state =", ReportRef.State.ACTIVE)
          .list();
    List<ReportWrapper> versions = new ArrayList<>();
    for (StoredReport report : reports) {
      Optional<StoredReportVersion> version =
          tryLoadReportVersion (report, report.getLatestVersion());
      if (version.isPresent()) {
        versions.add(version.get().toReportWrapper());
      } else {
        log.warning("Unable to load report " + report.getReportId()
            + " version: " + report.getLatestVersion()
            + " for user: " + report.getUser().getName());
      }
    }
    return ImmutableList.copyOf(versions);
  }

  public void markAllReportsInactive() {
    for (ReportWrapper wrapper : getActiveReports()) {
      doUpdateReport(doSetReportOld(wrapper));
    }
  }

  private ReportWrapper doAddReport(String userId, Report report) {
    User user = loadOrCreateUser(userId);

    StoredReport storedReport = StoredReport.builder()
        .setReportId(user.getHighestReportId() + 1)
        .setUser(user.getKey())
        .setLatestVersion(1) // start at 1 as we are adding it now.
        .setState(ReportRef.State.ACTIVE)
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
    User user = loadOrCreateUser(ref.getOwnerId());
    StoredReport report = loadReport(user, ref.getReportId());

    if (report.getLatestVersion() != ref.getVersion()) {
      // TODO(edcoyne): plug in conflict handling here.
      //throw new UnsupportedOperationException(
      log.warning(
          "Attempting to update with old version, We don't support conflict resolution yet..."
          + " Server version: " + report.getLatestVersion() + " Client version: "
              + ref.getVersion());
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

  private ReportWrapper doSetReportOld(ReportWrapper wrapper) {
    ReportWrapper.Builder builder = wrapper.toBuilder();
    builder.getRefBuilder().setState(ReportRef.State.OLD);
    return builder.build();
  }

  private void doDeleteReport(ReportRef ref) {
    User user = loadOrCreateUser(ref.getOwnerId());
    StoredReport report = loadReport(user, ref.getReportId());

    if (report.getLatestVersion() != ref.getVersion()) {
      // TODO(edcoyne): plug in conflict handling here.
      //throw new UnsupportedOperationException(
      log.warning(
          "Attempting to update with old version, We don't support conflict resultion yet..."
          + " Server version: " + report.getLatestVersion() + " Client version: "
              + ref.getVersion());
    }
    report.setState(ReportRef.State.DELETED);

    backend().save().entities(report).now();
  }

  private ImmutableList<ReportWrapper> doGetLatestReportsForUser(String userId) {
    User user = loadOrCreateUser(userId);
    List<StoredReport> reports = backend().load()
        .type(StoredReport.class).ancestor(user)
        .filter("state =", ReportRef.State.ACTIVE)
        .list();
    List<ReportWrapper> versions = new ArrayList<>();
    for (StoredReport report : reports) {
      versions.add(loadReportVersion(report, report.getLatestVersion()).toReportWrapper());
    }
    return ImmutableList.copyOf(versions);
  }

  private User loadOrCreateUser(String userId) {
    Optional<User> userOpt = tryLoadUser(userId);
    if (!userOpt.isPresent()) {
      addUser(userId);
      userOpt = tryLoadUser(userId);
    }
    Preconditions.checkArgument(userOpt.isPresent(), "Unable to create user: " + userId);
    return userOpt.get();
  }

  public Optional<StoredReport> tryLoadReport(String userId, long reportId) {
    Optional<User> user = tryLoadUser(userId);
    if (!user.isPresent()) {
      return Optional.absent();
    }
    return Optional.fromNullable(loadReport(user.get(), reportId));
  }

  private Optional<User> tryLoadUser(String userId) {
    return Optional.fromNullable(
        backend().load().type(User.class).id(userId).now());
  }

  private StoredReport loadReport(User user, long reportId) {
    StoredReport report = backend().load().type(StoredReport.class).parent(user).id(reportId).now();
    Preconditions.checkNotNull(report,
        "Missing report, user:" + user.getId() + "report: " + reportId);
    return report;
  }

  private Optional<StoredReportVersion> tryLoadReportVersion(StoredReport report, long version) {
    StoredReportVersion reportVersion =
        backend().load().type(StoredReportVersion.class).parent(report).id(version).now();
    return Optional.fromNullable(reportVersion);
  }

  private StoredReportVersion loadReportVersion(StoredReport report, long version) {
    Optional<StoredReportVersion> reportVersion = tryLoadReportVersion(report, version);
    Preconditions.checkArgument(reportVersion.isPresent(),
        "Missing report, report: " + report.getReportId() + " version:" + version);
    return reportVersion.get();
  }


  private static Objectify backend() {
    // Docs suggest never caching the result of this.
    return ObjectifyService.ofy();
  }
}
