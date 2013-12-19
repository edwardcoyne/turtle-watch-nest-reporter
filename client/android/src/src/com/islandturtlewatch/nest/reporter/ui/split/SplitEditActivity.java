package com.islandturtlewatch.nest.reporter.ui.split;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditFragment;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.ClickHandler;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment.EventHandler;

public class SplitEditActivity extends FragmentActivity implements EditView {
	@SuppressWarnings("unused")
	private EditPresenter presenter;
	private ImmutableMap<Integer, ClickHandler> clickHandlers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.split_edit_activity);

		presenter = new EditPresenter(new ReportsModel(), this);
	}

	@Override
	public void setEditFragment(EditFragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.section_edit_container, fragment)
				.commit();
		clickHandlers = fragment.getClickHandlers();
	}

	@Override
	public void setSectionListEventHandler(EventHandler handler) {
		ReportSectionListFragment fragment = (ReportSectionListFragment)getSupportFragmentManager()
				.findFragmentById(R.id.report_section_list);
		fragment.setEventHandler(handler);
	}

	public void handleClick(View view) {
		Preconditions.checkArgument(clickHandlers.containsKey(view.getId()),
				"No click handler registered for %s", view.getId());

		clickHandlers.get(view.getId()).handleClick(view);
	}
}
