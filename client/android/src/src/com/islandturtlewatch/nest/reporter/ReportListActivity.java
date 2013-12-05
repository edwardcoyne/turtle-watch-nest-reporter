package com.islandturtlewatch.nest.reporter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


/**
 * An activity representing a list of Reports. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ReportDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ReportListFragment} and the item details
 * (if present) is a {@link ReportDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ReportListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ReportListActivity extends FragmentActivity
        implements ReportListFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        if (findViewById(R.id.report_detail_container) != null) {
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ReportListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.report_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link ReportListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        // In two-pane mode, show the detail view in this activity by
        // adding or replacing the detail fragment using a
        // fragment transaction.
        Bundle arguments = new Bundle();
        arguments.putString(ReportDetailFragment.ARG_ITEM_ID, id);
        ReportDetailFragment fragment = new ReportDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.report_detail_container, fragment)
                .commit();
    }
}
