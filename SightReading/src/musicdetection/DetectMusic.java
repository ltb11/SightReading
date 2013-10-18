package musicdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import utils.Utils;
import android.util.Log;

public class DetectMusic {

	private static final double staveLengthTolerance = 0.1;
	public static Mat noteHead;
	private static double staveGap;
	private static double noteWidth;
	private static List<Line> ourStaves = new LinkedList<Line>();

	public static Mat detectMusic(Mat sheet) {

		Mat output = new Mat(sheet, Range.all());
		Imgproc.cvtColor(sheet, output, Imgproc.COLOR_GRAY2BGR);

		int width = sheet.cols();
		int height = sheet.rows();
		int sep = 200;
		for (int j = 0; j < width; j += sep) {
			for (int i = 0; i < height; i += sep) {
				int xMax = Math.min(j + sep, width);
				int yMax = Math.min(i + sep, height);
				Mat section = new Mat(sheet, new Range(i, yMax), new Range(j,
						xMax));

				double mean = Core.mean(section).val[0];
				mean = Math.max(Math.min(mean - 15, 200), 70);
				Imgproc.threshold(section, section, mean, 256,
						Imgproc.THRESH_BINARY);

				Rect area = new Rect(new Point(j, i), section.size());
				Mat selectedArea = sheet.submat(area);
				section.copyTo(selectedArea);

			}
		}

		Mat scaledSheet = ScaleMat(sheet, 1000);

		// invert and get houghlines
		Utils.invertColors(scaledSheet);
		// Mat newSheet = correctImage(sheet);

		List<Line> lines = GetLines(scaledSheet);

		// Pre - lines sorted into clumps
		List<Line> linesSection = lines;

		double[] histogram = new double[sheet.height()];
		double averageAngle = 0;
		for (Line line : lines) {
			averageAngle += line.angle(); // TODO i need to know more about what
			// this angle is
		}
		averageAngle = averageAngle / lines.size();

		for (Line line : lines) {
			Point start = line.start();
			Point end = line.end();

			int startPosition = (int) (start.y + start.x
					* Math.tan(averageAngle));
			int endPosition = (int) (end.y + end.x * Math.tan(averageAngle));

			double weight = line.length();
			int centrePosition = (startPosition + endPosition) / 2;

			histogram[centrePosition] = weight;

			int range = Math.abs(endPosition - startPosition);
			double reducedWeight;
			for (int i = 1; i < range; i++) {
				reducedWeight = weight / range * (range - i);
				histogram[centrePosition + i] = reducedWeight;
				histogram[centrePosition - i] = reducedWeight;
			}
		}

		// threshold histogram
		double total = 0;
		for (double n : histogram) {
			total += n;
		}
		double mean = total / histogram.length;
		for (double n : histogram) {
			n -= mean;
		}

		List<Integer> stavePositions = new LinkedList<Integer>();

		// look for 5 blips representing staves
		for (int position = 0; position < histogram.length; position++) {
			if (histogram[position] != 0) {
				stavePositions.add(position);
				do {
					position++;
				} while (histogram[position] != 0);
				stavePositions.add(position);
			}
		}

		// check if this is a stave
		if (stavePositions.size() == 10) {
			// TODO retain it if it is, discard it if it isn't
		}

		Mat lineMat = new Mat(scaledSheet.size(), scaledSheet.type());
		// Utils.invertColors(scaledSheet);
		Imgproc.cvtColor(lineMat, lineMat, Imgproc.COLOR_GRAY2BGR);
		Utils.printLines(lineMat, lines);

		// print lines and return
		// Utils.invertColors(sheet);

		// printLines(output,lines);
		return lineMat;

		// TODO: no lines are printed on the returned mat, must fix
	}

	private static Mat ScaleMat(Mat input, int i) {
		float width = input.cols();
		float height = input.rows();

		float ratio = width / height;

		int newWidth = 2000;
		int newHeight = (int) (newWidth / ratio);

		Size newSize = new Size(newWidth, newHeight);
		Mat scaled = new Mat(newSize, input.type());

		Point topLeft = new Point(0, 0);
		Point topRight = new Point(width, 0);
		Point bottomRight = new Point(width, height);
		Point bottomLeft = new Point(0, height);

		Point newTopLeft = new Point(0, 0);
		Point newTopRight = new Point(newWidth, 0);
		Point newBottomRight = new Point(newWidth, newHeight);
		Point newBottomLeft = new Point(0, newHeight);

		MatOfPoint2f src = new MatOfPoint2f(topLeft, topRight, bottomRight,
				bottomLeft);
		MatOfPoint2f dst = new MatOfPoint2f(newTopLeft, newTopRight,
				newBottomRight, newBottomLeft);
		Mat transform = Imgproc.getPerspectiveTransform(src, dst);

		Imgproc.warpPerspective(input, scaled, transform, newSize);
		return scaled;
	}

	private static List<Line> GetLines(Mat sheet) {
		Mat linesMat = new Mat();
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100, 1, 15);
		List<Line> lines = Utils.getHoughLinesFromMat(linesMat);

		// sew lines

		return lines;
	}

	public static List<Note> detectNotes(Mat sheet) {
		List<Note> notes = new LinkedList<Note>();
		Mat result = new Mat();
		Mat mask = new Mat(new Size(noteWidth, staveGap), sheet.type());
		for (int i = 0; i < mask.cols(); i++) {
			for (int j = 0; j < mask.rows(); j++) {
				mask.put(j, i, new double[] { 0 });
			}
		}
		Imgproc.matchTemplate(sheet, noteHead, result, Imgproc.TM_SQDIFF);
		// Imgproc.threshold(result, result, 0.9, 1., Imgproc.THRESH_TOZERO);
		int breaker = 0;

		while (breaker < 100) {
			double threshold = 0.8;
			MinMaxLocResult minMaxRes = Core.minMaxLoc(result);
			double minVal = minMaxRes.minVal;
			double maxVal = minMaxRes.maxVal;
			Point minLoc = minMaxRes.minLoc;
			Point maxLoc = minMaxRes.maxLoc;
			if (maxVal >= threshold) {
				Log.v("SHIT", "CHECK");
				notes.add(new Note(new Point(maxLoc.x + noteWidth / 2, maxLoc.y
						+ staveGap / 2)));
				Imgproc.floodFill(result, new Mat(), maxLoc, new Scalar(0));
				/*
				 * MinMaxLocResult minMaxRes = Core.minMaxLoc(result); double
				 * maxVal = minMaxRes.maxVal; Point maxLoc = minMaxRes.maxLoc;
				 * double maxAllowedVal = maxVal * 0.966; while (breaker < 60) {
				 * minMaxRes = Core.minMaxLoc(result); maxVal =
				 * minMaxRes.maxVal; maxLoc = minMaxRes.maxLoc; if (maxVal >
				 * maxAllowedVal) { notes.add(new Note(new Point(maxLoc.x +
				 * noteWidth / 2, maxLoc.y + staveGap / 2))); Rect area = new
				 * Rect(maxLoc, mask.size()); Mat selectedArea =
				 * result.submat(area); mask.copyTo(selectedArea);
				 * Log.v("conrad", String.valueOf(result.get((int) maxLoc.y,
				 * (int) maxLoc.x)[0]) + " " + String.valueOf(maxLoc.y) + ", " +
				 * String.valueOf(maxLoc.x)); Utils.zeroInMatrix(result, new
				 * Point(maxLoc.x, maxLoc.y), (int) noteWidth, (int) staveGap);
				 */
			} else
				break;
			breaker++;
		}
		Log.v("SHIT", "CHECK2");
		return notes;
	}

	public static List<Stave> detectStaves(List<Line> lines) {
		// sort them by length (longest first)
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line line0, Line line1) {
				return (int) (Math.signum(line1.length() - line0.length()));
			}
		});

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
			List<Line> staveLines = Utils.getSpacedLines(subset, lines);
			if (staveLines.size() != 5) {
				outside--;
				continue;
			}

			staves.add(new Stave(staveLines));
			ourStaves.addAll(staveLines);
			lines.removeAll(staveLines);
			// Need to start all over again since lines has been changed
			outside = -1;

			// Stave stave = calculateStave(subset);
			// if stave.isValid() return stave;

		}
		return staves;
	}

	public static Mat correctImage(Mat sheet) {

		// detect staves
		List<Line> lines = GetLines(sheet);

		List<Stave> staves = detectStaves(lines);

		//
		Line top = staves.get(0).TopLine();
		Line bottom = staves.get(staves.size() - 1).BottomLine();

		Point topLeft = top.start();
		Point topRight = top.end();
		Point bottomRight = bottom.end();
		Point bottomLeft = bottom.start();

		Core.line(sheet, topLeft, topRight, new Scalar(128, 0, 0), 10);
		Core.line(sheet, topRight, bottomRight, new Scalar(128, 0, 0), 10);
		Core.line(sheet, bottomRight, bottomLeft, new Scalar(128, 0, 0), 10);
		Core.line(sheet, bottomLeft, topLeft, new Scalar(128, 0, 0), 10);

		for (Stave s : staves)
			s.draw(sheet);

		float width = sheet.cols();
		float height = sheet.rows();

		float ratio = width / height;

		int newWidth = 2000;
		int newHeight = (int) (newWidth / ratio);

		Size newSize = new Size(newWidth, newHeight);
		Mat scaledSheet = new Mat(newSize, sheet.type());// CvType.CV_8UC1);

		Point newTopLeft = new Point(0, 0);
		Point newTopRight = new Point(newWidth, 0);
		Point newBottomRight = new Point(newWidth, newHeight);
		Point newBottomLeft = new Point(0, newHeight);

		MatOfPoint2f src = new MatOfPoint2f(topLeft, topRight, bottomRight,
				bottomLeft);
		MatOfPoint2f dst = new MatOfPoint2f(newTopLeft, newTopRight,
				newBottomRight, newBottomLeft);
		Mat transform = Imgproc.getPerspectiveTransform(src, dst);

		Imgproc.warpPerspective(sheet, scaledSheet, transform, newSize);

		return sheet;
	}
}

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
 * //Original image for (Note n : notes) { Core.circle(tmpSheet, n.center(), 1,
 * new Scalar(0,0,255)); }
 */
// invertColors(tmpSheet);
/*
 * Imgproc.erode(tmpSheet, tmpSheet, Imgproc.getStructuringElement(
 * Imgproc.MORPH_RECT, new Size(3, 3))); // Imgproc.dilate(sheet, sheet,
 * Imgproc.getStructuringElement( // Imgproc.MORPH_RECT, new Size(3, 3))); //
 * Imgproc.Canny(tmpSheet, tmpSheet, 0, 100); // invertColors(tmpSheet);
 * 
 * Imgproc.threshold(tmpSheet, tmpSheet, 80, 150, Imgproc.THRESH_BINARY);
 * 
 * // because findContours modifies the image I back it up
 * 
 * List<MatOfPoint> contours = new ArrayList<MatOfPoint>(200);
 * Imgproc.findContours(tmpSheet, contours, new Mat(), Imgproc.RETR_TREE,
 * Imgproc.CHAIN_APPROX_NONE);
 * 
 * Imgproc.cvtColor(tmpSheet, tmpSheet, Imgproc.COLOR_GRAY2BGR);
 * 
 * List<Point> validContours = new LinkedList<Point>();
 * 
 * for (MatOfPoint points : contours) { List<Point> contour = points.toList();
 * double minX = contour.get(0).x; double maxX = contour.get(0).x; double minY =
 * contour.get(0).y; double maxY = contour.get(0).y; for (int i = 1; i <
 * contour.size(); i++) { if (contour.get(i).x < minX) minX = contour.get(i).x;
 * if (contour.get(i).x > maxX) maxX = contour.get(i).x; if (contour.get(i).y <
 * minY) minY = contour.get(i).y; if (contour.get(i).y > maxY) maxY =
 * contour.get(i).y; } double width = maxX - minX; double height = maxY - minY;
 * if (Math.abs(Imgproc.contourArea(points) - Math.PI * width * height) < 0.2 *
 * Imgproc .contourArea(points)) { Log.v("CHECK", "CHECK " + width + "," +
 * height); Core.circle(output, new Point(minX + width / 2, minY + height / 2),
 * 3, new Scalar(0, 0, 255)); } }
 */

// Mat section = new Mat(sheet, new Range(j,xMax), new
// Range(i,yMax));

// Mat section = new Mat(sheet, new Range(0,3000), new
// Range(0,2000));

// Imgproc.GaussianBlur(sheet, sheet, new Size(9,9), 1);
// Imgproc.erode(sheet, sheet, Imgproc.getStructuringElement(
// Imgproc.MORPH_RECT, new Size(4, 4)));
// Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
// Imgproc.MORPH_RECT, new Size(3, 3)));

// erase the staves, dilate to fill gaps
/*
 * for (Stave s : staves) s.eraseFromMat(sheet); staveGap =
 * staves.get(0).staveGap(); Utils.resizeImage(noteHead, staveGap); noteWidth =
 * noteHead.cols(); Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
 * Imgproc.MORPH_RECT, new Size(3, 3)));
 * 
 * Mat tmpSheet = new Mat(sheet, Range.all()); List<Note> notes =
 * detectNotes(tmpSheet);
 * 
 * for (Note n : notes) { Core.circle(output, n.center(), 10, new Scalar(0, 0,
 * 255)); }
 * 
 * // Core.circle(output, new Point(258, 209), 10, new Scalar(0,0,255)); // get
 * new houghlines // Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180,
 * 100); // lines = Utils.getHoughLinesFromMat(linesMat);
 */

/*
 * List<Line> sortedLines = lines; Iterator<Line> startLines =
 * sortedLines.iterator(); Iterator<Line> linesToMatch = sortedLines.iterator();
 * // what happens to // the iterator // when the list // changes Line
 * startLine; Line lineToMatch; while (startLines.hasNext()) { startLine =
 * startLines.next(); double startLineEnd = startLine.end().x; if (){ //sew the
 * lines } }
 */

/*
 * // find staves double[] histogram = new double[sheet.height()];
 * 
 * for (Line line : lines) { Point start = line.start(); double angle =
 * line.angle(); // TODO i need to know more about what // this // angle is int
 * position = (int) (start.y + start.x * Math.tan(angle)); double weight =
 * line.length(); histogram[position] = weight; }
 * 
 * double total = 0; for (double n : histogram) { total += n; } double mean =
 * total / histogram.length; for (double n : histogram) { n -= mean; }
 * 
 * // so then here i need to work out where the staves are - either a point //
 * or a range?
 * 
 * 
 * // then given that I have them sorted so that the right ones are next to //
 * each other - no because if I know all those lines are in the stave // then I
 * can go ahead and sew all of them
 * 
 * // find corners
 */
