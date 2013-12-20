package com.islandturtlewatch.nest.reporter.ui;

import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.ImmutableList;

public class ReportSectionListFragment extends ListFragment {

    private EventHandler eventHandler = new EventHandler.Dummy();
    private ReportSectionAdapter reportSections;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReportSectionListFragment() {
    }

    public void setEventHandler(EventHandler eventHandler) {
    	this.eventHandler = eventHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportSections = new ReportSectionAdapter();
        setListAdapter(reportSections);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        eventHandler.onSectionSelected(reportSections.getSection(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private class ReportSectionAdapter extends ArrayAdapter<ReportSection> {
    	private final List<ReportSection> sections = ImmutableList.copyOf(ReportSection.values());

    	ReportSectionAdapter() {
    		super(getActivity(),
    		    android.R.layout.simple_list_item_activated_1,
                android.R.id.text1);
    		addAll(sections);
    	}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return sections.get(position).isEnabled();
		}

		ReportSection getSection(int position) {
			return sections.get(position);
		}
  }

	public interface EventHandler {
	  public void onSectionSelected(ReportSection section);

	  public class Dummy implements EventHandler {
		@Override
		public void onSectionSelected(ReportSection section) {}
	  }
	}
}
