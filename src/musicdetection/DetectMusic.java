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

public class DetectMusic {

	private static final double staveLengthTolerance = 0.1;
	public static Mat noteHead;
	private static double staveGap;
	private static double noteWidth;
	private static List<Line> ourStaves = new LinkedList<Line>();

	public static Mat detectMusic(Mat sheet) {

        
		Mat output = new Mat(sheet, Range.all());
		Imgproc.cvtColor(sheet, output, Imgproc.COLOR_GRAY2BGR);

		// TODO Is this block still necessary?
		/*
		 * int width = sheet.cols(); int height = sheet.rows(); int sep = 200;
		 * for (int j = 0; j < width; j += sep) { for (int i = 0; i < height; i
		 * += sep) { int xMax = Math.min(j + sep, width); int yMax = Math.min(i
		 * + sep, height); Mat section = new Mat(sheet, new Range(i, yMax), new
		 * Range(j, xMax));
		 * 
		 * double mean = Core.mean(section).val[0]; mean =
		 * Math.max(Math.min(mean - 15, 200), 70); Imgproc.threshold(section,
		 * section, mean, 256, Imgproc.THRESH_BINARY);
		 * 
		 * Rect area = new Rect(new Point(j, i), section.size()); Mat
		 * selectedArea = sheet.submat(area); section.copyTo(selectedArea); } }
		 */

		// invert and get houghlines
		Utils.invertColors(sheet);
		Mat linesMat = new Mat();
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100);
		List<Line> lines = Utils.getHoughLinesFromMat(linesMat);

		// scale the image
		Mat scaledSheet = ScaleMat(sheet, 1000);

		// invert and get houghlines
		Utils.invertColors(scaledSheet);
		// Mat newSheet = correctImage(sheet);

		// List<Line> lines = GetLines(scaledSheet);

		// Pre - lines sorted into clumps
		List<Line> linesSection = lines;

		double[] histogram = new double[sheet.height()];
		double averageAngle = 0;
		for (Line line : linesSection) {
			averageAngle += line.angle();
		}
		averageAngle = averageAngle / linesSection.size();
		double tanOfAverageAngle = Math.tan(averageAngle);
		boolean anglePositive;
		if (averageAngle > 0) {
			anglePositive = true;
		} else {
			anglePositive = false;
		}
		// make a histogram of the lines
		for (Line line : linesSection) {
			Point start = line.start();
			Point end = line.end();
			int startPosition = getHistogramPosition(sheet.width(),
					sheet.height(), tanOfAverageAngle, anglePositive, start);
			int endPosition = getHistogramPosition(sheet.width(),
					sheet.height(), tanOfAverageAngle, anglePositive, end);

			double weight = line.length();

			addToHistogram(histogram, startPosition, endPosition, weight);
		}

		// threshold histogram
		/*
		 * double total = 0; int numberOfValues = 0; for (double n : histogram)
		 * { if (n != 0) { total += n; numberOfValues++; } } double mean = total
		 * / numberOfValues; for (double n : histogram) { n = Math.max(n - mean,
		 * 0); }
		 */
		double total = 0;
		for (double n : histogram) {
			total += n;
		}
		double mean = total / histogram.length;
		for (double n : histogram) {
			n = Math.max(n - mean, 0);
		}

		// TODO this code may no longer be relevant
		/*
		 * List<Integer> stavePositions = new LinkedList<Integer>();
		 * 
		 * 
		 * // look for blips representing staves int position = 0; while
		 * (position < histogram.length) { if (histogram[position] != 0) { int
		 * upperBound = position; while (position < histogram.length &&
		 * histogram[position] != 0) { position++; } int lowerBound = position -
		 * 1; stavePositions.add((upperBound + lowerBound) / 2); } position++; }
		 * 
		 * // check if this is a stave, i.e. 5 blips if (stavePositions.size()
		 * == 10) { // TODO retain it if it is, discard it if it isn't }
		 * 
		 * List<Line> histagramDetectiveStaves = new LinkedList<Line>(); for
		 * (Integer p : stavePositions) { double offset = sheet.width() / 2 *
		 * tanOfAverageAngle; if (anglePositive) { offset = (-1) * offset; }
		 * Point start = new Point(0, p + offset); Point end = new
		 * Point(sheet.width(), p - offset); Line line = new Line(start, end);
		 * histagramDetectiveStaves.add(line);
		 */

		// TODO following code may now be redundant
		/*
		 * // erase the staves, dilate to fill gaps for (Stave s : staves)
		 * s.eraseFromMat(sheet); staveGap = staves.get(0).staveGap();
		 * Utils.resizeImage(noteHead, staveGap); noteWidth = noteHead.cols();
		 * Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
		 * Imgproc.MORPH_RECT, new Size(3,3)));
		 */
		// Utils.writeImage(sheet, Utils.getPath("output/dilated.png"));
		Mat noteDetectedSheet = sheet.clone();
		Mat erodedSheet = sheet.clone();
		Imgproc.erode(erodedSheet, erodedSheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(12, 12)));
		Utils.writeImage(noteDetectedSheet, Utils.getPath("output/test.png"));
		List<Note> notes = detectNotes(noteDetectedSheet, erodedSheet);

		for (Note n : notes) {
			Core.circle(output, n.center(), (int) staveGap / 2, new Scalar(0,
					0, 255));
		}

		Mat lineMat = new Mat(scaledSheet.size(), scaledSheet.type());
		// Utils.invertColors(scaledSheet);
		Imgproc.cvtColor(lineMat, lineMat, Imgproc.COLOR_GRAY2BGR);
		Scalar green = new Scalar(0, 255, 0);
		Scalar white = new Scalar(255, 255, 255);
		// Utils.printMulticolouredLines(lineMat, lines);
		// Utils.printLines(lineMat, histagramDetectiveStaves, white);
		Utils.printLines(lineMat, lines, white);
		// Utils.printMulticolouredLines(lineMat, histagramDetectiveStaves);

		// print lines and return
		// Utils.invertColors(sheet);

		// printLines(output,lines);
		return lineMat;
	}

	private static void addToHistogram(double[] histogram, int startPosition,
			int endPosition, double weight) {
		int centrePosition = (startPosition + endPosition) / 2;
		int range = Math.abs(endPosition - startPosition);

		if (centrePosition > 0 - range
				&& centrePosition < histogram.length + range) {
			if (centrePosition > 0 && centrePosition < histogram.length) {
				histogram[centrePosition] = weight;
			}

			double reducedWeight;
			int upperOffset;
			int lowerOffset;

			for (int i = 1; i < range; i++) {
				reducedWeight = weight / range * (range - i);
				upperOffset = centrePosition + i;
				if (upperOffset > 0 && upperOffset < histogram.length) {
					histogram[upperOffset] = reducedWeight;
				}
				lowerOffset = centrePosition - i;
				if (lowerOffset > 0 && lowerOffset < histogram.length) {
					histogram[lowerOffset] = reducedWeight;
				}
			}

		}
	}

	private static int getHistogramPosition(int width, int height,
			double tanOfAverageAngle, boolean anglePositive, Point point) {
		double x = point.x;
		double y = point.y;
		double adjacent = x - width / 2;
		if (!anglePositive) {
			adjacent = (-1) * adjacent;
		}
		return (int) (y + adjacent * tanOfAverageAngle);
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

	public static List<Note> detectNotes(Mat sheet, Mat ref) {
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
		MinMaxLocResult minMaxRes = Core.minMaxLoc(result);
		double maxVal = minMaxRes.maxVal;
		Point maxLoc = minMaxRes.maxLoc;
		double maxAllowedVal = maxVal * 0.90;
		while (breaker < 60) {
			minMaxRes = Core.minMaxLoc(result);
			maxVal = minMaxRes.maxVal;
			maxLoc = minMaxRes.maxLoc;
			if (maxVal > maxAllowedVal) {
				Point centre = new Point(maxLoc.x + noteWidth / 2, maxLoc.y
						+ staveGap / 2);
				if (Utils.isInCircle(centre, staveGap, ref)) {
					notes.add(new Note(new Point(maxLoc.x + noteWidth / 2,
							maxLoc.y + staveGap / 2)));
					Rect area = new Rect(maxLoc, mask.size());
					Mat selectedArea = result.submat(area);
					mask.copyTo(selectedArea);
					Utils.zeroInMatrix(result, new Point(maxLoc.x, maxLoc.y),
							(int) noteWidth, (int) staveGap);
				}
			} else
				break;
			breaker++;
		}

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
