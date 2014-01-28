package com.AppsOfAwesome.ultimatemetronome;

import com.AppsOfAwesome.ultimatemetronome.R;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment holding a listview of events that can be reordered, created, and deleted. The parent activity must implement the ListParent interface.
 * @author Stephen Rodriguez
 *
 */
public class SongListFragment extends SherlockFragment {

	private static final String Tag = "List Fragment";
	private ListParent parentActivity;
	private DragSortListView list;// = (DragSortListView)findViewById(R.id.list);
	private CustomArrayAdapter dataAdapter;
	private LayoutInflater inflater;
	private String songName;

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		SherlockFragmentActivity parent = getSherlockActivity();
		if (!(parent instanceof ListParent)) {
			throw new IllegalStateException("SongListFragment must be a child of a class implementing the ListParent Interface");
		}
		parentActivity = (ListParent) getSherlockActivity();
		View view = inflater.inflate(R.layout.song_list_fragment,
				container, false);
		this.inflater = inflater;
		list = (DragSortListView)view.findViewById(R.id.list);
		dataAdapter = new CustomArrayAdapter((Context) parentActivity, R.id.event_name, parentActivity.getList());
		//Log.d(Tag, "adapter created.");
		list.setAdapter(dataAdapter);
		list.setDropListener(dataAdapter);
		return view;
	}
	
	public void addEvent(View view) {
		dataAdapter.addEvent();
	}

	private class CustomArrayAdapter extends ArrayAdapter<EventCreateObject> implements DropListener {   
		private CustomLinkedList<EventCreateObject> songList;
		
		//this custom adapter receives an ArrayList of EventCreateObject objects.
		//EventCreateObject is my class that represents the data for a single row and could be anything.
		public CustomArrayAdapter(Context context, int textViewResourceId, CustomLinkedList<EventCreateObject> EventCreateObjectList) {
			//populate the local list with data.
			super(context, textViewResourceId, EventCreateObjectList);
			 songList = EventCreateObjectList;
		}


		public void addEvent() {
			songList.add(new EventCreateObject());
			notifyDataSetChanged();
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			//creating the ViewHolder we defined earlier.
			ViewHolder holder = new ViewHolder(); 

			//inflating the row layout we defined earlier.
			convertView = inflater.inflate(R.layout.song_create_view_layout, null);
			
			//getting the event create object associated with this view
			EventCreateObject event = songList.get(position);

			//setting the views into the ViewHolder.
			holder.eventName = (TextView) convertView.findViewById(R.id.event_name);
			holder.deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
			holder.tempoText = (TextView) convertView.findViewById(R.id.tempo_textview);
			holder.volumeText = (TextView) convertView.findViewById(R.id.volume_textview);

			//			//set a click listener for the list items themselves. 
			//			((DragSortListView)parent).setOnItemClickListener(new OnItemClickListener() 
			//			{
			//				public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
			//				{
			//					Toast.makeText(getContext(), "row " + position + " was pressed", Toast.LENGTH_LONG).show();
			//				}
			//			});

			//onclickListener for the text
			holder.deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					songList.remove(position);
					notifyDataSetChanged();
				}
			});
			
			holder.editButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					launchEdit(position);
				}
			});

			//define an onClickListener for the ImageView.
//			holder.editButton.setOnClickListener(new OnClickListener() 
//			{           
//				@Override
//				public void onClick(View v) 
//				{
//					launchEdit(position);
//					Toast.makeText(getContext(), "Image from row " + position + " was pressed", Toast.LENGTH_LONG).show();
//				}
//			});
			//holder.checked = (CheckBox) convertView.findViewById(R.id.cbCheckListItem);
			//holder.checked.setTag(position);

			//define an onClickListener for the CheckBox.
//			holder.checked.setOnClickListener(new OnClickListener() {       
//				@Override
//				public void onClick(View v)
//				{
//					//assign check-box state to the corresponding object in list.    
//					CheckBox checkbox = (CheckBox) v;
//					songList.get(position).setComplex(checkbox.isChecked());
//					Toast.makeText(getContext(), "CheckBox from row " + position + " was checked", Toast.LENGTH_LONG).show();    
//				}
//			});

			
			//setting data into the the ViewHolder.
			holder.eventName.setText(event.getName());
			holder.tempoText.setText(Double.toString(event.getTempo()));
			holder.volumeText.setText(Float.toString(event.getVolume()));
			
			
			
			//return the row view.
			return convertView;
		}



		void launchEdit(int position) {
			parentActivity.editEvent(songList.get(position));
		}

		@Override
		public void drop(final int from, final int to) {
			Log.d(Tag, "Moving " + from + " to " + to);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					songList.moveElement(from, to);
				}
			});
			t.start();
			CustomArrayAdapter.this.notifyDataSetChanged();
		}
	}

	//holds views from the listview
	static class ViewHolder {
		ImageButton deleteButton;
		ImageButton editButton;
		TextView tempoText;
		TextView volumeText;
		//CheckBox checked;
		//ImageView image1;
		
		TextView eventName;
	}
	
	

	public void setSort(boolean sort) {
	    list.setDragEnabled(sort);
    }



	public void notifyDataChanged() {
		dataAdapter.notifyDataSetChanged();
	}

}
