package com.islandturtlewatch.nest.reporter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islandturtlewatch.nest.reporter.R;

public class EditFragmentNestResolution extends EditFragment {

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.edit_fragment_nest_resolution, container, false);
  }
}
