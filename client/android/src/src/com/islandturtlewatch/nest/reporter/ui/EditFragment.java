package com.islandturtlewatch.nest.reporter.ui;

import android.support.v4.app.Fragment;
import android.view.View;

import com.google.common.collect.ImmutableMap;

public class EditFragment extends Fragment {
	public ImmutableMap<Integer, ClickHandler> getClickHandlers() {
		return ImmutableMap.of();
	}

	public static abstract class ClickHandler {
		private final int resourceId;
		protected ClickHandler(int resourceId) {
	    this.resourceId = resourceId;
    }
		public abstract void handleClick(View view);

		public static ImmutableMap<Integer, ClickHandler> toMap(ClickHandler... handlers) {
			ImmutableMap.Builder<Integer, ClickHandler> mapBuilder = ImmutableMap.builder();
			for (ClickHandler handler : handlers) {
				mapBuilder.put(handler.resourceId, handler);
			}
			return mapBuilder.build();
		}
	}
}
