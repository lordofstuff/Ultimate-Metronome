/**
 * A class that plays metronome sounds. Includes useful methods for working with this implementation.
 */
package com.Stephen.ultimatemetronome.metronomepackage;





import com.Stephen.ultimatemetronome.R;
import com.Stephen.ultimatemetronome.Utility;
//import com.Stephen.ultimatemetronome.R.array;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * @author Stephen Rodriguez
 * This class is a metronome whose tempo, time signature, and volume can dynamically change. 
 * This allows for incredible freedom without creating tons of instances.  
 * will run in parallel with MetronomeController which will handle changing values. 
 */
class MyMetronome implements Runnable{

	//fields
	//things that might be configurable later. 
	enum Sounds {set1, set2}; //currently there is only one set, and it is incomplete. TODO

	//min and max legal values. may be tweaked
	static final double MAX_TEMPO = 250;
	static final double MIN_TEMPO = 4;

	//stuff borrowed from practice tools
	private static final int WRITE_CHUNK_IN_FRAMES = 8820; // 200 ms
	private static final int SAMPLE_RATE = 22050;
	private static final int BUFFER_SIZE = 22050;
	private final short[] primarySoundData;
	private final short[] secondarySoundData;
	private final AudioTrack track;
	boolean playing = false;


	/** Measured in BPM, defined as the time between "quarter notes" in whatever time signature is being used. */
	volatile double tempo;
	/** Must be between 0 and 1, those being the min and max volumes. */
	volatile double volume;

	/** contains four numbers: (simple time signatures)
	 *  The "top" of the time signature
	 *  The "bottom" of the time signature
	 *  The number of the smallest unit that make a single beat.
	 *  TODO probably get rid of this
	 */
	private volatile int[] timeSig;

	/**
	 * An array of boolean determining whether each smallest subdivision of a beat should be sounded.
	 * TODO probably get rid of this
	 */
	private volatile boolean[] soundTick;

	/**
	 * An array of ints where each number represents the smallest subdivision of a beat. 
	 * Possible values:
	 * 0 does not sound
	 * 1 primary tick (for example, quarter notes)
	 * 2 secondary tock (for example, eighth notes)
	 * 3 tertiary tack (for example, 16th notes)
	 * 4 quaternary tick if desired
	 */
	volatile int[] pattern;

	/**
	 * the number of the smallest subdivision that make up one beat (for tempo purposes)
	 */
	volatile int beat;
	int currentBeat;
	Song song;
	MetronomeController mc;

	//constructor(s)
	/**
	 * TODO
	 * @param tempo
	 * @param volume
	 * @param timeSig
	 */
	MyMetronome(Context context, double tempo, double volume, int[] anyTimeSig, int beat, Sounds soundSet, Song song, MetronomeController mc) {
		this.tempo = tempo;
		this.volume = volume;
		this.pattern = anyTimeSig;
		this.beat = beat;
		this.song = song;
		this.mc = mc;

		if (soundSet == Sounds.set1) {
			primarySoundData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tick_pcm));
			secondarySoundData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tock_pcm));
			//add other sounds to this set
		}
		// TODO add more possible .wav files? 
		//		else if (soundSet == sounds.set2){
		//			//add other sounds here
		//		}
		else {
			//default to set1
			primarySoundData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tick_pcm));
			secondarySoundData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tock_pcm));
		}
		track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);

	}

	//methods

	/**
	 * Converts a simple time signature to an arbitrary one. 
	 * @return
	 */
	static int[][] convertSimpleToComplex() {

		//TODO something with this
		//not yet implemented


		return new int[][] {{1}};
	}

	@Override
	public void run() {
		Log.d("MyMetronome","It has actually started, maybe.");

		//must run on one thread and notify the controller thread of when it finishes something so it can have values updated. 

		track.play();
		int interval_in_frames = (int) (60 * SAMPLE_RATE / (tempo * beat));
		int frames_since_played = interval_in_frames;

		while (playing) {
			Log.d("MyMetronome","still playing");
			interval_in_frames = (int) (60 * SAMPLE_RATE / (tempo * beat)); // Recalculate in case tempo changed
			//frames_since_played = interval_in_frames; //also in case tempo changes
			
			if (frames_since_played >= interval_in_frames) {
				writeNextBeatOfPattern();
				frames_since_played = primarySoundData.length;
			} else {
				int frames_left_to_wait = interval_in_frames - frames_since_played;

				// Rest for a full write chunk or until the next click needs to play, whichever is less.
				int rest_length_in_frames = Math.min(frames_left_to_wait, WRITE_CHUNK_IN_FRAMES);
				track.write(new short[rest_length_in_frames], 0, rest_length_in_frames);

				frames_since_played += rest_length_in_frames;
			}
		}
		track.stop();
		track.release();
	}
	
	public void start() {
	    //update(tempo, beatsOn, beatsOff);
	    playing = true;

	    new Thread(this).start();
	    //mExecutor.execute(mClicker);
	  }

	  /**
	   * Stops the metronome if it was running.
	   */
	  public void stop() {
	    playing = false;
	    //mClicker = null;
	  }

	
	private void writeNextBeatOfPattern() {
		if (pattern[currentBeat] == 1) {
			track.write(primarySoundData, 0, primarySoundData.length);
		} 
		else if (pattern[currentBeat] == 2 ){
			track.write(secondarySoundData, 0, secondarySoundData.length);
		}
		// TODO add cases for 3 and 4 type beats.
		else {
			// Write the amount of rest that a tick or tock would normally take up
			track.write(new short[primarySoundData.length], 0, primarySoundData.length);
		}
		//currentBeat++;
		//currentBeat %= anyTimeSig.length;
		//adapted to allow for a pattern length that changes. count starts on 1.
		if (currentBeat + 1 >= pattern.length) {
			currentBeat = 0; 
			new Thread(mc).start(); //inform the controller that it has finished a measure
			
		}
		else {
			currentBeat++;
		}
	}


	/**
	 * for debugging only.
	 * 
	 */
	public static void test() {
		//create stuff to be passed in

		double tempo = 120;
		double volume = 1;
		int[] ts = new int[] {1, 3, 2, 0, 1, 3, 2, 3};

	}

}
