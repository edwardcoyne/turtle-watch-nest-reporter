package com.islandturtlewatch.nest.reporter.util;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorUtil {
  private static final String TAG = ErrorUtil.class.getSimpleName();
  private ErrorUtil() {} // Only static

  public static void showErrorMessage(Fragment fragment, String message) {
    showErrorMessage(fragment.getActivity(), message);
  }

  public static void showErrorMessage(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    Log.e(TAG, "Displayed error: " + message);
  }
}
