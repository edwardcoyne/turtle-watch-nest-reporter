package com.islandturtlewatch.nest.reporter;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.TextFormat;
import com.islandturtlewatch.nest.data.ImageProto.ImageDownloadRef;
import com.islandturtlewatch.nest.data.ImageProto.ImageRef;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.net.EndPointFactory;
import com.islandturtlewatch.nest.reporter.net.EndPointFactory.ApplicationName;
import com.islandturtlewatch.nest.reporter.transport.imageEndpoint.ImageEndpoint;
import com.islandturtlewatch.nest.reporter.transport.imageEndpoint.model.EncodedImageRef;
import com.islandturtlewatch.nest.reporter.transport.imageEndpoint.model.SerializedProto;
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
  ImageEndpoint imageService;
  LocalDataStore store;
  Runnable callback;
  ProgressDialog dialog;

  public ReportRestorer(Context context) {
    this.context = context;
    Optional<ReportEndpoint> reportServiceOpt =
        EndPointFactory.createReportEndpoint(context, ApplicationName.REPORT_RESTORE);
    Preconditions.checkArgument(reportServiceOpt.isPresent());
    reportService = reportServiceOpt.get();

    Optional<ImageEndpoint> imageServiceOpt =
        EndPointFactory.createImageEndpoint(context, ApplicationName.REPORT_RESTORE);
    Preconditions.checkArgument(imageServiceOpt.isPresent());
    imageService = imageServiceOpt.get();
    store = new LocalDataStore(context);
  }

  private void updateProgress(final int reportsDone, final int reportsTotal,
      Optional<Integer> photosDone, Optional<Integer> photosTotal) {
    final int progressDialogTotal = 10000;
    dialog.setProgress((reportsDone/reportsTotal) * progressDialogTotal);
    String line1 = "Downloading Report " + reportsDone + "/" + reportsTotal;
    String line2 = (photosDone.isPresent() && photosTotal.isPresent()) ?
        "\n\tPhoto " + photosDone.get() + "/" + photosTotal.get() : "";
    dialog.setMessage(line1 + line2);
    if (photosDone.isPresent() && photosTotal.isPresent()) {
      dialog.setProgress(photosDone.get());
      dialog.setMax(photosTotal.get());
    }
  }

  public void restoreReports(Runnable callback) {
    this.callback = callback;
    dialog = new ProgressDialog(context);
    dialog.setTitle("Restoring reports from server.");
    dialog.setMessage("Please wait.");
    dialog.setIndeterminate(false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.show();
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

        int reportNum = 0;
        int reportsTotal = encodedRefs.getItems().size();
        for (EncodedReportRef encodedRef : encodedRefs.getItems()) {
          publishProgress(++reportNum, reportsTotal);
          EncodedReport encodedReport = reportService.fetchReport(encodedRef).execute();
          long startTimestamp = System.currentTimeMillis();

          Report report = Report.parseFrom(
              BaseEncoding.base64().decode(encodedReport.getReportEncoded()));

          Log.d(TAG, String.format("Decoded proto in %f s.",
              (System.currentTimeMillis() - startTimestamp) / 1000.0));
          ReportRef ref =
              ReportRef.parseFrom(BaseEncoding.base64().decode(encodedRef.getRefEncoded()));

          ReportWrapper wrapper = ReportWrapper.newBuilder()
              .setActive(true)
              .setRef(ref)
              .setReport(report)
              .build();
          store.updateFromServer(wrapper);

          long localId = store.getLocalReportId(ref.getReportId());
          int imageNum = 0;
          for (Image image : report.getImageList()) {
            publishProgress(reportNum, reportsTotal,
                ++imageNum, report.getImageList().size());
            if (!store.hasImage(localId, image.getFileName())) {
              ImageRef imageRef = ImageRef.newBuilder()
                  .setOwnerId(ref.getOwnerId())
                  .setReportId(ref.getReportId())
                  .setImageName(image.getFileName())
                  .build();
              EncodedImageRef encodedImageRef = new EncodedImageRef().setRefEncoded(
                  BaseEncoding.base64().encode(imageRef.toByteArray()));

              SerializedProto resultProto = imageService.imageDownload(encodedImageRef).execute();

              ImageDownloadRef.Builder downloadRef = ImageDownloadRef.newBuilder();
              TextFormat.merge(resultProto.getSerializedProto(), downloadRef);

              ImageUtil.downloadImage(context, downloadRef.build());

              store.addImage(localId, image.getFileName(),
                  ImageUtil.getModifiedTime(context, image.getFileName()));
              store.markImagesSynced(localId, ImmutableList.of(image.getFileName()));
            }
          }
        }
        Log.d(TAG, "Done restoring reports");
        callback.run();
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      } finally {
        dialog.dismiss();
      }
      return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      Optional<Integer> photosDone = (values.length > 2)
          ? Optional.of(values[2]) : Optional.<Integer>absent();
      Optional<Integer> photosTotal = (values.length > 3)
          ? Optional.of(values[3]) : Optional.<Integer>absent();
      updateProgress(values[0], values[1], photosDone, photosTotal);
    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (!result) {
        DialogUtil.acknowledge(context,
            "There was an error while loading the reports from server.");
      }
    }
  }
}
