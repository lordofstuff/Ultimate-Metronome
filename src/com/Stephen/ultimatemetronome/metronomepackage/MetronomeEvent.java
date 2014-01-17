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
	
	final int[] emphasis;
	final int timeSigTop;
	final int timeSigBottom;
	final boolean complex;
	
	int playbackPosition; //used for static playback maybe
	
	//constructor
	//used for loading version 1 files, may be removed later. 
	public MetronomeEvent(double tempo, int[] pattern, float volume, int repeats, int beat, String name) {
	    super();
	    this.tempo = tempo;
	    this.pattern = pattern;
	    this.volume = volume;
	    this.repeats = repeats;
	    this.beat = beat;
	    this.name = name;
	    emphasis = null;
		timeSigTop = 0;
		timeSigBottom = 0;
		complex = true;
    }
	
	//used for version 2 files. does not depend on the old one. 
	public MetronomeEvent(double tempo, int[] pattern, float volume, int repeats, int beat, String name, int[] emphasis, int timeSigTop, int timeSigBottom, boolean complex) {
	    this.tempo = tempo;
	    this.pattern = pattern;
	    this.volume = volume;
	    this.repeats = repeats;
	    this.beat = beat;
	    this.name = name;
	    this.emphasis = emphasis;
		this.timeSigTop = timeSigTop;
		this.timeSigBottom = timeSigBottom;
		this.complex = complex;
	}
	
	
	
	//methods
	
	
	
}
