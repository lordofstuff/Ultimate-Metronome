package com.Stephen.ultimatemetronome.metronomepackage;

import java.util.Iterator;

import com.Stephen.ultimatemetronome.R;
import com.Stephen.ultimatemetronome.Utility;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
	private MetronomeState state;
	private MetronomeListener listener;
	enum Sounds {set1, set2}; //currently there is only one set, and it is incomplete. TODO
	Context context;
	private Song song;


	//constructor
	/**
	 * Creates a controller which will handle all Metronome operations. 
	 * @param context The app context, needed to access sound resources.
	 * @param song the song to be played back. 
	 */
	public MetronomeController(Context context, Song song) {
		this.song = song;
		this.context = context;
		state = MetronomeState.NotYetPlayed;
		listener = null;
	}


	//methods
	/**
	 * Starts this metronome playing for the first time or restarts it after if is stopped or finishes.
	 * @throws IllegalStateException if it is called while playing, paused, or before it has finished destroying the old one. 
	 */
	public void startMet() throws IllegalStateException {
		if (state == MetronomeState.NotYetPlayed) {
		Log.d(Tag, "Starting metronome");
		it = song.iterator();
		currentEvent = it.next();
		if (listener != null) {
			listener.EventUpdate(currentEvent);
		}
		measureInCurrentEvent = 1;
		met = new MyMetronome(context, currentEvent.tempo, currentEvent.volume, currentEvent.pattern, currentEvent.beat, Sounds.set1, this);
		state = MetronomeState.Playing;
		met.start(); 
		}
		else {
			throw new IllegalStateException("startMet called on Metronome that was not in an uninitialized state. (state == NotYetPlayed)");
		}
	}

	/**
	 * Adds a MetronomeListener, which will be notified (off of the UI Thread) when certain events happen. 
	 * @param listener
	 */
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

	/**
	 * makes sure all information for the event about to be written is current with information from the song. 
	 */
	private void updateMet() {
		//Log.d(Tag, "switching events");
		met.tempo = currentEvent.tempo;
		met.pattern = currentEvent.pattern;
		met.volume = currentEvent.volume;
		met.beat = currentEvent.beat;
		met.updateLengths();
		met.updated = true;
	}

	/**
	 * Gets the state of the Metronome;
	 * @return the state of the Metronome.
	 */
	MetronomeState getState() {
		return state;
	}

	/**
	 * Changes this metronome's playback position to the beginning of the passed in event. 
	 * @param event the event to move to. 
	 */
	void ChangeCurrentEvent(MetronomeEvent event) {
		//TODO get all of them working together
		if (state == MetronomeState.Playing) {
			met.pause();
		}
		currentEvent = event;
		updateMet();
		if (state == MetronomeState.Playing) {
			met.resume();
		}
		met.updated = true;
	}
	
//	void nextMeasure() {
//		if (state == MetronomeState.Playing) {
//			met.pause();
//		}
//	}

	/**
	 * Pauses a playing metronome and set it to the beginning of the measure it is on. 
	 * @throws IllegalStateException if called on a metronome that is not playing currently. 
	 */
	void pause() throws IllegalStateException {
		if (state == MetronomeState.Playing) {
			ChangeCurrentEvent(currentEvent);
			state = MetronomeState.Paused;
		}
		else {
			throw new IllegalStateException("Pause called on Metronome that was not playing.");
		}
	}

	/**
	 * Resumes the metronome from being paused at the beginning of the measure it was on when paused. 
	 * @throws IllegalStateException if called when the track is not already paused. 
	 */
	void resume() throws IllegalStateException {
		if (state == MetronomeState.Paused) {
			met.resume();
			state = MetronomeState.Playing;
		}
		else {
			throw new IllegalStateException("resume called on a metronome that was not paused.");
		}
	}

	/**
	 * Immediately stops the metronome if it is playing or paused. 
	 * @throws IllegalStateException if called on a metronome which is not playing or paused (has never been played or already finished).
	 */
	void stop() throws IllegalStateException{
		if (state == MetronomeState.Playing || state == MetronomeState.Paused) {
			state = MetronomeState.Stopping;
			met.stop();
		}
		else if (state == MetronomeState.Stopping) {
			//they pressed the button too fast twice in a row. ignore it. 
		}
		else if (state == MetronomeState.NotYetPlayed) {
			throw new IllegalStateException("Stop called while metronome was uninitialized");
		}
	}

	/**
	 * @author Stephen Rodriguez
	 * This class is a metronome whose tempo, time signature, and volume can dynamically change. 
	 * This allows for incredible freedom without creating tons of instances.  
	 * will run in parallel with MetronomeController which will handle changing values. 
	 */
	private class MyMetronome implements Runnable {

		//fields

		//min and max legal values. may be tweaked
		static final double MAX_TEMPO = 900; //approximately
		static final double MIN_TEMPO = 4;

		private static final int SAMPLE_RATE = 22050;
		private static final int BUFFER_SIZE = 5000;//10000;//22050; //lowered to help with performance TODO check this, experiment
		private final short[] primarySoundData;
		private final short[] secondarySoundData;
		private static final int WriteChunk = 4410; //100 ms

		private AudioTrack track;
		boolean playing = false;

		//debug things:
		private static final String Tag = "MyMetronome";

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
		private volatile boolean paused;
		private int written = 0;
		private int position;
		private Thread metThread;
		private boolean stopping;

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
			int minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
			Log.v(Tag, "min buffer size = " + minBuffer);
			Log.v(Tag, "actual Buffer: " + BUFFER_SIZE);
		}

		//methods

		public void flush() {
			track.flush();
			written = 0;
			resetBeat();
		}

		public void resetBeat() {
			currentBeat = 0;
		}

		@Override
		public void run() {
			smallestSubdivisionInFrames = (int) (60 * SAMPLE_RATE / (tempo * beat));
			beatSoundLength = primarySoundData.length;
			updateLengths();
			//initialize for play (the parameters are up to date to begin with). 
			stopping = false;
			updated = true;
			track.play();
			while (playing) {
				Log.d(Tag, "starting play loop");
				if (stopping) { //exit out without doing anything else
					return;
				}
				if (!updated) {
					Log.v(Tag, "Updating");
					updateMeasure(); //inform the controller that it has finished a measure
				}
				while (paused) {
					synchronized(metLock) {
						Log.v(Tag, "Paused, waiting.");
						Log.v(Tag, "Flushing");
						flush();
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
			if (paused) {
				//do anything? TODO
				Log.v(Tag, "pause detected after play loop exit.");
			}
			else {
				Log.v(Tag, "Exiting Play loop");
				track.stop(); //should allow it to finish all that is written to the buffer. 
				new Thread(mc).start(); //will destroy this object, null things out. 
			}
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
				//Log.d(Tag, "too fast!");
			}
		}

		private void writeNextBeatOfPattern() {
			short[] data;
			switch(pattern[currentBeat]){				
				case 1:
					data = primarySoundData;
					Log.v(Tag, "Primary");
					break;
				case 2:
					data = secondarySoundData;
					Log.v(Tag, "secondary");
					break;
					//case 3:
					//case 4:
				default:
					data = new short[primarySoundData.length];
					Log.v(Tag, "0 or other");
					break;
			}
			int wrote = track.write(data, 0, beatSoundLength);
			Log.v(Tag, "write beat " + (currentBeat + 1) + "/" + pattern.length);
			written += wrote; //TODO check for errors first
			if (paused) { //should not increment further if it is paused
				return;
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
				beatSoundLength = primarySoundData.length;
			}
			else {
				beatSoundLength = smallestSubdivisionInFrames;
			}
		}

		/**
		 * Starts this metronome playing for the first time in its own thread. 
		 */
		public void start() {
			Log.v(Tag, "Starting Metronome");
			playing = true;
			metThread = new Thread(this);
			metThread.start();
		}

		/**
		 * Stops the metronome once it finishes playing what is already queued if it was running.
		 */
		public void finish(){
			finishQueue = true;
			playing = false;
		}

		private void stop() {
			if (!paused) {
				Log.v(Tag, "Stop called while track was paused.");
				//TODO make sure there are no issues with this. 
			}
			stopping  = true;
			track.pause();
			track.flush();
			new Thread(mc).start();
		}


		void pause() {
			if (paused) {
				throw new IllegalStateException("pause called while already paused.");
			}
			if (!playing) {
				Log.v(Tag, "Pausing after track done being written.");
			}
			track.pause();
			flush();
			//position  = track.getPlaybackHeadPosition();
			//Log.v(Tag, "Pausing; playback Head Position " + position);
			paused = true;
		}

		void resume() {
			if (!paused) {
				throw new IllegalStateException("Resume called while metronome was not paused");
			}
			Log.v(Tag, "Resuming Metronome");
			Log.v(Tag, "written " + written);
			//track.setPlaybackHeadPosition(position);
			if (!playing) {
				Log.v(Tag, "resuming after all values written");
				//track.play();
			}
			paused = false;
			flush();
			track.play();
			synchronized(metLock) {
				metLock.notifyAll();
			}
			Log.v(Tag, "notified to unpause.");
		}


	}

}
