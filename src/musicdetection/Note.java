package musicdetection;

import musicrepresentation.NoteName;
import musicrepresentation.PlayedNote;

import org.opencv.core.Point;

import android.util.Log;
import utils.OurUtils;

public class Note {

	private Point center;
	private double duration;
	private NoteName name;
	private int octave = 0;
	private int dot;
	
	public Note(Point center, double d) {
		this.center = center;
		this.duration = d;
		this.dot = 0;
	}

	public void incrementDot(){
		dot++;
	}
	
	/*public int getDot(){
		return dot;
	}*/
	
	public Point center() {
		return center;
	}
	
	public double duration() {
		return duration;
	}
	
	public void setDuration(double d) {
		this.duration = d;
	}
	
	public void halveDuration() {
		this.duration /= 2;
	}

	public void setName(NoteName name) {
		this.name=name;
	}

	public void setOctave(int octave) {
		this.octave=octave;
	}

	public PlayedNote toPlayedNote() {
		// TODO Auto-generated method stub
		return new PlayedNote(name, octave, OurUtils.getDuration(duration), dot);
	}
	
	public void display() {
		Log.d("Guillaume", "x: " + center.x + ", y: " + center.y);
	}
	
	@Override
	public String toString() {
		String result = "";
		result += name + " at octave " + octave + ", it's a " + duration;
		return result;
	}
}
