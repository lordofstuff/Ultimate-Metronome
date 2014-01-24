package com.AppsOfAwesome.ultimatemetronome;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class CustomNumberPicker extends LinearLayout {

	//TODO change this to be sliders instead?
	Context context;
	
	//fields
	private int maximum = 5;
	private int minimum = 0; //both inclusive

	private int currentValue;

	//UI elements; may change if I change what it looks like
	EditText numberText;
	ImageButton upButton;
	ImageButton downButton;
	
	
	public CustomNumberPicker(Context context, int min, int max, int currentValue) {
		super(context);
		LayoutInflater layoutInflater = (LayoutInflater)context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.custom_number_picker, this);
		this.context = context;
		this.minimum = min;
		this.maximum = max;
		this.currentValue = currentValue;
		init2();
		
	}
	

	public CustomNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater layoutInflater = (LayoutInflater)context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.custom_number_picker, this);
		
		this.context = context;
		init(attrs);
		
		
		
		init2();
	}
	
	private void init2() {
		//MAY CHANGE DRASTICALLY
		//sets views for the components, etc.
		numberText = (EditText) this.findViewById(R.id.number);
		upButton = (ImageButton) this.findViewById(R.id.up_button);
		downButton = (ImageButton) this.findViewById(R.id.down_button);
		numberText.addTextChangedListener(new TextListener());
		upButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				upClick(null);
			}	
		});
		downButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downClick(null);
			}	
		});
	}

	private void init(AttributeSet attrs) {
		if (this.isInEditMode()) {
			return;
		}
		TypedArray a = context.obtainStyledAttributes(attrs,
			    R.styleable.CustomNumberPicker);
			 
			final int N = a.getIndexCount();
			for (int i = 0; i < N; ++i)
			{
			    int attr = a.getIndex(i);
			    switch (attr)
			    {
			        case R.styleable.CustomNumberPicker_min:
			            minimum = a.getInt(i, 0);
			            break;
			        case R.styleable.CustomNumberPicker_max:
			            maximum =  a.getInt(i, 0);
			            break;
			    }
			}
			a.recycle();

	}

	public int getValue() {
		return currentValue;
	}
	
	private void setCurrentValue(int value) {
		currentValue = value;
	}

	public boolean setValue(int newValue) {
		if (newValue >= minimum && newValue <= maximum) {
			setCurrentValue(newValue);
			updateValue();
			return true;
		}
		else {
			return false;
		}
	}

	private void updateValue() {
		numberText.setText(Integer.toString(getValue()));
	}
	
	
	//may change a lot
	private class TextListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable arg0) {
			int value = Integer.parseInt(arg0.toString());
			if (value >= minimum && value <= maximum) {
				setCurrentValue(value);
				//updateValue();
			}	
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub	
		}
		
	}
	
	public void upClick(View view) {
		if (currentValue < maximum) {
			currentValue++;
			updateValue();
		}
	}
	
	public void downClick(View view) {
		if (currentValue > minimum) {
			currentValue--;
			updateValue();
		}
	}

}
