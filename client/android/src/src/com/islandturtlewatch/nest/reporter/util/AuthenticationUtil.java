package com.islandturtlewatch.nest.reporter.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

public class AuthenticationUtil {
  // NOTE: this is the WEB client id, not the android client id. Ask google why, not me.
  private static final String WEB_CLIENT_ID = "583713553896.apps.googleusercontent.com";
  private static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

  public static GoogleAccountCredential getCredential(Context context, String username) {
    Preconditions.checkNotNull(username, "No username set.");
    GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(context,
        AUDIENCE);
    credential.setSelectedAccountName(username);
    return credential;
  }


  public static boolean checkGooglePlayServicesAvailable(Activity activity) {
    final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
    if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
      showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
      return false;
  }
  return true;
  }

  public static void showGooglePlayServicesAvailabilityErrorDialog(final Activity activity,
    final int connectionStatusCode) {
    final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
            connectionStatusCode, activity, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
      }
    });
  }
}
