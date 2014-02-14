package com.AppsOfAwesome.ultimatemetronome;

//import java.util.ArrayList;
//import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class EventCreateObject implements Parcelable{
	//fields
	//default values
	static final String DEFAULT_NAME = "Set";
	static final double DEFAULT_TEMPO = 120;
	static final float DEFAULT_VOLUME = 1;
	static final int DEFAULT_REPEATS = 1;
	static final int[] DEFAULT_PATTERN = new int[] {4, 3, 3, 3};
	static final int DEFAULT_BEAT = 1;
	static final int DEFAULT_TIME_SIG_TOP = 4;
	static final int DEFAULT_TIME_SIG_BOTTOM = 4;
	private static final String Tag = "EventCreateObject";
	private static final int[] DEFAULT_EMPHASIS = new int[] {1, 0, 0, 0};


	//actual values
	private String name;
	private double tempo;
	private float volume;
	private int repeats;


	//determines if complex time signatures are exposed
	private boolean complex;

	//used regardless of complexity, but not exposed if not complex
	private int[] pattern;
	private int beat;
	private int[] emphasis;

	//fields used only for simple time signatures
	private int timeSigTop;
	private int timeSigBottom;
	private String timeSigInfo;


	//for parceling
	public static EventCreator CREATOR = new EventCreator();


	//constructors
	/**
	 * To be used to create an instance from a parcel.
	 * @param source The parcel from which this object will be constructed. 
	 */
	private EventCreateObject(Parcel source) {
		this();
		name = source.readString();
		tempo = source.readDouble();
		volume = source.readFloat();
		repeats = source.readInt();
		complex = source.readByte() != 0;     //myBoolean == true if byte != 0
		int patternLength = source.readInt();
		pattern = new int[patternLength];
		source.readIntArray(pattern);
		beat = source.readInt();
		timeSigTop = source.readInt();
		timeSigBottom = source.readInt();
	}

	/**
	 * Creates an editable event with default values.
	 */
	public EventCreateObject() {
		this(DEFAULT_NAME, DEFAULT_TEMPO, DEFAULT_VOLUME, DEFAULT_REPEATS, false, DEFAULT_PATTERN.clone(), DEFAULT_BEAT, DEFAULT_TIME_SIG_TOP, DEFAULT_TIME_SIG_BOTTOM, DEFAULT_EMPHASIS.clone());
	}

	/**
	 * Creates an editable event with completely configurable values, especially for loading from a file.
	 * @param name The name of this event.
	 * @param tempo The tempo of this event in beats/minute.
	 * @param volume The volume of this event as a number between 0 and 1.
	 * @param repeats The number of measures this event repeats for.
	 * @param complex Whether complex (true) or simple (false) time signatures are exposed to the user.
	 * @param pattern The pattern of beats in a measure.
	 * @param beat The number of subdivisions that make up one beat for tempo purposes.
	 * @param timeSigTop The top of a simple time signature.
	 * @param timeSigBottom The bottom of a simple time signature.
	 * @param emphasis determines which beats are treated as "major" beats for the purposes of counting in the UI
	 */
	public EventCreateObject(String name, double tempo, float volume,
			int repeats, boolean complex, int[] pattern, int beat,
			int timeSigTop, int timeSigBottom, int[] emphasis) {
		super();
		this.name = name;
		this.tempo = tempo;
		this.volume = volume;
		this.repeats = repeats;
		this.complex = complex;
		this.pattern = pattern;
		this.beat = beat;
		this.timeSigTop = timeSigTop;
		this.timeSigBottom = timeSigBottom;
		this.emphasis = emphasis;
	}

	//getters and setters
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the tempo
	 */
	public double getTempo() {
		return tempo;
	}

	/**
	 * @param tempo the tempo to set
	 */
	public void setTempo(double tempo) {
		this.tempo = tempo;
	}

	/**
	 * @return the volume
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * @param volume the volume to set
	 */
	public void setVolume(float volume) {
		this.volume = volume;
	}

	/**
	 * @return the repeats
	 */
	public int getRepeats() {
		return repeats;
	}

	/**
	 * @param repeats the repeats to set
	 */
	public void setRepeats(int repeats) {
		this.repeats = repeats;
	}

	/**
	 * @return the complex
	 */
	public boolean isComplex() {
		return complex;
	}

	/**
	 * @param complex the complex to set
	 */
	public void setComplex(boolean complex) {
		this.complex = complex;
	}

	/**
	 * @return the pattern
	 */
	public int[] getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(int[] pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the beat
	 */
	public int getBeat() {
		return beat;
	}

	/**
	 * @param beat the beat to set
	 */
	public void setBeat(int beat) {
		this.beat = beat;
	}

	/**
	 * @return the timeSigTop
	 */
	public int getTimeSigTop() {
		return timeSigTop;
	}

	/**
	 * @param timeSigTop the timeSigTop to set
	 */
	public void setTimeSigTop(int timeSigTop) {
		this.timeSigTop = timeSigTop;
	}

	/**
	 * @return the timeSigBottom
	 */
	public int getTimeSigBottom() {
		return timeSigBottom;
	}

	/**
	 * @param timeSigBottom the timeSigBottom to set
	 */
	public void setTimeSigBottom(int timeSigBottom) {
		this.timeSigBottom = timeSigBottom;
	}

	//other methods

	public static CustomLinkedList<EventCreateObject> defaultList() {
		CustomLinkedList<EventCreateObject> list = new CustomLinkedList<EventCreateObject>();
		list.add(new EventCreateObject());
		list.add(new EventCreateObject());
		Log.d("defaultList method", "stuff added");
		list.get(0).setName("First one");
		list.get(1).setName("second one");
		Log.d("defaultList method", "stuff gotten");
		return list;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		Log.v(Tag, "Writing to parcel: flag " + flags);
		out.writeString(name);
		out.writeDouble(tempo);
		out.writeFloat(volume);
		out.writeInt(repeats);
		out.writeByte((byte) (complex ? 1 : 0));     //if myBoolean == true, byte == 1
		out.writeInt(pattern.length);
		out.writeIntArray(pattern);
		out.writeInt(beat);
		out.writeInt(timeSigTop);
		out.writeInt(timeSigBottom);
		//TODO do something with flags?

	}

	public static final class EventCreator implements Parcelable.Creator<EventCreateObject> {

		@Override
		public EventCreateObject createFromParcel(Parcel source) {
			return new EventCreateObject(source);
		}

		@Override
		public EventCreateObject[] newArray(int size) {
			return new EventCreateObject[size];
		}

	}

	public String getEmphasisString() {
		// TODO make this do something
		return "1111";
	}

	public String getTimeSigInfo() {
		return "Filler time sig info"; //TODO
	}

	public String getNotes() {
		// TODO this should return user editable notes about a set
		return "filler notes";
	}

	public void addtimeSigInfo(String timeSigInfo) {
		this.timeSigInfo = timeSigInfo;
	}








}
