package com.islandturtlewatch.nest.reporter.ui;

import android.support.v4.app.Fragment;


public interface EditView {
	void setEditFragment(Fragment fragment);
	void setSectionListEventHandler(ReportSectionListFragment.EventHandler handler);
}
