package musicdetection;

import org.opencv.core.Point;

public class Note {

	private Point center;
	
	public Note(Point center) {
		this.center = center;
	}
	
	public Point center() {
		return center;
	}
}
