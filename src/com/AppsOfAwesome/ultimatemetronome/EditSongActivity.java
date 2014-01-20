package com.AppsOfAwesome.ultimatemetronome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.AppsOfAwesome.ultimatemetronome.metronomepackage.FileFormatException;
import com.AppsOfAwesome.ultimatemetronome.metronomepackage.Song;
import com.AppsOfAwesome.ultimatemetronome.R;
//import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;




import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;




public class EditSongActivity extends SherlockFragmentActivity {

	private static final String Tag = "Edit Song Activity";
	public static final int NEW_FLAG = 1;
	public static final int EDIT_FLAG = 2;
	private CustomLinkedList<EventCreateObject> songList = null;
	private SongListFragment listFragment;
	protected int songName;
	private int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_song);

		//load the song/create a new one
		Intent intent = getIntent();
		int flag = intent.getIntExtra("LoadFlag", NEW_FLAG);
		if (flag == NEW_FLAG) {
			songList = new CustomLinkedList<EventCreateObject>();
		}
		else {
			loadSongEdit(intent.getStringExtra("fileName"));
		}

		//add the list fragment in
		String listFragmentTag = "ListFragment";
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.create_song_container, new SongListFragment(), listFragmentTag);
		ft.commit(); 
		//listFragment = (SongListFragment) fm.findFragmentByTag(listFragmentTag); //returns null for some reason...
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.create_song_menu, menu);
		return true;
	}

	CustomLinkedList<EventCreateObject> getSongList() {
		return songList;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		listFragment = (SongListFragment) getSupportFragmentManager().findFragmentByTag("ListFragment");
		switch (item.getItemId()) {
			case R.id.add_event_action:

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
						sb.append(EditEventActivity.convertFromPattern(e.getPattern()));
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


	public void editEvent(int position) {
		//small screen behavior
		this.position = position;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.create_song_container, new EditEventFragment(), "EditFragment");
		ft.addToBackStack(null);
		ft.commit();
		
	}
	
	int getPosition() {
		return position;
	}





}
