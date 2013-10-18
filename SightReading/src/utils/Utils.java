package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import musicdetection.Line;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Utils {

	private static final int minLineDirectionVectorDiff = 10;
	private static final int minLineGap = 3;
	private static final double horizontalError = 5;
	private static final double staveGapTolerance = 0.2;

	public static Scalar createHsvColor(float hue, float saturation, float value) {
		return new Scalar(0,255,0);
		/*int h = (int) ((hue % 1f) * 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		switch (h) {
		case 0:
			return new Scalar(value, t, p);
		case 1:
			return new Scalar(q, value, p);
		case 2:
			return new Scalar(p, value, t);
		case 3:
			return new Scalar(p, q, value);
		case 4:
			return new Scalar(t, p, value);
		case 5:
			return new Scalar(value, p, q);
		default:
			throw new RuntimeException(
					"Something went wrong when converting from HSV to RGB. Input was "
							+ hue + ", " + saturation + ", " + value);
		}*/
	}

	public boolean isHorizontal(Line l) {
		return (Math.abs(l.directingVector().second) < horizontalError);
	}

	public static void invertColors(Mat mat) {
		Core.bitwise_not(mat, mat);
	}

	/*
	 * This works as so: we check the line to test with every line already in
	 * and test: if the vector directing the line to test is close enough to the
	 * reference line (minLineDirectionVectorDiff) and the point on the test
	 * line with the same x as the start of the reference line is close enough
	 * to this start (minLineGap), the line is considered a duplicate and is
	 * dropped. We need to perform this check on both x and y coordinates
	 * because of horizontal/vertical lines
	 */
	public static boolean areTwoLinesDifferent(Point pt1, Point pt2, Mat lines,
			int i) {
		for (int j = 0; j < i; j++) {
			double[] lineToTest = lines.get(0, j);
			Point ptToTest1 = new Point(lineToTest[0], lineToTest[1]);
			Point ptToTest2 = new Point(lineToTest[2], lineToTest[3]);
			double[] v = new double[] { pt2.x - pt1.x, pt2.y - pt1.y };
			double[] vTest = new double[] { ptToTest2.x - ptToTest1.x,
					ptToTest2.y - ptToTest1.y };
			if ((Math.abs(vTest[1] / vTest[0] - v[1] / v[0]) < minLineDirectionVectorDiff && Math
					.abs(ptToTest1.y - pt1.y + (pt1.x - ptToTest1.x) * vTest[1]
							/ vTest[0]) < minLineGap)
					|| (Math.abs(vTest[0] / vTest[1] - v[0] / v[1]) < minLineDirectionVectorDiff && Math
							.abs(ptToTest1.x - pt1.x + (pt1.y - ptToTest1.y)
									* vTest[0] / vTest[1]) < minLineGap)) {
				return false;
			}
		}
		return true;
	}

	public static List<Line> getHoughLinesFromMat(Mat linesMat) {
		List<Line> lines = new LinkedList<Line>();
		double dataa[];
		for (int i = 0; i < linesMat.cols(); i++) {
			dataa = linesMat.get(0, i);
			Point pt1 = new Point(dataa[0], dataa[1]);
			Point pt2 = new Point(dataa[2], dataa[3]);
			Line line = new Line(pt1, pt2);
			if (line.length() > 200)
				lines.add(line);
		}
		return lines;
	}

	/*
	 * PRE: Given a list of horizontal lines of similar length Checks that the
	 * lines are equally spaced
	 */
	public static List<Line> getSpacedLines(List<Line> lines,
			List<Line> actualLines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line line0, Line line1) {
				return (int) (Math.signum((line0.start().y - line1.start().y)));
			}
		});
		// MID: lines is sorted highest to lowest

		Line first = lines.get(0);
		for (int i = 1; i < lines.size(); i++) {
			Line second = lines.get(i);
			double space = second.start().y - first.start().y;
			double pos = second.start().y + space;
			List<Line> result = new LinkedList<Line>();
			result.add(first);
			result.add(second);
			for (int j = i + 1; j < lines.size(); j++) {
				if (Math.abs(lines.get(j).start().y - pos) < space
						* staveGapTolerance) {
					pos += space;
					result.add(lines.get(j));
					if (result.size() == 5)
						return result;
				}
			}
		}

		actualLines.remove(first);

		/*
		 * POST: Return only the lines that do belong to a stave
		 */
		return new LinkedList<Line>();
	}

	public static void resizeImage(Mat image, double newHeight) {
		double newWidth = newHeight * image.cols() / image.rows();
		Size newSize = new Size(newWidth, newHeight);
		Imgproc.resize(image, image, newSize);
	}
	
	public static void printLines(Mat mat, List<Line> lines) {
		for (int i = 0; i < lines.size(); i++) {
			Scalar col = createHsvColor((i*10)/255,1,1);
			//Scalar col = new Scalar(255, 0, 0);
			Core.line(mat, lines.get(i).start(), lines.get(i).end(), col, 5);
		}
	}

}
