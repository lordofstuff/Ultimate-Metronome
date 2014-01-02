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
	static final double MAX_TEMPO = 450; //approximately
	static final double MIN_TEMPO = 4;


	//private static final int WRITE_CHUNK_IN_FRAMES = 8820; // 200 ms
	private static final int SAMPLE_RATE = 22050;
	private static final int BUFFER_SIZE = 22050;
	private final short[] primarySoundData;
	private final short[] secondarySoundData;
	private final short[] primaryHalfLength;
	private final short[] secondaryHalfLength;
	private short[] primaryData;
	private short[] secondaryData;

	private final AudioTrack track;
	boolean playing = false;

	//debug things:
	private static final String Tag = "MyMetronome";


	/** Measured in BPM, defined as the time between "quarter notes" in whatever time signature is being used. */
	volatile double tempo;
	/** Must be between 0 and 1, those being the min and max volumes. */
	volatile double volume;


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
	private boolean finishQueue;
	boolean updated = true;
	//private int lastEventInterval;
	//private boolean wroteSilence = false;
	Object metLock = new Object();
	private int beatSoundLength;
	private int smallestSubdivisionInFrames;
	private boolean paused;

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
			primaryHalfLength = Utility.intToShortArrayHalf(context.getResources().getIntArray(R.array.tick_pcm));
			secondaryHalfLength = Utility.intToShortArrayHalf(context.getResources().getIntArray(R.array.tock_pcm));


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
			primaryHalfLength = Utility.intToShortArrayHalf(context.getResources().getIntArray(R.array.tick_pcm));
			secondaryHalfLength = Utility.intToShortArrayHalf(context.getResources().getIntArray(R.array.tock_pcm));
		}
		track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);

	}

	//methods

	@Override
	public void run() {
		//must run on one thread and notify the controller thread of when it finishes something so it can have values updated. 
		smallestSubdivisionInFrames = (int) (60 * SAMPLE_RATE / (tempo * beat));
		beatSoundLength = primarySoundData.length;
		updateLengths();
		beatSoundLength = primaryData.length;
		//initialize for play (the parameters are up to date to begin with). 
		updated = true;

		track.play();
		while (playing) {
			//Log.d(Tag, "starting play loop");
			if (!updated) {
				//Log.v(Tag, "Updating");
				new Thread(mc).start(); //inform the controller that it has finished a measure
			}
			while (!updated || paused) {
				synchronized(metLock) {
					try {
						metLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (finishQueue) {
				finishThenStop();
			}
			writeNextBeatOfPattern();
			writeSilence();
		}
		Log.v(Tag, "Exiting Play loop");
	}

	private void writeSilence() {
		//smallestSubdivisionInFrames = (int) (60 * SAMPLE_RATE / (tempo * beat)); // Recalculate in case tempo changed
		if (beatSoundLength < smallestSubdivisionInFrames) {
			int restLength = smallestSubdivisionInFrames - beatSoundLength;
			track.write(new short[restLength], 0, restLength);
			//Log.d(Tag, "wrote silence in " + restLength);
		}
		else {
			Log.d(Tag, "too fast!");
		}
	}

	private void writeNextBeatOfPattern() {
		if (pattern[currentBeat] == 1) {
			track.write(primaryData, 0, primaryData.length);
			//Log.v(Tag, "write primary beat " + (currentBeat + 1) + "/" + pattern.length);
		} 
		else if (pattern[currentBeat] == 2 ){
			track.write(secondaryData, 0, secondaryData.length);
			//Log.v(Tag, "write secondary beat " + (currentBeat + 1) + "/" + pattern.length);
		}
		// TODO add cases for 3 and 4 type beats.
		else {
			// Write the amount of rest that a tick or tock would normally take up
			track.write(new short[primaryData.length], 0, primaryData.length);
			//Log.v(Tag, "write rest beat " + (currentBeat + 1) + "/" + pattern.length);
		}
		//increment the beat and measure if needed. 
		if (currentBeat + 1 >= pattern.length) {
			currentBeat = 0; 
			updated = false;
		}
		else {
			currentBeat++;
		}
	}

	private void finishThenStop() {
		//set a marker at the end of the buffered audio?
		track.setNotificationMarkerPosition(1);
		track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

			@Override
			public void onMarkerReached(AudioTrack track) {
				Log.v(Tag, "Stopping AudioTrack, releasing.");
				playing = false;
				track.stop();
				track.release();
				nullOut();
			}
			@Override
			public void onPeriodicNotification(AudioTrack arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	void updateLengths() {
		smallestSubdivisionInFrames = (int) (60 * SAMPLE_RATE / (tempo * beat)); // Recalculate in case tempo changed
		if ((beatSoundLength <= smallestSubdivisionInFrames)) {
			primaryData = primarySoundData;
			secondaryData = secondarySoundData;
		}
		else {
			primaryData = primaryHalfLength;
			secondaryData = secondaryHalfLength;
		}
		smallestSubdivisionInFrames = (int) (60 * SAMPLE_RATE / (tempo * beat)); // Recalculate in case tempo changed
		beatSoundLength = primaryData.length;
	}

	public void start() {
		playing = true;
		new Thread(this).start();
	}

	/**
	 * Stops the metronome once it finishes playing what is already queued if it was running.
	 */
	public void finish() {
		finishQueue = true;
	}

	private void nullOut() {
		// TODO Auto-generated method stub
		//will eventually null out all values not needed to release memory after it finishes. 
	}

	public void stop() {
		if (!paused) {
			Log.v(Tag, "Stop called while track was paused.");
			//TODO make sure there are no issues with this. 
		}
		track.stop();
		finishQueue = false;
		playing = false;
		track.release();
		nullOut();
	}

	public void pause() {
		if (paused) {
			throw new IllegalStateException("pause called while already paused.");
		}
		track.pause();
		paused = true;
	}

	public void resume() {
		if (!paused) {
			throw new IllegalStateException("Resume called while metronome was not paused");
		}
		track.play();
		paused = false;
		metLock.notifyAll();
	}


}
