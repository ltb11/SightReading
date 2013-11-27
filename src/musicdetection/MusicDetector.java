package musicdetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import musicrepresentation.Bar;
import musicrepresentation.Piece;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.sightreader.SightReaderActivity;

import utils.OurUtils;
import utils.SheetStrip;
import android.util.Log;

public class MusicDetector {

	public static final Mat masterTrebleClef = OurUtils.readImage(OurUtils
			.getPath("assets/GClef.png"));
	public static final Mat masterFourFour = OurUtils.readImage(OurUtils
			.getPath("assets/44.png"));
	public static final Mat masterFlat_inter = OurUtils.readImage(OurUtils
			.getPath("assets/flat_inter.png"));
	public static final Mat masterFlat_on = OurUtils.readImage(OurUtils
			.getPath("assets/flat_on.png"));
	public static final Mat masterHalf_note = OurUtils.readImage(OurUtils
			.getPath("assets/half_note.png"));
	public static final Mat masterHalf_note_on = OurUtils.readImage(OurUtils
			.getPath("assets/half_note_on.png"));
	public static final Mat masterWhole_note_on = OurUtils.readImage(OurUtils
			.getPath("assets/whole_note_on.png"));
	public static final Mat masterWhole_note = OurUtils.readImage(OurUtils
			.getPath("assets/whole_note.png"));

	private final List<Mat> master_half_notes = new LinkedList<Mat>();
	private final List<Mat> master_whole_notes = new LinkedList<Mat>();

	public static final int beamLengthTolerance = 30;

	private Mat trebleClef;
	private Mat fourFour;
	// private Mat flat_inter;
	private Mat flat_on;
	private Mat half_note;

	// For printing purposes only
	private int dotWidth;
	private int dotHeight;

	private final Mat workingSheet;
	private final double staveLengthTolerance = 0.1;
	private double staveGap;
	private double noteWidth;
	private List<Stave> staves = new LinkedList<Stave>();
	private List<Point> trebleClefs = new LinkedList<Point>();
	private List<Point> fourFours = new LinkedList<Point>();
	private List<Note> notes = new LinkedList<Note>();
	private List<Line> beams = new LinkedList<Line>();
	private List<Point> flats = new LinkedList<Point>();
	private Map<Point, Note> dots = new HashMap<Point, Note>();
	private List<Note> potentialNotes = new LinkedList<Note>();
	private int beamXTolerance = 4;
	private int beamYTolerance = 3;
	private int beamMinLength = 25;

	public MusicDetector(final Mat input) throws NoMusicDetectedException {
		workingSheet = preprocess(input.clone());
		master_half_notes.add(masterHalf_note);
		master_half_notes.add(masterHalf_note_on);
		master_whole_notes.add(masterWhole_note);
		master_whole_notes.add(masterWhole_note_on);
	}

	private Mat preprocess(Mat input) throws NoMusicDetectedException {
		// scale and threshold
		OurUtils.thresholdImage(input);

		// create projections
		Mat projection = input.clone();
		Mat proj = OurUtils.horizontalProjection(projection);
		OurUtils.writeImage(proj, OurUtils.getPath("output/fullProj.jpg"));
		LinkedList<Integer> divisions = OurUtils.detectDivisions(proj, 190);
		List<SheetStrip> strips = OurUtils.sliceSheet(input, divisions);

		// detect staves
		List<StaveLine> lines = new LinkedList<StaveLine>();
		for (SheetStrip strip : strips) {
			lines.addAll(strip.FindLines());
		}
		detectStaves(lines);

		OurUtils.invertColors(input);

		return input;
	}

	/*
	 * Order to respect: detectBeams detectNotes correctBeams sortNotes
	 * detectFlats detectQuavers
	 */
	public void detect() {
		long startTimeOfEachMethod = SightReaderActivity.startTime;
		long startTime = startTimeOfEachMethod;
		Log.v("Guillaume",
				"Start time of detection: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectTrebleClefs();
		Log.v("Guillaume",
				"Treble detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectTime();
		Log.v("Guillaume",
				"Time detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectNotes();
		Log.v("Guillaume",
				"Note detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectBeams();
		Log.v("Guillaume",
				"Beam detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectHalfNotes();
		Log.v("Guillaume",
				"Half-note detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		detectWholeNotes();
		sortNotes();
		startTimeOfEachMethod = System.currentTimeMillis();
		detectFlats();
		Log.v("Guillaume",
				"Flat detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectDots();
		Log.v("Guillaume", "Dot detection time: "
				+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectQuavers();
		Log.v("Guillaume",
				"Quavers detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis()
				- SightReaderActivity.startTime;
		Log.v("Guillaume",
				"Total time for detection: "
						+ (System.currentTimeMillis() - startTime));
	}

	public Piece toPiece() {

		List<Bar> bars = new LinkedList<Bar>();
		for (Stave s : staves) {
			s.orderNotes();
			s.calculateNotePitch();

			bars.addAll(s.toBars());
		}

		Piece piece = new Piece(bars);

		return piece;
	}

	private void detectTrebleClefs() {
		for (Stave s : staves) {
			Mat result = new Mat();
			trebleClef = OurUtils.resizeImage(masterTrebleClef,
					s.staveGap() * 8);
			Imgproc.matchTemplate(workingSheet.submat(
					s.yRange(workingSheet.rows()),
					new Range(0, workingSheet.cols())), trebleClef, result,
					Imgproc.TM_CCOEFF);
			Point minLoc = Core.minMaxLoc(result).minLoc;
			double minVal = Core.minMaxLoc(result).minVal;
			double minAllowed = minVal * 0.9;
			while (minVal < minAllowed) {
				Point p = new Point(minLoc.x, s.startYRange() + minLoc.y);
				trebleClefs.add(p);
				s.addClef(Clef.Treble, p, trebleClef.cols());
				OurUtils.zeroInMatrix(result, minLoc, (int) trebleClef.cols(),
						(int) trebleClef.rows());
				minLoc = Core.minMaxLoc(result).minLoc;
				minVal = Core.minMaxLoc(result).minVal;
			}
		}
	}

	private void detectDots() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(4, 4)));
		for (Stave s : staves) {
			for (Note n : s.notes()) {
				List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
				Imgproc.findContours(eroded.submat((int) Math.max(0,
						n.center().y - staveGap), Math.min(eroded.rows(),
						(int) (n.center().y + staveGap / 2)), (int) Math.max(0,
						n.center().x + noteWidth), Math.min(eroded.cols(),
						(int) (n.center().x + 2 * noteWidth))), contours,
						new Mat(), Imgproc.RETR_LIST,
						Imgproc.CHAIN_APPROX_SIMPLE);
				List<Moments> mu = new ArrayList<Moments>(contours.size());
				for (int i = 0; i < contours.size(); i++) {
					mu.add(i, Imgproc.moments(contours.get(i), false));
					Moments m = mu.get(i);
					double x = (m.get_m10() / m.get_m00());
					double y = (m.get_m01() / m.get_m00());
					Point p = new Point(x + n.center().x + noteWidth, y
							+ n.center().y - staveGap);
					double difference = Math.abs(p.y - s.getTopYAtPos(p));
					difference %= s.staveGapAtPos(p);
					difference -= s.staveGapAtPos(p) / 2;
					difference = Math.abs(difference);
					if (difference < 3) {
						Rect r = Imgproc.boundingRect(contours.get(i));
						dotWidth = r.width;
						dotHeight = r.height;
						dots.put(p, n);
						break;
					}
				}
			}
		}
	}

	private void detectQuavers() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(staveGap / 3, staveGap / 3)));
		for (Stave s : staves) {
			for (Note n : s.notes()) {
				if (n.duration() != 1)
					continue;
				Mat region = eroded
						.submat(new Range((int) Math.max(0, n.center().y - 4
								* staveGap), (int) Math.max(0, n.center().y - 2
								* staveGap)),
								new Range((int) Math.min(eroded.cols(),
										n.center().x + noteWidth / 2),
										(int) Math.min(eroded.cols(),
												n.center().x + 3 * noteWidth
														/ 2)));
				List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
				Imgproc.findContours(region, contours, new Mat(),
						Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
				for (int i = 0; i < contours.size(); i++) {
					if (contours.get(i).rows() > 2 * staveGap / 3)
						n.setDuration(n.duration() / 2);
				}
			}
		}
	}

	private void detectTime() {
		fourFour = OurUtils.resizeImage(masterFourFour, staveGap * 4);
		Mat result = new Mat();
		List<Point> points = new ArrayList<Point>();
		List<Double> values = new ArrayList<Double>();
		double minAllowed;
		for (Stave s : staves) {
			Imgproc.matchTemplate(workingSheet.submat(
					s.yRange(workingSheet.rows()),
					new Range(0, workingSheet.cols())), fourFour, result,
					Imgproc.TM_CCOEFF);
			Point minLoc;
			double minVal = Core.minMaxLoc(result).minVal;
			minAllowed = minVal * 0.95;
			while (minVal < minAllowed) {
				minLoc = Core.minMaxLoc(result).minLoc;
				points.add(new Point(minLoc.x, s.startYRange() + minLoc.y));
				values.add(minVal);
				OurUtils.zeroInMatrix(result, minLoc, (int) fourFour.cols(),
						(int) fourFour.rows());
				minVal = Core.minMaxLoc(result).minVal;
			}
		}
		minAllowed = Collections.min(values) * 0.95;
		for (int i = 0; i < points.size(); i++) {
			if (values.get(i) < minAllowed) {
				fourFours.add(points.get(i));
				OurUtils.whichStaveDoesAPointBelongTo(points.get(i), staves,
						workingSheet.rows()).addTime(Time.FourFour,
						points.get(i), fourFour.cols());
			}
		}
	}

	private void detectFlats() {
		flat_on = OurUtils.resizeImage(masterFlat_on, staveGap);
		Mat result = new Mat();
		for (Stave s : staves) {
			for (Note n : s.notes()) {
				flat_on = OurUtils.resizeImage(masterFlat_on,
						s.staveGapAtPos(n.center()));
				// Not up to n.center().x because of half-notes that are
				// detected as flat if too close
				Imgproc.matchTemplate(workingSheet.submat(Math.max(0,
						(int) (n.center().y - staveGap)), Math.min(
						workingSheet.rows(), (int) (n.center().y + staveGap)),
						Math.max(0, (int) (n.center().x - 3 * noteWidth)), Math
								.min(workingSheet.cols(),
										(int) (n.center().x - noteWidth / 2))),
						flat_on, result, Imgproc.TM_CCOEFF_NORMED);
				Point minLoc;
				if (Core.minMaxLoc(result).minVal < -0.4) {
					minLoc = Core.minMaxLoc(result).minLoc;
					Point p = new Point(
							minLoc.x + n.center().x - 3 * noteWidth, minLoc.y
									+ n.center().y - staveGap);
					if (!OurUtils.isThereANoteAtThisPosition(p, OurUtils
							.whichStaveDoesAPointBelongTo(p, staves,
									workingSheet.rows())))
						flats.add(p);
					OurUtils.zeroInMatrix(result, minLoc, (int) flat_on.cols(),
							(int) flat_on.rows());
				}
			}
		}
	}

	// TODO: Change the order to detectNotes -> detectBeams -> prune wrongly
	// detected notes
	// TODO: Get the previous rotateOnSheet algorithm and combine both of them
	// TODO: Create big lines from consecutive small lines
	private void detectBeams() {
		Log.d("Guillaume", "staveGap: " + staveGap);
		List<Line> allLines = new LinkedList<Line>();
		Mat part = workingSheet.clone();
		Imgproc.erode(part, part, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(8, 8)));
		for (Stave s : staves) {
			Mat partRot = rotateSheetOnStave(s);
			Imgproc.erode(partRot, partRot, Imgproc.getStructuringElement(
					Imgproc.MORPH_RECT, new Size(6, 6)));
			Point startDetection = s.startDetection();
			Mat partNorm = part.submat((s.yRange(workingSheet.rows())),
					s.xRange());
			/*
			 * OurUtils.writeImage(part.clone(),
			 * OurUtils.getPath("output/erodedSW_" + s.startYRange()) + ".jpg");
			 */

			/*
			 * for (int colLength = 1500; colLength > 0; colLength -= 400) { for
			 * (int cols = 0; cols < partNorm.cols() - colLength; cols +=
			 * colLength) allLines.addAll(extractBeams(startDetection, partNorm,
			 * cols, colLength)); for (int cols = 0; cols < partRot.cols() -
			 * colLength; cols += colLength)
			 * allLines.addAll(extractBeams(startDetection, partRot, cols,
			 * colLength)); }
			 */

			allLines.addAll(extractBeams(startDetection, partNorm, 0,
					partNorm.cols()));
			allLines.addAll(extractBeams(startDetection, partRot, 0,
					partRot.cols()));
		}

		/*
		 * int cutLength = 30; for (int angle = -20; angle <= 20; angle += 5) {
		 * Log.d("Guillaume", "" + angle); for (int cut = (int)
		 * startDetection.x; cut < s.xRange().end - cutLength; cut += cutLength)
		 * { int height = s.yRange(part.rows()).size(); Mat tmp =
		 * OurUtils.rotateMatrix( part.clone().submat(s.yRange(part.rows()), new
		 * Range(0, (int) (Math.max(0, cutLength * Math.cos(angle)) +
		 * Math.max(0, height * Math.sin(angle)))), angle); Mat proj = new
		 * Mat(); Core.reduce(tmp, proj, 1, Core.REDUCE_AVG, CvType.CV_32S); if
		 * (cut >= 1630 && cut < 1630 + cutLength && s.topLine().start().y >
		 * 1500 && s.topLine().start().y < 1700 && angle == -20) {
		 * Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2BGR);
		 * Core.rectangle(tmp, new Point(0, 0), new Point(tmp.cols() - 1,
		 * tmp.rows() - 1), new Scalar(0, 0, 255)); OurUtils.writeImage(proj,
		 * OurUtils.getPath("output/proj.jpg")); OurUtils.writeImage(tmp,
		 * OurUtils.getPath("output/tmp.jpg")); OurUtils.writeImage(
		 * workingSheet.clone().submat( s.yRange(part.rows()), new Range(cut,
		 * cut + cutLength)), OurUtils.getPath("output/orig.jpg")); } int
		 * lastValidStart = 0; int minThickness = 4; for (int i = 0; i <
		 * proj.rows(); i++) { int[] v = new int[3]; proj.get(i, 0, v); if (i >=
		 * lastValidStart + minThickness) { Point start = new Point(cut,
		 * s.startYRange() + lastValidStart + minThickness / 2); Point end = new
		 * Point(cut + cutLength - 1, (s.startYRange() + lastValidStart +
		 * minThickness / 2 + cutLength Math.tan(angle))); Log.d("Guillaume",
		 * "Added new line: " + start.x + "," + start.y + " / " + end.x + "," +
		 * end.y); allLines.add(new Line(start.clone(), end.clone()));
		 * lastValidStart = i; } if (v[0] < 240) lastValidStart = i; else if
		 * (s.topLine().start().y > 1400 && s.topLine().start().y < 1800 && cut
		 * >= 2400 && cut < 2430 && (i % 5) == 0) Log.d("Guillaume", "" + v[0]);
		 * } } }
		 * 
		 * }
		 */
		Collections.sort(allLines, new Comparator<Line>() {

			@Override
			public int compare(Line lhs, Line rhs) {
				return (int) (lhs.start().x - rhs.start().x);
			}

		});
		List<Line> joinedLines = new LinkedList<Line>();
		for (int i = 0; i < allLines.size(); i++) {
			Line first = allLines.get(i);
			for (int j = i + 1; j < allLines.size(); j++) {
				Line second = allLines.get(j);
				if (Math.abs(first.end().x - second.start().x) < beamXTolerance
						&& Math.abs(first.end().y - second.start().y) < beamYTolerance) {
					first = new Line(first.start(), second.end());
					allLines.remove(j);
				}
				if (first.end().x + 10 < second.start().x)
					break;
			}
			if (first.length > beamMinLength
					&& first.length < workingSheet.cols() / 6)
				joinedLines.add(first);
			allLines.remove(i);
			i--;
		}
		allLines = joinedLines;
		Log.d("Guillaume", "@sort, allLines.size: " + allLines.size());
		Collections.sort(allLines, new Comparator<Line>() {

			@Override
			public int compare(Line lhs, Line rhs) {
				return (int) (Math.sqrt((Math.pow(rhs.end().x - rhs.start().x,
						2) - Math.pow(rhs.end().y - rhs.start().y, 2))) - Math
						.sqrt((Math.pow(lhs.end().x - lhs.start().x, 2) - Math
								.pow(lhs.end().y - lhs.start().y, 2))));
			}

		});
		for (int i = 0; i < allLines.size(); i++) {
			Line l = allLines.get(i);
			Stave s = OurUtils.whichStaveDoesAPointBelongTo(l.start(), staves,
					workingSheet.rows());
			if (!OurUtils.isThereASimilarLine(beams, l)
			/* && OurUtils.isABeam(l, s) */) {
				beams.add(l);
				for (int j = 0; j < s.notes().size(); j++) {
					Note n = s.notes().get(j);
					if (OurUtils.isOnBeamLine(n.center(), noteWidth, staveGap,
							l)) {
						notes.remove(n);
						s.removeNote(n);
						j--;
					}
				}
			}
		}
		Log.d("Guillaume", "@potentialNotes, beams.size: " + beams.size());
		for (int j = 0; j < potentialNotes.size(); j++) {
			Note n = potentialNotes.get(j);
			Stave potentialStave = OurUtils.whichStaveDoesAPointBelongTo(
					n.center(), staves, workingSheet.rows());
			if (!OurUtils
					.isThereANoteAtThisPosition(n.center(), potentialStave)
					&& !OurUtils.isOnBeamLine(n.center(), noteWidth, staveGap,
							beams)) {
				potentialNotes.remove(n);
				notes.add(n);
				potentialStave.addNote(n);
				j--;
			}
		}

		for (Line l : beams) {
			Stave s = OurUtils.whichStaveDoesAPointBelongTo(l.start(), staves,
					workingSheet.rows());
			for (Note n : s.notes()) {
				if (n.center().x > l.start().x - beamLengthTolerance
						&& n.center().x < l.end().x + beamLengthTolerance) {
					n.setDuration(n.duration() / 2);
				}
			}
		}
	}

	private List<Line> extractBeams(Point startDetection, Mat part, int cols,
			int colLength) {
		List<Line> allLines = new LinkedList<Line>();
		Mat lines = new Mat();
		Imgproc.HoughLinesP(part.submat(0, part.rows(), cols, cols + colLength)
				.clone(), lines, 0.5, Math.PI / 360, 100);
		for (int i = 0; i < lines.cols(); i++) {
			double[] data = lines.get(0, i);
			Point start = new Point(data[0] + startDetection.x + cols, data[1]
					+ startDetection.y);
			Point end = new Point(data[2] + startDetection.x + cols, data[3]
					+ startDetection.y);
			Line potentialLine = new Line(start, end);
			Line correctedLine = OurUtils.correctLine(potentialLine, part,
					staveGap);
			double lengthX = correctedLine.length;
			if (lengthX < part.cols() / 6)
				allLines.add(correctedLine);
		}
		return allLines;
	}

	private void detectHalfNotes() {
		for (Mat m : master_half_notes) {
			half_note = OurUtils.resizeImage(m, staveGap);
			for (Stave s : staves) {
				Mat result = new Mat();
				Imgproc.matchTemplate(
						workingSheet.submat(s.yRange(workingSheet.rows()),
								s.xRange()), half_note, result,
						Imgproc.TM_CCOEFF);
				Point minLoc;
				double minVal = Core.minMaxLoc(result).minVal;
				double minAllowed = minVal * 0.999;
				while (minVal < minAllowed) {
					minLoc = Core.minMaxLoc(result).minLoc;
					minVal = Core.minMaxLoc(result).minVal;
					Point p = new Point(s.startDetection().x + minLoc.x
							+ half_note.cols() / 2, minLoc.y + s.startYRange()
							+ half_note.rows() / 2);
					if (!OurUtils.isThereANoteAtThisPosition(p, s)) {
						Note n = new Note(p, 2);
						notes.add(n);
						s.addNote(n);
					}
					OurUtils.zeroInMatrix(result, minLoc,
							(int) half_note.cols(), (int) half_note.rows());
				}
			}
		}
	}

	private void detectWholeNotes() {
		for (Mat m : master_whole_notes) {
			half_note = OurUtils.resizeImage(m, staveGap);
			for (Stave s : staves) {
				Mat result = new Mat();
				Imgproc.matchTemplate(
						workingSheet.submat(s.yRange(workingSheet.rows()),
								s.xRange()), half_note, result,
						Imgproc.TM_CCOEFF_NORMED);
				Point minLoc;
				// double minVal = Core.minMaxLoc(result).minVal;
				// double minAllowed = minVal * 0.999;
				while (Core.minMaxLoc(result).minVal < -0.6) {
					minLoc = Core.minMaxLoc(result).minLoc;
					// minVal = Core.minMaxLoc(result).minVal;
					Point p = new Point(minLoc.x + s.startDetection().x
							+ half_note.cols() / 2, minLoc.y + s.startYRange()
							+ half_note.rows() / 2);
					if (!OurUtils.isThereANoteAtThisPosition(p, s)) {
						Note n = new Note(p, 4);
						notes.add(n);
						s.addNote(n);
					}
					OurUtils.zeroInMatrix(result, minLoc,
							(int) half_note.cols(), (int) half_note.rows());
				}
			}
		}
	}

	private void detectNotes() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT,
				new Size(3 * staveGap / 4, 3 * staveGap / 4)));
		OurUtils.writeImage(eroded, OurUtils.getPath("output/eroded.png"));
		noteWidth = staveGap * 7 / 6;
		for (Stave s : staves)
			detectNoteOnPart(eroded, s);
	}

	private void detectNoteOnPart(Mat ref, Stave s) {
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		Imgproc.findContours(
				ref.submat(s.yRange(workingSheet.rows()), s.xRange()),
				contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Collections.sort(contours, new Comparator<MatOfPoint>() {

			@Override
			public int compare(MatOfPoint lhs, MatOfPoint rhs) {
				Rect r1 = Imgproc.boundingRect(lhs);
				Rect r2 = Imgproc.boundingRect(rhs);
				return (r2.width - r1.width);
			}
		});
		List<Moments> mu = new ArrayList<Moments>(contours.size());
		for (int i = 0; i < contours.size(); i++) {
			mu.add(i, Imgproc.moments(contours.get(i), false));
			Moments m = mu.get(i);
			Note potentialNote = null;
			if (m.get_m00() != 0) {
				double x = (m.get_m10() / m.get_m00()) + s.startDetection().x;
				double y = (m.get_m01() / m.get_m00()) + s.startYRange();
				potentialNote = new Note(new Point(x, y), 1);
			} else {
				Rect r = Imgproc.boundingRect(contours.get(i));
				potentialNote = new Note(new Point(r.x + r.width / 2
						+ s.startDetection().x, r.y + r.height / 2
						+ s.startYRange()), 1);
			}
			if (!OurUtils.isInAnyRectangle(trebleClefs, trebleClef.cols(),
					trebleClef.rows(), potentialNote.center())
					&& !OurUtils.isInAnyRectangle(fourFours, fourFour.cols(),
							fourFour.rows(), potentialNote.center())
					&& potentialNote.center().x < s.topLine().end().x * 0.98) {
				if (!OurUtils.isThereANoteAtThisPosition(
						potentialNote.center(), s)) {
					notes.add(potentialNote);
					s.addNote(potentialNote);
				} else
					potentialNotes.add(potentialNote);
			}
		}
	}

	private void detectStaves(List<StaveLine> lines)
			throws NoMusicDetectedException {
		// sort them by length (longest first)
		Collections.sort(lines, new Comparator<StaveLine>() {
			@Override
			public int compare(StaveLine line0, StaveLine line1) {
				return (int) (Math.signum(line1.toLine().length()
						- line0.toLine().length()));
			}
		});

		int outside, inside;
		for (outside = 0; outside < lines.size(); outside++) {
			Line start = lines.get(outside).toLine();

			// TODO: !!!! this line may be causing problems, not sure
			if (start.length() < OurUtils.STANDARD_IMAGE_WIDTH * 0.5)
				break;

			List<StaveLine> subset = new LinkedList<StaveLine>();

			for (inside = outside; inside < lines.size(); inside++) {
				StaveLine line = lines.get(inside);
				if (Math.abs(start.length() - line.toLine().length()) < start
						.length() * staveLengthTolerance
						&& start.start().x <= line.toLine().start().x + 10
						&& start.end().x >= line.toLine().end().x - 10) {
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
			List<StaveLine> staveLines = OurUtils.getSpacedLines(subset, lines);
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
		if (staves.size() == 0)
			throw new NoMusicDetectedException();

		staveGap = staves.get(0).staveGap();
	}

	private Mat rotateSheetOnStave(Stave s) {
		Line l = s.topLine();
		double angleToRotate = Math.atan((l.end().y - l.start().y)
				/ (l.end().x - l.start().x));
		Mat rotated = OurUtils.rotateMatrix(
				workingSheet.clone().submat(s.yRange(workingSheet.rows()),
						s.xRange()), angleToRotate);
		return rotated;
	}

	private void correctBeams() {
		for (int i = 0; i < beams.size(); i++) {
			Stave stave = OurUtils.whichStaveDoesAPointBelongTo(beams.get(i)
					.start(), staves, workingSheet.rows());
			if (!OurUtils.isABeam(beams.get(i), stave)) {
				beams.remove(i);
				i--;
			} else {
				for (Note n : stave.notes()) {
					if (n.center().x > beams.get(i).start().x
							- beamLengthTolerance
							&& n.center().x < beams.get(i).end().x
									+ beamLengthTolerance)
						n.setDuration(n.duration() / 2);
				}
			}
		}
	}

	public void print(Mat sheet) {
		printStaves(sheet);
		printFourFour(sheet);
		printTreble(sheet);
		printNotes(sheet);
		printFlats(sheet);
		printDots(sheet);
		printBeams(sheet);
		printScale(sheet);
	}

	private void printScale(Mat sheet) {
		for (int i = 0; i < sheet.rows(); i += 100) {
			Core.line(sheet, new Point(30, i), new Point((i % 1000 == 0) ? 60
					: (i % 500 == 0) ? 50 : 40, i), new Scalar(0, 0, 255),
					(i % 1000 == 0) ? 10 : (i % 500 == 0) ? 8 : 5);
		}
		for (int j = 0; j < sheet.cols(); j += 100) {
			int rows = sheet.rows();
			Core.line(sheet, new Point(j, 20), new Point(j,
					(j % 1000 == 0) ? 60 : (j % 500 == 0) ? 50 : 40),
					new Scalar(0, 0, 255), (j % 1000 == 0) ? 10
							: (j % 500 == 0) ? 8 : 5);
			Core.line(sheet, new Point(j, rows - 20), new Point(j, rows
					- ((j % 1000 == 0) ? 60 : (j % 500 == 0) ? 50 : 40)),
					new Scalar(0, 0, 255), (j % 1000 == 0) ? 10
							: (j % 500 == 0) ? 8 : 5);
		}
	}

	private void sortNotes() {
		for (Stave s : staves)
			s.orderNotes();
	}

	private void printStaves(Mat sheet) {
		for (Stave s : staves) {
			s.drawDetailed(sheet);
		}
	}

	private void printBeams(Mat sheet) {
		for (Line l : beams) {
			Core.line(sheet, l.start(), l.end(), new Scalar(0, 255, 255), 4);
		}
	}

	private void printTreble(Mat sheet) {
		for (Point p : trebleClefs)
			Core.rectangle(sheet, p, new Point(p.x + trebleClef.cols(), p.y
					+ trebleClef.rows()), new Scalar(0, 255, 0), 3);
	}

	private void printFourFour(Mat sheet) {
		for (Point p : fourFours)
			Core.rectangle(sheet, p, new Point(p.x + fourFour.cols(), p.y
					+ fourFour.rows()), new Scalar(255, 255, 0), 3);
	}

	private void printNotes(Mat sheet) {
		for (Note n : notes)
			Core.circle(
					sheet,
					n.center(),
					(int) (staveGap / 2),
					n.duration() == 1. ? new Scalar(0, 0, 255)
							: n.duration() == 0.5 ? new Scalar(255, 0, 255)
									: n.duration() == 2.0 ? new Scalar(128,
											128, 0)
											: n.duration() == 0.25 ? new Scalar(
													128, 0, 128) : new Scalar(
													0, 127, 255), -1);
	}

	private void printFlats(Mat sheet) {
		for (Point p : flats)
			Core.rectangle(sheet, p, new Point(p.x + flat_on.cols(), p.y
					+ flat_on.rows()), new Scalar(255, 0, 127), 4);
	}

	private void printDots(Mat sheet) {
		for (Point p : dots.keySet())
			Core.rectangle(sheet, new Point(p.x - dotWidth / 2, p.y - dotHeight
					/ 2), new Point(p.x + dotWidth / 2, p.y + dotHeight / 2),
					new Scalar(0, 255, 128), 6);
	}

}
