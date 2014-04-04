package com.islandturtlewatch.nest.reporter.test.data;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

public class LocalDataStoreTest extends AndroidTestCase {
  @Override
  public Context getContext() {
    // DBs will be created in memory, ensure no leakage between tests.
    return new RenamingDelegatingContext(super.getContext(),
        ":memory:");
  }

  public void testCreateUpdateGet() {
    LocalDataStore store = new LocalDataStore(getContext());
    long localId = store.createReport();
    assertEquals(1, localId);

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
