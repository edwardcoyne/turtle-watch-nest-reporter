package com.islandturtlewatch.nest.reporter.data;

import com.islandturtlewatch.nest.data.ReportProto.Report;

public interface ReportMutation {
  public Report apply(Report oldReport);

  public interface RequiresReportsModel {
    //this is pointless
    public void setModel(ReportsModel model);
  }
}
