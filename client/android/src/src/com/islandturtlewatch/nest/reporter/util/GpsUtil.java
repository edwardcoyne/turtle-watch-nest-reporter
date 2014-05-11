package com.islandturtlewatch.nest.reporter.util;

import java.util.Locale;

import android.annotation.SuppressLint;

import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;

public class GpsUtil {
  private GpsUtil() {} // static only

  @SuppressLint("DefaultLocale") // Not using default locale but still a warning...
  public static String format(GpsCoordinates coordinates) {
    return String.format(Locale.US, "%.5f, %.5f", coordinates.getLat(), coordinates.getLong());
  }

}
