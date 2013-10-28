package musicdetection;

import org.opencv.core.Point;

public class Note {

	private Point center;
	private double duration;
	private Stave stave;
	
	public Note(Point center, Stave s) {
		this.center = center;
		this.duration = 1;
		this.stave = s;
	}
	
	public Note(Point center, double d, Stave s) {
		this.center = center;
		this.duration = 2;
		this.stave = s;
	}
	
	public Stave stave() {
		return stave;
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
}
