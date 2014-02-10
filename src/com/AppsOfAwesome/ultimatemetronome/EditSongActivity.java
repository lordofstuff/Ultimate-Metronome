package com.AppsOfAwesome.ultimatemetronome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.AppsOfAwesome.ultimatemetronome.metronomepackage.FileFormatException;
import com.AppsOfAwesome.ultimatemetronome.metronomepackage.Song;
import com.AppsOfAwesome.ultimatemetronome.EditEventFragment.EditEventParent;
import com.AppsOfAwesome.ultimatemetronome.PatternPickerFragment.PatternFragmentParent;
import com.AppsOfAwesome.ultimatemetronome.R;
import com.AppsOfAwesome.ultimatemetronome.SongListFragment.ListParent;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;




import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;




public class EditSongActivity extends SherlockFragmentActivity implements ListParent, EditEventParent, PatternFragmentParent{

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		autoSave();
	}

	private void autoSave() {
		// TODO Auto-generated method stub
		Log.v(Tag, "Autosave not yet implemented.");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//use this to save the song and any other relevant data for recreation after rotation, resuming, etc. 
		outState.putParcelable(songListKey, songList);
		outState.putBoolean(patternOutKey, patternOut);
		outState.putBoolean(eventOutKey, eventOut);
		outState.putInt(indexKey, songList.getIndex(currentEvent));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			songList = savedInstanceState.getParcelable(songListKey);
			patternOut = savedInstanceState.getBoolean(patternOutKey);
			eventOut = savedInstanceState.getBoolean(eventOutKey);
			currentEvent = songList.get(savedInstanceState.getInt(indexKey));
			
			//get the UI back to how it was
			if (eventOut) {
				editEvent(currentEvent);
			}
			if (patternOut) {
				editPattern(0);
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	public void finishActivity(int requestCode) {
		// TODO Auto-generated method stub
		super.finishActivity(requestCode);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	public void recreate() {
		// TODO Auto-generated method stub
		super.recreate();
	}

	private static final String Tag = "Edit Song Activity";
	public static final int NEW_FLAG = 1;
	public static final int EDIT_FLAG = 2;
	private static final int NORMAL_PATTERN = 0;
	private static final int BEAT_PATTERN = 1;

	//keys for bundling:
	private static final String songListKey = "songList";
	private static final String eventOutKey = "eventOut";
	private static final String patternOutKey = "patternOut";
	private static final String indexKey = "currentIndex";


	private CustomLinkedList<EventCreateObject> songList = null;
	private SongListFragment listFragment;
	private EditEventFragment eventFragment;
	private PatternPickerFragment patternFragment;
	protected String songName;
	//private int position;
	private EventCreateObject currentEvent;

	private boolean eventOut;
	private boolean patternOut;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_song);

		if (savedInstanceState == null) {
			//load the song/create a new one
			Intent intent = getIntent();
			int flag = intent.getIntExtra("LoadFlag", NEW_FLAG);
			if (flag == NEW_FLAG) {
				songList = new CustomLinkedList<EventCreateObject>();
			}
			else {
				songList = loadSongEdit(intent.getStringExtra("fileName"));
			}
		}


		//add the list fragment in
		String listFragmentTag = "ListFragment";
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (!getResources().getBoolean(R.bool.tablet_layout)) {
			ft.add(R.id.create_song_container, new SongListFragment(), listFragmentTag);
		}
		else {
			//hide the unused containers
			findViewById(R.id.pattern_container).setVisibility(View.GONE);
			findViewById(R.id.event_container).setVisibility(View.GONE);
			eventOut = false;
			patternOut = false;
			ft.add(R.id.list_container, new SongListFragment(), listFragmentTag);
		}
		ft.commit();
		//listFragment = (SongListFragment) fm.findFragmentByTag(listFragmentTag); //returns null for some reason...
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.create_song_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		listFragment = (SongListFragment) getSupportFragmentManager().findFragmentByTag("ListFragment");
		switch (item.getItemId()) {
		case R.id.add_event_action:
			//findViewById(R.id.create_song_container).invalidate();
			listFragment.addEvent(null);
			break;
		case R.id.toggle_sort_action:
			if (item.isChecked()) { //it was already checked; disable sorting and uncheck it
				listFragment.setSort(false);
				item.setChecked(false);
			}
			else {
				listFragment.setSort(true);
				item.setChecked(true);
			}
			break;
		case R.id.save_action:
			saveSong(null);
			break;
		default:
			break;
		}
		return true;
	} 

	public void saveSong(View view) {
		new Thread(new Runnable() {
			public void run() {
				//file version 2.1 (modified to include extra info on simple time sig subdivisions and any additional notes
				String fileName = "song.txt";
				CustomLinkedList<EventCreateObject> list = songList;
				FileOutputStream outputStream;
				StringBuilder sb = new StringBuilder(200);
				String string;
				sb.append("file version 2.1\n");
				sb.append(list.size() + "\n" + songName);
				sb.append("\n#lines beginning with a # are comments which are ignored.\n");
				sb.append("#blank lines are also ignored\n");
				sb.append("#Files will have the following format (version 2)\n\n");
				sb.append("#name\n#tempo\n#volume\n#pattern\n#repeats\n#beat\n");
				sb.append("#complex (1) or simple (2)\n");
				sb.append("#beat designator (major vs subdivision beats)\n");
				sb.append("#top of time signature\n");
				sb.append("#bottom of time signature\n");
				sb.append("#information about simple time signature subdivisions\n");
				sb.append("#<notes>\n");
				sb.append("#additional information to be displayed about the song, which can be several lines long\n");
				sb.append("#</notes>\n\n");
				string = sb.toString();

				try {
					outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
					outputStream.write(string.getBytes());
					for (EventCreateObject e: songList) {
						sb.setLength(0);
						sb.append(e.getName());
						sb.append("\n");
						sb.append(e.getTempo());
						sb.append("\n");
						sb.append(e.getVolume());
						sb.append("\n");
						sb.append(EditEventFragment.convertFromPattern(e.getPattern()));
						sb.append("\n");
						sb.append(e.getRepeats());
						sb.append("\n");
						sb.append(e.getBeat());
						sb.append("\n");
						if (e.isComplex()) {
							sb.append("1\n");
						}
						else {
							sb.append("0\n");
						}
						sb.append(e.getEmphasisString());
						sb.append("\n");
						sb.append(e.getTimeSigTop());
						sb.append("\n");
						sb.append(e.getTimeSigBottom());
						sb.append("\n");
						sb.append(e.getTimeSigInfo());
						sb.append("\n<notes>\n");
						sb.append(e.getNotes());
						sb.append("\n</notes>\n\n");
						string = sb.toString();
						outputStream.write(string.getBytes());
					}
					outputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		//TODO display a toast when done
		Toast.makeText(getBaseContext(), "DONE!", Toast.LENGTH_LONG).show();
	}

	private CustomLinkedList<EventCreateObject> loadSongEdit(String fileName) {
		File file = new File(getBaseContext().getFilesDir(), fileName);
		try {
			return Song.createFromFileForEdit(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(Tag, "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; //it will not reach this point, hopefully. 
	}

	public void editDataChanged() {
		listFragment = (SongListFragment) getSupportFragmentManager().findFragmentByTag("ListFragment");
		listFragment.notifyDataChanged();
	}

	public void patternFragmentDetach() {
		if (!getResources().getBoolean(R.bool.tablet_layout)) {
			//TODO anything?
		}
		else {
			patternOut = false;
			//make the container invisible again.
			findViewById(R.id.pattern_container).setVisibility(View.GONE);
			//make the list visible again
			findViewById(R.id.list_container).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public CustomLinkedList<EventCreateObject> getList() {
		return songList;
	}

	@Override
	public void editEvent(EventCreateObject event) {
		//first, set the event so it is accessible to the fragment
		this.currentEvent = event;
		if (!getResources().getBoolean(R.bool.tablet_layout)) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.create_song_container, new EditEventFragment(), "EditFragment");
			ft.addToBackStack(null);
			ft.commit();
		}
		else {
			if (eventOut) {
				//currentEvent = event;
				eventFragment =  (EditEventFragment) getSupportFragmentManager().findFragmentByTag("EditFragment");
				eventFragment.changed();
			}
			else {

				//for tablets running HC or newer; should work with 7 inch and up
				Log.v(Tag, "tablet behavior!");

				//eventFragment =  (EditEventFragment) getSupportFragmentManager().findFragmentByTag("EditFragment");
				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.event_container, new EditEventFragment(), "EditFragment");
				findViewById(R.id.event_container).setVisibility(View.VISIBLE);
				eventOut = true;
				ft.addToBackStack(null); //remove? TODO
				ft.commit();
			}
		}
	}

	@Override
	public EventCreateObject getCurrentEvent() {
		return currentEvent;
	}

	@Override
	public void detachEditFragment() {
		if (!getResources().getBoolean(R.bool.tablet_layout)) {
			//TODO anything?
		}
		else {
			//make the container invisible again.
			findViewById(R.id.event_container).setVisibility(View.GONE);
			eventOut = false;
		}
	}

	@Override
	public void editPattern(int flag) {
		if (!getResources().getBoolean(R.bool.tablet_layout)) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.create_song_container, new PatternPickerFragment(), "PatternFragment");
			ft.addToBackStack(null);
			ft.commit();
		}
		else {
			if (patternOut) {
				//this means the list is invisible, and the nothing should happen (I think)
				//just need to notify the pattern fragment to update the ui in case it is editing a different pattern or the event has changed
				((PatternPickerFragment) getSupportFragmentManager().findFragmentByTag("PatternFragment")).dataChanged();
			}
			else {
				//make the container visible again.
				findViewById(R.id.pattern_container).setVisibility(View.VISIBLE);
				//make the list container invisible
				findViewById(R.id.list_container).setVisibility(View.GONE);

				//adding the pattern picker to the layout
				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.pattern_container, new PatternPickerFragment(), "PatternFragment");
				ft.addToBackStack(null);
				patternOut = true;
				ft.commit();
			}
		}
	}

	@Override
	public int getNormalPatternConstant() {
		return NORMAL_PATTERN;
	}

	@Override
	public int getBeatPatternConstant() {
		return BEAT_PATTERN;
	}



}
