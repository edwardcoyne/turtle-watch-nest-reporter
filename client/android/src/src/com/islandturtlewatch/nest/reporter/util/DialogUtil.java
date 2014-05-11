package com.islandturtlewatch.nest.reporter.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DialogUtil {
  private DialogUtil() {} // static only

  public static void confirm(Context context, String message, final Runnable onConfirm) {
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setPositiveButton("yes", new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          onConfirm.run();
        }
      })
      .setNegativeButton("No", new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
      })
      .show();
  }

}
