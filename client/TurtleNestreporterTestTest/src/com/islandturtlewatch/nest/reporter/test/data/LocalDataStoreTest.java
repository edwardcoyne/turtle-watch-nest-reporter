package com.islandturtlewatch.nest.reporter.test.data;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.google.common.collect.ImmutableList;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

public class LocalDataStoreTest extends AndroidTestCase {
  @Override
  public Context getContext() {
    // DBs will be created in memory, ensure no leakage between tests.
    return new RenamingDelegatingContext(super.getContext(),
        ":memory:");
  }

  public void testCreateReport() {
    LocalDataStore store = new LocalDataStore(getContext());
    CachedReportWrapper report = store.createReport();
    assertEquals(1, report.getLocalId());
    assertFalse(report.isSynched());
    assertTrue(report.isActive());
    assertEquals(Report.getDefaultInstance(), report.getReport());
  }

  public void testSaveReport_LocalUpdate() {
    LocalDataStore store = new LocalDataStore(getContext());
    long localId = store.createReport().getLocalId();

    Report report = Report.newBuilder()
        .setTimestampFoundMs(100L)
        .setAdditionalNotes("TEST")
        .build();
    store.saveReport(localId, report);

    // No exception is only real test here.
  }

  public void testCreateLocalUpdateGet() {
    LocalDataStore store = new LocalDataStore(getContext());
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

  public void testSaveReport_FromServerAddNew() {
    LocalDataStore store = new LocalDataStore(getContext());
    ReportWrapper wrapper = ReportWrapper.newBuilder()
        .setActive(true)
        .setRef(ReportRef.newBuilder()
            .setReportId(1)
            .setVersion(2)
            .setOwnerId(3))
        .setReport(Report.newBuilder().setTimestampFoundMs(4))
        .build();
    store.saveReport(wrapper);
    // No exceptions, working.
  }

  public void testListActive() {
    LocalDataStore store = new LocalDataStore(getContext());

    // Should be ignored.
    store.saveReport(
        ReportWrapper.newBuilder().setActive(false).build());

    Set<Long> localIds = new HashSet<>();
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
}
