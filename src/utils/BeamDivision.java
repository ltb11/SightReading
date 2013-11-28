package utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Point;

public class BeamDivision {

	private Point startingPoint;
	private Point endPoint;
	private Map<Point, Integer> intermediatePoints;
	
	public BeamDivision(List<Point> points) {
		this.startingPoint = points.get(0);
		this.endPoint = points.get(points.size() - 1);
	}
	
	public Point startingPoint() {
		return startingPoint;
	}
	
	public Point endPoint() {
		return endPoint;
	}
	
}
