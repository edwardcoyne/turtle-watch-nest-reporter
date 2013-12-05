package com.islandturtlewatch.nest.reporter.data;

import com.islandturtlewatch.nest.data.ReportProto.Report;

public class ReportsModel {
	private Report activeReport;
	
	public ReportsModel() {
		startNewActiveReport();
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
