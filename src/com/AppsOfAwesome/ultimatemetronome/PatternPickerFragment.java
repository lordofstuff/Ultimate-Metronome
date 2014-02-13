package com.AppsOfAwesome.ultimatemetronome;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PatternPickerFragment extends SherlockFragment implements CustomNumberPicker.OnValueChangeListener{
	
	private View view;
	private int[] pattern;
	private ArrayList<CustomNumberPicker> pickerArrayList;
	private EditSongActivity parentActivity;
	private EventCreateObject currentEvent;
	private LinearLayout layout;
	private ScrollView scroller;
	private String[] stringArray = new String[] {"rest", "quaternary", "teriary", "secondary", "primary"};
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				
		view = inflater.inflate(R.layout.pattern_picker, container, false);
		parentActivity = (EditSongActivity) getSherlockActivity();
		
		
		layout = (LinearLayout) view.findViewById(R.id.picker_layout);
		scroller = (ScrollView) view.findViewById(R.id.picker_scrollview);
		dataChanged();
		
		
		
		//add listeners to the buttons
		view.findViewById(R.id.add_picker_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomNumberPicker current = new CustomNumberPicker(parentActivity, getDefaultValue(), getStrings());
				current.setCustomTag(pickerArrayList.size());
				current.setOnValueChangeListener(PatternPickerFragment.this);
				layout.addView(current);
				pickerArrayList.add(current);
				scroller.fullScroll(View.FOCUS_DOWN); //TODO not working completely?
				updateWholePattern();
			}
		});
		
		view.findViewById(R.id.remove_picker_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomNumberPicker current = pickerArrayList.remove(pickerArrayList.size() - 1);
				layout.removeView(current);
				updateWholePattern();
			}
		});
		
		return view;
	}
	
	protected void updateWholePattern() {
		//create a new array of the appropriate size
		int[] newArray = new int[pickerArrayList.size()];
		//copy data into it
		for (int i = 0; i< newArray.length; i++) {
			newArray[i] = pickerArrayList.get(i).getValue();
		}
		//set this to be the new pattern in the current event;
		currentEvent.setPattern(newArray);
		this.pattern = newArray;
	}

	protected int getDefaultValue() {
		return 0;
	}

	protected String[] getStrings() {
		return stringArray;
	}

	public int[] getPattern() {
		return pattern;
	}
	
	public void setPattern(int[] pattern) {
		this.pattern = pattern;
	}
	
	//forces the UI of the whole fragment to update to match new data. 
	public void dataChanged() {
		//get rid of old stuff if present
		layout.removeAllViews();
		
		//make sure data is up to date
		currentEvent = parentActivity.getCurrentEvent();
		((TextView) view.findViewById(R.id.pattern_text)).setText(currentEvent.getName());
		pattern = currentEvent.getPattern(); //TODO change this if another pattern needs to be edited
		pickerArrayList = new ArrayList<CustomNumberPicker>(pattern.length);
		
		for(int i=0; i < pattern.length; i++) {
			//add a new CustomNumberPicker to the linear layout
			CustomNumberPicker current = new CustomNumberPicker(parentActivity, pattern[i], getStrings());
			current.setCustomTag(i);
			current.setOnValueChangeListener(this);
			layout.addView(current);
			pickerArrayList.add(current);
		}
	}

	@Override
	public void onDetach() {
		
		super.onDetach();
		parentActivity.patternFragmentDetach();
	}
	
	public interface PatternFragmentParent {
		
		EventCreateObject getCurrentEvent();
		
		void patternFragmentDetach();
		
	}

	@Override
	public void valueChanged(int tag, int value) {
		pattern[tag] = value;
	}
	
	
	
	
	
	
}
