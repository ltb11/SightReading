package musicdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
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
		// Imgproc.erode(sheet, sheet, Imgproc.getStructuringElement(
		// Imgproc.MORPH_RECT, new Size(10, 10)));
		// Imgproc.erode(sheet, sheet, Imgproc.getStructuringElement(
		// Imgproc.MORPH_RECT, new Size(7, 7)));
		// Highgui.imwrite(Environment.getExternalStorageDirectory()
		// .getAbsolutePath() + "/DCIM/eroded.png", sheet);
		Mat result = new Mat();
		Imgproc.matchTemplate(sheet, noteHead, result, Imgproc.TM_SQDIFF);
		Imgproc.threshold(result, result, 0.8, 1., Imgproc.THRESH_TOZERO);
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
				notes.add(new Note(new Point(maxLoc.x + noteWidth / 2, maxLoc.y + staveGap / 2)));
				Imgproc.floodFill(result, new Mat(), maxLoc, new Scalar(255,255,255));
			} else 
				break;
			breaker++;
		}
		Log.v("SHIT", "CHECK2");
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
		return staves;
	}

}
