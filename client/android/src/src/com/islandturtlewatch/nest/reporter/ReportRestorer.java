package com.islandturtlewatch.nest.reporter;

import java.io.IOException;

import android.content.Context;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.net.EndPointFactory;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.CollectionResponseEncodedReportRef;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.EncodedReport;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.EncodedReportRef;

public class ReportRestorer {
  Context context;
  ReportEndpoint reportService;
  LocalDataStore store;
  Runnable callback;

  public ReportRestorer(Context context) {
    this.context = context;
    Optional<ReportEndpoint> reportServiceOpt = EndPointFactory.createReportEndpoint(context);
    Preconditions.checkArgument(reportServiceOpt.isPresent());
    reportService = reportServiceOpt.get();
    store = new LocalDataStore(context);
  }

  public void restoreReports(Runnable callback) {
    this.callback = callback;
    new Thread(new Worker()).start();
  }

  private class Worker implements Runnable {
    @Override
    public void run() {
      try {
        CollectionResponseEncodedReportRef encodedRefs =
            reportService.getLatestRefsForUser().execute();

        for (EncodedReportRef encodedRef : encodedRefs.getItems()) {
          EncodedReport encodedReport = reportService.fetchReport(encodedRef).execute();
          Report report =
              Report.parseFrom(BaseEncoding.base64().decode(encodedReport.getReportEncoded()));
          ReportRef ref =
              ReportRef.parseFrom(BaseEncoding.base64().decode(encodedRef.getRefEncoded()));
          ReportWrapper wrapper = ReportWrapper.newBuilder()
              .setActive(true)
              .setRef(ref)
              .setReport(report)
              .build();
          store.updateFromServer(wrapper);
        }
        callback.run();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
