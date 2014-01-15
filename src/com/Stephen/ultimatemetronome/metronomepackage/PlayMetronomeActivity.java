package com.Stephen.ultimatemetronome.metronomepackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.Stephen.ultimatemetronome.R;
import com.Stephen.ultimatemetronome.metronomepackage.MetronomeController.MetronomeState;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class PlayMetronomeActivity extends SherlockFragmentActivity implements MetronomeListener {

	private static final String Tag = "PlayMetronomeActivity";
	Song song;
	MetronomeController mc;
	Metronome met;
	TextView measureAndBeat;
	TextView eventName;
	Button playPausebutton;
	Button stopButton;
	static enum Mode {Streaming, Static};
	Mode mode = Mode.Streaming;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_metronome_activity);
		Intent intent = getIntent();
		String fileName = intent.getStringExtra("Filename");
		song = loadSong(fileName);
		if (mode == Mode.Streaming) {
			mc = new MetronomeController(getApplicationContext(), song);
			mc.addMetronomeListener(this);
		}
		else {
			met = new Metronome(song, getApplicationContext());
		}
		eventName = (TextView) findViewById(R.id.Current_event_name_textview);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.play_metronome_activity, menu);
		return true;
	}

	private Song loadSong(String fileName) {
		Song song = null;
		File file = new File(getBaseContext().getFilesDir(), fileName);
		try {
			song = Song.createFromFileForPlayback(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return song;
	}


	public void playOrPause(View view) {
		if (mode == Mode.Streaming) {
			synchronized(mc) {
				if (mc.getState() == MetronomeState.NotYetPlayed) {
					Log.v(Tag, "(re)Starting Metronome");
					mc.startMet();
				}
				else if (mc.getState() == MetronomeState.Playing) {
					Log.v(Tag, "Pausing Metronome");
					mc.pause();
				}
				else if (mc.getState() == MetronomeState.Paused) {
					Log.v(Tag, "Resuming Metronome");
					mc.resume();
				}
			}
		}
		else { //Static mode
			if (met.getState() == Metronome.MetronomeState.Initialized) {
				Log.v(Tag, "(re)Starting Metronome");
				met.play();
			}
			else if (met.getState() == Metronome.MetronomeState.Playing) {
				Log.v(Tag, "Pausing Metronome");
				met.pause();
			}
			else if (met.getState() == Metronome.MetronomeState.Paused) {
				Log.v(Tag, "Resuming Metronome");
				met.resume();
			}
		}
	}

	public void stop(View view) {
		if (mode == Mode.Streaming) {
			if (mc.getState() == MetronomeState.Playing) {
				mc.stop();
			}
			else {
				Log.v(Tag, "Stop called when it was not playing");
			}
		}
		else {
			met.stop();
		}
	}

	@Override
	public void minorBeatUpdate(int beat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void majorBeatUpdate(int beat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void measureUpdate(int measureInEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void EventUpdate(final MetronomeEvent newEvent) {
		Log.v(Tag, "New Event: " + newEvent.name);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				eventName.setText(newEvent.name);
			}	
		});
	}

	@Override
	public void SongEnd() {
		// TODO Auto-generated method stub

	}

}
