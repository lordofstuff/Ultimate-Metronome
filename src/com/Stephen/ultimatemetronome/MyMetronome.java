/**
 * A class that plays metronome sounds. Includes useful methods for working with this implementation.
 */
package com.Stephen.ultimatemetronome;

/**
 * @author Stephen Rodriguez
 *
 */
class MyMetronome implements Runnable{

	//fields
	/** Measured in BPM, defined as the time between "quarter notes" in whatever time signature is being used. */
	private volatile double tempo;
	/** Must be between 0 and 1, those being the min and max volumes. */
	private volatile double volume;
	/** contains four numbers: (simple time signatures)
	 *  The "top" of the time signature
	 *  The "bottom" of the time signature
	 *  The number 
	 *  
	 */
	private volatile int[] timeSig;
	
	//constructor
	MyMetronome(double tempo, double volume, int[] timeSig) {
		this.tempo = tempo;
		this.volume = volume;
	}
	
	//methods
	
	@Override
    public void run() {
	    // TODO Auto-generated method stub
	    
    }
	
	
}
