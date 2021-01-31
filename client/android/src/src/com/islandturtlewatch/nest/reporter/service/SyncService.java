package com.islandturtlewatch.nest.reporter.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import java.lang.Object;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;
import com.islandturtlewatch.nest.reporter.util.FirestoreUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SyncService extends Service {
  private static final String TAG = SyncService.class.getSimpleName();
  private static final int NOTIFICATION_ID = SyncService.class.hashCode();
  private static final int DB_POLL_PERIOD_S = 30;
  private static final String NOTIFICATION_CHANNEL_ID =
          "com.islandturtlewatch.nest.reporter.sync_service";

  private Handler uiThreadHandler;

  private NotificationManager notificationManager;
  private ConnectivityManager connectivityManager;

  private NotificationCompat.Builder notification;
  private final AtomicBoolean networkConnected = new AtomicBoolean(false);
  private final Optional<String> errorMessage = Optional.absent();
  private final BlockingQueue<Upload> pendingUploads = new LinkedBlockingDeque<>();
  private final DbMonitor dbMonitor = new DbMonitor();
  private final Uploader uploader = new Uploader();

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static void start(Context context) {
    Intent intent = new Intent(context, SyncService.class);
    context.startForegroundService(intent);

    Log.i(TAG, "Starting sync service.");
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    uiThreadHandler = new Handler();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    // Listen for network changes.
    registerReceiver(new OnNetChange(),
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    startForeground();
    setNotification("Sync Service Started");
    return Service.START_STICKY;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void startForeground() {
    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Sync Service", NotificationManager.IMPORTANCE_NONE);
    chan.setLightColor(Color.BLUE);
    chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    assert manager != null;
    manager.createNotificationChannel(chan);

    notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getText(R.string.sync_service_name))
        .setContentText("")
        .setSmallIcon(R.drawable.ic_launcher);

    Intent foregroundIntent = new Intent(this, SyncService.class);

    //TODO(edcoyne): Will be called on click, some type of transfer ui?
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);
    notification.setContentIntent(pendingIntent);

    startForeground(NOTIFICATION_ID, notification.build());
    updateNetworkStatus();
  }


  @Override
  public void onDestroy() {
    displayError(this.getString(R.string.app_name) + " Sync Service Stopped");
  }

  private void updateNetworkStatus() {
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      networkUp();
    } else {
      networkDown();
    }
    updateNotification();
  }

  private void networkUp() {
    if (networkConnected.get()) {
      return;
    }
    networkConnected.set(true);
    dbMonitor.start();
    uploader.start();
  }

  private void networkDown() {
    if (!networkConnected.get()) {
      return;
    }
    networkConnected.set(false);
    dbMonitor.stop();
    uploader.stop();
  }

  private void updateNotification() {
    if (errorMessage.isPresent()) {
      setNotification(errorMessage.get());
    } else if (!networkConnected.get()) {
      setNotification("Network disconnected, sleeping...");
    } else if (pendingUploads.size() == 0) {
      setNotification("Connected.");
    } else {
      setNotification(String.format("Uploading, %d Pending", pendingUploads.size()));
    }
  }

  private void setNotification(String text) {
    notification.setContentText(text);
    notificationManager.notify(NOTIFICATION_ID, notification.build());
  }

  private void displayError(final String errorMsg) {
    if (uiThreadHandler != null) {
      uiThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          ErrorUtil.showErrorMessage(SyncService.this, errorMsg);
        }
      });
    } else {
      ErrorUtil.showErrorMessage(this, errorMsg);
    }
    if (notification != null) {
      setNotification(errorMsg);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Unsupported.
    return null;
  }

  private class DbMonitor {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    AtomicBoolean running = new AtomicBoolean(false);
    ScheduledFuture<?> currentTask;
    Map<Integer, Long> pendingUploadTimestampMap = new HashMap<>();

    void start() {
      Log.i(TAG, "Starting DB monitor");
      running.set(true);
      currentTask = executor.scheduleAtFixedRate(new CheckForUnsyncedDataTask(),
          0, DB_POLL_PERIOD_S, TimeUnit.SECONDS);
    }

    void stop() {
      running.set(false);
      if (currentTask != null) {
        currentTask.cancel(false);
        currentTask = null;
      }
      pendingUploads.clear();
      pendingUploadTimestampMap.clear();
    }

    private class CheckForUnsyncedDataTask implements Runnable {
      LocalDataStore dataStore = new LocalDataStore(SyncService.this);
      @Override
      public void run() {
        Log.d(TAG, "Checking for unsynced reports.");
        for (CachedReportWrapper report : dataStore.listUnsyncedReports()) {
          Long lastSeenTimestamp = pendingUploadTimestampMap.get(report.getLocalId());
          if (lastSeenTimestamp == null || lastSeenTimestamp != report.getLastUpdatedTimestamp()) {
            Log.d(TAG, "Adding report for upload:" + report.getLocalId()
                + " ts:" + report.getLastUpdatedTimestamp());
            pendingUploadTimestampMap.put(report.getLocalId(), report.getLastUpdatedTimestamp());
            AddUpload(report);
          }
        }
      }

      private void AddUpload(CachedReportWrapper report) {
        Upload upload = new Upload(getApplicationContext(),
            dataStore, report.getLocalId(), 1);

        pendingUploads.remove(upload);
        pendingUploads.add(upload);
        updateNotification();
      }
    }
  }

  private class Uploader implements Runnable {
    private LocalDataStore dataStore;
    private Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private FirebaseFirestore db;

    public void start() {
      running.set(true);
      thread = new Thread(this);
      dataStore = new LocalDataStore(SyncService.this);
      thread.start();

      db = FirebaseFirestore.getInstance();
    }

    public void stop() {
      running.set(false);
      thread.interrupt();
    }

    @Override
    public void run() {
      Log.i(TAG, "Starting uploader.");
      while (running.get()) {
        try {
          Upload upload = pendingUploads.take();
          Log.d(TAG, "Starting upload");
          if (!handleUpload(upload)) {
            Log.i(TAG, "Upload failed adding back to queue.");
            pendingUploads.add(upload.getRetry());
          } else {
            Log.i(TAG, "Upload finished successfully");
            updateNotification();
          }
        } catch (InterruptedException e) {
          Log.e(TAG, "Interrupted while getting upload:", e);
        }
      }
      Log.i(TAG, "Shutting down uploader.");
    }

    private boolean handleUpload(Upload upload) {
      upload.delayIfRetry();
      try {
        Optional<CachedReportWrapper> wrapper = upload.getReportWrapperIfNotSynced();
        if (!wrapper.isPresent()) {
          return true;
        }

        if (wrapper.get().isDeleted()) {
          return handleDelete(wrapper.get());
        } else if (!wrapper.get().getReportId().isPresent()) {
          return handleCreate(wrapper.get());
        } else {
          return handleUpdate(wrapper.get());
        }
      } catch (IOException ex) {
        Log.e(TAG, "Call failed, exception:", ex);
        return false;
      }
    }

    private boolean handleCreate(CachedReportWrapper wrapper) throws IOException {
      Log.i(TAG, "Creating report on server");

      final String encoded = BaseEncoding.base64().encode(wrapper.getReport().toByteArray());
      Task<Long> transaction = db.runTransaction(new Transaction.Function<Long>() {
        @Override
        public Long apply(Transaction transaction) throws FirebaseFirestoreException {
          DocumentSnapshot userData = transaction.get(db.document(FirestoreUtil.UserPath()));

          // Find id for new report.
          Long last_report_id = userData.getLong("last_report_id");
          long newId = (last_report_id != null ? last_report_id : 0) + 1;
          transaction.set(userData.getReference(),
                  Collections.singletonMap("last_report_id", newId),
                  SetOptions.merge());

          // Set version to 1.
          DocumentReference reportRef =
                  db.document(FirestoreUtil.UserPath() + "/report/" + newId);
          transaction.set(reportRef,
                  ImmutableMap.of("last_version", 1, "deleted", false),
                  SetOptions.merge());

          Log.i(TAG, "ref: " + reportRef.getPath());

          // Store encoded proto.
          transaction.set(
                  db.document(reportRef.getPath() + "/version/1"),
                  Collections.singletonMap("proto", encoded));
          return newId;
        }
      });

      try {
        Long reportId = Tasks.await(transaction);
        ReportRef.Builder ref = ReportRef.newBuilder();
        ref.setReportId(reportId);
        ref.setVersion(1);
        dataStore.setServerSideData(wrapper.getLocalId(), ref.build());
        return true;

      } catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "Create failed: ", e);
      }
      return false;
    }

    private boolean handleUpdate(CachedReportWrapper wrapper) throws IOException {
      Log.i(TAG, "Updating report on server");

      final String encoded = BaseEncoding.base64().encode(wrapper.getReport().toByteArray());
      Task<Long> transaction = db.runTransaction(new Transaction.Function<Long>() {
        @Override
        public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
          DocumentReference reportRef =
                  db.document(FirestoreUtil.UserPath() + "/report/" + wrapper.getReportId().get());

          // Find id for new report.
          Long newVersion = transaction.get(reportRef).getLong("last_version") + 1;
          transaction.update(reportRef, "last_version", newVersion);

          DocumentReference versionRef =
                  db.document(reportRef.getPath() + "/version/" + newVersion);
          Log.i(TAG, "ref: " + versionRef.getPath());

          // Store encoded proto.
          transaction.set(versionRef,
                  Collections.singletonMap("proto", encoded));
          return newVersion;
        }
      });

      try {
        Long versionId = Tasks.await(transaction);
        ReportRef.Builder ref = ReportRef.newBuilder()
                .setReportId(wrapper.getReportId().get())
                .setVersion(versionId);
        dataStore.setServerSideData(wrapper.getLocalId(), ref.build());
        return true;

      } catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "Update failed: ", e);
      }
      return false;
    }

    private boolean handleDelete(CachedReportWrapper wrapper) throws IOException {
      Log.i(TAG, "Deleting report from server");

      // Only send delete to server if we ever sent the report there in the first place.
      if (!wrapper.getReportId().isPresent()) {
        dataStore.markSynced(wrapper.getLocalId());
        return true;
      }

      Task<Void> transaction = db.runTransaction(new Transaction.Function<Void>() {
        @Override
        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
          DocumentReference reportRef =
                  db.document(FirestoreUtil.UserPath() + "/report/" + wrapper.getReportId().get());
          Log.i(TAG, "ref: " + reportRef.getPath());
          transaction.update(reportRef, "deleted", true);
          return null;
        }
      });

      try {
        Tasks.await(transaction);
        dataStore.markSynced(wrapper.getLocalId());
        return true;

      } catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "Delete failed: ", e);
      }
      return false;
    }
  }

  private static class Upload {
    private static final int MAX_RETRY_DELAY_S = 45;
    private final long localReportId;
    private final LocalDataStore dataStore;
    private final int retryDelayS;
    private final Context context;

    private Upload( Context context, LocalDataStore dataStore, long localReportId, int retryDelayS) {
      this.localReportId = localReportId;
      this.dataStore = dataStore;
      this.retryDelayS = retryDelayS;
      this.context = context;
    }

    public Optional<CachedReportWrapper> getReportWrapperIfNotSynced() throws IOException {
      CachedReportWrapper wrapper = dataStore.getReport(localReportId);
      if (wrapper.isSynched()) {
        return Optional.absent();
      }
      return Optional.of(wrapper);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Upload)) {
        return false;
      }
      return localReportId == ((Upload)o).localReportId;
    }

    public void delayIfRetry() {
      if (retryDelayS > 0) {
        Log.d(TAG, "Sleeping before retry: " + retryDelayS + "s");
        sleep(retryDelayS);
      }
    }

    public Upload getRetry() {
      return new Upload(context, dataStore, localReportId,
          Math.min(retryDelayS * 2, MAX_RETRY_DELAY_S));
    }
  }

  private class OnNetChange extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "NetworkChanged");
      updateNetworkStatus();
    }
  }

  /**
   * Listens for the BOOT_COMPLETED message from the system and starts up the sync service.
   */
  public static class OnBoot extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "On Boot");
      SyncService.start(context);
    }
  }

  private static void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      Log.e(TAG, "Sleep interrupted.", e);
    }
  }
}
