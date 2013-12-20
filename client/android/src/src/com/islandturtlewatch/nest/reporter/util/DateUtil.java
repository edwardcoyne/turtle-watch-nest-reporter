package com.islandturtlewatch.nest.reporter.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtil {
	private static final DateTimeFormatter PATTERN = DateTimeFormat.forPattern("yyyy/MM/dd");

	private DateUtil() {} // static util

	public static long getTimestampInMs(int year, int month, int day) {
		long timestampAtStartOfDayMs = new LocalDate(year, month, day)
				.toDateTimeAtStartOfDay()
				.getMillis();
		return timestampAtStartOfDayMs;
	}

	public static String getFormattedDate(long timestampInMs) {
		return PATTERN.print(timestampInMs);
	}

	public static long plusDays(long timestampMs, int days) {
		return new DateTime(timestampMs).plusDays(days).getMillis();
	}
}
