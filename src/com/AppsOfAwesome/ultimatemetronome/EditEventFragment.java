package com.AppsOfAwesome.ultimatemetronome;

import com.AppsOfAwesome.ultimatemetronome.R;
import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
//import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class EditEventFragment extends SherlockFragment {

	protected static final String Tag = "Edit event fragment";
	private View view;
	private TextView eventNameText;
	private TextView tempoEdit;

	private EventCreateObject myEvent;
	private int position;
	private EditSongActivity parentActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_event,
				container, false);
		
		this.view = view;
		eventNameText = (TextView) view.findViewById(R.id.edit_event_name);
		tempoEdit = (TextView) view.findViewById(R.id.tempo_input);
		//TODO add other views here. 

		//set up important variables
		parentActivity = (EditSongActivity)getSherlockActivity();
		

		//set UI to match the event info
		changed();

		//add listeners (this should maybe be in another on*** method) TODO
		eventNameText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable charset) {
				myEvent.setName(charset.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO 
			}
		});

		tempoEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				try {
					myEvent.setTempo(Double.parseDouble(s.toString()));
				}
				catch (NumberFormatException e){
					Log.v(Tag, "number format exception; value not committed to tempo.");
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub 
			}
		});

		return view;
	}



	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}



	public static String convertFromPattern(int[] pattern) {
		// TODO Auto-generated method stub
		return null;
	}



	public void changed() {
		//update position and set all UI to match new data
		position = parentActivity.getPosition();
		myEvent = parentActivity.getSongList().get(position);
		eventNameText.setText(myEvent.getName());
		tempoEdit.setText(Double.toString(myEvent.getTempo()));
		view.invalidate();
	}


	//	public void setName(String item) {
	//		//TextView view = (TextView) getView().findViewById(R.id.detailsText);
	//		eventNameText.setText(item);
	//	}


}
