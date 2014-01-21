package utils;

import android.util.Log;

import org.opencv.core.Point;

import java.util.List;
import java.util.Map;

import musicdetection.Line;

public class BeamDivision {

	private Point startingPoint;
	private Point endPoint;
	private Map<Point, Integer> intermediatePoints;

	public BeamDivision(List<Point> points) {
		if (points.size() < 2)
			Log.e("Guillaume",
					"Couldn't start the beam with less than 2 points!");
		this.startingPoint = points.get(0);
		this.endPoint = points.get(points.size() - 1);
	}

	public Point startingPoint() {
		return startingPoint;
	}

	public Point endPoint() {
		return endPoint;
	}

	public Line toLine(double offsetX, double offsetY) {
		Log.d("Guillaume", "Start point: " + startingPoint.x + ","
				+ startingPoint.y + "/End point: " + endPoint.x + ","
				+ endPoint.y + "/Offset: " + offsetX + "," + offsetY);
		return new Line(new Point(startingPoint.x + offsetX, startingPoint.y
				+ offsetY), new Point(endPoint.x + offsetX, endPoint.y
				+ offsetY));
	}

}
