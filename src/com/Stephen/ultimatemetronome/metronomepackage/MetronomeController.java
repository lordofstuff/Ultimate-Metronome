package com.Stephen.ultimatemetronome.metronomepackage;

import java.util.Iterator;

import com.Stephen.ultimatemetronome.R;
import com.Stephen.ultimatemetronome.Utility;
import com.Stephen.ultimatemetronome.metronomepackage.MyMetronome.Sounds;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
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
	private int measureInCurrentEvent;
	private MetronomeEvent currentEvent;
	private Iterator<MetronomeEvent> it;
	private MyMetronome met = null;
	static enum MetronomeState{NotYetPlayed, Playing, Paused, Stopping};
	private MetronomeState state = MetronomeState.NotYetPlayed;
	private MetronomeListener listener = null;
	enum Sounds {set1, set2}; //currently there is only one set, and it is incomplete. TODO
	Context context;
	private Song song;


	//constructor
	public MetronomeController(Context context, Song song) {
		this.song = song;
		this.context = context;
		state = MetronomeState.NotYetPlayed;
	}


	//methods
	public void startMet() {
		Log.d(Tag, "Starting metronome");
		it = song.iterator();
		currentEvent = it.next();
		measureInCurrentEvent = 1;
		met = new MyMetronome(context, currentEvent.tempo, currentEvent.volume, currentEvent.pattern, currentEvent.beat, Sounds.set1, this);
		state = MetronomeState.Playing;
		met.start(); 
	}

	public void addMetronomeListener(MetronomeListener listener) {
		this.listener = listener;
	}

	/**
	 * Called by MyMetronome every time it reaches the end of a measure. 
	 * increments measure numbers and events, including updating the parameters of the metronome as necessary.
	 */
	public void updateMeasure() {
		//gets called every time the metronome finishes a measure
		//thus, it will increment the measure number and change values for a different event when needed.
		Log.v(Tag, "Finished measure " + measureInCurrentEvent + "/" + currentEvent.repeats);

		//cases: it is just a new measure in the set
		if (measureInCurrentEvent < currentEvent.repeats) {
			Log.d(Tag, "finished measure " + measureInCurrentEvent + "//" + currentEvent.repeats);
			measureInCurrentEvent++;
			if (listener != null) {
				listener.measureUpdate(measureInCurrentEvent);
			}
			met.updated = true;
		}

		//it is the end of an event and another follows
		else if (it.hasNext()) {
			currentEvent = it.next();
			if (listener != null) {
				listener.EventUpdate(currentEvent);
			}
			measureInCurrentEvent = 1;
			updateMet();
		}

		//it is the end of the song. 
		else {
			Log.d(Tag, "should be finishing");
			met.finish();
			met.updated = true;
		}
	}

	/**
	 * Called only when the metronome finishes playing or is stopped. Will release all resources and null out unneeded values.  
	 */
	@Override
	public synchronized void run() {
		Log.v(Tag, "Cleaning up");
		met.track.flush();
		met.track.stop();
		met.track.release();
		met = null;
		state = MetronomeState.NotYetPlayed;
		Log.v(Tag, "Cleaned up");
	}

	private void updateMet() {
		//Log.d(Tag, "switching events");
		met.tempo = currentEvent.tempo;
		met.pattern = currentEvent.pattern;
		met.volume = currentEvent.volume;
		met.beat = currentEvent.beat;
		met.updateLengths();
		met.updated = true;
	}

	MetronomeState getState() {
		return state;
	}

	void ChangeCurrentEvent(MetronomeEvent event) {
		//TODO
	}

	void pause() {
		state = MetronomeState.Paused;
		met.pause();
	}

	void resume() {
		state = MetronomeState.Playing;
		met.resume();
	}

	/**
	 * @author Stephen Rodriguez
	 * This class is a metronome whose tempo, time signature, and volume can dynamically change. 
	 * This allows for incredible freedom without creating tons of instances.  
	 * will run in parallel with MetronomeController which will handle changing values. 
	 */
	private class MyMetronome implements Runnable{

		//fields

		//min and max legal values. may be tweaked
		static final double MAX_TEMPO = 900; //approximately
		static final double MIN_TEMPO = 4;

		private static final int SAMPLE_RATE = 22050;
		private static final int BUFFER_SIZE = 22050;
		// Arrays representing audio for a full length beat and half length beats. 
		private final short[] primarySoundData;
		private final short[] secondarySoundData;
		private final short[] primaryHalfLength;
		private final short[] secondaryHalfLength;
		//Will actually hold the reference to the one being used
		private short[] primaryData;
		private short[] secondaryData;

		private AudioTrack track;
		boolean playing = false;

		//debug things:
		private static final String Tag = "MyMetronome";
		private static final int WriteChunk = 8820; //200 ms


		/** Measured in BPM, defined as the time between "quarter notes" in whatever time signature is being used. */
		double tempo;
		/** Must be between 0 and 1, those being the min and max volumes. */
		double volume;


		/**
		 * An array of ints where each number represents the smallest subdivision of a beat. 
		 * Possible values:
		 * 0 does not sound
		 * 1 primary tick (for example, quarter notes)
		 * 2 secondary tock (for example, eighth notes)
		 * 3 tertiary tack (for example, 16th notes)
		 * 4 quaternary tick if desired
		 */
		int[] pattern;

		/**
		 * the number of the smallest subdivision that make up one beat (for tempo purposes)
		 */
		int beat;
		int currentBeat;
		MetronomeController mc;
		private boolean finishQueue;
		boolean updated = true;
		Object metLock = new Object();
		private int beatSoundLength;
		private int smallestSubdivisionInFrames;
		private boolean paused;
		private int written = 0;

		//constructor(s)
		MyMetronome(Context context, double tempo, double volume, int[] pattern, int beat, Sounds soundSet, MetronomeController mc) {
			this.tempo = tempo;
			this.volume = volume;
			this.pattern = pattern;
			this.beat = beat;
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
			int minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
			Log.v(Tag, "min buffer size = " + minBuffer);
			Log.v(Tag, "actual Buffer: " + BUFFER_SIZE);
		}

		//methods

		@Override
		public void run() {
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
					Log.v(Tag, "Updating");
					updateMeasure(); //inform the controller that it has finished a measure
				}
				while (paused) {
					synchronized(metLock) {
						try {
							metLock.wait();
						} catch (InterruptedException e) {
							Log.v(Tag, "Met was interuppted during wait.");
						}
					}
				}
				if (!finishQueue) {
					writeNextBeatOfPattern();
					writeSilence();
				}
			}
			Log.v(Tag, "Exiting Play loop");
			track.setNotificationMarkerPosition(written - (smallestSubdivisionInFrames - beatSoundLength));
			track.setPlaybackPositionUpdateListener( new OnPlaybackPositionUpdateListener() {

				@Override
                public void onMarkerReached(AudioTrack arg0) {
					new Thread(mc).start(); //will destroy this object, null things out. 
                }

				@Override
                public void onPeriodicNotification(AudioTrack arg0) {
	                // TODO Auto-generated method stub
	                
                }
			}); 
		}

		private void writeSilence() {
			if (beatSoundLength < smallestSubdivisionInFrames) {
				int restLength = smallestSubdivisionInFrames - beatSoundLength;
				int leftToWrite = restLength;
				while (leftToWrite > 0) {
					int writeLength = Math.min(leftToWrite, WriteChunk);
					track.write(new short[writeLength], 0, writeLength);
					Log.d(Tag, "wrote silence in " + writeLength);
					written += writeLength;
					leftToWrite -= writeLength;
				}
			}
			else {
				Log.d(Tag, "too fast!");
			}
		}

		private void writeNextBeatOfPattern() {
			if (pattern[currentBeat] == 1) {
				track.write(primaryData, 0, primaryData.length);
				Log.v(Tag, "write primary beat " + (currentBeat + 1) + "/" + pattern.length);
				written += primaryData.length;
			} 
			else if (pattern[currentBeat] == 2 ){
				track.write(secondaryData, 0, secondaryData.length);
				Log.v(Tag, "write secondary beat " + (currentBeat + 1) + "/" + pattern.length);
				written += primaryData.length;
			}
			// TODO add cases for 3 and 4 type beats.
			else {
				// Write the amount of rest that a tick or tock would normally take up
				track.write(new short[primaryData.length], 0, primaryData.length);
				Log.v(Tag, "write rest beat " + (currentBeat + 1) + "/" + pattern.length);
				written += primaryData.length;
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

		/**
		 * Starts this metronome playing for the first time in its own thread. 
		 */
		public void start() {
			Log.v(Tag, "Starting Metronome");
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

		private void stop() {
			if (!paused) {
				Log.v(Tag, "Stop called while track was paused.");
				//TODO make sure there are no issues with this. 
			}
			new Thread(mc).start();
		}

		void pause() {
			if (paused) {
				throw new IllegalStateException("pause called while already paused.");
			}
			Log.v(Tag, "Pausing metronome");
			Log.v(Tag, "written " + written);
			Log.v(Tag, "playback Head Position " + track.getPlaybackHeadPosition());
			track.pause();
			Log.v(Tag, "playback Head Position " + track.getPlaybackHeadPosition());
			Log.v(Tag, "playback Head Position " + track.getPlaybackHeadPosition());
			Log.v(Tag, "playback Head Position " + track.getPlaybackHeadPosition());
			paused = true;
		}

		void resume() {
			if (!paused) {
				throw new IllegalStateException("Resume called while metronome was not paused");
			}
			Log.v(Tag, "Resuming Metronome");
			Log.v(Tag, "written " + written);
			Log.v(Tag, "playback Head Position " + track.getPlaybackHeadPosition());
			paused = false;
			synchronized(metLock) {
				metLock.notifyAll();
			}
			track.play();
		}


	}

}
