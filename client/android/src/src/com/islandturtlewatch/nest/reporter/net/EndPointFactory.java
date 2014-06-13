package com.islandturtlewatch.nest.reporter.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Optional;
import com.islandturtlewatch.nest.reporter.RunEnvironment;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;
import com.islandturtlewatch.nest.reporter.util.AuthenticationUtil;
import com.islandturtlewatch.nest.reporter.util.SettingsUtil;

public class EndPointFactory {
  private static final String TAG = EndPointFactory.class.getSimpleName();

  public static Optional<ReportEndpoint> createReportEndpoint(Context context) {
    SharedPreferences settings =
        context.getSharedPreferences(SettingsUtil.SETTINGS_ID, Context.MODE_PRIVATE);

    if (!settings.contains(SettingsUtil.KEY_USERNAME)) {
      Log.e(TAG, "No username in settings, cannot create endpoint.");
      return Optional.absent();
    }
    Log.d(TAG, "Using user : " + settings.getString(SettingsUtil.KEY_USERNAME, null));

    ReportEndpoint.Builder serviceBuilder = new ReportEndpoint.Builder(
        AndroidHttp.newCompatibleTransport(), new GsonFactory(),
        AuthenticationUtil.getCredential(context,
            settings.getString(SettingsUtil.KEY_USERNAME, null)));
    serviceBuilder.setApplicationName("TurtleNestReporter-SyncService");
    Log.d(TAG, "connecting to backend: " + RunEnvironment.getRootBackendUrl());
    serviceBuilder.setRootUrl(RunEnvironment.getRootBackendUrl());
    return Optional.of(serviceBuilder.build());
  }
}
