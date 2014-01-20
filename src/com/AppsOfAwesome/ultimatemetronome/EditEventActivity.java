package com.AppsOfAwesome.ultimatemetronome;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.AppsOfAwesome.ultimatemetronome.R;
import com.actionbarsherlock.app.SherlockActivity;
//import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;
import android.os.Parcelable;
//import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
//import android.view.Menu;
import android.view.View;
import android.widget.EditText;
//import android.widget.TextView;

public class EditEventActivity extends SherlockActivity {

	EventCreateObject myEvent;
	//objects for editing stuff
	private EditText nameEditText;
	private EditText tempoEditText;
	private EditText patternEditText;
	private EditText beatEditText;
	private EditText repeatEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_event);
		//Intent intent = getIntent();
		//		String message = intent.getStringExtra("EventName");

		//better code to reinstate maybe at some point.
		//	    Parcelable eventparcel = (intent.getParcelableExtra("EventData"));
		//	    EventCreateObject myEvent = (EventCreateObject)eventparcel;

		/* TURD 1 */
		myEvent = CreateSongActivity.currentEventObject;
		CreateSongActivity.currentEventObject = null;
		/* TURD 1 */

		nameEditText = (EditText) findViewById(R.id.edit_event_name);
		nameEditText.setText(myEvent.getName());
		nameEditText.addTextChangedListener(new TextWatcher() {

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

		//TURD
		tempoEditText = (EditText) findViewById(R.id.tempo_edit_text);
		tempoEditText.setText(Double.toString(myEvent.getTempo()));
		tempoEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable charset) {
				try {
					myEvent.setTempo(Double.parseDouble(charset.toString()));
				}
				catch (Exception e) {
					//TODO
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
				// TODO 
			}
		});

		//another TURD
		patternEditText = (EditText) findViewById(R.id.pattern_edit_text);
		patternEditText.setText(convertFromPattern(myEvent.getPattern()));
		patternEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable charset) {
				try {
					myEvent.setPattern(convertToPattern(charset));
				}
				catch (Exception e) {
					//TODO
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
				// TODO 
			}
		});

		//another TURD
		beatEditText = (EditText) findViewById(R.id.beat_edit_text);
		beatEditText.setText(Integer.toString(myEvent.getBeat()));
		beatEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable charset) {
				try {
					myEvent.setBeat(Integer.parseInt(charset.toString()));
				}
				catch (Exception e) {
					//TODO
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
				// TODO 
			}
		});


		//another TURD
		repeatEditText = (EditText) findViewById(R.id.repeat_edit_text);
		repeatEditText.setText(Integer.toString(myEvent.getRepeats()));
		repeatEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable charset) {
				try {
					myEvent.setRepeats(Integer.parseInt(charset.toString()));
				}
				catch (Exception e) {
					//TODO
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
				// TODO 
			}
		});
	}


	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.edit_event, menu);
	//		return true;
	//	}

	//TURD
	protected int[] convertToPattern(Editable charset) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		String s = charset.toString();
		for (int i = 0; i < charset.length(); i++) {
			if (Character.isDigit(s.charAt(i))) {
				al.add(Character.getNumericValue(s.charAt(i)));
			}
		}
		return convertIntegers(al);
	}

	//TURD
	static String convertFromPattern(int[] pattern) {
		StringBuilder sb = new StringBuilder();
		for (int i: pattern) {
			sb.append(i);
		}
		return sb.toString();
	}

	//TURD
	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	public void saveEvent(View view) {
		//TODO save things and return to list?
	}

}
