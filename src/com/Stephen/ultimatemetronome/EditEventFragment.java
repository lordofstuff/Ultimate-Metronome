package com.Stephen.ultimatemetronome;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class EditEventFragment extends Fragment {
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_edit_event,
	        container, false);
	    return view;
	  }

	  public void setText(String item) {
	    //TextView view = (TextView) getView().findViewById(R.id.detailsText);
	    //view.setText(item);
	  }
}
