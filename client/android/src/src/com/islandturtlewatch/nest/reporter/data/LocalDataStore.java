package com.islandturtlewatch.nest.reporter.data;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Builder;

import com.islandturtlewatch.nest.data.ReportProto.Report;

public class LocalDataStore {

  public LocalDataStore() {
    initBackend();
  }

  private void initBackend() {

  }

  public CachedReportWrapper getReport(int localId) {
    return null;
  }

  public void saveReport(int localId, Report report) {

  }

  /**
   * Creates empty report record and returns the local id.
   * @return local_id
   */
  public int createReport() {
    return 0;
  }

  @Data
  @Builder(fluent=false)
  public static class CachedReportWrapper {
    private boolean synched;
    private boolean active;
    @NonNull private Report report;
  }
}
