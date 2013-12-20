package com.islandturtlewatch.nest.reporter.data;

import android.os.Bundle;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.islandturtlewatch.nest.data.ReportProto.Report;

public class ReportsModel {
  private static final String TAG = ReportsModel.class.getSimpleName();

  private static final String KEY_REPORT = "CurrentReport";

	private Report activeReport;

	public ReportsModel() {
		startNewActiveReport();
	}

	public void persistToBundle(Bundle outState) {
	  outState.putByteArray(KEY_REPORT, activeReport.toByteArray());
	}

	public void restoreFromBundle(Bundle inState) {
	  if (inState.containsKey("CurrentReport")) {
        try {
          activeReport = Report.parseFrom(inState.getByteArray("CurrentReport"));
        } catch (InvalidProtocolBufferException e) {
          Log.e(TAG,"Could not restore proto from seriazlied state, " + e.getMessage());
        }
	  }
	}

	/**
	 * Creates a new report and sets it active.
	 *
	 * <p> The currently active report should be submitted or abandoned.
	 */
	public void startNewActiveReport() {
		setActiveReport(Report.getDefaultInstance());
	}

	/**
	 * Get report we are currently working on.
	 */
	public Report getActiveReport() {
		return activeReport;
	}

	/**
	 * Set report we are currently working on.
	 */
	public void setActiveReport(Report report) {
		activeReport = report;
	}

	/**
	 * Save report we are currently working with to disk.
	 */
	public void saveActiveReport() {
		throw new UnsupportedOperationException("Not Implemented");
	}

	/**
	 * Submit report we are currently working with to cloud.
	 */
	public void submitActiveReport() {
		throw new UnsupportedOperationException("Not Implemented");
	}

	/**
	 * List all reports we have previously submitted.
	 */
	public Iterable<Report> listReports() {
		throw new UnsupportedOperationException("Not Implemented");
	}
}
