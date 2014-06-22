package com.islandturtlewatch.nest.reporter.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

// Handles uploading a file as part of a multipart form. Have to do this the hardway because the
// built in http client does not support this and adding another client is like 4mb for this.
public class MultiPartEntityUploader {
  private static final String TAG = MultiPartEntityUploader.class.getSimpleName();

  private static String attachmentName = "data";
  //String attachmentFileName = "bitmap.bmp";
  private static String crlf = "\r\n";
  private static String twoHyphens = "--";
  private static String boundary =  "*****";

  public static void upload(String urlString, String filename, String mimeType, byte[] bytes)
      throws IOException {
    HttpURLConnection httpUrlConnection = null;
    URL url = new URL(urlString);
    httpUrlConnection = (HttpURLConnection) url.openConnection();
    httpUrlConnection.setUseCaches(false);
    httpUrlConnection.setDoOutput(true);

    httpUrlConnection.setRequestMethod("POST");
    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
    httpUrlConnection.setRequestProperty("Content-Type",
        "multipart/form-data;boundary=" + boundary);

    DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

    request.writeBytes(twoHyphens + boundary + crlf);
    request.writeBytes("Content-Disposition: form-data; name=\""
        + attachmentName + "\";filename=\"" + filename + "\"" + crlf);
    request.writeBytes("Content-Type: " + mimeType + crlf);
    request.writeBytes(crlf);

    request.write(bytes);

    request.writeBytes(crlf);
    request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

    request.flush();
    request.close();

    // wait for response
    InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());

    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
    String line = "";
    StringBuilder stringBuilder = new StringBuilder();
    while ((line = responseStreamReader.readLine()) != null)
    {
        stringBuilder.append(line).append("\n");
    }
    responseStreamReader.close();

    String response = stringBuilder.toString();
    Log.d(TAG, "response:" + response);

    httpUrlConnection.disconnect();
  }

}
