package com.AppsOfAwesome.ultimatemetronome;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.AppsOfAwesome.ultimatemetronome.metronomepackage.FileFormatException;
import com.AppsOfAwesome.ultimatemetronome.metronomepackage.Song;
import com.AppsOfAwesome.ultimatemetronome.R;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;


public class CreateSongActivity extends SherlockFragmentActivity {

	
	public static final int NEW_FLAG = 1;
	public static final int EDIT_FLAG = 2;
	
	
	String Tag = "CreateSongActivity";
	DragSortListView list;// = (DragSortListView)findViewById(R.id.list);
	CustomArrayAdapter dataAdapter;
	
	
	private String songName = "Poop";

	/* TURD 1 */
	public static EventCreateObject currentEventObject = null;
	/* TURD 1 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(Tag, "activity created");
		setContentView(R.layout.activity_create_song);
		
		//add the list fragment in:
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.create_song_container, new SongListFragment(), "ListFragment");
		ft.commit(); 
		
		CustomLinkedList<EventCreateObject> songList = null;
		Intent intent = getIntent();
		int flag = intent.getIntExtra("LoadFlag", NEW_FLAG);
		if (flag == NEW_FLAG) {
			songList = new CustomLinkedList<EventCreateObject>();
		}
		else {
			String fileName = intent.getStringExtra("fileName");
			File file = new File(getBaseContext().getFilesDir(), fileName);
			try {
	            songList = Song.createFromFileForEdit(file);
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
		}
		dataAdapter = new CustomArrayAdapter(this, R.id.tvItemTitle, songList);
		//Log.d(Tag, "adapter created.");
		list.setAdapter(dataAdapter);
		list.setDropListener(dataAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.create_song_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_event_action:
				addEvent(null);
				break;
			case R.id.toggle_sort_action:
				//TODO add visual indication that sorting is active, disable other behavior when it is.

				if (item.isChecked()) { //it was already checked; disable sorting and uncheck it
					item.setChecked(false);
					list.setDragEnabled(false);
				}
				else {
					item.setChecked(true);
					list.setDragEnabled(true);
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


	@Override
	public void onResume() {
		super.onResume();
		dataAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		dataAdapter.notifyDataSetChanged();
	}

	public void addEvent(View view) {
		dataAdapter.addEvent();
	}


	//should maybe include this as part of eventcreateobject?
	//holds views from the listview
	static class ViewHolder 
	{
		TextView title;
		CheckBox checked;
		ImageView image1;
		TextView eventName;
	}

	private class CustomArrayAdapter extends ArrayAdapter<EventCreateObject> implements DropListener {   
		private CustomLinkedList<EventCreateObject> list;

		//this custom adapter receives an ArrayList of EventCreateObject objects.
		//EventCreateObject is my class that represents the data for a single row and could be anything.
		public CustomArrayAdapter(Context context, int textViewResourceId, CustomLinkedList<EventCreateObject> EventCreateObjectList) 
		{
			//populate the local list with data.
			super(context, textViewResourceId, EventCreateObjectList);
			//			this.list = new CustomLinkedList<EventCreateObject>();
			//			this.list.addAll(EventCreateObjectList);
			this.list = EventCreateObjectList;
		}


		CustomLinkedList<EventCreateObject> getList() {
			return list;
		}

		public void addEvent() {
			list.add(new EventCreateObject());
			CustomArrayAdapter.this.notifyDataSetChanged();
		}

		public View getView(final int position, View convertView, ViewGroup parent)
		{
			//creating the ViewHolder we defined earlier.
			ViewHolder holder = new ViewHolder(); 

			//creating LayoutInflator for inflating the row layout.
			LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			//inflating the row layout we defined earlier.
			convertView = inflator.inflate(R.layout.song_create_view_layout, null);

			//setting the views into the ViewHolder.
			holder.title = (TextView) convertView.findViewById(R.id.tvItemTitle);
			holder.image1 = (ImageView) convertView.findViewById(R.id.iStatus);
			holder.image1.setTag(position);
			holder.eventName = (TextView) convertView.findViewById(R.id.eventName);
			holder.eventName.setText(list.get(position).getName());

			//			//set a click listener for the list items themselves. 
			//			((DragSortListView)parent).setOnItemClickListener(new OnItemClickListener() 
			//			{
			//				public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
			//				{
			//					Toast.makeText(getContext(), "row " + position + " was pressed", Toast.LENGTH_LONG).show();
			//				}
			//			});

			//onclickListener for the text
			holder.title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					list.remove(position);
					notifyDataSetChanged();
				}

			});

			//define an onClickListener for the ImageView.
			holder.image1.setOnClickListener(new OnClickListener() 
			{           
				@Override
				public void onClick(View v) 
				{
					launchEdit(position);
					Toast.makeText(getContext(), "Image from row " + position + " was pressed", Toast.LENGTH_LONG).show();
				}
			});
			holder.checked = (CheckBox) convertView.findViewById(R.id.cbCheckListItem);
			holder.checked.setTag(position);

			//define an onClickListener for the CheckBox.
			holder.checked.setOnClickListener(new OnClickListener() {       
				@Override
				public void onClick(View v)
				{
					//assign check-box state to the corresponding object in list.    
					CheckBox checkbox = (CheckBox) v;
					list.get(position).setComplex(checkbox.isChecked());
					Toast.makeText(getContext(), "CheckBox from row " + position + " was checked", Toast.LENGTH_LONG).show();    
				}
			});

			holder.eventName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					launchEdit(position);
				}
			});
			//setting data into the the ViewHolder.
			holder.title.setText("Delete Set");
			holder.checked.setChecked(list.get(position).isComplex());
			//return the row view.
			return convertView;
		}



		void launchEdit(int position) {
			//intent to launch new activity where you can edit all properties of this particular event
//			Intent intent = new Intent(getContext(), EditEventActivity.class);
//			intent.putExtra("EventName", list.get(position).getName());
//			intent.putExtra("EventData", list.get(position));
//			// TURD 1
//			CreateSongActivity.currentEventObject = list.get(position);
//			// TURD 1 
//			startActivity(intent);
			
			//alternate mechanism using a fragment.
			
			
		}

		@Override
		public void drop(final int from, final int to) {
			Log.d(Tag, "Moving " + from + " to " + to);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					list.moveElement(from, to);
				}
			});
			t.start();
			CustomArrayAdapter.this.notifyDataSetChanged();
		}
	}

	public void saveSong(View view) {
		new Thread(new Runnable() { 
			public void run() {
				
				/*
				*				//TURD legacy code, remove when I am sure everything else is working. 
				//				String fileName = "TheOneSongToRuleThemAll";
				//				//File file = new File(getBaseContext().getFilesDir(), fileName);
				//				FileOutputStream outputStream;
				//
				//				String string = "file version 1\n" +
				//						"#lines beginning with a # are comments which are ignored.\n" +
				//						"#blank lines are also ignored\n" +
				//						"#Files will have the following format (version 1)\n\n" +
				//						"#name\n#tempo\n#volume\n#pattern\n#repeats\n#beat\n\n";
				//
				//				try {
				//					outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
				//					outputStream.write(string.getBytes());
				//					//outputStream.write(string.getBytes());
				//					for (EventCreateObject e: dataAdapter.getList()) {
				//						string = e.getName() + "\n" + e.getTempo() + "\n" + 
				//								e.getVolume() + "\n" + EditEventActivity.convertFromPattern(e.getPattern()) + "\n" +
				//								e.getRepeats() + "\n" + e.getBeat() + "\n\n";
				//						outputStream.write(string.getBytes());
				//					}
				//					outputStream.close();
				//				} catch (Exception e) {
				//					e.printStackTrace();
				//				}

				//				//file version 2.0
				//				String fileName = "song.txt";
				//				CustomLinkedList<EventCreateObject> list = dataAdapter.getList();
				//				FileOutputStream outputStream;
				//				StringBuilder sb = new StringBuilder(200);
				//				String string;
				//				sb.append("file version 2.0\n");
				//				sb.append(list.size() + "\n" + songName);
				//				sb.append("\n#lines beginning with a # are comments which are ignored.\n");
				//				sb.append("#blank lines are also ignored\n");
				//				sb.append("#Files will have the following format (version 2)\n\n");
				//				sb.append("#name\n#tempo\n#volume\n#pattern\n#repeats\n#beat\n");
				//				sb.append("#complex (1) or simple (2)\n");
				//				sb.append("#beat designator (major vs subdivision beats)\n");
				//				sb.append("#top of time signature\n");
				//				sb.append("#bottom of time signature");
				//				sb.append("#\n\n");
				//				string = sb.toString();
				//
				//				try {
				//					outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
				//					outputStream.write(string.getBytes());
				//					for (EventCreateObject e: dataAdapter.getList()) {
				//						sb.setLength(0);
				//						sb.append(e.getName());
				//						sb.append("\n");
				//						sb.append(e.getTempo());
				//						sb.append("\n");
				//						sb.append(e.getVolume());
				//						sb.append("\n");
				//						sb.append(EditEventActivity.convertFromPattern(e.getPattern()));
				//						sb.append("\n");
				//						sb.append(e.getRepeats());
				//						sb.append("\n");
				//						sb.append(e.getBeat());
				//						sb.append("\n");
				//						if (e.isComplex()) {
				//							sb.append("1\n");
				//						}
				//						else {
				//							sb.append("0\n");
				//						}
				//						sb.append(e.getEmphasisString());
				//						sb.append("\n");
				//						sb.append(e.getTimeSigTop());
				//						sb.append("\n");
				//						sb.append(e.getTimeSigBottom());
				//						sb.append("\n\n");
				//						string = sb.toString();
				//						outputStream.write(string.getBytes());
				//					}
				//					outputStream.close();
				//				} catch (Exception e) {
				//					e.printStackTrace();
				//				}
				//			}	
				 * 
				 */

				//file version 2.1 (modified to include extra info on simple time sig subdivisions and any additional notes
				String fileName = "song.txt";
				CustomLinkedList<EventCreateObject> list = dataAdapter.getList();
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
					for (EventCreateObject e: dataAdapter.getList()) {
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

	


}
