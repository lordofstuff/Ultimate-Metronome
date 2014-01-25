package com.AppsOfAwesome.ultimatemetronome;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PatternPickerFragment extends SherlockFragment{
	
	private View view;
	private int[] pattern;
	private int max = 4;
	private int min = 0;
	private ArrayList<CustomNumberPicker> pickerArrayList;
	private EditSongActivity parentActivity;
	private EventCreateObject currentEvent;
	private LinearLayout layout;
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				
		view = inflater.inflate(R.layout.pattern_picker, container, false);
		parentActivity = (EditSongActivity) getSherlockActivity();
		currentEvent = parentActivity.getCurrentEvent();
		pattern = currentEvent.getPattern();
		pickerArrayList = new ArrayList<CustomNumberPicker>(pattern.length);
		layout = (LinearLayout) view.findViewById(R.id.picker_layout);
		
		int min = 0;
		int max = 4; //FIXME
		
		
		for(int i=0; i < pattern.length; i++) {
			//add a new CustomNumberPicker to the linear layout
			CustomNumberPicker current = new CustomNumberPicker(parentActivity, min, max, pattern[i]);
			layout.addView(current);
			pickerArrayList.add(current);
			
			
		}
		
		
		return view;
	}
	
	public int[] getPattern() {
		return pattern;
	}
	
	public void setPattern(int[] pattern) {
		this.pattern = pattern;
	}
	
	//forces the UI of the whole fragment to update to match new data. 
	public void dataChanged() {
		//TODO
	}

	@Override
	public void onDetach() {
		
		super.onDetach();
		parentActivity.patternFragmentDetach();
	}
	
	
	
	
}
