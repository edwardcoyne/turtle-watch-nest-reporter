package com.islandturtlewatch.nest.reporter.test.data;

import android.test.AndroidTestCase;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

public class LocalDataStoreTest extends AndroidTestCase {
  public void testCreateUpdateGet() {
    LocalDataStore store = new LocalDataStore();
    int localId = store.createReport();

    Report report = Report.newBuilder()
        .setTimestampFoundMs(100L)
        .setAdditionalNotes("TEST")
        .build();
    store.saveReport(localId, report);

    CachedReportWrapper resultReport = store.getReport(localId);
    assertEquals(report, resultReport.getReport());
    assertFalse(resultReport.isSynched());
    assertTrue(resultReport.isActive());
  }
}
