package com.islandturtlewatch.nest.reporter.util;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

public class AuthenticationUtil {
  private static final String TAG = AuthenticationUtil.class.getSimpleName();
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

  public static void testAuthentication(final Context context) {
    new Thread(new Runnable(){
      @Override
      public void run() {
        testAuthenticatonImpl(context);
      }}).start();
  }

  private static boolean testAuthenticatonImpl(Context context) {
    SharedPreferences settings =
        context.getSharedPreferences(SettingsUtil.SETTINGS_ID, Context.MODE_PRIVATE);
    if (!settings.contains(SettingsUtil.KEY_USERNAME)) {
      return false;
    }

    try {
      String email = settings.getString(SettingsUtil.KEY_USERNAME, null);
      // If the application has the appropriate access then a token will be retrieved, otherwise
      // an error will be thrown.
      Log.d(TAG, "email: " + email);
      GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
          context, AUDIENCE);
      credential.setSelectedAccountName(email);

      credential.getToken();


     // GoogleAuthUtil.getToken(context, email, "https://www.googleapis.com/auth/userinfo.email");
      Log.d(TAG, "AccessToken retrieved");

      // Success.
      return true;
    } catch (GoogleAuthException unrecoverableException) {
      Log.e(TAG, "Exception checking OAuth2 authentication.", unrecoverableException);
      //publishProgress(R.string.toast_exception_checking_authorization);
      // Failure.
      return false;
    } catch (IOException ioException) {
      Log.e(TAG, "Exception checking OAuth2 authentication.", ioException);
      //publishProgress(R.string.toast_exception_checking_authorization);
      // Failure or cancel request.
      return false;
    }
  }
}
