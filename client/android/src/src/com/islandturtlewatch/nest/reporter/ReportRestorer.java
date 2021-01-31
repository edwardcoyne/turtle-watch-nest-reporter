package com.islandturtlewatch.nest.reporter;

import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.util.DialogUtil;
import com.islandturtlewatch.nest.reporter.util.FirestoreUtil;

public class ReportRestorer {
  private static final String TAG = ReportRestorer.class.getSimpleName();

  Context context;
  LocalDataStore store;
  Runnable callback;
  ProgressDialog dialog;
  private FirebaseFirestore db;

  public ReportRestorer(Context context) {
    this.context = context;
    db = FirebaseFirestore.getInstance();
    store = new LocalDataStore(context);
  }

  private void updateProgress(final int reportsDone, final int reportsTotal,
      Optional<Integer> photosDone, Optional<Integer> photosTotal) {
    final int progressDialogTotal = 10000;
    dialog.setProgress((reportsDone/reportsTotal) * progressDialogTotal);
    String line1 = "Downloading Report " + reportsDone + "/" + reportsTotal;
    dialog.setMessage(line1);
  }

  public void restoreReports(Runnable callback) {
    this.callback = callback;
    dialog = new ProgressDialog(context);
    dialog.setTitle("Restoring reports from server.");
    dialog.setMessage("Please wait.");
    dialog.setIndeterminate(false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.show();
    new Worker().execute(db);
  }
  private class Worker extends AsyncTask<FirebaseFirestore, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(FirebaseFirestore... params) {
      try {

        CollectionReference reportRef = db.collection(FirestoreUtil.UserPath() + "/report");
        Task<QuerySnapshot> read_reports = reportRef.get();

        QuerySnapshot reports = Tasks.await(read_reports);
        int reportNum = 0;
        final int reportsTotal = reports.size();
        for (QueryDocumentSnapshot report : reports) {
          if (report.get("deleted", Boolean.class)) {
            Log.d(TAG, "Skipping deleted report.");
            continue;
          }

          final Integer latest_version = report.get("last_version", Integer.class);
          Preconditions.checkArgument(latest_version >= 0);

          DocumentReference versionRef = reportRef.document(report.getId())
                  .collection("version")
                  .document(latest_version.toString());
          Log.d(TAG, "Fetching version from path: " + versionRef.getPath());

          DocumentSnapshot version = Tasks.await(versionRef.get());

          Log.d(TAG, "Report version data: " + version.getData().toString());

          Report reportProto = Report.parseFrom(
                  BaseEncoding.base64().decode(version.get("proto", String.class)));

          ReportRef.Builder ref = ReportRef.newBuilder()
                  .setReportId(Long.parseLong(report.getId()))
                  .setVersion(latest_version);

          ReportWrapper wrapper = ReportWrapper.newBuilder()
                  .setActive(true)
                  .setRef(ref)
                  .setReport(reportProto)
                  .build();
          store.updateFromServer(wrapper);

          publishProgress(++reportNum, reportsTotal);
        }
        // If we successfully restored remote data, let's remove a vestigial "report 1" if this
        // was a new app install (likely).
        LocalDataStore.CachedReportWrapper report1 = store.getReport(1);
        if (!report1.getReportId().isPresent() && report1.isSynched()) {
          Log.i(TAG, "Deleting empty local report 1.");
          store.deleteReport(1);
        }

        callback.run();

      } catch (ExecutionException | InterruptedException | InvalidProtocolBufferException e) {
        Log.e(TAG, "Reading documents failed: ", e);
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
