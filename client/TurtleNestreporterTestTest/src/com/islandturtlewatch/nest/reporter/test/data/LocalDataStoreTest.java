package com.islandturtlewatch.nest.reporter.test.data;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

public class LocalDataStoreTest extends AndroidTestCase {
  private LocalDataStore store;

  @Override
  public Context getContext() {
    // DBs will be created in memory, ensure no leakage between tests.
    return new RenamingDelegatingContext(super.getContext(),
        ":memory:");
  }

  @Override
  protected void setUp() throws Exception {
    store = new LocalDataStore(getContext());
  }

  public void testCreateReport() {
    CachedReportWrapper report = store.createReport();
    assertEquals(1, report.getLocalId());
    assertFalse(report.isSynched());
    assertTrue(report.isActive());
    assertEquals(Report.newBuilder().setNestNumber(1).build(), report.getReport());
  }

  public void testSaveReportGetReport() {
    long localId = store.createReport().getLocalId();

    Report report = Report.newBuilder()
        .setTimestampFoundMs(100L)
        .setAdditionalNotes("TEST")
        .build();
    store.saveReport(localId, report);

    CachedReportWrapper savedReport = store.getReport(localId);
    assertEquals(report, savedReport.getReport());
  }

  public void testCreateLocalUpdateGet() {
    CachedReportWrapper wrapper = store.createReport();

    Report report = wrapper.getReport().toBuilder()
        .setTimestampFoundMs(100L)
        .setAdditionalNotes("TEST")
        .build();
    store.saveReport(wrapper.getLocalId(), report);

    CachedReportWrapper resultReport = store.getReport(wrapper.getLocalId());
    assertEquals(report, resultReport.getReport());
    assertFalse(resultReport.isSynched());
    assertTrue(resultReport.isActive());
  }

  public void testUpdateFromServer_addNew() {
    ReportWrapper wrapper = ReportWrapper.newBuilder()
        .setActive(true)
        .setRef(ReportRef.newBuilder()
            .setReportId(1)
            .setVersion(2)
            .setOwnerId("USER_ID"))
        .setReport(Report.newBuilder().setTimestampFoundMs(4))
        .build();
    store.updateFromServer(wrapper);

    ImmutableList<CachedReportWrapper> activeReports = store.listActiveReports();
    assertEquals(1, activeReports.size());
    assertEquals(wrapper.getReport(), activeReports.get(0).getReport());
  }

  public void testListActive() {
    // Should be ignored.
    store.updateFromServer(
        ReportWrapper.newBuilder().setActive(false).build());

    Set<Integer> localIds = new HashSet<>();
    int numberToTest = 5;
    for (int i = 0; i < numberToTest; i++) {
      assertTrue("Id already returned",
          localIds.add(store.createReport().getLocalId()));
    }

    ImmutableList<CachedReportWrapper> wrappers =
        store.listActiveReports();
    assertEquals(numberToTest, wrappers.size());
    for (CachedReportWrapper wrapper : wrappers) {
      assertTrue(wrapper.isActive());
    }
  }

  public void testCountActive() {
    // Should be ignored.
    store.updateFromServer(
        ReportWrapper.newBuilder().setActive(false).build());

    Set<Integer> localIds = new HashSet<>();
    int numberToTest = 5;
    for (int i = 0; i < numberToTest; i++) {
      assertTrue("Id already returned",
          localIds.add(store.createReport().getLocalId()));
    }

    assertEquals(numberToTest, store.activeReportCount());
  }

  public void testDelete() {
    long localIdToDelete = store.createReport().getLocalId();
    assertEquals(1, store.activeReportCount());

    store.deleteReport(localIdToDelete);
    assertEquals(0, store.activeReportCount());
  }

  public void testListUnsycned() {
    // Unsynced
    long unsynchedId = store.createReport().getLocalId();

    // Synced
    CachedReportWrapper syncedReport = store.createReport();
    ReportRef syncedRef = ReportRef.newBuilder().setReportId(10).build();
    store.setServerSideData(syncedReport.getLocalId(), syncedRef);
    store.updateFromServer(
        ReportWrapper.newBuilder()
          .setRef(syncedRef)
          .setReport(syncedReport.getReport()).build());

    ImmutableList<CachedReportWrapper> wrappers =
        store.listUnsyncedReports();
    assertEquals(1, wrappers.size());
    assertEquals(unsynchedId, wrappers.get(0).getLocalId());
  }

  public void testSetUnsynced() {
    CachedReportWrapper report = store.createReport();
    assertEquals(1, store.listUnsyncedReports().size());

    long localReportId = report.getLocalId();
    ReportRef ref = ReportRef.newBuilder()
      .setReportId(1)
      .setVersion(2)
      .setOwnerId("USER_ID")
      .build();
    store.setServerSideData(localReportId, ref);

    // Should be synced now.
    assertEquals(0, store.listUnsyncedReports().size());

    store.setReportUnsynced(localReportId);
    assertEquals(1, store.listUnsyncedReports().size());
  }

  public void testSetServersideData() {
    CachedReportWrapper report = store.createReport();

    Long reportId = 7357L;
    Long version = 5L;
    String user = "TEST_USER"; //Ignored.

    long localReportId = report.getLocalId();
    ReportRef ref = ReportRef.newBuilder()
      .setReportId(reportId)
      .setVersion(version)
      .setOwnerId(user)
      .build();
    store.setServerSideData(localReportId, ref);

    CachedReportWrapper reportWrapper = store.getReport(localReportId);
    assertTrue(reportWrapper.getReportId().isPresent());
    assertEquals(reportId, reportWrapper.getReportId().get());
    assertTrue(reportWrapper.getVersion().isPresent());
    assertEquals(version, reportWrapper.getVersion().get());
    assertTrue(reportWrapper.isSynched());
  }

  public void testUdpateFromServer() {
    CachedReportWrapper report = store.createReport();

    Long reportId = 7357L;

    long localReportId = report.getLocalId();
    ReportRef ref = ReportRef.newBuilder()
      .setReportId(reportId)
      .setVersion(1L)
      .setOwnerId("USER_ID")
      .build();
    store.setServerSideData(localReportId, ref);

    Report updatedReport = report.getReport().toBuilder().setTimestampFoundMs(31337L).build();
    ReportWrapper updatedWrapper = ReportWrapper.newBuilder()
        .setActive(false)
        .setRef(ref)
        .setReport(updatedReport)
        .build();
    store.updateFromServer(updatedWrapper);

    CachedReportWrapper result = store.getReport(localReportId);
    assertTrue(result.isSynched());
    assertFalse(result.isActive());
    assertEquals(updatedReport, result.getReport());
  }

  public void testAddImage() {
    String filename = "test.jpg";

    CachedReportWrapper report = store.createReport();
    long localReportId = report.getLocalId();

    store.addImage(localReportId, filename, 7357L);

    Set<String> unsynchedImages = store.getUnsycnedImageFileNames();
    assertEquals(1, unsynchedImages.size());
    assertEquals(filename, unsynchedImages.iterator().next());

    Optional<Long> updatedTimestamp = store.getImageUpdatedTimestamp(localReportId, filename);
    assertTrue(updatedTimestamp.isPresent());
    assertEquals(Long.valueOf(7357L), updatedTimestamp.get());
  }

  public void testGetUnsycnedImagesFileNamesAndMarkSynced() {
    CachedReportWrapper report = store.createReport();
    long localReportId = report.getLocalId();

    Set<String> expectedFileNames = new HashSet<>();
    for (int i = 0; i < 5; ++i) {
      String filename = i + ".jpg";
      store.addImage(localReportId, filename, 1L);
      expectedFileNames.add(filename);
    }
    store.markImagesSynced(localReportId, ImmutableList.of("4.jpg"));
    expectedFileNames.remove("4.jpg");

    Set<String> unsynchedImages = store.getUnsycnedImageFileNames();
    assertEquals(4, unsynchedImages.size());
    assertEquals(expectedFileNames, unsynchedImages);
  }


  public void testTouchImage() {
    String fileName = "test.jpg";

    CachedReportWrapper report = store.createReport();
    long localReportId = report.getLocalId();

    store.addImage(localReportId, fileName, 7357L);
    assertEquals(1, store.getUnsycnedImageFileNames().size());

    store.markImagesSynced(1, ImmutableList.of(fileName));
    assertEquals(0, store.getUnsycnedImageFileNames().size());

    Long newTimestamp = 31337L;
    store.setImageUnsynced(localReportId, fileName, newTimestamp);
    assertEquals(1, store.getUnsycnedImageFileNames().size());

    Optional<Long> timestamp = store.getImageUpdatedTimestamp(localReportId, fileName);
    assertEquals(newTimestamp, timestamp.get());
  }

  public void getImageTimestamp() {
    String fileName = "test.jpg";

    CachedReportWrapper report = store.createReport();
    long localReportId = report.getLocalId();

    Long timestamp = 7357L;
    store.addImage(localReportId, fileName, timestamp);
    assertEquals(1, store.getUnsycnedImageFileNames().size());

    Optional<Long> updateTimestamp = store.getImageUpdatedTimestamp(localReportId, fileName);
    assertEquals(timestamp, updateTimestamp.get());
  }

  public void deleteReport() {
    CachedReportWrapper report = store.createReport();
    long localReportId = report.getLocalId();

    assertEquals(1, store.activeReportCount());
    assertFalse(store.getReport(localReportId).isDeleted());

    store.deleteReport(localReportId);

    // Deleted are not considered active.
    assertEquals(0, store.activeReportCount());
    assertTrue(store.getReport(localReportId).isDeleted());
  }

  public void testGetHighestNestNumber() {
    CachedReportWrapper report1 = store.createReport();
    long localReportId1 = report1.getLocalId();
    assertEquals(1, store.getHighestNestNumber());

    @SuppressWarnings("unused")
    CachedReportWrapper report2 = store.createReport();
    assertEquals(2, store.getHighestNestNumber());

    store.deleteReport(localReportId1);

    // regardless of removing 1 since there is a 2 we should be at 3.
    CachedReportWrapper report3 = store.createReport();
    long localReportId3 = report3.getLocalId();
    assertEquals(3, store.getHighestNestNumber());

    store.deleteReport(localReportId3);

    // Since we removed 3, the last highest we should get 3 again.
    CachedReportWrapper report4 = store.createReport();
    long localReportId4 = report4.getLocalId();
    assertEquals(3, store.getHighestNestNumber());

    store.saveReport(localReportId4,
        report4.getReport().toBuilder().clearNestNumber().setFalseCrawlNumber(1).build());

    // Since old 3 has now become a false crawl should give us 3 again.
    @SuppressWarnings("unused")
    CachedReportWrapper report5 = store.createReport();
    assertEquals(3, store.getHighestNestNumber());
  }

  public void testGetHighestFalseCrawlNumber() {
    CachedReportWrapper report1 = store.createReport();
    long localReportId1 = report1.getLocalId();
    assertEquals(0, store.getHighestFalseCrawlNumber());

    store.saveReport(localReportId1,
        report1.getReport().toBuilder().clearNestNumber().setFalseCrawlNumber(1).build());
    assertEquals(1, store.getHighestFalseCrawlNumber());

    store.saveReport(localReportId1,
        report1.getReport().toBuilder().clearNestNumber().setFalseCrawlNumber(7357).build());
    assertEquals(7357, store.getHighestFalseCrawlNumber());
  }
}
