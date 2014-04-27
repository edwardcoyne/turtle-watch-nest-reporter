package com.islandturtlewatch.nest.reporter.util;

import java.util.Locale;

import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;

public class GpsUtil {
  private GpsUtil() {} // static only

  public static String format(GpsCoordinates coordinates) {
    return String.format(Locale.US, "%.4f, %.4f", coordinates.getLat(), coordinates.getLong());
  }

}
