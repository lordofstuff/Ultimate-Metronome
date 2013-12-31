package com.Stephen.ultimatemetronome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.Stephen.ultimatemetronome.metronomepackage.*;
import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
//import android.app.Activity;
import android.content.Intent;
import android.util.Log;
//import android.view.Menu;
import android.view.View;
//import android.widget.EditText;

public class MainActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Main", "activity created.");
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
	
	public void testMet(View view) {
		Log.d("main Activity", "button click registered");
		Song song = Song.testSong();
		MetronomeController mc = new MetronomeController(getApplicationContext(), song);
		mc.startMet();
	}

	public void createSong(View view) {
		Intent intent = new Intent(this, CreateSongActivity.class);	
		startActivity(intent);	
	}
	
	public void loadSong(View view) {
		Song song = null;
		//TURD this should not be hardcoded in
		String fileName = "TheOneSongToRuleThemAll";
		File file = new File(getBaseContext().getFilesDir(), fileName);
		try {
	        song = Song.createFromFile(file);
        } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (FileFormatException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		MetronomeController mc = new MetronomeController(getApplicationContext(), song);
		mc.startMet();
		
	}
	
}
