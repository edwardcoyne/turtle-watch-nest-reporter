package com.islandturtlewatch.nest.reporter.backend.storage.test;

import junit.framework.TestCase;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;

import java.io.Closeable;

public class ReportStoreTest extends TestCase {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private ReportStore store;
  Closeable session;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    // Need to clear Objectify caches.
    ObjectifyService.ofy().clear();
    session = ObjectifyService.begin();
    store = new ReportStore();
    store.init();
  }

  @Override
  protected void tearDown() throws Exception {
    session.close();
    helper.tearDown();
    super.tearDown();
  }

  public void testAddUser_newUser() {
    String userId = "USER_ID";
    assertFalse(store.hasUser(userId));
    assertTrue(store.addUser(userId));
    assertTrue(store.hasUser(userId));
  }

  public void testAddReport() {
    String userId = "USER_ID";
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper result = store.addReport(userId, report);

    assertEquals(report, result.getReport());
    ReportRef ref = result.getRef();
    assertEquals(1L, ref.getReportId());
    assertEquals(1L, ref.getVersion());
    assertEquals(userId, ref.getOwnerId());

    assertEquals(result,
        store.getReportLatestVersion(userId, ref.getReportId()));
  }

  public void testUpdateReport() {
    String userId = "USER_ID";
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper createResult = store.addReport(userId, report);

    Report updateReport = report.toBuilder().setAdditionalNotes("TEST").build();
    ReportWrapper update = createResult.toBuilder().setReport(updateReport).build();
    ReportWrapper result = store.updateReport(update);
    assertEquals(updateReport, result.getReport());
    ReportRef ref = result.getRef();
    assertEquals(2L, ref.getVersion());
    assertEquals(1L, ref.getReportId());
    assertEquals(userId, ref.getOwnerId());

    assertEquals(result,
        store.getReportLatestVersion(userId, ref.getReportId()));
  }

  public void testUpdateReport_conflicts() {
    String userId = "USER_ID";
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper createResult = store.addReport(userId, report);
    ReportWrapper update =  createResult.toBuilder()
        .setRef(createResult.getRef().toBuilder().setVersion(0L))
        .build();
    try {
      store.updateReport(update);
      fail("Should have thrown exception, version is not latest.");
    } catch (UnsupportedOperationException ex) {
      // Expected
    }
  }

  public void testGetLatestReportsForUser() {
    String userId = "USER_ID";
    store.addUser(userId);

    Report report1 = Report.newBuilder().setTimestampFoundMs(100L).build();
    Report report2 = Report.newBuilder().setTimestampFoundMs(200L).build();

    ReportWrapper createResult1_1 = store.addReport(userId, report1);
    ReportWrapper createResult2_1 = store.addReport(userId, report2);

    ReportWrapper createResult1_2 = store.updateReport(createResult1_1);
    ReportWrapper createResult2_2 = store.updateReport(createResult2_1);

    ReportWrapper createResult2_3 = store.updateReport(createResult2_2);

    ImmutableList<ReportWrapper> latest = store.getLatestReportsForUser(userId);
    assertTrue(latest.contains(createResult1_2));
    assertTrue(latest.contains(createResult2_3));

    assertFalse(latest.contains(createResult1_1));
    assertFalse(latest.contains(createResult2_2));
  }
}
