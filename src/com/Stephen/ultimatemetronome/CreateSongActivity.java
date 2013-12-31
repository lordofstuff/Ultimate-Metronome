package com.Stephen.ultimatemetronome;

//import java.util.ArrayList;
//import java.util.LinkedList;

import java.io.File;
import java.io.FileOutputStream;

import com.actionbarsherlock.app.SherlockActivity;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
//import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
//import android.app.Activity;
//import android.view.Menu;

public class CreateSongActivity extends SherlockActivity {

	String Tag = "CreateSongActivity";
	CustomArrayAdapter dataAdapter;

	/* TURD 1 */
	public static EventCreateObject currentEventObject = null;
	/* TURD 1 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Tag, "activity created");
		setContentView(R.layout.activity_create_song);
		DragSortListView list = (DragSortListView)findViewById(R.id.list);
		dataAdapter = new CustomArrayAdapter(this, R.id.tvItemTitle, EventCreateObject.defaultList());
		Log.d(Tag, "adapter created.");
		list.setAdapter(dataAdapter);
		list.setDropListener(dataAdapter);
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.opening_menu, menu);
	//		return true;
	//	}

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

			//set a click listener for the list items themselves. 
			((DragSortListView)parent).setOnItemClickListener(new OnItemClickListener() 
			{
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
				{
					Toast.makeText(getContext(), "row " + position + " was pressed", Toast.LENGTH_LONG).show();
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
			holder.title.setText("example text");
			holder.checked.setChecked(list.get(position).isComplex());


			//return the row view.
			return convertView;
		}


		void launchEdit(int position) {
			//intent to launch new activity where you can edit all properties of this particular event
			Intent intent = new Intent(getContext(), EditEventActivity.class);
			intent.putExtra("EventName", list.get(position).getName());
			intent.putExtra("EventData", list.get(position));
			// TURD 1
			CreateSongActivity.currentEventObject = list.get(position);
			// TURD 1 
			startActivity(intent);
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
				//TURD this should not be hardcoded in
				String fileName = "TheOneSongToRuleThemAll";
				File file = new File(getBaseContext().getFilesDir(), fileName);
				FileOutputStream outputStream;

				String string = "file version 1\n" +
						"#lines beginning with a # are comments which are ignored.\n" +
						"#blank lines are also ignored\n" +
						"#Files will have the following format (version 1)\n\n" +
						"#name\n#tempo\n#volume\n#pattern\n#repeats\n#beat\n\n";

				try {
					outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
					outputStream.write(string.getBytes());
					//outputStream.write(string.getBytes());
					for (EventCreateObject e: dataAdapter.getList()) {
						string = e.getName() + "\n" + e.getTempo() + "\n" + 
								e.getVolume() + "\n" + EditEventActivity.convertFromPattern(e.getPattern()) + "\n" +
								e.getRepeats() + "\n" + e.getBeat() + "\n\n";
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
