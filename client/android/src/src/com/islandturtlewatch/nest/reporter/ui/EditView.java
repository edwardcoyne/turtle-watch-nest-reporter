package com.islandturtlewatch.nest.reporter.ui;

import com.islandturtlewatch.nest.data.ReportProto.Report;

/**
 * Interface that provides an abstract view of the UI.
 */
public interface EditView {
	void updateDisplay(Report report);
}
