package com.Stephen.ultimatemetronome.metronomepackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;


import com.Stephen.ultimatemetronome.CustomLinkedList;
//import com.Stephen.ultimatemetronome.CustomLinkedList.DLIterator;
import com.Stephen.ultimatemetronome.EventCreateObject;

import android.util.Log;

/**
 * A wrapper class for a linked list of MetronomeEvents, optimized for playback, not editing. 
 * @author Stephen Rodriguez
 *
 */
public class Song implements Iterable<MetronomeEvent>{
	private static final String Tag = "Song";
	//fields
	//private CustomLinkedList<MetronomeEvent> events;
	private MetronomeEvent[] eventArray;
	private String title;
	private static int PLAYBACK = 1;
	private static int EDIT = 2;

	//constructors
	Song(int length) {
		//events = new CustomLinkedList<MetronomeEvent>();
		eventArray = new MetronomeEvent[length];
	}

	//	private Song(CustomLinkedList<MetronomeEvent> list) {
	//		//events = list;
	//	}

	private Song(MetronomeEvent[] events) {
		eventArray = events;
	}

	//methods

	
	public static Song createFromFileForPlayback(File file) throws IOException, FileNotFoundException, FileFormatException {
		//return new Song((loadFile(file, PLAYBACK)));
		
		return (Song)(loadFile(file, PLAYBACK)); 
	}



	/**
	 * This method parses the given file and returns a list of events based on it. The list may contain either eventCreateObject or MetronomeEvent, depending on the flag given. 
	 * They are both done in this one method despite type difference to avoid duplicated code for loading from a file. 
	 * @param file The file to be loaded.
	 * @param flag A flag indicating that this is for playback or for editing. 
	 * @return a list of events in one form or another. 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws FileFormatException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object loadFile(File file, int flag) throws IOException, FileNotFoundException, FileFormatException {
		CustomLinkedList list;
		MetronomeEvent[] array = new MetronomeEvent[0];
		if (flag == PLAYBACK) {
			list = new CustomLinkedList<MetronomeEvent>();
		}
		else if (flag == EDIT) {
			list = new CustomLinkedList<EventCreateObject>();
		}
		else {
			throw new IllegalArgumentException();
		}
		//local variables to create the elements:
		String name;
		double tempo;
		int[] pattern;
		float volume;
		int repeats ;
		int beat;
		boolean complex;
		int[] emphasis;
		int timeSigTop;
		int timeSigBottom;
		String timeSigInfo;
		String songName = "";
		
		/*
		 file version 1
		 #lines beginning with a # are comments which are ignored.
		 #blank lines are also ignored 
		 #Files will have the following format (version 1)

		 #name
		 #tempo
		 #volume
		 #pattern
		 #repeats
		 #beat

		 */
		
		/*
 		 file version 2.0
	 	 number of events 
		 songName
		 
		 #lines beginning with a # are comments which are ignored.
		 #blank lines are also ignored
		 #Files will have the following format (version 2)
		 
		 #name
		 #tempo
		 #volume
		 #pattern
		 #repeats
		 #beat
		 #complex (1) or simple (2)
		 #beat designator (major vs subdivision beats)
		 #top of time signature
		 #bottom of time signature
		 
		 */

		//read from the file, adding things as you go. 
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String s = br.readLine();
		if (s.equals("file version 1")){
			s = br.readLine();
			while (s != null) {
				Log.d(Tag, "Read line: " + s);
				if (!s.equals("") && s.charAt(0) != '#') { //if not a comment or blank line

					Log.d(Tag, "starting to read a real one");
					name = s;
					tempo = Double.parseDouble(br.readLine());
					// TODO check for exceptions here
					volume = Float.parseFloat(br.readLine());
					pattern = toPatternArray(br.readLine()); //convert this to an array of ints
					repeats = Integer.parseInt(br.readLine());
					beat = Integer.parseInt(br.readLine());
					if (flag == PLAYBACK) {
						list.add(new MetronomeEvent(tempo, pattern, volume, repeats, beat, name));
					}
					if (flag == EDIT) {
						list.add(new EventCreateObject(s, tempo, volume, repeats, false, pattern, beat, 0, 0, new int[] {0}));
						//TODO add new file version which stores these extras
					}
				}
				else {
					//Log.d(Tag, "Ignoring comment or blank line");
				}
				s = br.readLine();
			}
		}
		else if (s.equals("file version 2.0")) {

			s = br.readLine(); //will be the number of events in the song
			array = new MetronomeEvent[Integer.parseInt(s)];
			songName = br.readLine();
			s = br.readLine();
			
			int i = 0;
			while (s != null) {
				Log.d(Tag, "Read line: " + s);
				if (!s.equals("") && s.charAt(0) != '#') { //if not a comment or blank line
					//Log.d(Tag, "starting to read a real one");
					name = s;
					tempo = Double.parseDouble(br.readLine());
					// TODO check for exceptions here
					volume = Float.parseFloat(br.readLine());
					pattern = toPatternArray(br.readLine()); //convert this to an array of ints
					repeats = Integer.parseInt(br.readLine());
					beat = Integer.parseInt(br.readLine());
					s = br.readLine();
					if (s.equals("2")) {
						complex = false;
					}
					else {
						complex = true;
					}
					emphasis = toPatternArray(br.readLine());
					timeSigTop = Integer.parseInt(br.readLine());
					timeSigBottom = Integer.parseInt(br.readLine());
					
					if (flag == PLAYBACK) {
						array[i++] = (new MetronomeEvent(tempo, pattern, volume, repeats, beat, name, emphasis, timeSigTop, timeSigBottom, complex));
					}
					if (flag == EDIT) {
						list.add(new EventCreateObject(name, tempo, volume, repeats, complex, pattern, beat, timeSigTop, timeSigBottom, emphasis));
					}
				}
				else {
					//Log.d(Tag, "Ignoring comment or blank line");
				}
				s = br.readLine();
			}

		}
		else if (s.equals("file version 2.1")) {

			s = br.readLine(); //will be the number of events in the song
			array = new MetronomeEvent[Integer.parseInt(s)];
			songName = br.readLine();
			s = br.readLine();
			String notes;
			
			int i = 0;
			while (s != null) {
				Log.d(Tag, "Read line: " + s);
				if (!s.equals("") && s.charAt(0) != '#') { //if not a comment or blank line
					//Log.d(Tag, "starting to read a real one");
					name = s;
					tempo = Double.parseDouble(br.readLine());
					// TODO check for exceptions here
					volume = Float.parseFloat(br.readLine());
					pattern = toPatternArray(br.readLine()); //convert this to an array of ints
					repeats = Integer.parseInt(br.readLine());
					beat = Integer.parseInt(br.readLine());
					s = br.readLine();
					if (s.equals("2")) {
						complex = false;
					}
					else {
						complex = true;
					}
					emphasis = toPatternArray(br.readLine());
					timeSigTop = Integer.parseInt(br.readLine());
					timeSigBottom = Integer.parseInt(br.readLine());
					timeSigInfo = br.readLine();
					br.readLine(); //read over "<notes>
					s = br.readLine(); //read into the first line of the notes
					StringBuilder sb2 = new StringBuilder();
					while (!s.equals("</notes>")) {
						sb2.append(s);
						sb2.append("\n");
						s = br.readLine();
					}
					notes = sb2.toString();
					
					if (flag == PLAYBACK) {
						array[i] = (new MetronomeEvent(tempo, pattern, volume, repeats, beat, name, emphasis, timeSigTop, timeSigBottom, complex));
						array[i].setNotes(notes);
						i++;
					}
					if (flag == EDIT) {
						list.add(new EventCreateObject(name, tempo, volume, repeats, complex, pattern, beat, timeSigTop, timeSigBottom, emphasis));
						((EventCreateObject) list.getLastElement()).addtimeSigInfo(timeSigInfo);
					}
				}
				else {
					//Log.d(Tag, "Ignoring comment or blank line");
				}
				s = br.readLine();
			}

		}
		//add further file versions here as else clauses
		else {
			br.close();
			fr.close();
			throw new FileFormatException();
		}

		br.close();
		fr.close();

		if (flag == EDIT) {
			return list;
		}
		else if (flag == PLAYBACK) {
			Song song = new Song(array);
			song.setSongTitle(songName);
			return song;
		}
		else {
			return null; //it will never reach this point because it will already have thrown an exception above if the flag was not one of these. 
		}
	}

	private void setSongTitle(String songName) {
	    this.title = songName;
    }

	private static int[] toPatternArray(String numbers) {
		//the string will be in the form of space separated whole numbers 0-4 inclusive. anything else will be ignored. 
		int[] array = new int[numbers.length()];
		for (int i = 0; i < numbers.length(); i++){
			switch (numbers.charAt(i)) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
					array[i] = Character.getNumericValue(numbers.charAt(i));
					break;
				default:
					array[i] = 0;
					//if it is not one of these numbers, assume it is 0.
			}
		}
		return array;
	}


	@Override
	public Iterator<MetronomeEvent> iterator() {
		//return events.iterator();
		return new CustomIterator(false);
	}

	/**
	 * A custom iterator implementation that allows movement to an arbitrary element and moving backwards as needed.
	 * @param backwards
	 * @return
	 */
	public CustomIterator iterator(boolean backwards) {
		//return events.iterator(backwards);
		return new CustomIterator(backwards);
	}

	public MetronomeEvent getFirstEvent() {
		//return events.get(0);
		return eventArray[0];
	}

	public MetronomeEvent getLastEvent() {
		//return events.get(events.size() - 1);
		return eventArray[eventArray.length - 1];
	}

	class CustomIterator implements Iterator<MetronomeEvent>{
		//fields
		int index;

		private CustomIterator(boolean backwards) {
			if (backwards) {
				index = eventArray.length - 1;
			}
			else {
				index = -1;
			}
		}

		@Override
		public boolean hasNext() {
			return !(index + 1 >= eventArray.length);
		}

		public boolean hasPrevious() {
			return !(index == 0);
		}

		@Override
		public MetronomeEvent next() {
			return eventArray[++index];
		}

		public MetronomeEvent previous() {
			return eventArray[--index];
		}

		public MetronomeEvent current() {
			if (index >= 0 && index < eventArray.length) {
				return eventArray[index];
			}
			else {
				throw new IllegalStateException("You must call next, previous, or set before calling this method. ");
			}
		}

		public int getCurrentIndex() {
			if (index >= 0 && index < eventArray.length) {
				return index;
			}
			else {
				throw new IllegalStateException("You must call next, previous, or set before calling this method. ");
			}
		}

		public void set(int newIndex) throws IndexOutOfBoundsException {
			if (newIndex >= eventArray.length || newIndex < 0) {
				throw new IndexOutOfBoundsException();
			}
			index = newIndex;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * sets the iterator to point to the specified event. less efficient than just using set(int) with the index. 
		 * @param event the event this iterator should point to. 
		 */
		public void set(MetronomeEvent event) {
			// just a linear search, since they are not ordered by any comparable property
			for (int i = 0; i < eventArray.length; i++) {
				if (eventArray[i] == event) {
					index = i;
					return;
				}
			}
			//if it reaches this point, it is not in there
			throw new NoSuchElementException();
		}
	}

}
