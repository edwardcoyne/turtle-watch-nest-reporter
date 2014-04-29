package com.islandturtlewatch.nest.reporter.util;

import java.io.File;
import java.util.UUID;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class ImageUtil {
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
}
