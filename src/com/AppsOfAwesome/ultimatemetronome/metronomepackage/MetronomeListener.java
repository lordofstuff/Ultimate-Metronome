package com.AppsOfAwesome.ultimatemetronome.metronomepackage;

/**
 * An interface for an object that will be notified of changes to the current metronome's state, such as updates to the 
 * @author Stephen
 *
 */
interface MetronomeListener {
	
	void majorBeatUpdate(int beat);
	
	void measureUpdate(int measureInEvent);
	
	void eventUpdate(MetronomeEvent newEvent);
	
	void songEnd();
	
}
