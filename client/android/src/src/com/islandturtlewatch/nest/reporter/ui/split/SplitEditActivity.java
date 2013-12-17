package com.islandturtlewatch.nest.reporter.ui.split;

import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment.EventHandler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class SplitEditActivity extends FragmentActivity implements EditView {
	@SuppressWarnings("unused")
  private EditPresenter presenter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
      setContentView(R.layout.split_edit_activity);  
      
      presenter = new EditPresenter(new ReportsModel(), this);
	}
	
	public void setEditFragment(Fragment fragment) {
	  getSupportFragmentManager().beginTransaction()
        .replace(R.id.section_edit_container, fragment)
        .commit();
	}

	@Override
  public void setSectionListEventHandler(EventHandler handler) {
		ReportSectionListFragment fragment = (ReportSectionListFragment)getSupportFragmentManager()
				.findFragmentById(R.id.report_section_list);
		fragment.setEventHandler(handler);
	}
}
