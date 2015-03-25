package com.islandturtlewatch.nest.reporter.data;

import org.joda.time.LocalDate;

/**
 * Simple date storage.
 */
public class Date {
  private int day;
  // 1 indexed Months.
  private int month;
  private int year;

  public Date(int year, int month, int day) {
    this.day = day;
    this.month = month;
    this.year = year;
  }

  public int getDay() {
    return day;
  }

  public int getMonth() {
    return month;
  }

  public int getYear() {
    return year;
  }

  public long getTimestampMs() {
    return new LocalDate(year, month, day)
        .toDateTimeAtStartOfDay()
        .getMillis();
  }
}
