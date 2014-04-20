package com.islandturtlewatch.nest.reporter.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorUtil {
  private static final String TAG = ErrorUtil.class.getSimpleName();
  private ErrorUtil() {} // Only static

  public static void showErrorMessage(Context context, String message) {
    Toast.makeText(context, "My Service Stopped", Toast.LENGTH_LONG).show();
    Log.e(TAG, "Displayed error: " + message);
  }
}
