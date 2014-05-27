package com.islandturtlewatch.nest.reporter.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.Result.StorageResult.Code;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.RunEnvironment;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportRequest;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.model.ReportResponse;
import com.islandturtlewatch.nest.reporter.util.AuthenticationUtil;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;
import com.islandturtlewatch.nest.reporter.util.ImageUtil;
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
        Upload upload = Upload.builder()
            .setContext(getApplicationContext())
            .setDataStore(dataStore)
            .setLocalReportId(report.getLocalId())
            .setRetryDelayS(1)
            .build();

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

    private void initService() {
      // We need a username to proceed.
      while (!settings.contains(SettingsUtil.KEY_USERNAME)) {
        if (!running.get()) {
          return;
        }
        Log.i(TAG, "No username in settings, sleeping...");
        sleep(30);
      }
      Log.d(TAG, "Using user : " + settings.getString(SettingsUtil.KEY_USERNAME, null));

      ReportEndpoint.Builder serviceBuilder = new ReportEndpoint.Builder(
          AndroidHttp.newCompatibleTransport(), new GsonFactory(),
          AuthenticationUtil.getCredential(SyncService.this,
              settings.getString(SettingsUtil.KEY_USERNAME, null)));
      serviceBuilder.setApplicationName("TurtleNestReporter-SyncService");
      Log.d(TAG, "connecting to backend: " + RunEnvironment.getRootBackendUrl());
      serviceBuilder.setRootUrl(RunEnvironment.getRootBackendUrl());

      reportService = serviceBuilder.build();
    }

    private boolean handleUpload(Upload upload) {
      upload.delayIfRetry();
      try {
        Optional<CachedReportWrapper> wrapper = upload.getReportWrapperIfNotSynced();
        if (!wrapper.isPresent()) {
          return true;
        }

        if (!wrapper.get().getReportId().isPresent()) {
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
      ReportRequest request = new ReportRequest();
      request.setReportEncoded(BaseEncoding.base64().encode(wrapper.getReport().toByteArray()));

      ReportResponse response = reportService.createReport(request).execute();
      if (!response.getCode().equals(Code.OK.name())) {
        Log.e(TAG, "Call failed: " + response.getCode() + " :: " + response.getErrorMessage());
        return false;
      }

      ReportRef reportRef = ReportRef.newBuilder()
          .mergeFrom(BaseEncoding.base64().decode(response.getReportRefEncoded()))
          .build();
      LocalDataStore dataStore = new LocalDataStore(SyncService.this);
      dataStore.setServerSideData(wrapper.getLocalId(), reportRef);
      dataStore.markImagesSynced(wrapper.getLocalId(), wrapper.getUnsynchedImageFileNames());

      return true;
    }

    private boolean handleUpdate(CachedReportWrapper wrapper) throws IOException {
      ReportRequest request = new ReportRequest();
      ReportRef ref = ReportRef.newBuilder()
          .setReportId(wrapper.getReportId().get())
          .setVersion(wrapper.getVersion().get())
          .build();
      request.setReportRefEncoded(BaseEncoding.base64().encode(ref.toByteArray()));
      request.setReportEncoded(BaseEncoding.base64().encode(wrapper.getReport().toByteArray()));

      ReportResponse response = reportService.updateReport(request).execute();
      if (!response.getCode().equals(Code.OK.name())) {
        Log.e(TAG, "Call failed: " + response.getCode() + " :: " + response.getErrorMessage());
        return false;
      }

      ReportRef reportRef = ReportRef.newBuilder()
          .mergeFrom(BaseEncoding.base64().decode(response.getReportRefEncoded()))
          .build();
      Log.d(TAG, "Updateing from server: " + reportRef.toString());
      LocalDataStore dataStore = new LocalDataStore(SyncService.this);
      dataStore.setServerSideData(wrapper.getLocalId(), reportRef);
      // TODO(edcoyne): this is a poor implmentation, there could be changes to the same image while
      // this upload is taking place that will be missed until next time its changed.
      dataStore.markImagesSynced(wrapper.getLocalId(), wrapper.getUnsynchedImageFileNames());

      return true;
    }
  }

  @ToString
  @Builder(fluent=false)
  private static class Upload {
    //private static final int MAX_RETRY_DELAY_S = 300; // 5 min.
    private static final int MAX_RETRY_DELAY_S = 45;

    private final long localReportId;
    private final LocalDataStore dataStore;
    private final int retryDelayS;
    private final Context context;

    public Optional<CachedReportWrapper> getReportWrapperIfNotSynced() throws IOException {
      CachedReportWrapper wrapper = dataStore.getReport(localReportId);
      if (wrapper.isSynched()) {
        return Optional.absent();
      }
      inlineUnsyncedImages(wrapper);
      return Optional.of(wrapper);
    }

    private void inlineUnsyncedImages(CachedReportWrapper wrapper) throws IOException {
      Set<String> unsycnedPhotosFileNames = dataStore.getUnsycnedImageFileNames();
      Report.Builder builder = wrapper.getReport().toBuilder();
      for (Image.Builder image : builder.getImageBuilderList()) {
        if (unsycnedPhotosFileNames.contains(image.getFileName())) {
          Log.d(TAG, "Adding unsynced photo to report: " + image.getFileName() + ".");
          image.setRawData(ByteString.copyFrom(
              ImageUtil.getImageBytes(context, image.getFileName())));
          wrapper.getUnsynchedImageFileNames().add(image.getFileName());
          Log.d(TAG, "Done adding unsynced photo to report: " + image.getFileName() + ".");
        }
      }
      wrapper.setReport(builder.build());
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
      return Upload.builder()
          .setContext(context)
          .setDataStore(dataStore)
          .setLocalReportId(localReportId)
          .setRetryDelayS(Math.min(retryDelayS * 2, MAX_RETRY_DELAY_S))
          .build();
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

  private static void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      Log.e(TAG, "Sleep interupted.", e);
    }
  }
}
