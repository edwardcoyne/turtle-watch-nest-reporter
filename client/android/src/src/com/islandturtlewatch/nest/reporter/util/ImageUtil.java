package com.islandturtlewatch.nest.reporter.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import lombok.Cleanup;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.islandturtlewatch.nest.data.ImageProto.ImageDownloadRef;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.Builder;
import com.islandturtlewatch.nest.reporter.RunEnvironment;

public class ImageUtil {
  private static final String TAG = ImageUtil.class.getSimpleName();

  private ImageUtil() {} // static only

  public static String createNewFileName() {
    return UUID.randomUUID() + ".jpg";
  }

  public static long getModifiedTime(Context context, String fileName) {
    File image = new File(getImagePath(context, fileName).getPath());
    return image.lastModified();
  }

  public static byte[] readImageBytes(Context context, String fileName) throws IOException {
    Uri imagePath = getImagePath(context, fileName);
    File imageFile = new File(imagePath.getPath());
    return Files.toByteArray(imageFile);
  }

  public static void writeImageBytes(Context context, String fileName, byte[] bytes)
      throws IOException {
    Uri imagePath = getImagePath(context, fileName);
    File imageFile = new File(imagePath.getPath());
    Files.write(bytes, imageFile);
  }

  public static Uri getImagePath(View view, String fileName){
    return getImagePath(view.getContext(), fileName);
  }

  public static Uri getImagePath(Context context, String fileName){
    File mediaStorageDir = new File(context.getExternalFilesDir(
              Environment.DIRECTORY_PICTURES), "IslandTurtleWatch");

    if (!mediaStorageDir.exists()){
      Preconditions.checkArgument(mediaStorageDir.mkdirs(), "Failed to create image dir");
    }

    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

    return Uri.fromFile(mediaFile);
  }

  public static String getFileName(Uri imageUri) {
    File file = new File(imageUri.getPath());
    return file.getName();
  }

  public static void downloadImage(Context context, ImageDownloadRef ref) throws IOException {
    String downloadUrl = RunEnvironment.rewriteUrlIfLocalHost(ref.getUrl());
    Log.d(TAG, "downloading image from: " + downloadUrl);

    URL url = new URL(downloadUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.connect();

    Preconditions.checkArgument(connection.getResponseCode() == HttpURLConnection.HTTP_OK,
        "Error from server while downloading image:" + ref.toString() + "\t response: "
         + connection.getResponseCode());

    @Cleanup
    InputStream in = connection.getInputStream();
    Uri localPath = getImagePath(context, ref.getImage().getImageName());

    @Cleanup
    OutputStream out = new BufferedOutputStream(new FileOutputStream(localPath.getPath()));
    long bytesCopied = ByteStreams.copy(in, out);
    Log.d(TAG, "Copied " + bytesCopied + " from " + downloadUrl + " to " + localPath);
  }

  public static void copyFromContentUri(
      Activity activity, Uri contentUri, Uri destinationFileUri) throws IOException {
    @Cleanup
    InputStream in = activity.getContentResolver().openInputStream(contentUri);

    @Cleanup
    OutputStream out = new BufferedOutputStream(new FileOutputStream(destinationFileUri.getPath()));
    long bytesCopied = ByteStreams.copy(in, out);
    Log.d(TAG, "Copied " + bytesCopied + " from " + contentUri + " to " + destinationFileUri);
  }
}
