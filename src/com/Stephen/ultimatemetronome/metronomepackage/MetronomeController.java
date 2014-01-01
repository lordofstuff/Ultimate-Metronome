package com.Stephen.ultimatemetronome.metronomepackage;

import java.util.Iterator;

import com.Stephen.ultimatemetronome.metronomepackage.MyMetronome.Sounds;

import android.content.Context;
import android.util.Log;


/**
 * This class will run on another thread and read from a buffer of 
 * known tempos and patterns to control the metronome class
 * @author Stephen
 *
 */
public class MetronomeController implements Runnable{

	private static final String Tag = "MetronomeController";
	//fields
	//private Song song;
	private int measureInCurrentEvent;
	private MetronomeEvent currentEvent;
	private Iterator<MetronomeEvent> it;
	private final MyMetronome met;


	//constructor
	public MetronomeController(Context context, Song song) {
		//this.song = song;

		it = song.iterator();
		currentEvent = it.next();
		measureInCurrentEvent = 1;

		//create a metronome object with the initial values needed.
		met = new MyMetronome(context, currentEvent.tempo, currentEvent.volume, currentEvent.pattern, currentEvent.beat, Sounds.set1, song, this);
	}


	//methods

	/**
	 * Called by MyMetronome every time it reaches the end of a measure. 
	 * increments measure numbers and events, including updating the parameters of the metronome as necessary.
	 */
	@Override
	public void run() {
		//gets called every time the metronome finishes a measure
		//thus, it will increment the measure number and change values for a different event when needed.
		//Log.v(Tag, "Finished measure " + measureInCurrentEvent + "/" + currentEvent.repeats);
		//cases: it is just a new measure in the set
		if (measureInCurrentEvent < currentEvent.repeats) {
			measureInCurrentEvent++;
			met.updated = true;
			synchronized(met.metLock) {
				met.metLock.notify();
			}
		}
		//it is the end of an event and another follows
		else if (it.hasNext()) {
			currentEvent = it.next();
			measureInCurrentEvent = 1;
			updateMet();
		}
		//it is the end of the song. 
		else {

			met.finish();
		}
	}

	private void updateMet() {
		Log.d(Tag, "switching events");
		met.tempo = currentEvent.tempo;
		met.pattern = currentEvent.pattern;
		met.volume = currentEvent.volume;

		met.beat = currentEvent.beat;
		//met.currentBeat = 0;
		met.updated = true;
		synchronized(met.metLock) {
			met.metLock.notify();
		}
	}

	public void startMet() {
		Log.d(Tag, "Starting metronome");
		met.start(); //FIXME
		//new Thread(met).start();

	}

}
