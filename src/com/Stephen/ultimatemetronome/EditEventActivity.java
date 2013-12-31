package com.Stephen.ultimatemetronome;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditEventActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_event);
		Intent intent = getIntent();
		//int position = intent.getExtra("position");
	    //		String message = intent.getStringExtra("EventName");
	    Parcelable eventparcel = (intent.getParcelableExtra("EventData"));
	    EventCreateObject myEvent = (EventCreateObject)eventparcel;
	    //EventCreateObject.CREATOR.createFromParcel(event);
		((EditText) findViewById(R.id.edit_event_name)).setText(myEvent.getName());
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.edit_event, menu);
//		return true;
//	}
	
	public void saveEvent(View view) {
		//TODO save things and return to list?
	}

}
