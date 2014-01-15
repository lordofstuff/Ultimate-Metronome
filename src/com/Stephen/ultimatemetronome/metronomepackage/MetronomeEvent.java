package com.Stephen.ultimatemetronome.metronomepackage;

public class MetronomeEvent {
	/*
	 * In this class, the fields are final and there are no getter methods, both for efficiency of execution.
	 * Since this is created for playback, there is no need to edit them, so final is not an issue, and package level access provides quick access by other classes in this package. 
	 */
	
	//fields
	final double tempo;
	final int[] pattern;
	final float volume;
	final int repeats;
	final int beat;
	final String name;
	int playbackPosition; //used for static playback maybe
	
	//constructor
	public MetronomeEvent(double tempo, int[] pattern, float volume, int repeats, int beat, String name) {
	    super();
	    this.tempo = tempo;
	    this.pattern = pattern;
	    this.volume = volume;
	    this.repeats = repeats;
	    this.beat = beat;
	    this.name = name;
    }
	
	//methods
	
	
	
}
