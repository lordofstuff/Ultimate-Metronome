package com.AppsOfAwesome.ultimatemetronome;

import android.content.Context;
import android.content.res.TypedArray;
//import android.text.Editable;
//import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CustomNumberPicker extends LinearLayout {

	Context context;

	//fields
	private int maximum;
	private int minimum;
	private String[] values = null;
	int tag = -1;

	private int currentValue;

	//UI elements: pre HC
	EditText numberText;
	ImageButton upButton;
	ImageButton downButton;

	//UI Elements: post HC
	SeekBar valueSlider;
	TextView valueText;

	private OnValueChangeListener listener;

	public CustomNumberPicker(Context context, int currentValue, String... values) {
		this(context, 0, values.length -1, currentValue);
		setValues(values);
	}

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
		if (!getResources().getBoolean(R.bool.holo_compat)) {
			numberText = (EditText) this.findViewById(R.id.number);
			upButton = (ImageButton) this.findViewById(R.id.up_button);
			downButton = (ImageButton) this.findViewById(R.id.down_button);
			//numberText.addTextChangedListener(new TextListener());
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
			return; //make sure it does not keep going for pre HC devices
		}
		// things for the post HC layout:
		valueSlider = (SeekBar) findViewById(R.id.value_seekbar);
		valueText = (TextView) findViewById(R.id.value_textview);
		valueSlider.setMax(maximum - minimum);
		valueSlider.setOnSeekBarChangeListener(new SeekListener());
		valueSlider.setProgress(currentValue - minimum);
		updateValue();

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

	public void setCustomTag(int tag) {
		this.tag = tag;
	}

	public int getCustomTag() {
		return tag;
	}

	public int getValue() {
		return currentValue;
	}

	private void setValues(String[] values) {
		this.values = values;
		updateValue();	
	}


	public boolean setValue(int newValue) {
		if (newValue >= minimum && newValue <= maximum) {
			currentValue = newValue;
			updateValue();
			return true;
		}
		else {
			return false;
		}
	}

	public void setMax(int newMax) {
		//check that newMax is greater than minimum
		if (newMax <= minimum) {
			throw new IllegalArgumentException("newMax must be greater than minimum");
		}
		//if it is increasing the max, do not change the value, but do make sure the UI updates
		if (newMax > maximum) {
			maximum = newMax;
			updateValue();
		}
		else {
			//if it is decreasing the max and the old value would be invalid, throw exception. it should not change the value without the user knowing. 
			if (getValue() > newMax) {
				throw new IllegalArgumentException("the new maximum is less than the current value.");
			}
			else {
				//if it is decreasing and the value is within the new valid range, leave it alone. 
				maximum = newMax;
				updateValue();
			}
		}
	}

	private void updateValue() {
		if (!getResources().getBoolean(R.bool.holo_compat)) {
			if (values == null) {
				numberText.setText(Integer.toString(getValue())); 
			}
			else {
				numberText.setText(values[currentValue]);
			}
		}
		else { //post HC devices
			if (values != null) {
				valueText.setText(values[currentValue]);
			}
			else {
				valueText.setText(Integer.toString(currentValue)); 
			}
		}
		if (listener != null) {
			listener.valueChanged(tag, currentValue);
		}
	}


	//may change a lot
	//	private class TextListener implements TextWatcher {
	//		@Override
	//		public void afterTextChanged(Editable arg0) {
	//			int value = Integer.parseInt(arg0.toString());
	//			if (value >= minimum && value <= maximum) {
	//				setCurrentValue(value);
	//				//updateValue();
	//			}
	//		}
	//		@Override
	//		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
	//				int arg3) {
	//			
	//		}
	//
	//		@Override
	//		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
	//				int arg3) {
	//			
	//		}
	//		
	//	}

	private class SeekListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
			//when the slider moves, the stored value and the displayed string need to both be updated.

			//assume the value is valid (it should be)
			currentValue = value + minimum;
			//setCurrentValue(value); //TODO make this work with non 0 minimum
			if (fromUser) {
				updateValue();
			}
		}
		@Override public void onStartTrackingTouch(SeekBar arg0) {}
		@Override public void onStopTrackingTouch(SeekBar arg0) {}
	}
	
	//only used for pre HC devices
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

	public void setOnValueChangeListener(OnValueChangeListener listener) {
		this.listener = listener;
	}

	public interface OnValueChangeListener {
		void valueChanged(int tag, int value);
	}

}
