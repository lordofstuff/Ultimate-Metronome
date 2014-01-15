package com.Stephen.ultimatemetronome.metronomepackage;

import java.util.Iterator;

import com.Stephen.ultimatemetronome.R;
import com.Stephen.ultimatemetronome.Utility;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Handler;
import android.util.Log;

public class Metronome {

	//fields for writing
	private MetronomeEvent writeCurrentEvent;
	private int writeMeasureInCurrentEvent;
	private int[] writePattern;
	private double writeTempo;
	private float writeVolume;
	private int writeSmallestSubdivision;
	private int writeBeat;
	private int writeCurrentBeat;

	//fields for playback
	private MetronomeEvent PBCurrentEvent;
	private int PBMeasureInCurrentEvent;
	private int[] PBPattern;
	private double PBTempo;
	private float PBVolume;
	private int PBSmallestSubdivision;
	private int PBBeat;
	private int PBCurrentBeat;

	//other fields
	static enum MetronomeState{Initialized, Playing, Paused, Uninitialized};
	private static final int SAMPLE_RATE = 22050;
	private final int BUFFER_SIZE; 
	private static final String Tag = "Metronome";
	boolean updated = true;
	private Ticker met;
	private Song song;
	private Iterator<MetronomeEvent> it;
	private MetronomeState state;
	static enum Sounds {set1, set2}; //currently there is only one set, and it is incomplete. TODO
	Sounds soundSet = Sounds.set1;
	Context context;




	//Constructor
	Metronome(Song song, Context context) {
		this.song = song;
		this.context = context;
		//calculate needed buffer
		int bufferFrames = 0;
		for (MetronomeEvent m : song) {
			//set position of this event
			m.playbackPosition = bufferFrames;
			//add to buffer
			int smallestSubdivision = (int) (60 * SAMPLE_RATE / (m.tempo * m.beat));
			bufferFrames += m.pattern.length * smallestSubdivision;
		}
		BUFFER_SIZE = bufferFrames ;/// 2; //two frames per byte in 16 bit mode. 
		Log.v(Tag, "Buffer size: " + bufferFrames);

		//create ticker object object
		state = MetronomeState.Uninitialized;
		initializeMet();
		state = MetronomeState.Initialized;
	}


	//methods

	private void initializeMet() {
		Log.v(Tag, "Loading song");
		it = song.iterator();
		writeCurrentEvent = it.next();
		met = new Ticker(BUFFER_SIZE, context);
		new Thread(met).start();
	}



	/**
	 * called when the song finishes loading into memory.
	 */
	public void finishedWriting() {
		state = MetronomeState.Initialized;
		Log.v(Tag, "whole song loaded");
		//Log.v(Tag, "Audiotrack state: " + met.track.getState());
		Log.v(Tag, "written: " + met.written);
	}

	MetronomeState getState() {
		return state;
	}

	void ChangeCurrentEvent(MetronomeEvent event) {
		//TODO get all of them working together
		PBCurrentEvent = event;
		updateMet();
		updated = true;
	}

	void play() {
		state = MetronomeState.Playing;
		met.start();
	}

	void pause() {
		state = MetronomeState.Paused;
		met.track.pause();
		//ChangeCurrentEvent(currentEvent);
	}

	void resume() {
		state = MetronomeState.Playing;
		met.track.play();
	}

	private void updateMeasure() {
		//gets called every time the metronome finishes a measure
		//thus, it will increment the measure number and change values for a different event when needed.
		Log.v(Tag, "Finished measure " + writeMeasureInCurrentEvent + "/" + writeCurrentEvent.repeats);

		//cases: it is just a new measure in the set
		if (writeMeasureInCurrentEvent < writeCurrentEvent.repeats) {
			Log.d(Tag, "finished measure " + writeMeasureInCurrentEvent + "//" + writeCurrentEvent.repeats);
			writeMeasureInCurrentEvent++;
			//					if (listener != null) {
			//						listener.measureUpdate(measureInCurrentEvent);
			//					}
			updated = true;
		}

		//it is the end of an event and another follows
		else if (it.hasNext()) {
			writeCurrentEvent = it.next();
			//					if (listener != null) {
			//						listener.EventUpdate(currentEvent);
			//					}
			writeMeasureInCurrentEvent = 1;
			updateMet();
		}

		//it is the end of the song. 
		else {
			Log.d(Tag, "should be finishing");
			met.finish();
			updated = true;
		}
	}

	private void updateMet() {
		//actually updates values
		writeTempo = writeCurrentEvent.tempo;
		writeBeat = writeCurrentEvent.beat;
		writePattern = writeCurrentEvent.pattern;
		writeVolume = writeCurrentEvent.volume;
		met.updateLengths();
		updated = true;
	}

	private class Ticker implements Runnable{

		private static final String Tag = "Ticker";
		//fields
		private int writeLength;
		private AudioTrack track;
		private volatile boolean writing;
		private boolean finishQueue;
		private int written;
		PositionListener posListener;

		//sounds:
		private short[] primaryData;
		private short[] secondaryData;
		private Object soundSet;


		Ticker(int BUFFER_SIZE, Context context) {
			if (soundSet == Sounds.set1) {
				primaryData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tick_pcm));
				secondaryData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tock_pcm));
				//add other sounds to this set
			}
			// TODO add more possible .wav files? 
			//		else if (soundSet == sounds.set2){
			//			//add other sounds here
			//		}
			else {
				//default to set1
				primaryData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tick_pcm));
				secondaryData = Utility.intToShortArray(context.getResources().getIntArray(R.array.tock_pcm));
			}
			track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STATIC);
			posListener = new PositionListener();
			track.setPlaybackPositionUpdateListener(posListener, posListener);
		}

		//writes the entire song to the buffer
		@Override
		public void run() {
			updateLengths();
			updateMet();

			//other stuff for initialization
			updated = true;
			writing = true;
			writeCurrentBeat = 0;
			writeMeasureInCurrentEvent = 1;


			while (writing) {
				//starting play loop
				if (!updated) {
					updateMeasure();
				}
				if (!finishQueue) {
					writeBeat();
					writeSilence();
				}
			}
			Log.v(Tag, "exiting write loop.");
			finishedWriting();
		}

		public void finish() {
			finishQueue = true;
			writing = false;
		}

		/**
		 * Truncates sounds if needed so that there is virtually no limit to the tempo that is possible to achieve.  
		 */
		void updateLengths() {
			writeSmallestSubdivision = (int) (60 * SAMPLE_RATE / (writeTempo * writeBeat)); // Recalculate in case tempo changed
			//after recalculating the subdivision length, this will truncate the click sounds if needed (if the tempo is fast enough that the beats are longer than the subdivision length
			if (primaryData.length > writeSmallestSubdivision) {
				writeLength = writeSmallestSubdivision;
			}
			else {
				writeLength = primaryData.length;
			}
		}

		private void writeBeat() {
			if (writePattern[writeCurrentBeat] == 1) {
				write(primaryData);
				Log.v(Tag, "write primary beat " + (writeCurrentBeat + 1) + "/" + writePattern.length);
				//written += primaryData.length;
			}
			else if (writePattern[writeCurrentBeat] == 2 ){
				write(secondaryData);
				Log.v(Tag, "write secondary beat " + (writeCurrentBeat + 1) + "/" + writePattern.length);
				//written += primaryData.length;
			}
			// TODO add cases for 3 and 4 type beats.
			else {
				// Write the amount of rest that a tick or tock would normally take up
				write(new short[primaryData.length]);
				Log.v(Tag, "write rest beat " + (writeCurrentBeat + 1) + "/" + writePattern.length);
				//written += primaryData.length;
			}
			//increment the beat and measure if needed. 
			if (writeCurrentBeat + 1 >= writePattern.length) {
				writeCurrentBeat = 0;
				updated = false;
			}
			else {
				writeCurrentBeat++;
			}
		}

		private void writeSilence() {
			if (writeLength < writeSmallestSubdivision) {
				int restLength = writeSmallestSubdivision - writeLength;
				if (writeLength > 0) {
					write(new short[restLength]);
					Log.d(Tag, "wrote silence in " + restLength);
				}
			}
		}

		//		void updatePositionNotifier() {
		//			track.setPositionNotificationPeriod(writeSmallestSubdivision);
		//		}


		/**
		 * writes to the AudioTrack buffer, automatically taking care of truncation and offset. 
		 * @param data
		 * @return
		 */
		private boolean write(short[] data) {
			int temp = track.write(data, 0, writeLength);
			if (temp > 0) {
				written += temp;
				return true;
			}
			else {
				Log.v(Tag, "Error given on write");
				return false;
			}
		}

		/**
		 * Starts this metronome playing for the first time.
		 */
		public void start() {
			Log.v(Tag, "AudioTrack state: " + track.getPlayState());
			Log.v(Tag, "Starting Metronome");
			track.play();
			Log.v(Tag, "AudioTrack state: " + track.getPlayState());
		}


		private class PositionListener extends Handler implements OnPlaybackPositionUpdateListener {

			@Override
			public void onMarkerReached(AudioTrack arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPeriodicNotification(AudioTrack track) {
				// TODO Auto-generated method stub
				//will update the UI every beat
			}

		}


	}

	public void stop() {
		// TODO Auto-generated method stub
		//temp FIXME
		Log.v(Tag, "PlaybackHead Position: " + met.track.getPlaybackHeadPosition());
	}
}
