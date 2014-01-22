package com.AppsOfAwesome.ultimatemetronome;

//import java.io.FileOutputStream;

import com.AppsOfAwesome.ultimatemetronome.R;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SongListFragment extends SherlockFragment {

	private static final String Tag = "List Fragment";
	private EditSongActivity parentActivity;
	private DragSortListView list;// = (DragSortListView)findViewById(R.id.list);
	private CustomArrayAdapter dataAdapter;
	private CustomLinkedList<EventCreateObject> songList;
	private LayoutInflater inflater;
	private String songName;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



	}



	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#getSherlockActivity()
	 */
	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		// TODO Auto-generated method stub
		return super.getSherlockActivity();
	}



	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		parentActivity = (EditSongActivity) activity;
		songList = parentActivity.getSongList();
	}



	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onDetach()
	 */
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}



	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.song_list_fragment,
				container, false);
		this.inflater = inflater;
		list = (DragSortListView)view.findViewById(R.id.list);
		dataAdapter = new CustomArrayAdapter(parentActivity, R.id.tvItemTitle, songList);
		//Log.d(Tag, "adapter created.");
		list.setAdapter(dataAdapter);
		list.setDropListener(dataAdapter);
		return view;
	}
	
	public void addEvent(View view) {
		dataAdapter.addEvent();
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
			//TODO

			//inflating the row layout we defined earlier.
			convertView = inflater.inflate(R.layout.song_create_view_layout, null);

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
			//call method from hosting activity
			parentActivity.editEvent(position);

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

	//should maybe include this as part of eventcreateobject?
	//holds views from the listview
	static class ViewHolder {
		TextView title;
		CheckBox checked;
		ImageView image1;
		TextView eventName;
	}
	
	

	public void setSort(boolean sort) {
	    // TODO Auto-generated method stub
	    list.setDragEnabled(sort);
    }



	public void notifyDataChanged() {
		dataAdapter.notifyDataSetChanged();
	}

}
