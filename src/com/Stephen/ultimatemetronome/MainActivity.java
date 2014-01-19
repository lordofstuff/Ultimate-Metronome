package com.Stephen.ultimatemetronome;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;

import com.Stephen.ultimatemetronome.metronomepackage.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.os.Bundle;
//import android.app.Activity;
import android.content.Intent;
import android.util.Log;
//import android.view.Menu;
import android.view.View;

public class MainActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Main", "activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void loadSongEdit(View view) {
		Intent intent = new Intent(this, EditSongActivity.class);
		intent.putExtra("LoadFlag", CreateSongActivity.EDIT_FLAG);
		intent.putExtra("fileName", promtForNameEdit());
		startActivity(intent);	
	}
	
	public void createSong(View view) {
		Intent intent = new Intent(this, EditSongActivity.class);
		intent.putExtra("LoadFlag", CreateSongActivity.NEW_FLAG);
		startActivity(intent);	
	}
	
	public void loadSong(View view) {
		String fileName = promptForNameLoad();
		Intent intent = new Intent(this, PlayMetronomeActivity.class);
		intent.putExtra("Filename", fileName);
		startActivity(intent);
	}

	static String promptForNameLoad() {
	    //TODO
	    return "song.txt";
    }
	
	static String promtForNameEdit() {
		//TODO
		return "song.txt";
	}
	
}
