package com.islandturtlewatch.nest.reporter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.reporter.R;

import java.util.Map;

/**
 * Created by ReverendCode on 4/21/16.
 */
public class EditFragmentStorms extends EditFragment {

    private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
            ClickHandler.toMap();

    private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
            TextChangeHandler.toMap();
    @Override
    public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
        return TEXT_CHANGE_HANDLERS;
    }


    @Override
    public Map<Integer, ClickHandler> getClickHandlers() {
        return CLICK_HANDLERS;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_fragment_nest_condition, container, false);

    }


    @Override
    public void updateSection(ReportProto.Report report) {}

}
