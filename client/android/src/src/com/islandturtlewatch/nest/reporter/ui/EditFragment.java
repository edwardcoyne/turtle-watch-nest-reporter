package com.islandturtlewatch.nest.reporter.ui;

import com.islandturtlewatch.nest.reporter.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditFragment extends Fragment {
	
	 @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
       Bundle savedInstanceState) {
       // Inflate the layout for this fragment
       return inflater.inflate(R.layout.edit_fragment_info, container, false);
   }
}
