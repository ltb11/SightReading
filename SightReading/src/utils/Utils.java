package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sightreading.Line;
import org.sightreading.Note;
import org.sightreading.Stave;

import android.util.Log;

public class Utils {

	private static final int minLineDirectionVectorDiff = 10;
	private static final int minLineGap = 3;
	private static final double horizontalError = 5;
	private static final double staveLengthTolerance = 0.1;
	private static final double staveGapTolerance = 0.2;

	public static Scalar createHsvColor(float hue, float saturation, float value) {
		int h = (int) (hue * 6);
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
		}
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

	public static Mat detectMusic(Mat sheet) {

		Mat output = new Mat(sheet, Range.all());
		Imgproc.cvtColor(sheet, output, Imgproc.COLOR_GRAY2BGR);

		// invert and get houghlines
		invertColors(sheet);
		Mat linesMat = new Mat();
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100);
		List<Line> lines = getHoughLinesFromMat(linesMat);

		// sort them by length (longest first)
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line line0, Line line1) {
				return (int) (Math.signum(line1.length() - line0.length()));
			}
		});

		// detect staves
		List<Stave> staves = detectStaves(lines);

		// copy mat
		// dil
		// canny
		// h circles
		/*
		 * Mat tmpSheet = new Mat(sheet, Range.all()); List<Note> notes =
		 * detectNotes(tmpSheet);
		 * 
		 * Imgproc.cvtColor(tmpSheet, tmpSheet, Imgproc.COLOR_GRAY2BGR);
		 * 
		 * invertColors(tmpSheet);
		 * 
		 * //Original image for (Note n : notes) { Core.circle(tmpSheet,
		 * n.center(), 1, new Scalar(0,0,255)); }
		 */
		Mat tmpSheet = new Mat(sheet, Range.all());
		// invertColors(tmpSheet);
		Imgproc.erode(tmpSheet, tmpSheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(3, 3)));
		// Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
		// Imgproc.MORPH_RECT, new Size(3, 3)));
		// Imgproc.Canny(tmpSheet, tmpSheet, 0, 100);
		// invertColors(tmpSheet);

		Imgproc.threshold(tmpSheet, tmpSheet, 80, 150, Imgproc.THRESH_BINARY);

		// because findContours modifies the image I back it up

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(200);
		Imgproc.findContours(tmpSheet, contours, new Mat(), Imgproc.RETR_TREE,
				Imgproc.CHAIN_APPROX_NONE);

		Imgproc.cvtColor(tmpSheet, tmpSheet, Imgproc.COLOR_GRAY2BGR);

		List<Point> validContours = new LinkedList<Point>();

		for (MatOfPoint points : contours) {
			List<Point> contour = points.toList();
			double minX = contour.get(0).x;
			double maxX = contour.get(0).x;
			double minY = contour.get(0).y;
			double maxY = contour.get(0).y;
			for (int i = 1; i < contour.size(); i++) {
				if (contour.get(i).x < minX)
					minX = contour.get(i).x;
				if (contour.get(i).x > maxX)
					maxX = contour.get(i).x;
				if (contour.get(i).y < minY)
					minY = contour.get(i).y;
				if (contour.get(i).y > maxY)
					maxY = contour.get(i).y;
			}
			double width = maxX - minX;
			double height = maxY - minY;
			if (Math.abs(Imgproc.contourArea(points) - Math.PI * width * height) < 0.2 * Imgproc
					.contourArea(points)) {
				Log.v("CHECK", "CHECK " + width + "," + height);
				Core.circle(output, new Point(minX + width / 2, minY + height
						/ 2), 3, new Scalar(0, 0, 255));
			}
		}
		
		// erase the staves, dilate to fill gaps
		for (Stave s : staves)
			s.eraseFromMat(sheet);
		Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(3, 3)));

		// get new houghlines
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100);

		lines = getHoughLinesFromMat(linesMat);

		// print lines and return
		Utils.invertColors(sheet);

		// printLines(output,lines);
		return output;

		// TODO: no lines are printed on the returned mat, must fix
	}

	public static List<Note> detectNotes(Mat sheet) {
		Imgproc.erode(sheet, sheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(3, 3)));
		Imgproc.Canny(sheet, sheet, 0, 100);
		Mat circles = new Mat();
		Imgproc.HoughCircles(sheet, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 1,
				200, 100, 0, 10);
		double data[];
		List<Note> notes = new LinkedList<Note>();
		for (int i = 0; i < circles.cols(); i++) {
			data = circles.get(0, i);
			Point pt = new Point(data[0], data[1]);
			Note note = new Note(pt);
			notes.add(note);
		}
		return notes;
	}

	public static List<Stave> detectStaves(List<Line> lines) {
		List<Stave> staves = new LinkedList<Stave>();
		int outside, inside;
		for (outside = 0; outside < lines.size(); outside++) {
			Line start = lines.get(outside);
			List<Line> subset = new LinkedList<Line>();

			for (inside = outside; inside < lines.size(); inside++) {
				Line line = lines.get(inside);
				if (Math.abs(start.length() - line.length()) < start.length()
						* staveLengthTolerance) {
					subset.add(line);
				} else
					break;
			}

			// MID: subset contains all lines within tolerance of the start line
			if (subset.size() < 5)
				continue;

			// getStaveLines must return (in y-axis order) all lines that belong
			// to a stave
			// so they can be pulled out, 5 at a time, to create the staves
			List<Line> staveLines = getSpacedLines(subset, lines);
			if (staveLines.size() != 5) {
				outside--;
				continue;
			}

			staves.add(new Stave(staveLines));
			lines.removeAll(staveLines);
			// Need to start all over again since lines has been changed
			outside = -1;

			// Stave stave = calculateStave(subset);
			// if stave.isValid() return stave;

		}
		return staves;
	}

	private static List<Line> getHoughLinesFromMat(Mat linesMat) {
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
	private static List<Line> getSpacedLines(List<Line> lines,
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

	public static void printLines(Mat mat, List<Line> lines) {
		for (int i = 0; i < lines.size(); i++) {
			// Scalar col = createHsvColor((i*10)/255,1,1);
			Scalar col = new Scalar(255, 0, 0);
			Core.line(mat, lines.get(i).start(), lines.get(i).end(), col, 1);
		}
	}

}
