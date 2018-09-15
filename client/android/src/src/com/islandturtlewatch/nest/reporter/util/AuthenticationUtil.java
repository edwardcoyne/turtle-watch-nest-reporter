package com.islandturtlewatch.nest.reporter.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

public class AuthenticationUtil {
  // NOTE: this is the WEB client id, not the android client id. Ask google why, not me.
  private static final String WEB_CLIENT_ID = "362099484578.apps.googleusercontent.com";
  //private static final String WEB_CLIENT_ID = "362099484578-8pgeec1f3r137278po1btff19cs9spv6.apps.googleusercontent.com";
  private static final String ANDROID_CLIENT_ID = "362099484578-lbl3ef0b8mi6t13fpdthvr3n0qssqq4m.apps.googleusercontent.com";
  private static final String DEV_CLIENT_ID = "625582312057-mft8ce438s5dt5rfujufd7dlsavt5bk9.apps.googleusercontent.com";


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
