package com.islandturtlewatch.nest.reporter.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.common.io.ByteStreams;

public class ImageUtil {
  private static final String TAG = ImageUtil.class.getSimpleName();

  private ImageUtil() {} // static only

  public static String createNewFileName() {
    return UUID.randomUUID() + ".jpg";
  }

  public static Uri getImagePath(View view, String fileName){
    File mediaStorageDir = new File(view.getContext().getExternalFilesDir(
              Environment.DIRECTORY_PICTURES), "IslandTurtleWatch");

    if (! mediaStorageDir.exists()){
        if (! mediaStorageDir.mkdirs()){
            Log.d("MyCameraApp", "failed to create directory");
            return null;
        }
    }

    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

    return Uri.fromFile(mediaFile);
  }

  public static void copyFromContentUri(
      Activity activity, Uri contentUri, Uri destinationFileUri) throws IOException {
    InputStream in = activity.getContentResolver().openInputStream(contentUri);

    File outFile = new File(destinationFileUri.getPath());
    OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
    long bytesCopied = ByteStreams.copy(in, out);
    Log.d(TAG, "Copied " + bytesCopied + " from " + contentUri + " to " + destinationFileUri);
  }
}
