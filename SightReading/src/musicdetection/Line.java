package musicdetection;

import org.opencv.core.Point;

import android.util.Pair;

public class Line {

	private Point start, end;
	private Pair<Double, Double> directingVector;
	double length;
	double angle;

	public Line(Point start, Point end) {
		this.start = start;
		this.end = end;
		double directionX = end.x - start.x;
		double directionY = end.y - start.y;
		double one = Integer.valueOf(1);
		if (directionX == 0) {
			double zero = Integer.valueOf(0);
			directingVector = new Pair<Double, Double>(zero, one);
			angle = Math.PI / 2;
		} else {
			directingVector = new Pair<Double, Double>(one, directionY
					/ directionX);
			// angle = Math.cos(directingVector.second) /
			// Math.sin(directingVector.first);
			angle = Math.atan(directingVector.second);
		}
		length = Math.sqrt(Math.pow(end.x - start.x, 2)
				+ Math.pow(end.y - start.y, 2));
	}

	public Pair<Double, Double> directingVector() {
		return directingVector;
	}

	public Point start() {
		return start;
	}

	public Point end() {
		return end;
	}

	public double length() {
		return length;
	}

	public double angle() {
		return angle;
	}

}
