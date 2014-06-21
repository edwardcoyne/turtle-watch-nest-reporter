package com.islandturtlewatch.nest.reporter;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.net.EndPointFactory;
import com.islandturtlewatch.nest.reporter.net.EndPointFactory.ApplicationName;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.CollectionResponseEncodedReportRef;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.EncodedReport;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.EncodedReportRef;
import com.islandturtlewatch.nest.reporter.util.DialogUtil;
import com.islandturtlewatch.nest.reporter.util.ImageUtil;

public class ReportRestorer {
  private static final String TAG = ReportRestorer.class.getSimpleName();

  Context context;
  ReportEndpoint reportService;
  LocalDataStore store;
  Runnable callback;
  AlertDialog dialog;

  public ReportRestorer(Context context) {
    this.context = context;
    Optional<ReportEndpoint> reportServiceOpt =
        EndPointFactory.createReportEndpoint(context, ApplicationName.REPORT_RESTORE);
    Preconditions.checkArgument(reportServiceOpt.isPresent());
    reportService = reportServiceOpt.get();
    store = new LocalDataStore(context);
  }

  public void restoreReports(Runnable callback) {
    this.callback = callback;
    dialog = new AlertDialog.Builder(context)
        .setTitle("Restoring reports from server, please wait...")
        .show();
    new Thread(new Worker()).start();
    // TODO(edcoyne): add ui
  }

  private class Worker implements Runnable {
    @Override
    public void run() {
      try {
        CollectionResponseEncodedReportRef encodedRefs =
            reportService.getLatestRefsForUser().execute();

        for (EncodedReportRef encodedRef : encodedRefs.getItems()) {
          EncodedReport encodedReport = reportService.fetchReport(encodedRef).execute();
          long startTimestamp = System.currentTimeMillis();
          Report report = Report.parseFrom(
              BaseEncoding.base64().decode(encodedReport.getReportEncoded()));
          Log.d(TAG, String.format("Decoded proto in %f s.",
              (System.currentTimeMillis() - startTimestamp) / 1000.0));
          ReportRef ref =
              ReportRef.parseFrom(BaseEncoding.base64().decode(encodedRef.getRefEncoded()));

          report = ImageUtil.stripAndWriteEmbeddedImage(context, report);

          ReportWrapper wrapper = ReportWrapper.newBuilder()
              .setActive(true)
              .setRef(ref)
              .setReport(report)
              .build();
          store.updateFromServer(wrapper);

          long localId = store.getLocalReportId(ref.getReportId());
          for (Image image : report.getImageList()) {
            if (!store.hasImage(localId, image.getFileName())) {
              store.addImage(localId, image.getFileName(),
                  ImageUtil.getModifiedTime(context, image.getFileName()));
            }
          }
        }
        Log.d(TAG, "Done restoring reports");
        callback.run();
      } catch (IOException e) {
        e.printStackTrace();
        dialog.dismiss();
        DialogUtil.acknowledge(context,
            "There was an error while loading the reports from server.");
      } finally {
        dialog.dismiss();
      }
    }
  }

}
