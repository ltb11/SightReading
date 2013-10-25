package musicdetection;

import org.opencv.core.Point;

public class Note {

	private Point center;
	private double duration;
	
	public Note(Point center, double duration) {
		this.center = center;
		this.duration = duration;
	}
	
	// Only for debugging purposes
	public Note(Point center) {
		this.center = center;
		this.duration = 1.0;
	}
	
	public Point center() {
		return center;
	}
	
	public double duration() {
		return duration;
	}
	
}
