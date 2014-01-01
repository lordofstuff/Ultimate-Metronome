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
	Object metLock = new Object();

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
			while (!updated) {
				synchronized(metLock) {
					try {
						metLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			interval_in_frames = (int) (60 * SAMPLE_RATE / (tempo * beat)); // Recalculate in case tempo changed

			if (frames_since_played >= interval_in_frames) {
				writeNextBeatOfPattern();
				frames_since_played = primarySoundData.length;
			} else {
				int frames_left_to_wait = interval_in_frames - frames_since_played;

				// Rest for a full write chunk or until the next click needs to play, whichever is less.
				int rest_length_in_frames = Math.min(frames_left_to_wait, WRITE_CHUNK_IN_FRAMES);
				track.write(new short[rest_length_in_frames], 0, rest_length_in_frames);
				Log.d(Tag, "wrote silence in " + rest_length_in_frames);
				frames_since_played += rest_length_in_frames;
			}
		}
		//since it is cutting off early the solution is to either wait the buffer period
		//or set up a listener that stops it when it knows it is done. 
		if (finishQueue) {
			try {
				Log.v(Tag, "Finishing Queue");
				Thread.sleep(BUFFER_SIZE / WRITE_CHUNK_IN_FRAMES * 200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.v(Tag, "Stopping AudioTrack, releasing.");
		track.stop();
		track.release();
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
		playing = false;

	}


	private void writeNextBeatOfPattern() {
		if (pattern[currentBeat] == 1) {
			track.write(primarySoundData, 0, primarySoundData.length);
			Log.v(Tag, "write primary beat " + (currentBeat + 1) + "/" + pattern.length);
		} 
		else if (pattern[currentBeat] == 2 ){
			track.write(secondarySoundData, 0, secondarySoundData.length);
			Log.v(Tag, "write secondary beat " + (currentBeat + 1) + "/" + pattern.length);
		}
		// TODO add cases for 3 and 4 type beats.
		else {
			// Write the amount of rest that a tick or tock would normally take up
			track.write(new short[primarySoundData.length], 0, primarySoundData.length);
			Log.v(Tag, "write rest beat " + (currentBeat + 1) + "/" + pattern.length);
		}
		//currentBeat++;
		//currentBeat %= anyTimeSig.length;
		//adapted to allow for a pattern length that changes. count starts on 0.
		if (currentBeat + 1 >= pattern.length) {
			currentBeat = 0; 
			updated = false;
			new Thread(mc).start(); //inform the controller that it has finished a measure
		}

		else {
			currentBeat++;
		}
	}

}
