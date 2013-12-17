package com.islandturtlewatch.nest.reporter.ui;

import com.islandturtlewatch.nest.reporter.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditFragmentInfo extends EditFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
    		ViewGroup container,
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_fragment_info, container, false);
    }
}
