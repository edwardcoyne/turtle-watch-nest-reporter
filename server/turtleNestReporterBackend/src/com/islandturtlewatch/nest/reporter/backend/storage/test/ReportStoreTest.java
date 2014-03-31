package com.islandturtlewatch.nest.reporter.backend.storage.test;

import junit.framework.TestCase;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;

public class ReportStoreTest extends TestCase {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private ReportStore store;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    store = new ReportStore();
    store.init();
  }

  @Override
  protected void tearDown() throws Exception {
    helper.tearDown();
    super.tearDown();
  }

  public void testAddUser_newUser() {
    long userId = 3L;
    assertFalse(store.hasUser(userId));
    assertTrue(store.addUser(userId));
    assertTrue(store.hasUser(userId));
  }

  public void testAddReport() {
    long userId = 3L;
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper result = store.addReport(userId, report);

    assertEquals(report, result.getReport());
    assertEquals(1L, result.getReportId());
    assertEquals(1L, result.getVersion());
    assertEquals(userId, result.getOwnerId());

    assertEquals(result,
        store.getReportLatestVersion(userId, result.getReportId()));
  }

  public void testUpdateReport() {
    long userId = 3L;
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper createResult = store.addReport(userId, report);

    Report updateReport = report.toBuilder().setAdditionalNotes("TEST").build();
    ReportWrapper update =
        createResult.toBuilder().setReport(updateReport).build();
    ReportWrapper result = store.updateReport(update);
    assertEquals(updateReport, result.getReport());
    assertEquals(2L, result.getVersion());
    assertEquals(1L, result.getReportId());
    assertEquals(userId, result.getOwnerId());

    assertEquals(result,
        store.getReportLatestVersion(userId, result.getReportId()));
  }

  public void testUpdateReport_conflicts() {
    long userId = 3L;
    store.addUser(userId);

    Report report = Report.newBuilder().setTimestampFoundMs(500L).build();
    ReportWrapper createResult = store.addReport(userId, report);
    ReportWrapper update =
        createResult.toBuilder().setVersion(0L).build();
    try {
      store.updateReport(update);
      fail("Should have thrown exception, version is not latest.");
    } catch (UnsupportedOperationException ex) {
      // Expected
    }
  }
}
