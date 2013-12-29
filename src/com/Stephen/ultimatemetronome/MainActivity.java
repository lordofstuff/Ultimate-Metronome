package com.Stephen.ultimatemetronome;

import com.Stephen.ultimatemetronome.metronomepackage.*;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Main", "activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void testMet(View view) {
		Log.d("main Activity", "button click registered");
		Song song = Song.testSong();
		MetronomeController mc = new MetronomeController(getApplicationContext(), song);
		mc.startMet();
	}

}
