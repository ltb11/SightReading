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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import utils.Utils;
import android.util.Log;

public class DetectMusic {

	private static final double staveLengthTolerance = 0.1;
	public static Mat noteHead;
	public static Mat trebleClef;
	public static Mat fourFour;
	public static Mat bar;
	public static Mat flat_inter;
	public static Mat flat_on;
	public static Mat half_note;
	private static double staveGap;
	private static double noteWidth;
	private static List<Stave> staves = new LinkedList<Stave>();
	private static List<Point> trebleClefs = new LinkedList<Point>();
	private static List<Point> fourFours = new LinkedList<Point>();
	private static List<Note> notes = new LinkedList<Note>();
	private static List<Line> beams = new LinkedList<Line>();
	private static List<Line> bars = new LinkedList<Line>();
	private static List<Point> flats = new LinkedList<Point>();

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
			Core.circle(sheet, n.center(), (int) (staveGap / 2),
					(n.duration() == 1. ? new Scalar(0, 0, 255)
							: (n.duration() == 2. ? new Scalar(128, 128, 0)
									: new Scalar(255, 0, 255))), -1);
	}

	private static void printBeams(Mat sheet) {
		for (Line l : beams)
			Core.line(sheet, l.start(), l.end(), new Scalar(0, 255, 255), 3);
	}

	private static void printFlats(Mat sheet) {
		for (Point p : flats)
			Core.rectangle(sheet, p, new Point(p.x + flat_on.cols(), p.y
					+ flat_on.rows()), new Scalar(255, 0, 127), 4);
	}

	private static void printBars(Mat sheet) {
		for (Line l : bars)
			Core.line(sheet, l.start(), l.end(), new Scalar(0, 255, 255), 3);
	}

	private static void printNoteRepresentations() {
		Collections.sort(notes, new Comparator<Note>() {

			@Override
			public int compare(Note lhs, Note rhs) {
				if (lhs.stave().topLine().start().y != rhs.stave().topLine().start().y)
					return (int) (lhs.stave().topLine().start().y - rhs.stave().topLine().start().y);
				
				return (int) (lhs.center().x - rhs.center().x);
			}
			
		});
		for (Note n : notes)
			Log.v("Guillaume", Utils.noteToNoteRepresentation(n).toString());
	}
	
	public static void printAll(Mat sheet) {
		printStaves(sheet);
		printFourFour(sheet);
		printTreble(sheet);
		printNotes(sheet);
		printBars(sheet);
		printFlats(sheet);
		printNoteRepresentations();
		// printBeams(sheet);
	}

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
		Point minLoc = Core.minMaxLoc(result).minLoc;
		double minVal = Core.minMaxLoc(result).minVal;
		double minAllowed = minVal * 0.90;
		while (minVal < minAllowed) {
			minLoc = Core.minMaxLoc(result).minLoc;
			minVal = Core.minMaxLoc(result).minVal;
			trebleClefs.add(minLoc);
			Utils.whichStaveDoesAPointBelongTo(minLoc, staves, staveGap)
					.addClef(Clef.Treble, minLoc);
			Utils.zeroInMatrix(result, minLoc, (int) trebleClef.cols(),
					(int) trebleClef.rows());
		}
	}

	public static void detectTime(Mat sheet) {
		fourFour = Utils.resizeImage(fourFour, staveGap * 4);
		Mat result = new Mat();
		Imgproc.matchTemplate(sheet, fourFour, result, Imgproc.TM_CCOEFF);
		Point minLoc;
		double minVal = Core.minMaxLoc(result).minVal;
		double minAllowed = minVal * 0.95;
		while (minVal < minAllowed) {
			minLoc = Core.minMaxLoc(result).minLoc;
			minVal = Core.minMaxLoc(result).minVal;
			if (minLoc.x < staves.get(0).topLine().end().x * 0.95)
				fourFours.add(minLoc);
			Utils.zeroInMatrix(result, minLoc, (int) fourFour.cols(),
					(int) fourFour.rows());
		}
	}

	public static void detectFlats(Mat sheet) {
		flat_on = Utils.resizeImage(flat_on, staveGap);
		// for (Stave s : staves) {
		// Mat part = rotateSheetOnStave(sheet, s);
		// Point clef = Utils.getClefPoint(s, trebleClefs, staveGap);
		Mat result = new Mat();
		Imgproc.matchTemplate(sheet, flat_on, result, Imgproc.TM_CCOEFF);
		Point minLoc;
		double minVal = Core.minMaxLoc(result).minVal;
		double minAllowed = minVal * 0.9;
		while (minVal < minAllowed) {
			minLoc = Core.minMaxLoc(result).minLoc;
			minVal = Core.minMaxLoc(result).minVal;
			Log.v("Guillaume",
					Double.toString(minLoc.x) + "," + Double.toString(minLoc.y));
			// if (minLoc.x < staves.get(0).topLine().end().x * 0.95)
			flats.add(/*
					 * new Point(minLoc.x + clef.x, minLoc.y +
					 * s.topLine().start().y - 4 * staveGap)
					 */minLoc);
			Utils.zeroInMatrix(result, minLoc, (int) flat_on.cols(),
					(int) flat_on.rows());
		}
		// }
	}

	public static void detectBeams(Mat sheet) {
		Mat lines = new Mat();
		List<Line> allLines = new LinkedList<Line>();
		for (Stave s : staves) {
			Mat part = rotateSheetOnStave(sheet, s);
			Imgproc.erode(part, part, Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(6, 6)));
			Point clef = s.originalClef();
			Imgproc.HoughLinesP(part, lines, 1, Math.PI / 180, 100);
			for (int i = 0; i < lines.cols(); i++) {
				double[] data = lines.get(0, i);
				Point start = new Point(data[0] + clef.x, data[1]
						+ s.topLine().start().y - 4 * staveGap);
				Point end = new Point(data[2] + clef.x, data[3]
						+ s.topLine().start().y - 4 * staveGap);
				if (end.x - start.x > part.cols() / 30)
					allLines.add(new Line(start, end));
			}
		}
		Collections.sort(allLines, new Comparator<Line>() {

			@Override
			public int compare(Line lhs, Line rhs) {
				return (int) ((rhs.end().x - rhs.start().x) - (lhs.end().x - lhs
						.start().x));
			}

		});
		for (int i = 0; i < allLines.size(); i++) {
			Line l = allLines.get(i);
			if (!Utils.isThereASimilarLine(beams, l))
				beams.add(l);
		}
	}

	public static void detectHalfNotes(Mat sheet) {
		half_note = Utils.resizeImage(half_note, staveGap);
		for (Stave s : staves) {
			Mat part = rotateSheetOnStave(sheet, s);
			Point clef = s.originalClef();
			Mat result = new Mat();
			Imgproc.matchTemplate(part, half_note, result, Imgproc.TM_CCOEFF);
			Point minLoc;
			double minVal = Core.minMaxLoc(result).minVal;
			double minAllowed = minVal * 0.999;
			while (minVal < minAllowed) {
				minLoc = Core.minMaxLoc(result).minLoc;
				minVal = Core.minMaxLoc(result).minVal;
				Point p = new Point(minLoc.x + clef.x + half_note.cols() / 2,
						minLoc.y + s.topLine().start().y - 4 * staveGap
								+ half_note.rows() / 2);
				if (!Utils.isThereANoteAtThisPosition(p, notes, staves,
						staveGap))
					notes.add(new Note(p, 2, s));
				Utils.zeroInMatrix(result, minLoc, (int) half_note.cols(),
						(int) half_note.rows());
			}
		}
	}

	public static void detectNotes(Mat sheet) {
		Mat eroded = sheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(staveGap - 4, staveGap - 4)));
		noteHead = Utils.resizeImage(noteHead, staveGap * 0.9);
		noteWidth = noteHead.cols();
		for (Stave s : staves)
			detectNoteOnPart(sheet, eroded,
					(int) (s.topLine().start().y - 4 * staveGap), (int) (s
							.bottomLine().start().y + 4 * staveGap));
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
						&& !Utils.isOnBeamLine(centre, noteWidth, staveGap,
								beams)) {
					Point p = Utils.findNearestNeighbour(centre, ref);
					notes.add(new Note(p, Utils.whichStaveDoesAPointBelongTo(p,
							staves, staveGap)));
				}

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

	private static Mat rotateSheetOnStave(Mat sheet, Stave s) {
		Line l = s.topLine();
		Point clef = s.originalClef();
		double angleToRotate = (l.end().y - l.start().y)
				/ (l.end().x - l.start().x);
		Mat rotated = Utils.rotateMatrix(
				sheet.submat((int) (l.start().y - 4 * staveGap), (int) (s
						.bottomLine().start().y + 4 * staveGap), (int) clef.x,
						sheet.cols()), angleToRotate);
		return rotated;
	}

	public static void correctBeams(Mat revertSheet) {
		for (int i = 0; i < beams.size(); i++) {
			if (!Utils.isABeam(beams.get(i), notes, staveGap)) {
				beams.remove(i);
				i--;
			}
		}
		for (Line l : beams) {
			for (Note n : notes) {
				if (Math.abs(l.start().y - n.center().y) > 2 * staveGap
						&& Math.abs(l.start().y - n.center().y) < 4 * staveGap
						&& n.center().x > l.start().x - 20
						&& n.center().x < l.end().x + 20)
					n.setDuration(0.5);
			}
		}
	}

}
