package com.AppsOfAwesome.ultimatemetronome;

public class CustomListItem<T> {
	int graphicId; //0 for no graphic
	int textId;

	public static final int quarter = 0;
	public static final int eighth = 1;
	public static final int sixteenth = 2;
	public static final int thirty_second = 3;
	public static final int half = 4;

	public CustomListItem(int type) {
		switch (type) {
		case quarter:
			graphicId = R.drawable.ic_action_quarter_up;
			textId = R.string.um_quarter;
			break;
		case eighth:
			graphicId = R.drawable.ic_action_eighth_up;
			textId = R.string.um_eighth;
			break;
		case sixteenth:
			graphicId = R.drawable.ic_action_sixteenth_up;
			textId = R.string.um_eighth;
			break;
		case thirty_second:
			//graphicId = R.drawable.ic_action_thirtysecond_up;
			textId = R.string.um_thirtysecond;
			break;
		case half:
			graphicId = R.drawable.ic_action_half_up;
			textId = R.string.um_half;
			break;
		}
	}

}
