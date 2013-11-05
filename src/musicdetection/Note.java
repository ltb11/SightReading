package musicdetection;

import musicrepresentation.NoteName;
import musicrepresentation.PlayedNote;

import org.opencv.core.Point;

import utils.Utils;

public class Note {

	private Point center;
	private double duration;
	private NoteName name;
	private int octave = 0;
	
	public Note(Point center, double d) {
		this.center = center;
		this.duration = d;
	}

	public Point center() {
		return center;
	}
	
	public double duration() {
		return duration;
	}
	
	public void setDuration(double d) {
		this.duration = d;
	}

	public void setName(NoteName name) {
		this.name=name;
	}

	public void setOctave(int octave) {
		this.octave=octave;
	}

	public PlayedNote toPlayedNote() {
		// TODO Auto-generated method stub
		return new PlayedNote(name, octave, Utils.getDuration(duration), 0);
	}
	
	@Override
	public String toString() {
		String result = "";
		result += name + " at octave " + octave + ", it's a " + duration;
		return result;
	}
}
