package com.islandturtlewatch.nest.reporter;

import android.os.Bundle;

import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditFragment;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentInfo;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentMedia;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCare;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCondition;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestLocation;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestResolution;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.ReportSection;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;

public class EditPresenter {
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
	private final ReportsModel model;
	private final EditView view;

	private ReportSection currentSection;

	public EditPresenter(ReportsModel model, EditView activity) {
		this.model = model;
		this.view = activity;
		this.view.setSectionListEventHandler(new SectionListEventHandler());
	}

	private void setSection(ReportSection section) {
		EditFragment fragment = fragmentMap.get(section);

		Bundle arguments = new Bundle();
		//arguments.putString(ReportDetailFragment.ARG_ITEM_ID, id);
		fragment.setArguments(arguments);
		view.setEditFragment(fragment);
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
