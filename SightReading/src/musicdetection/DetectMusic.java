package musicdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sightreading.SightReadingActivity;

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

		// invert and get houghlines
		Utils.invertColors(sheet);
		Mat linesMat = new Mat();
		Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100);
		List<Line> lines = Utils.getHoughLinesFromMat(linesMat);

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
		// invertColors(tmpSheet);
		/*
		 * Imgproc.erode(tmpSheet, tmpSheet, Imgproc.getStructuringElement(
		 * Imgproc.MORPH_RECT, new Size(3, 3))); // Imgproc.dilate(sheet, sheet,
		 * Imgproc.getStructuringElement( // Imgproc.MORPH_RECT, new Size(3,
		 * 3))); // Imgproc.Canny(tmpSheet, tmpSheet, 0, 100); //
		 * invertColors(tmpSheet);
		 * 
		 * Imgproc.threshold(tmpSheet, tmpSheet, 80, 150,
		 * Imgproc.THRESH_BINARY);
		 * 
		 * // because findContours modifies the image I back it up
		 * 
		 * List<MatOfPoint> contours = new ArrayList<MatOfPoint>(200);
		 * Imgproc.findContours(tmpSheet, contours, new Mat(),
		 * Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
		 * 
		 * Imgproc.cvtColor(tmpSheet, tmpSheet, Imgproc.COLOR_GRAY2BGR);
		 * 
		 * List<Point> validContours = new LinkedList<Point>();
		 * 
		 * for (MatOfPoint points : contours) { List<Point> contour =
		 * points.toList(); double minX = contour.get(0).x; double maxX =
		 * contour.get(0).x; double minY = contour.get(0).y; double maxY =
		 * contour.get(0).y; for (int i = 1; i < contour.size(); i++) { if
		 * (contour.get(i).x < minX) minX = contour.get(i).x; if
		 * (contour.get(i).x > maxX) maxX = contour.get(i).x; if
		 * (contour.get(i).y < minY) minY = contour.get(i).y; if
		 * (contour.get(i).y > maxY) maxY = contour.get(i).y; } double width =
		 * maxX - minX; double height = maxY - minY; if
		 * (Math.abs(Imgproc.contourArea(points) - Math.PI * width * height) <
		 * 0.2 * Imgproc .contourArea(points)) { Log.v("CHECK", "CHECK " + width
		 * + "," + height); Core.circle(output, new Point(minX + width / 2, minY
		 * + height / 2), 3, new Scalar(0, 0, 255)); } }
		 */

		// erase the staves, dilate to fill gaps
		for (Stave s : staves)
			s.eraseFromMat(sheet);
		staveGap = staves.get(0).staveGap();
		Utils.resizeImage(noteHead, staveGap);
		noteWidth = noteHead.cols();
		Imgproc.dilate(sheet, sheet, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(3, 3)));

		Mat tmpSheet = new Mat(sheet, Range.all());
		List<Note> notes = detectNotes(tmpSheet);

		for (Note n : notes) {
			Core.circle(output, n.center(), 10, new Scalar(0, 0, 255));
		}

		// Core.circle(output, new Point(258, 209), 10, new Scalar(0,0,255));
		// get new houghlines
		// Imgproc.HoughLinesP(sheet, linesMat, 1, Math.PI / 180, 100);
		// lines = Utils.getHoughLinesFromMat(linesMat);

		// print lines and return
		Utils.invertColors(sheet);

		// printLines(output,lines);
		return output;

		// TODO: no lines are printed on the returned mat, must fix
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
		MinMaxLocResult minMaxRes = Core.minMaxLoc(result);
		double maxVal = minMaxRes.maxVal;
		Point maxLoc = minMaxRes.maxLoc;
		double maxAllowedVal = maxVal * 0.966;
		while (breaker < 60) {
			minMaxRes = Core.minMaxLoc(result);
			maxVal = minMaxRes.maxVal;
			maxLoc = minMaxRes.maxLoc;
			if (maxVal > maxAllowedVal && isExpected((int) maxLoc.y)) {
				notes.add(new Note(new Point(maxLoc.x + noteWidth / 2, maxLoc.y
						+ staveGap / 2)));
				Rect area = new Rect(maxLoc, mask.size());
				Mat selectedArea = result.submat(area);
				mask.copyTo(selectedArea);
				Log.v("conrad",
						String.valueOf(result.get((int) maxLoc.y,
								(int) maxLoc.x)[0])
								+ " "
								+ String.valueOf(maxLoc.y)
								+ ", "
								+ String.valueOf(maxLoc.x));
				int noteWidthInt = (int) noteWidth / 2;
				int noteHeightInt = (int) staveGap / 2;
				Utils.zeroInMatrix(result, new Point(maxLoc.x - noteWidthInt,
						maxLoc.y - noteHeightInt), (int) noteWidth,
						(int) staveGap);
			} else
				break;
			breaker++;
		}
		Log.v("SHIT", "CHECK2");
		return notes;
	}
	
	private static boolean isExpected(int y) {
		int staveGapInt = (int) staveGap;
		int maxGap = 100;
		Line l = ourStaves.get(0);
		int i = 0,j = (int) l.start().y;
		while (j < y) {
			i = j;
			j += staveGapInt/2;
		}
		maxGap = (int) Math.min(Math.abs(y - i), Math.abs(y - j));
		return (maxGap <= 1);
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

}
