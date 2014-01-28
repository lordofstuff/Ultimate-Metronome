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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

class EditEventFragment extends SherlockFragment {

	protected static final String Tag = "Edit event fragment";
	
	private View view;
	private TextView eventNameText;
	private TextView tempoEdit;
	private TextView testText;
	private CustomNumberPicker beatPicker;

	private EventCreateObject myEvent;
	//private int position;
	private EditEventParent parentActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_edit_event,
				container, false);
		
		eventNameText = (TextView) view.findViewById(R.id.edit_event_name);
		tempoEdit = (TextView) view.findViewById(R.id.tempo_input);
		
		testText = (TextView) view.findViewById(R.id.test_text);
		beatPicker = (CustomNumberPicker) view.findViewById(R.id.beat_picker);
		//TODO add other views here. 

		//set up important variables
		parentActivity = (EditEventParent)getSherlockActivity();
		

		//set UI to match the event info
		changed();

		//add listeners (this should maybe be in another on*** method) TODO
		testText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				parentActivity.editPattern(parentActivity.getNormalPatternConstant());
				
			}
		});
		eventNameText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable charset) {
				myEvent.setName(charset.toString());
				dataChanged();
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
					dataChanged();
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
		myEvent = parentActivity.getCurrentEvent();
		eventNameText.setText(myEvent.getName());
		tempoEdit.setText(Double.toString(myEvent.getTempo()));
		
	}
	
	private void dataChanged() {
		//tells the hosting class that the data has changed and the list needs to be redrawn
		parentActivity.editDataChanged();
	}
	


	//	public void setName(String item) {
	//		//TextView view = (TextView) getView().findViewById(R.id.detailsText);
	//		eventNameText.setText(item);
	//	}
	
	public interface EditEventParent {
		
		/**
		 * Gets the event to be edited. 
		 * @return the event to be edited. 
		 */
		EventCreateObject getCurrentEvent();
		
		/**
		 * informs the parent activity that the data on the current event has changed so that it can update the interface of other fragments to match. 
		 */
		void editDataChanged();
		
		/**
		 * called when the fragment detaches so that the parent activity can update the UI accordingly. 
		 */
		void detachEditFragment();
		
		/**
		 * called when a pattern needs to be edited.
		 * @param flag a flag indicating which pattern is to be edited, to be passed along to the corresponding fragment. 
		 */
		void editPattern(int flag);
		
		int getNormalPatternConstant();
		int getBeatPatternConstant();

	}


}
