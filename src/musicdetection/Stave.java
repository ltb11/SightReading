package musicdetection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Stave {

	private List<Line> lines;
	private double staveGap;
	private Map<Point, Clef> clefs;
	private Point originalClef;
	
	public Stave(List<Line> lines) {
		this.lines = lines;
		if (lines.size() != 5)
			throw new RuntimeException("Stave must have 5 lines!");
		staveGap = (lines.get(4).start().y - lines.get(0).start().y) / 4; 
		clefs = new HashMap<Point, Clef>();
		originalClef = null;
	}
	
	public void eraseFromMat(Mat image) {
		/*Scalar c1 = new Scalar(255, 0, 0);
		Scalar c2 = new Scalar(0, 255, 0);
		Scalar c3 = new Scalar(0, 0, 255);
		Scalar c4 = new Scalar(255, 0, 255);
		Scalar c5 = new Scalar(255, 255, 0);
		Scalar[] cs = new Scalar[] { c1, c2, c3, c4, c5};*/
		Scalar col = new Scalar(0,0,0);
		
		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).start(), lines.get(i).end(), col, 1);
		}
	}
	
	public void addClef(Clef c, Point p) {
		clefs.put(p, c);
		if (originalClef == null)
			originalClef = p;
	}
	
	public Point originalClef() {
		return originalClef;
	}
	
	public void draw(Mat image) {
		Scalar col = new Scalar(128,0,0);
		
		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).start(), lines.get(i).end(), col, 3);
		}
	}
	
	public double staveGap() {
		return staveGap;
	}

	public Line topLine() {
		return lines.get(0);
	}

	public Line bottomLine() {
		return lines.get(4);
	}

	public Clef getClefAtPos(Point p) {
		Clef result = clefs.get(originalClef);
		double lastClef = originalClef.x;
		for (Point point : clefs.keySet()) {
			if (point.x > lastClef && point.x < p.x) {
				result = clefs.get(point);
				lastClef = point.x;
			}
		}
		return result;
	}


}
