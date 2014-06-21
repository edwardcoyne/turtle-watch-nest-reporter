package com.islandturtlewatch.nest.reporter;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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
import com.islandturtlewatch.nest.reporter.util.ImageUtil;

public class ReportRestorer {
  private static final String TAG = ReportRestorer.class.getSimpleName();

  Context context;
  ReportEndpoint reportService;
  LocalDataStore store;
  Runnable callback;
  ProgressDialog dialog;

  public ReportRestorer(Context context) {
    this.context = context;
    Optional<ReportEndpoint> reportServiceOpt =
        EndPointFactory.createReportEndpoint(context, ApplicationName.REPORT_RESTORE);
    Preconditions.checkArgument(reportServiceOpt.isPresent());
    reportService = reportServiceOpt.get();
    store = new LocalDataStore(context);
  }

  private void updateProgress(final int reportsDone, final int reportsTotal) {
    final int progressDialogTotal = 10000;
    dialog.setProgress((reportsDone/reportsTotal) * progressDialogTotal);
    dialog.setMessage("Finsihed with Report " + reportsDone + "/" + reportsTotal);
  }

  public void restoreReports(Runnable callback) {
    this.callback = callback;
    dialog = ProgressDialog.show(context,
        "Restoring reports from server.",
        "Please wait.",
        false,
        false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    new Worker().execute(reportService);
  }

  private class Worker extends AsyncTask<ReportEndpoint, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(ReportEndpoint... params) {
      try {
        CollectionResponseEncodedReportRef encodedRefs =
            reportService.getLatestRefsForUser().execute();
        if (encodedRefs.getItems() == null) {
          // no reports to dl.
          return true;
        }

        int finishedCt = 0;
        for (EncodedReportRef encodedRef : encodedRefs.getItems()) {
          publishProgress(finishedCt, encodedRefs.getItems().size());
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
        //DialogUtil.acknowledge(context,
        //    "There was an error while loading the reports from server.");
      } finally {
        dialog.dismiss();
      }
      return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      updateProgress(values[0], values[1]);
    }
  }
}
