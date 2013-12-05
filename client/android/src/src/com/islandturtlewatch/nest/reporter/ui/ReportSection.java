package com.islandturtlewatch.nest.reporter.ui;

public enum ReportSection {
	INFO("Info"),
	NEST_LOCATION("Nest Location"),
	NEST_CONDITION("Nest Condition"),
	NEST_INTERVENTION("Nest Intervention"),
	NEST_CARE("Nest Care"),
	MEDIA("Pictures and Diagrams"),
	NOTES("Notes");
	
	private boolean enabled = true;
	private final String displayName;
	
	private ReportSection(String displayName) {
		this.displayName = displayName;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}