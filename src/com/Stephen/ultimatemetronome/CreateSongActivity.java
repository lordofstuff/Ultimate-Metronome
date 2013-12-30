package com.Stephen.ultimatemetronome;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
//import android.app.Activity;
//import android.view.Menu;

public class CreateSongActivity extends SherlockActivity {
	
	String Tag = "CreateSongActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Tag, "activity created");
		setContentView(R.layout.activity_create_song);
		ListView list = (ListView)findViewById(R.id.list);
	    CustomArrayAdapter dataAdapter = new CustomArrayAdapter(this, R.id.tvItemTitle, EventCreateObject.defaultList());
	    list.setAdapter(dataAdapter);
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.opening_menu, menu);
	//		return true;
	//	}
	
	
	//should maybe include this as part of eventcreateobject?
	//holds views from the listview
	static class ViewHolder 
	{
		TextView title;
		CheckBox checked;
		ImageView image1;
		TextView eventName;
	}

	private class CustomArrayAdapter extends ArrayAdapter<EventCreateObject>
	{   
		private ArrayList<EventCreateObject> list;

		//this custom adapter receives an ArrayList of EventCreateObject objects.
		//EventCreateObject is my class that represents the data for a single row and could be anything.
		public CustomArrayAdapter(Context context, int textViewResourceId, ArrayList<EventCreateObject> EventCreateObjectList) 
		{
			//populate the local list with data.
			super(context, textViewResourceId, EventCreateObjectList);
			this.list = new ArrayList<EventCreateObject>();
			this.list.addAll(EventCreateObjectList);
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

			//define an onClickListener for the ImageView.
			holder.image1.setOnClickListener(new OnClickListener() 
			{           
				@Override
				public void onClick(View v) 
				{
					Toast.makeText(getContext(), "Image from row " + position + " was pressed", Toast.LENGTH_LONG).show();
				}
			});
			holder.checked = (CheckBox) convertView.findViewById(R.id.cbCheckListItem);
			holder.checked.setTag(position);

			//define an onClickListener for the CheckBox.
			holder.checked.setOnClickListener(new OnClickListener() 
			{       
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
	                //intent to launch new activity where you can edit all properties of this particular event
					Intent intent = new Intent(getContext(), EditEventActivity.class);
					//TODO PASS THE ENTIRE OBJECT, not just name
					//will need to implement parcelable
					intent.putExtra("EventName", list.get(position).getName());
					startActivity(intent);	
	                
                }
				
			});

			//setting data into the the ViewHolder.
			holder.title.setText("example text");
			holder.checked.setChecked(list.get(position).isComplex());

			//return the row view.
			return convertView;
		}
	}

}
