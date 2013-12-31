package com.Stephen.ultimatemetronome.metronomepackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import android.util.Log;

/**
 * A wrapper class for a linked list of MetronomeEvents, optimized for playback, not editing. 
 * @author Stephen Rodriguez
 *
 */
public class Song implements Iterable<MetronomeEvent>{
	private static final String Tag = "Song";
	//fields
	private LinkedList<MetronomeEvent> events;

	//constructor
	Song() {
		events = new LinkedList<MetronomeEvent>();
	}

	//methods

	public static Song createFromFile(File file) throws IOException, FileNotFoundException, FileFormatException {
		Song song = new Song();
		//local variables to create the elements:
		String name;
		double tempo;
		int[] pattern;
		float volume;
		int repeats ;
		int beat;
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
					song.events.add(new MetronomeEvent(tempo, pattern, volume, repeats, beat, name));
					
					
				}
				else {
					Log.d(Tag, "Ignoring comment or blank line");
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

		return song;
	}

	private static int[] toPatternArray(String numbers) {
		//the string will be in the form of space separated whole numbers 0-4 inclusive. anything else will throw an exception
		int[] array = new int[numbers.length()/2];
		int i1 = 0;
		for (int i = 0; i < numbers.length(); i++){
			switch (numbers.charAt(i)) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
					array[i1++] = Character.getNumericValue(numbers.charAt(i));
					break;
			}

		}
		return array;
	}
	
	public static Song testSong() {
		Song song = new Song();
		song.events.add(new MetronomeEvent(120, new int[] {1, 1, 1, 2, 2}, 1, 2, 2, "Event 1"));
		song.events.add(new MetronomeEvent(75, new int[] {0, 0, 0}, 1, 1, 1, "Event 2"));
		song.events.add(new MetronomeEvent(60, new int[] {1, 1, 2}, .5f, 2, 6, "Event 3"));

		return song;
	}

	@Override
	public Iterator<MetronomeEvent> iterator() {
		return events.iterator();
	}

}
