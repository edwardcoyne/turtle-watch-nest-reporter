package com.islandturtlewatch.nest.reporter.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.ToString;
import lombok.experimental.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import com.islandturtlewatch.nest.data.Result.StorageResult.Code;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportResponse;
import com.islandturtlewatch.nest.reporter.util.AuthenticationUtil;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;
import com.islandturtlewatch.nest.reporter.util.SettingsUtil;

public class SyncService extends Service {
  private static final String TAG = SyncService.class.getSimpleName();
  private static final int NOTIFICATION_ID = SyncService.class.hashCode();
  private static final int DB_POLL_PERIOD_S = 30;

  private Handler uiThreadHandler;

  private NotificationManager notificationManager;
  private ConnectivityManager connectivityManager;

  private SharedPreferences settings;
  private Notification.Builder notification;
  private final AtomicBoolean networkConnected = new AtomicBoolean(false);
  private final Optional<String> errorMessage = Optional.absent();
  private final BlockingQueue<Upload> pendingUploads = new LinkedBlockingDeque<>();
  private float currentUploadProgress;
  private final DbMonitor dbMonitor = new DbMonitor();
  private final Uploader uploader = new Uploader();

  public static void start(Context context) {
    Intent intent = new Intent(context, SyncService.class);
    context.startService(intent);
    Log.i(TAG, "Starting sync service");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    uiThreadHandler = new Handler();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    settings = getSharedPreferences(SettingsUtil.SETTINGS_ID, MODE_PRIVATE);

    // Listen for network changes.
    registerReceiver(new OnNetChange(),
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    startForeground();
    setNotification("Sync Service Started");
    return Service.START_STICKY;
  }

  private void startForeground() {
    notification = new Notification.Builder(this)
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
    } else {
      setNotification(String.format("Connected, Uploads Pending: %d Current: %02.1f%%",
          pendingUploads.size(), currentUploadProgress));
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
        Upload upload = new Upload(report);
        pendingUploads.remove(upload);
        pendingUploads.add(upload);
        updateNotification();
      }
    }
  }

  private class Uploader implements Runnable {
    private ReportEndpoint reportService;
    private Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void start() {
      running.set(true);
      thread = new Thread(this);
      thread.start();
    }

    public void stop() {
      running.set(false);
      thread.interrupt();
    }

    @Override
    public void run() {
      initService();
      while (running.get()) {
        Log.i(TAG, "Starting uploader.");
        try {
          Upload upload = pendingUploads.take();
          handleUpload(upload);
        } catch (InterruptedException | IOException e) {
          Log.e(TAG, "Exception while uploading: ", e);
        }
        Log.i(TAG, "Shutting down uploader.");
      }
    }

    private void initService() {
      // We need a username to proceed.
      while (!settings.contains(SettingsUtil.KEY_USERNAME)) {
        if (!running.get()) {
          return;
        }
        Log.i(TAG, "No username in settings, sleeping...");
        try {
          Thread.sleep(30000);
        } catch (InterruptedException e) {
          // Don't care.
        }
      }
      Log.d(TAG, "Using user : " + settings.getString(SettingsUtil.KEY_USERNAME, null));

      ReportEndpoint.Builder serviceBuilder = new ReportEndpoint.Builder(
          AndroidHttp.newCompatibleTransport(), new GsonFactory(),
          AuthenticationUtil.getCredential(SyncService.this,
              settings.getString(SettingsUtil.KEY_USERNAME, null)));
      serviceBuilder.setApplicationName("TurtleNestReporter-SyncService");
      if (Build.PRODUCT.contains("sdk")) {
        Log.i(TAG, "RUNNING IN EMULATOR, connecting local. ");
        // Running in emulator
        serviceBuilder.setRootUrl("http://10.255.0.43:8888/_ah/api");
      }
      reportService = serviceBuilder.build();
    }

    private void handleUpload(Upload upload) throws IOException {
      Log.d(TAG, "Uploading: " + upload.toString());
      ReportRequest request = new ReportRequest();
      request.setReportEncoded(BaseEncoding.base64().encode(
          upload.report.getReport().toByteArray()));
       ReportResponse response = reportService.createReport(request).execute();
       if (response.getCode() != Code.OK.name()) {
         Log.e(TAG, "Call failed: " + response.getCode() + " :: " + response.getErrorMessage());
       }
    }
  }

  @Builder
  @ToString
  private static class Upload {
    private final CachedReportWrapper report;
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Upload)) {
        return false;
      }
      return report.getLocalId() == ((Upload)o).report.getLocalId();
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
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "On Boot");
      SyncService.start(context);
    }
  }
}
