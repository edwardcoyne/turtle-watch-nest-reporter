package com.islandturtlewatch.nest.reporter.ui.split;

import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditFragment;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.ClickHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentInfo;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentMedia;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCare;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCondition;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestLocation;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestResolution;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.ReportSection;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment.EventHandler;

public class SplitEditActivity extends FragmentActivity implements EditView {
	private static final ImmutableMap<ReportSection, EditFragment> fragmentMap =
			ImmutableMap.<ReportSection, EditFragment>builder()
					.put(ReportSection.INFO, new EditFragmentInfo())
					.put(ReportSection.NEST_LOCATION, new EditFragmentNestLocation())
					.put(ReportSection.NEST_CONDITION, new EditFragmentNestCondition())
					.put(ReportSection.NEST_CARE, new EditFragmentNestCare())
					.put(ReportSection.NEST_RESOLUTION, new EditFragmentNestResolution())
					.put(ReportSection.MEDIA, new EditFragmentMedia())
					.put(ReportSection.NOTES, new EditFragment())
					.build();

	@SuppressWarnings("unused")
	private EditPresenter presenter;
	private SectionManager sectionManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.split_edit_activity);

		presenter = new EditPresenter(new ReportsModel(), this);
		sectionManager = new SectionManager();
	}

	@Override
  public void updateDisplay(Report report) {
		sectionManager.getCurrentFragment().updateDisplay(report);
  }

	public void handleClick(View view) {
		Map<Integer, ClickHandler> clickHandlers =
				sectionManager.getCurrentFragment().getClickHandlers();

		Preconditions.checkArgument(clickHandlers.containsKey(view.getId()),
				"No click handler registered for %s", view.getId());

		clickHandlers.get(view.getId()).handleClick(view);
	}

	/**
	 * Responsible for managing the current section and updating the display to change sections.
	 */
	private class SectionManager {
		private ReportSection currentSection;
		private EditFragment currentFragment;

		SectionManager() {
			setSectionListEventHandler(new SectionListEventHandler());
		}

		EditFragment getCurrentFragment() {
			return currentFragment;
		}

		private void setSection(ReportSection section) {
			EditFragment fragment = fragmentMap.get(section);

			Bundle arguments = new Bundle();
			fragment.setArguments(arguments);
			setEditFragment(fragment);
		}

		private void setEditFragment(EditFragment fragment) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.section_edit_container, fragment)
					.commit();
			currentFragment = fragment;
		}

		private void setSectionListEventHandler(EventHandler handler) {
			ReportSectionListFragment fragment = (ReportSectionListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.report_section_list);
			fragment.setEventHandler(handler);
		}

		private class SectionListEventHandler
				implements ReportSectionListFragment.EventHandler {
			@Override
			public void onSectionSelected(ReportSection section) {
				if (currentSection == section) {
					return;
				}
				currentSection = section;

				setSection(section);
			}
		}
	}
}
