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

import android.util.Log;
import utils.Interval;
import utils.Utils;

public class DetectMusic {

	private static final double staveLengthTolerance = 0.1;
	public static Mat noteHead;
	public static Mat trebleClef;
	public static Mat fourFour;
	public static Mat bar;
	private static double staveGap;
	private static double noteWidth;
	private static List<Stave> staves = new LinkedList<Stave>();
	private static List<Point> trebleClefs = new LinkedList<Point>();
	private static List<Point> fourFours = new LinkedList<Point>();
	private static List<Note> notes = new LinkedList<Note>();
	private static List<Line> quavers = new LinkedList<Line>();
	private static List<Line> bars = new LinkedList<Line>();

	private static void printStaves(Mat sheet) {
		for (Stave s : staves)
			s.draw(sheet);
	}

	private static void printTreble(Mat sheet) {
		for (Point p : trebleClefs)
			Core.rectangle(sheet, p, new Point(p.x + trebleClef.cols(), p.y
					+ trebleClef.rows()), new Scalar(0, 255, 0), 3);
	}

	private static void printFourFour(Mat sheet) {
		for (Point p : fourFours)
			Core.rectangle(sheet, p, new Point(p.x + fourFour.cols(), p.y
					+ fourFour.rows()), new Scalar(255, 255, 0), 3);
	}

	private static void printNotes(Mat sheet) {
		for (Note n : notes)
			Core.circle(sheet, n.center(), (int) (staveGap / 2), new Scalar(0,
					0, 255), -1);
	}

	private static void printBars(Mat sheet) {
		for (Line l : bars)
			Core.line(sheet, l.start(), l.end(), new Scalar(0, 255, 255), 3);
	}

	public static void printAll(Mat sheet) {
		printStaves(sheet);
		printFourFour(sheet);
		printTreble(sheet);
		printNotes(sheet);
		printBars(sheet);
	}

	/*
	 * public static Mat detectMusic(Mat sheet) {
	 * 
	 * Mat output = new Mat(sheet, Range.all()); Imgproc.cvtColor(sheet, output,
	 * Imgproc.COLOR_GRAY2BGR);
	 * 
	 * Utils.invertColors(sheet); Mat linesMat = new Mat();
	 * Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100); List<Line>
	 * lines = Utils.getHoughLinesFromMat(linesMat);
	 * 
	 * // scale the image Mat scaledSheet = ScaleMat(sheet, 1000);
	 * 
	 * // invert and get houghlines Utils.invertColors(scaledSheet); // Mat
	 * newSheet = correctImage(sheet);
	 * 
	 * // List<Line> lines = GetLines(scaledSheet);
	 * 
	 * // Pre - lines sorted into clumps List<Line> linesSection = lines;
	 * 
	 * double[] histogram = new double[sheet.height()]; double averageAngle = 0;
	 * for (Line line : linesSection) { averageAngle += line.angle(); }
	 * averageAngle = averageAngle / linesSection.size(); double
	 * tanOfAverageAngle = Math.tan(averageAngle); boolean anglePositive; if
	 * (averageAngle > 0) { anglePositive = true; } else { anglePositive =
	 * false; } // make a histogram of the lines for (Line line : linesSection)
	 * { Point start = line.start(); Point end = line.end(); int startPosition =
	 * getHistogramPosition(sheet.width(), sheet.height(), tanOfAverageAngle,
	 * anglePositive, start); int endPosition =
	 * getHistogramPosition(sheet.width(), sheet.height(), tanOfAverageAngle,
	 * anglePositive, end);
	 * 
	 * double weight = line.length();
	 * 
	 * addToHistogram(histogram, startPosition, endPosition, weight); }
	 * 
	 * // threshold histogram /* double total = 0; int numberOfValues = 0; for
	 * (double n : histogram) { if (n != 0) { total += n; numberOfValues++; } }
	 * double mean = total / numberOfValues; for (double n : histogram) { n =
	 * Math.max(n - mean, 0); }
	 * 
	 * double total = 0; for (double n : histogram) { total += n; } double mean
	 * = total / histogram.length; for (double n : histogram) { n = Math.max(n -
	 * mean, 0); }
	 * 
	 * // TODO this code may no longer be relevant /* List<Integer>
	 * stavePositions = new LinkedList<Integer>();
	 * 
	 * 
	 * // look for blips representing staves int position = 0; while (position <
	 * histogram.length) { if (histogram[position] != 0) { int upperBound =
	 * position; while (position < histogram.length && histogram[position] != 0)
	 * { position++; } int lowerBound = position - 1;
	 * stavePositions.add((upperBound + lowerBound) / 2); } position++; }
	 * 
	 * // check if this is a stave, i.e. 5 blips if (stavePositions.size() ==
	 * 10) { // TODO retain it if it is, discard it if it isn't }
	 * 
	 * List<Line> histagramDetectiveStaves = new LinkedList<Line>(); for
	 * (Integer p : stavePositions) { double offset = sheet.width() / 2 *
	 * tanOfAverageAngle; if (anglePositive) { offset = (-1) * offset; } Point
	 * start = new Point(0, p + offset); Point end = new Point(sheet.width(), p
	 * - offset); Line line = new Line(start, end);
	 * histagramDetectiveStaves.add(line);
	 */

	// TODO following code may now be redundant
	/*
	 * // erase the staves, dilate to fill gaps for (Stave s : staves)
	 * s.eraseFromMat(sheet); staveGap = staves.get(0).staveGap();
	 * Utils.resizeImage(noteHead, staveGap); noteWidth = noteHead.cols();
	 * Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
	 * Imgproc.MORPH_RECT, new Size(3,3)));
	 * 
	 * // Utils.writeImage(sheet, Utils.getPath("output/dilated.png")); Mat
	 * noteDetectedSheet = sheet.clone(); Mat erodedSheet = sheet.clone();
	 * Imgproc.erode(erodedSheet, erodedSheet, Imgproc.getStructuringElement(
	 * Imgproc.MORPH_RECT, new Size(12, 12)));
	 * Utils.writeImage(noteDetectedSheet, Utils.getPath("output/test.png")); //
	 * List<Note> notes = detectNotes(noteDetectedSheet, erodedSheet);
	 * 
	 * /* for (Note n : notes) { Core.circle(output, n.center(), (int) staveGap
	 * / 2, new Scalar(0, 0, 255)); }
	 * 
	 * 
	 * Mat lineMat = new Mat(scaledSheet.size(), scaledSheet.type()); //
	 * Utils.invertColors(scaledSheet); Imgproc.cvtColor(lineMat, lineMat,
	 * Imgproc.COLOR_GRAY2BGR); Scalar green = new Scalar(0, 255, 0); Scalar
	 * white = new Scalar(255, 255, 255); //
	 * Utils.printMulticolouredLines(lineMat, lines); //
	 * Utils.printLines(lineMat, histagramDetectiveStaves, white);
	 * Utils.printLines(lineMat, lines, white); //
	 * Utils.printMulticolouredLines(lineMat, histagramDetectiveStaves);
	 * 
	 * // print lines and return // Utils.invertColors(sheet);
	 * 
	 * // printLines(output,lines); return lineMat; }
	 * 
	 * private static void addToHistogram(double[] histogram, int startPosition,
	 * int endPosition, double weight) { int centrePosition = (startPosition +
	 * endPosition) / 2; int range = Math.abs(endPosition - startPosition);
	 * 
	 * if (centrePosition > 0 - range && centrePosition < histogram.length +
	 * range) { if (centrePosition > 0 && centrePosition < histogram.length) {
	 * histogram[centrePosition] = weight; }
	 * 
	 * double reducedWeight; int upperOffset; int lowerOffset;
	 * 
	 * for (int i = 1; i < range; i++) { reducedWeight = weight / range * (range
	 * - i); upperOffset = centrePosition + i; if (upperOffset > 0 &&
	 * upperOffset < histogram.length) { histogram[upperOffset] = reducedWeight;
	 * } lowerOffset = centrePosition - i; if (lowerOffset > 0 && lowerOffset <
	 * histogram.length) { histogram[lowerOffset] = reducedWeight; } }
	 * 
	 * } }
	 * 
	 * private static int getHistogramPosition(int width, int height, double
	 * tanOfAverageAngle, boolean anglePositive, Point point) { double x =
	 * point.x; double y = point.y; double adjacent = x - width / 2; if
	 * (!anglePositive) { adjacent = (-1) * adjacent; } return (int) (y +
	 * adjacent * tanOfAverageAngle); }
	 * 
	 * private static Mat ScaleMat(Mat input, int i) { float width =
	 * input.cols(); float height = input.rows();
	 * 
	 * float ratio = width / height;
	 * 
	 * int newWidth = 2000; int newHeight = (int) (newWidth / ratio);
	 * 
	 * Size newSize = new Size(newWidth, newHeight); Mat scaled = new
	 * Mat(newSize, input.type());
	 * 
	 * Point topLeft = new Point(0, 0); Point topRight = new Point(width, 0);
	 * Point bottomRight = new Point(width, height); Point bottomLeft = new
	 * Point(0, height);
	 * 
	 * Point newTopLeft = new Point(0, 0); Point newTopRight = new
	 * Point(newWidth, 0); Point newBottomRight = new Point(newWidth,
	 * newHeight); Point newBottomLeft = new Point(0, newHeight);
	 * 
	 * MatOfPoint2f src = new MatOfPoint2f(topLeft, topRight, bottomRight,
	 * bottomLeft); MatOfPoint2f dst = new MatOfPoint2f(newTopLeft, newTopRight,
	 * newBottomRight, newBottomLeft); Mat transform =
	 * Imgproc.getPerspectiveTransform(src, dst);
	 * 
	 * Imgproc.warpPerspective(input, scaled, transform, newSize); return
	 * scaled; }
	 */

	private static List<Line> getLines(Mat sheet) {
		Mat linesMat = new Mat();
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100, 1, 15);
		List<Line> lines = Utils.getHoughLinesFromMat(linesMat);

		// sew lines

		return lines;
	}

	public static void detectTrebleClefs(Mat sheet) {
		trebleClef = Utils.resizeImage(trebleClef, DetectMusic.staveGap * 8);
		Mat result = new Mat();
		Imgproc.matchTemplate(sheet, trebleClef, result, Imgproc.TM_CCOEFF);
		Point maxLoc = Core.minMaxLoc(result).minLoc;
		double maxVal = Core.minMaxLoc(result).minVal;
		double maxAllowed = maxVal * 0.90;
		while (maxVal < maxAllowed) {
			maxLoc = Core.minMaxLoc(result).minLoc;
			maxVal = Core.minMaxLoc(result).minVal;
			trebleClefs.add(maxLoc);
			Utils.zeroInMatrix(result, maxLoc, (int) trebleClef.cols(),
					(int) trebleClef.rows());
		}
	}

	public static void detectTime(Mat sheet) {
		fourFour = Utils.resizeImage(fourFour, staveGap * 4);
		Mat result = new Mat();
		Imgproc.matchTemplate(sheet, fourFour, result, Imgproc.TM_CCOEFF);
		Point maxLoc;
		double maxVal = Core.minMaxLoc(result).minVal;
		double maxAllowed = maxVal * 0.95;
		while (maxVal < maxAllowed) {
			maxLoc = Core.minMaxLoc(result).minLoc;
			maxVal = Core.minMaxLoc(result).minVal;
			if (maxLoc.x < staves.get(0).topLine().end().x * 0.95)
				fourFours.add(maxLoc);
			Utils.zeroInMatrix(result, maxLoc, (int) fourFour.cols(),
					(int) fourFour.rows());
		}
	}

	public static void detectQuavers(Mat sheet) {
		Mat lines = new Mat();
		Imgproc.HoughLinesP(sheet, lines, 1, Math.PI / 180, 100);
		for (int i = 0; i < lines.cols(); i++) {
			double[] data = lines.get(0, i);
			Point start = new Point(data[0], data[1]);
			Point end = new Point(data[2], data[3]);
			if (end.x - start.x > sheet.cols() / 30)
				quavers.add(new Line(start, end));
		}
	}

	public static void detectNotes(Mat sheet) {
		Mat detectNotesSheet = sheet.clone();
		Imgproc.erode(sheet, sheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(8, 8)));
		detectQuavers(sheet);
		noteHead = Utils.resizeImage(noteHead, staveGap * 0.9);
		noteWidth = noteHead.cols();
		for (Stave s : staves)
			detectNoteOnPart(detectNotesSheet, sheet, (int) (s.topLine()
					.start().y - 4 * staveGap),
					(int) (s.bottomLine().start().y + 4 * staveGap));
	}

	private static void detectNoteOnPart(Mat detectNotesSheet, Mat ref,
			int startY, int endY) {
		Mat result = new Mat();
		Imgproc.matchTemplate(
				detectNotesSheet.submat(startY, endY, 0,
						detectNotesSheet.cols()), noteHead, result,
				Imgproc.TM_SQDIFF);
		MinMaxLocResult minMaxRes = Core.minMaxLoc(result);
		double maxVal = minMaxRes.maxVal;
		Point maxLoc = minMaxRes.maxLoc;
		double maxAllowedVal = maxVal * 0.9;
		while (true) {
			minMaxRes = Core.minMaxLoc(result);
			maxVal = minMaxRes.maxVal;
			maxLoc = minMaxRes.maxLoc;
			if (maxVal > maxAllowedVal) {
				Point centre = new Point(maxLoc.x + noteWidth / 2, maxLoc.y
						+ startY + staveGap / 2);
				if (Utils.isInCircle(centre, (int) noteWidth / 2, ref)
						&& !Utils.isInAnyRectangle(trebleClefs,
								trebleClef.cols(), trebleClef.rows(), centre)
						&& !Utils.isInAnyRectangle(fourFours, fourFour.cols(),
								fourFour.rows(), centre)
						&& !Utils.isOnQuaverLine(centre, noteWidth, staveGap,
								quavers))
					notes.add(new Note(centre));
				Utils.zeroInMatrix(result, maxLoc, (int) noteWidth,
						(int) staveGap);
			} else
				break;
		}
	}

	public static void detectStaves(List<Line> lines) {
		// sort them by length (longest first)
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line line0, Line line1) {
				return (int) (Math.signum(line1.length() - line0.length()));
			}
		});

		int outside, inside;
		for (outside = 0; outside < lines.size(); outside++) {
			Line start = lines.get(outside);

			// TODO: !!!! this line may be causing problems, not sure
			if (start.length() < Utils.STANDARD_IMAGE_WIDTH * 0.5)
				break;

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
			lines.removeAll(staveLines);
			// Need to start all over again since lines has been changed
			outside = -1;

			// Stave stave = calculateStave(subset);
			// if stave.isValid() return stave;

		}
		staveGap = staves.get(0).staveGap();
	}

	public static Mat correctImage(Mat sheet) {

		// detect staves
		List<Line> lines = getLines(sheet);
		detectStaves(lines);

		//
		Line top = staves.get(0).topLine();
		Line bottom = staves.get(staves.size() - 1).bottomLine();

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

	private static void detectBarsOnPart(Mat sheet, Stave s) {
		Line l = s.topLine();
		int i = 0;
		Point clef = trebleClefs.get(i);
		while (!(new Interval((int) (s.topLine().start().y - 4 * staveGap), (int) (s.bottomLine().start().y)).contains((int) clef.y))) {
			i++;
			clef = trebleClefs.get(i);
		}
		double angleToRotate = (l.end().y - l.start().y)
				/ (l.end().x - l.start().x);
		Mat rotated = Utils.rotateMatrix(sheet, angleToRotate);
		Mat projection = Utils.verticalProjection(rotated.submat((int) (l
				.start().y - 4 * staveGap),
				(int) (s.bottomLine().start().y + 4 * staveGap),(int) clef.x, sheet
						.cols()));
		Utils.invertColors(rotated);
		Mat result = new Mat();
		Imgproc.matchTemplate(rotated, bar, result, Imgproc.TM_SQDIFF);
		MinMaxLocResult minMaxRes = Core.minMaxLoc(result);
		double maxVal = minMaxRes.maxVal;
		Point maxLoc = minMaxRes.maxLoc;
		while (Core.minMaxLoc(result).maxVal > maxVal * 0.9999) {
			Log.v("Guillaume", Double.toString(maxLoc.x) + "," + Double.toString(maxLoc.y));
			minMaxRes = Core.minMaxLoc(result);
			maxLoc = minMaxRes.maxLoc;
			Point p = new Point(maxLoc.x + clef.x, maxLoc.y + s.topLine().start().y);
			bars.add(new Line(p, new Point(p.x, p.y + staveGap * 3)));
			Utils.zeroInMatrix(result, maxLoc, 10,
					(int) (staveGap * 3));
		}
	}

	public static void detectBars(Mat sheet) {
		bar = bar.submat(0,(int) (staveGap * 3), 0, 1);
		for (int i = 2; i <= staves.size(); i++)
			detectBarsOnPart(sheet, staves.get(i));
	}
}
