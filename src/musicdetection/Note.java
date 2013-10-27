package musicdetection;

import org.opencv.core.Point;

public class Note {

	private Point center;
	private double duration;
	
	public Note(Point center) {
		this.center = center;
		this.duration = 1;
	}
	
	public Note(Point center, double d) {
		this.center = center;
		this.duration = 2;
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
