package com.AppsOfAwesome.ultimatemetronome;

public interface ListParent {
	
	CustomLinkedList<EventCreateObject> getList();
	
	/**
	 * Called when an event is selected by the user for edit. Can be called again and in this case should change what event is displayed in other parts of the User interface. 
	 * @param event the event to be edited.
	 */
	void editEvent(EventCreateObject event);
	
}
