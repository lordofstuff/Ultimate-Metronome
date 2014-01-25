package com.AppsOfAwesome.ultimatemetronome;

public interface EditEventParent {
	
	/**
	 * Gets the event to be edited. 
	 * @return the event to be edited. 
	 */
	EventCreateObject getCurrentEvent();
	
	/**
	 * informs the parent activity that the data on the current event has changed so that it can update the interface of other fragments to match. 
	 */
	void editDataChanged();
	
	/**
	 * called when the fragment detaches so that the parent activity can update the UI accordingly. 
	 */
	void detachEditFragment();
	
	/**
	 * called when a pattern needs to be edited.
	 * @param flag a flag indicating which pattern is to be edited, to be passed along to the corresponding fragment. 
	 */
	void editPattern(int flag);
	
	int getNormalPatternConstant();
	int getBeatPatternConstant();

}
