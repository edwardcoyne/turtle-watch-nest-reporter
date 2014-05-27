package com.islandturtlewatch.nest.reporter.util;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ErrorUtil {
  private static final String TAG = ErrorUtil.class.getSimpleName();
  private ErrorUtil() {} // Only static

  public static void showErrorMessage(final Fragment fragment, final String message) {
    if (fragment.getActivity() == null) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected void onPostExecute(Void params) {
          showErrorMessage(fragment.getActivity(), message);
        }
        @Override
        protected Void doInBackground(Void... params) {
          return null;
        }
      };
    } else {
      showErrorMessage(fragment.getActivity(), message);
    }
  }

  public static void showErrorMessage(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    Log.e(TAG, "Displayed error: " + message);
  }
}
