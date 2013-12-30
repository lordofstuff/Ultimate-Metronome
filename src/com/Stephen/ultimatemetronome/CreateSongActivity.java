package com.Stephen.ultimatemetronome;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		ImageView changeRowStatus;
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
			holder.changeRowStatus = (ImageView) convertView.findViewById(R.id.iStatus);
			holder.changeRowStatus.setTag(position);

			//define an onClickListener for the ImageView.
			holder.changeRowStatus.setOnClickListener(new OnClickListener() 
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

			//setting data into the the ViewHolder.
			holder.title.setText("example text");
			//holder.checked.setChecked(EventCreateObject.isComplex());

			//return the row view.
			return convertView;
		}
	}

}
