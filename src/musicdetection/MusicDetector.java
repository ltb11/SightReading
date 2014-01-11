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

import utils.Interval;
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
	public static final Mat masterSharp_on = OurUtils.readImage(OurUtils
			.getPath("assets/sharp_on.png"));
	public static final Mat masterSharp = OurUtils.readImage(OurUtils
			.getPath("assets/sharp.png"));
	public static final Mat masterNatural_on = OurUtils.readImage(OurUtils
			.getPath("assets/Natural_on.png"));
	public static final Mat masterHalf_note = OurUtils.readImage(OurUtils
			.getPath("assets/half_note.png"));
	public static final Mat masterHalf_note_on = OurUtils.readImage(OurUtils
			.getPath("assets/half_note_on.png"));
	public static final Mat masterWhole_note_on = OurUtils.readImage(OurUtils
			.getPath("assets/whole_note_on.png"));
	public static final Mat masterWhole_note = OurUtils.readImage(OurUtils
			.getPath("assets/whole_note.png"));
	private static final Mat masterQuaverRest = OurUtils.readImage(OurUtils
			.getPath("assets/quaver_rest.png"));
	private static final Mat masterNoteRest = OurUtils.readImage(OurUtils
			.getPath("assets/note_rest.png"));

	private final List<Mat> master_half_notes = new LinkedList<Mat>();
	private final List<Mat> master_whole_notes = new LinkedList<Mat>();

	public static final int beamLengthTolerance = 30;
	public static final int noteMinDistance = 25;

	private static final int beamToNoteTolerance = 25;
	private static final int beamJoinTolerance = 8;
	private static final int beamHorizontalThresholdTolerance = 6;

	private Mat trebleClef;
	private Mat fourFour;
	// private Mat flat_inter;
	private Mat flat_on;
	private Mat sharp_on;
	private Mat sharp = masterSharp;
	private Mat natural_on;
	private Mat half_note;
	private Mat quaverRest;
	private Mat noteRest;

	// For printing purposes only
	private int dotWidth;
	private int dotHeight;

	private final Mat workingSheet;
	private Mat output;
	private final double staveLengthTolerance = 0.1;
	private double staveGap;
	private double noteWidth;
	private List<Stave> staves = new LinkedList<Stave>();
	private List<Point> trebleClefs = new LinkedList<Point>();
	private List<Point> fourFours = new LinkedList<Point>();
	private List<Note> notes = new LinkedList<Note>();
	private List<Line> beams = new LinkedList<Line>();
	private List<Point> flats = new LinkedList<Point>();
	private List<Point> sharps = new LinkedList<Point>();
	private List<Point> naturals = new LinkedList<Point>();
	private List<Point> quaverRests = new LinkedList<Point>();
	private List<Point> noteRests = new LinkedList<Point>();

	private Map<Point, Note> dots = new HashMap<Point, Note>();

	/**
	 * Initialises music detector object and throws error if the preprocessing
	 * of the image fails
	 **/
	public MusicDetector(final Mat input) throws NoMusicDetectedException {
		workingSheet = preprocess(input);
		Log.d("Guillaume", "After preprocessing, width: " + workingSheet.cols());
		master_half_notes.add(masterHalf_note);
		master_half_notes.add(masterHalf_note_on);
		master_whole_notes.add(masterWhole_note);
		master_whole_notes.add(masterWhole_note_on);
	}

	/**
	 * Runs basic image processing such as thresholding on the image, and then
	 * attempts to detect staves. </br>If an image can't pass this step we throw
	 * a NoMusicDetectedException
	 **/
	private Mat preprocess(Mat rawInput) throws NoMusicDetectedException {
		Log.i("TEST", "" + rawInput.type());
		Mat input = OurUtils.resizeImage(rawInput,
				OurUtils.STANDARD_IMAGE_WIDTH);
		output = input.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		// scale and threshold
		OurUtils.thresholdImage(input);

		// prepped image
		OurUtils.writeImage(input, OurUtils.getPath("temp/PREPROC.png"));
		
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

		Log.i("PROC", "detecting staves");
		detectStaves(lines);

		OurUtils.invertColors(input);

		return input;
	}

	/*
	 * Order to respect: detectBeams detectNotes correctBeams sortNotes
	 * detectFlats detectQuavers
	 */
	public void detect() {
		long startTimeOfEachMethod = System.currentTimeMillis();
		long startTime = startTimeOfEachMethod;
		Log.v("Guillaume",
				"Start time of detection: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));

		Log.i("PROC", "detecting clefs");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectTrebleClefs();
		Log.v("Guillaume",
				"Treble detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		Log.i("PROC", "detecting time sig");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectTime();
		Log.v("Guillaume",
				"Time detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));

		Log.i("PROC", "detecting notes");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectNotes();
		Log.v("Guillaume",
				"Note detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));

		Log.i("PROC", "detecting flats");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectAccidentals();
		Log.v("Guillaume",
				"Accidental detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		Log.i("PROC", "detecting whole notes");
		// detectWholeNotes();
		sortNotes();

		Log.i("PROC", "detecting beams");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectBeams();
		Log.v("Guillaume",
				"Beam detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		Log.i("PROC", "detecting half notes");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectHalfNotes();
		Log.v("Guillaume",
				"Half-note detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		Log.i("PROC", "detecting quavers");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectQuavers();
		Log.v("Guillaume",
				"Quavers detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		startTimeOfEachMethod = System.currentTimeMillis();
		detectQuaverRests();
		Log.v("Guillaume",
				"Quaver Rests detection time: "
						+ (System.currentTimeMillis() - startTimeOfEachMethod));
		sortNotes();
		Log.i("PROC", "detecting dots");
		startTimeOfEachMethod = System.currentTimeMillis();
		detectDots();
		Log.v("Guillaume", "Dot detection time: "
				+ (System.currentTimeMillis() - startTimeOfEachMethod));

		Log.i("PROC", "detection complete");
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
		/*
		 * commented code in this method allows for the detection of multiple
		 * clefs per line
		 */
		for (Stave s : staves) {
			Mat result = new Mat();
			trebleClef = OurUtils.resizeImage(masterTrebleClef,
					s.staveGap() * 8);
			Mat clefArea = workingSheet.submat(s.yRange(workingSheet.rows()),
					new Range(0, workingSheet.cols() / 5));
			Imgproc.matchTemplate(clefArea, trebleClef, result,
					Imgproc.TM_CCOEFF);
			Point minLoc = Core.minMaxLoc(result).minLoc;
			/*
			 * double minVal = Core.minMaxLoc(result).minVal; double minAllowed
			 * = minVal * 0.9; while (minVal < minAllowed) {
			 */
			Point p = new Point(minLoc.x, s.startYRange() + minLoc.y);
			trebleClefs.add(p);
			s.addClef(Clef.Treble, p, trebleClef.cols());
			/*
			 * OurUtils.zeroInMatrix(result, minLoc, (int) trebleClef.cols(),
			 * (int) trebleClef.rows()); minLoc = Core.minMaxLoc(result).minLoc;
			 * minVal = Core.minMaxLoc(result).minVal; }
			 */
		}
	}

	private void detectDots() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(4, 4)));
		OurUtils.writeImage(eroded, OurUtils.getPath("output/dotsEroding.jpg"));
		for (Stave s : staves) {
			List<Note> notes = s.notes();
			for (int j = 0; j < notes.size(); j++) {
				Note n = notes.get(j);
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
					Rect r = Imgproc.boundingRect(contours.get(i));
					if (r.height > staveGap / 2)
						break;
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
						Note next = null;
						boolean truePositive = true;
						int count = j + 1;
						if (count < notes.size())
							next = notes.get(count);
						if (next != null) {
							do {
								next = notes.get(count);
								if (Math.abs(next.center().x - p.x) < 20)
									truePositive = false;
								count++;
							} while (count < notes.size()
									&& Math.abs(next.center().x - n.center().x) < 100);
						}
						if (truePositive
								&& !OurUtils.isInAnyRectangle(flats,
										flat_on.width(), flat_on.height(), p)
								&& !OurUtils.isInAnyRectangle(quaverRests,
										quaverRest.width(),
										quaverRest.height(), p)
								&& !OurUtils.isInAnyRectangle(sharps,
										sharp.width(), sharp.height(), p)) {
							//Rect r = Imgproc.boundingRect(contours.get(i));
							dotWidth = r.width;
							dotHeight = r.height;
							dots.put(p, n);
							break;
						}
					}
				}
			}
		}
	}

	private void detectQuavers() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(1, staveGap / 3)));
		OurUtils.writeImage(eroded,
				OurUtils.getPath("output/quaverEroding.jpg"));
		for (Stave s : staves) {
			for (Note n : s.notes()) {
				if (n.duration() != 1)
					continue;
				Mat region = eroded
						.submat(new Range((int) Math.max(0, n.center().y - 3
								* staveGap), (int) Math.max(0, n.center().y - 1
								* staveGap)),
								new Range((int) Math.min(eroded.cols(),
										n.center().x + noteWidth),
										(int) Math.min(eroded.cols(),
												n.center().x + 3 * noteWidth
														/ 2)));
				if (n.center().x < 300 && n.center().y < 1000)
					OurUtils.writeImage(region, OurUtils.getPath("output/quaverSection.jpg"));
				if (n.center().x > 1650 && n.center().y < 1300 && n.center().y > 1000)
					OurUtils.writeImage(region, OurUtils.getPath("output/quaverSectionOK.jpg"));
				List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
				Imgproc.findContours(region, contours, new Mat(),
						Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
				for (int i = 0; i < contours.size(); i++) {
					if (contours.get(i).rows() > staveGap / 2)
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
		Stave s = staves.get(0);
		Point clef = trebleClefs.get(0);
		Mat timeArea = workingSheet.submat(s.yRange(workingSheet.rows()),
				new Range((int) clef.x + 70, (int) clef.x + 250));
		Log.d("Guillaume", "Stave gap: " + staveGap);
		Log.d("Guillaume", fourFour.width() + "," + fourFour.height() + "/" + timeArea.width() + "," + timeArea.height());
		Imgproc.matchTemplate(timeArea, fourFour, result, Imgproc.TM_CCOEFF);
		Point minLoc;
		double minVal = Core.minMaxLoc(result).minVal;
		minAllowed = minVal * 0.95;
		while (minVal < minAllowed) {
			minLoc = Core.minMaxLoc(result).minLoc;
			points.add(new Point(minLoc.x + clef.x + 70, s.startYRange()
					+ minLoc.y));
			values.add(minVal);
			OurUtils.zeroInMatrix(result, minLoc, (int) fourFour.cols(),
					(int) fourFour.rows());
			minVal = Core.minMaxLoc(result).minVal;
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

	/**
	 * Returns a submatrix to the left of a given note bounded in the y
	 * direction by n.y plus or minus the stave gap and in the x direction
	 * between 3 noteWidths and half a noteWidth from n.x
	 */
	private Mat getAccidentalArea(Note n) {
		double nx = n.center().x;
		double ny = n.center().y;
		int rowStart = Math.max(0, (int) (ny - staveGap));
		int rowEnd = Math.min(workingSheet.rows(), (int) (ny + staveGap));
		int colStart = Math.max(0, (int) (nx - 3 * noteWidth));
		int colEnd = Math.min(workingSheet.cols(), (int) (nx - noteWidth / 2));
		return workingSheet.submat(rowStart, rowEnd, colStart, colEnd);
	}

	/**
	 * Iterate through every note on every stave, and for each note calculate
	 * the area to the left of the note where an accidental is likely to be
	 * found
	 * 
	 */
	private void detectAccidentals() {
		for (Stave s : staves) {
			for (Note n : s.notes()) {
				Mat accidentalArea = getAccidentalArea(n);
				detectFlats(accidentalArea, n, s);
				//detectSharps(accidentalArea, n, s);
				//detectNaturals(accidentalArea,n);
			}
		}
	}
	
	

	private void detectFlats(Mat accidentalArea, Note n, Stave s) {
		Mat result = new Mat();
		flat_on = OurUtils.resizeImage(masterFlat_on,
				s.staveGapAtPos(n.center()));
		// Not up to n.center().x because of half-notes that are
		// detected as flat if too close
		Imgproc.matchTemplate(accidentalArea, flat_on, result,
				Imgproc.TM_CCOEFF_NORMED);
		Point minLoc;
		if (Core.minMaxLoc(result).minVal < -0.4) {
			minLoc = Core.minMaxLoc(result).minLoc;
			/*
			 * Point p is minLoc in the coordinate system of the original image,
			 * not the accidentalArea
			 */
			Point p = new Point(minLoc.x + n.center().x - 3 * noteWidth,
					minLoc.y + n.center().y - staveGap);
			if (!OurUtils.isThereANoteAtThisPosition(p, OurUtils
					.whichStaveDoesAPointBelongTo(p, staves,
							workingSheet.rows())))
				flats.add(p);
			OurUtils.zeroInMatrix(result, minLoc, (int) flat_on.cols(),
					(int) flat_on.rows());
		}
	}

	private void detectSharps(Mat accidentalArea, Note n, Stave s) {
		Mat result = new Mat();
		sharp = OurUtils.resizeImage(masterSharp,
				s.staveGapAtPos(n.center()) * 2);
		sharp_on = OurUtils.resizeImage(masterSharp_on,
				s.staveGapAtPos(n.center()) * 2);
		OurUtils.writeImage(sharp_on, OurUtils.getPath("output/sharp_on.jpg"));
		// Not up to n.center().x because of half-notes that are
		// detected as flat if too close
		Imgproc.matchTemplate(accidentalArea, sharp_on, result,
				Imgproc.TM_CCOEFF_NORMED);
		Point minLoc;
		if (Core.minMaxLoc(result).minVal < -0.4) {
			minLoc = Core.minMaxLoc(result).minLoc;
			/*
			 * Point p is minLoc in the coordinate system of the original image,
			 * not the accidentalArea
			 */
			Point p = new Point(minLoc.x + n.center().x - 3 * noteWidth,
					minLoc.y + n.center().y - staveGap);
			if (!OurUtils.isThereANoteAtThisPosition(p, OurUtils
					.whichStaveDoesAPointBelongTo(p, staves,
							workingSheet.rows())))
				sharps.add(p);
			OurUtils.zeroInMatrix(result, minLoc, (int) sharp_on.cols(),
					(int) sharp_on.rows());
		}
		//OurUtils.pointListSubtraction(halfNotes, ps2, threshholdDistance)

	}

	private void detectBeams() {
		Mat eroded = workingSheet.clone();
		for (Stave s : staves)
			s.drawDetailed(eroded, new Scalar(255, 255, 255));
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(staveGap / 2, staveGap / 2)));
		for (Note n : notes) {
			if (n.duration() != 1)
				continue;
			OurUtils.makeColour(eroded, new Point(n.center().x - 1.5
					* noteWidth, n.center().y - staveGap),
					(int) (3 * noteWidth), (int) (2 * staveGap), new Scalar(0,
							0, 0));
		}
		OurUtils.writeImage(eroded, OurUtils.getPath("output/beamEroded.jpg"));
		boolean foundOne = false;
		do {
			for (Line l : beams) {
				Core.line(eroded, l.start(), l.end(), new Scalar(0, 0, 0),
						(int) staveGap);
			}
			foundOne = extractBeams(eroded);
		} while (foundOne);
		for (Line l : beams) {
			Stave s = OurUtils.whichStaveDoesAPointBelongTo(l.start(), staves,
					workingSheet.rows());
			List<Note> notes = s.notes();
			int i = 0;
			while (notes.get(i).center().x <= l.start().x
					- (l.length > 30 ? beamToNoteTolerance
							: beamToNoteTolerance / 2)
					&& i < notes.size() - 1)
				i++;
			while (notes.get(i).center().x <= l.end().x + beamToNoteTolerance) {
				if (notes.get(i).duration() <= 1)
					notes.get(i).halveDuration();
				i++;
				if (i >= notes.size())
					break;
			}
		}
	}

	private boolean extractBeams(Mat eroded) {
		List<Line> smallBeams = new LinkedList<Line>();
		for (Stave s : staves) {
			Mat part = eroded.clone().submat(s.yRange(workingSheet.rows()),
					s.xRange());
			Mat verticalProj = OurUtils.verticalProjection(part);
			List<Interval> potentialBeams = new LinkedList<Interval>();
			final int startDetectionX = (int) s.startDetection().x;
			int start = startDetectionX;
			boolean in = false;
			for (int col = 0; col < verticalProj.cols(); col++) {
				if (!in && verticalProj.get(0, col)[0] >= 2) {
					in = true;
					start = startDetectionX + col;
				} else if (in && verticalProj.get(0, col)[0] < 1) {
					in = false;
					int end = startDetectionX + col;
					if (end - start > 10)
						potentialBeams.add(new Interval(start, end));
				}
			}
			for (Interval interval : potentialBeams) {
				Line smallBeam = getBeams(
						eroded.clone().submat(s.yRange(workingSheet.rows()),
								interval.toRange()), s, interval);
				if (smallBeam != null)
					smallBeams.add(smallBeam);
			}
			// for (Note n : s.notes())
			// n.display();
		}
		joinBeams(smallBeams);
		beams.addAll(smallBeams);
		return (smallBeams.size() > 0);
	}

	private void joinBeams(List<Line> smallBeams) {
		for (int i = 0; i < smallBeams.size(); i++) {
			for (int j = i + 1; j < smallBeams.size(); j++) {
				Line beamI = smallBeams.get(i);
				Line beamJ = smallBeams.get(j);
				if (OurUtils.distanceBetweenTwoPoints(beamI.end(),
						beamJ.start()) < beamJoinTolerance) {
					smallBeams.add(new Line(beamI.start(), beamJ.end()));
					smallBeams.remove(beamI);
					smallBeams.remove(beamJ);
					i--;
					break;
				} else if (OurUtils.distanceBetweenTwoPoints(beamJ.end(),
						beamI.start()) < beamJoinTolerance) {
					smallBeams.add(new Line(beamJ.start(), beamI.end()));
					smallBeams.remove(beamI);
					smallBeams.remove(beamJ);
					i--;
					break;
				}
			}
		}
	}

	private Line getBeams(Mat part, Stave s, Interval interval) {
		Mat horizontalProj = OurUtils.horizontalProjection(part);
		int startDetectionY = (int) s.startDetection().y;
		int start = startDetectionY;
		boolean in = false;
		List<Note> notes = s.notes();
		int i = 0;
		Note n = notes.get(i);
		while (n.center().x <= interval.min() - beamToNoteTolerance
				&& i < notes.size() - 1) {
			i++;
			n = notes.get(i);
		}
		Note next = n;
		while (Math.abs(next.center().y - n.center().y) < staveGap / 4) {
			if (i < notes.size() - 1)
				i++;
			else
				break;
			next = notes.get(i);
		}
		boolean descending = n.center().y < next.center().y;
		int noteY = (int) n.center().y - startDetectionY;
		for (int row = 0; row < noteY; row++) {
			if (!in
					&& horizontalProj.get(row, 0)[0] >= beamHorizontalThresholdTolerance) {
				in = true;
				start = startDetectionY + row;
			} else if (in
					&& horizontalProj.get(row, 0)[0] < beamHorizontalThresholdTolerance) {
				in = false;
				int end = startDetectionY + row;
				if (descending)
					return new Line(new Point(interval.min(), start),
							new Point(interval.max(), end));
				else
					return new Line(new Point(interval.min(), end), new Point(
							interval.max(), start));
			}
		}
		for (int row = horizontalProj.rows() - 1; row > noteY; row--) {
			if (!in
					&& horizontalProj.get(row, 0)[0] >= beamHorizontalThresholdTolerance) {
				in = true;
				start = startDetectionY + row;
			} else if (in
					&& horizontalProj.get(row, 0)[0] < beamHorizontalThresholdTolerance) {
				in = false;
				int end = startDetectionY + row;
				if (descending)
					return new Line(new Point(interval.min(), end), new Point(
							interval.max(), start));
				else
					return new Line(new Point(interval.min(), start),
							new Point(interval.max(), end));
			}
		}
		return null;
	}

	private void detectHalfNotes() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(1, staveGap / 2)));
		OurUtils.writeImage(eroded, OurUtils.getPath("output/erodingTry.jpg"));
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
					if (/*
						 * !OurUtils.isThereANoteAtThisPosition(p, s) &&
						 */OurUtils.isAHalfNote(p, eroded, (int) staveGap)) {
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

	/*
	 * Need to run 2 detections and get the intersection of them because of
	 * noise created by the beams during the eroding
	 */
	private void detectNotes() {
		Mat eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT,
				new Size(3 * staveGap / 4, 3 * staveGap / 4)));
		OurUtils.writeImage(eroded, OurUtils.getPath("output/eroded.png"));
		noteWidth = staveGap * 7 / 6;
		List<Note> allNotesOne = new LinkedList<Note>();
		for (Stave s : staves)
			allNotesOne.addAll(detectNoteOnPart(eroded, s));
		eroded = workingSheet.clone();
		Imgproc.erode(eroded, eroded, Imgproc.getStructuringElement(
				Imgproc.MORPH_RECT, new Size(staveGap / 2, staveGap / 2)));
		OurUtils.writeImage(eroded, OurUtils.getPath("output/eroded2.png"));
		List<Note> allNotesTwo = new LinkedList<Note>();
		for (Stave s : staves)
			allNotesTwo.addAll(detectNoteOnPart(eroded, s));
		for (Note n1 : allNotesOne) {
			for (Note n2 : allNotesTwo) {
				if (OurUtils.distanceBetweenTwoPoints(n1.center(), n2.center()) < 3) {
					notes.add(n1);
					OurUtils.whichStaveDoesAPointBelongTo(n1.center(), staves,
							workingSheet.rows()).addNote(n1);
					allNotesTwo.remove(n2);
					break;
				}
			}
		}
	}

	private void detectQuaverRests() {
		for (Stave s : staves) {
			Mat result = new Mat();
			quaverRest = OurUtils.resizeImage(masterQuaverRest,
					s.staveGap() * 2);
			Mat quaverRestArea = workingSheet.submat(
					s.closeYRange(workingSheet.rows()), new Range(0, workingSheet.cols()));
			Imgproc.matchTemplate(quaverRestArea, quaverRest, result,
					Imgproc.TM_CCOEFF);
			Point minLoc = Core.minMaxLoc(result).minLoc;
			double minVal = Core.minMaxLoc(result).minVal;
			double minAllowed = minVal * 0.9;
			while (minVal < minAllowed) {
				Point p = new Point(minLoc.x, s.startYRange() + 4
						* s.staveGap() + minLoc.y);
				quaverRests.add(p);
				OurUtils.zeroInMatrix(result, minLoc, (int) quaverRest.cols(),
						(int) quaverRest.rows());
				minLoc = Core.minMaxLoc(result).minLoc;
				minVal = Core.minMaxLoc(result).minVal;
			}
		}
	}
	
	private void detectNoteRests(){
		for (Stave s : staves) {
			Mat result = new Mat();
			noteRest = OurUtils.resizeImage(masterNoteRest,
					s.staveGap()*3);
			Mat noteRestArea = workingSheet.submat(
					s.closeYRange(workingSheet.rows()),
					new Range(0, workingSheet.cols()));
			Imgproc.matchTemplate(noteRestArea, noteRest, result,
					Imgproc.TM_CCOEFF);
			Point minLoc = Core.minMaxLoc(result).minLoc;
			double minVal = Core.minMaxLoc(result).minVal;
			double minAllowed = minVal * 0.9;
			while (minVal < minAllowed) {
				Point p = new Point(minLoc.x, s.startYRange() + 4*s.staveGap() + minLoc.y);
				noteRests.add(p);
				OurUtils.zeroInMatrix(result, minLoc, (int) noteRest.cols(),
						(int) noteRest.rows());
				minLoc = Core.minMaxLoc(result).minLoc;
				minVal = Core.minMaxLoc(result).minVal;
			}
		}
	}
	
	private List<Note> detectNoteOnPart(Mat ref, Stave s) {
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		List<Note> result = new LinkedList<Note>();
		Imgproc.findContours(
				ref.submat(s.yRange(workingSheet.rows()), s.xRange()),
				contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Collections.sort(contours, new Comparator<MatOfPoint>() {

			@Override
			public int compare(MatOfPoint lhs, MatOfPoint rhs) {
				Rect r1 = Imgproc.boundingRect(lhs);
				Rect r2 = Imgproc.boundingRect(rhs);
				return (int) (r2.area() - r1.area());
			}
		});
		List<Moments> mu = new ArrayList<Moments>(contours.size());
		for (int i = 0; i < contours.size(); i++) {
			Rect r = Imgproc.boundingRect(contours.get(i));
			mu.add(i, Imgproc.moments(contours.get(i), false));
			Moments m = mu.get(i);
			Note potentialNote = null;
			if (r.width <= noteWidth) {
				if (m.get_m00() != 0) {
					double x = (m.get_m10() / m.get_m00())
							+ s.startDetection().x;
					double y = (m.get_m01() / m.get_m00()) + s.startYRange();
					potentialNote = new Note(new Point(x, y), 1);
				} else {
					potentialNote = new Note(new Point(r.x + r.width / 2
							+ s.startDetection().x, r.y + r.height / 2
							+ s.startYRange()), 1);
				}
			}
			if (potentialNote != null) {
				if (!OurUtils.isInAnyRectangle(trebleClefs, trebleClef.cols(),
						trebleClef.rows(), potentialNote.center())
						&& !OurUtils.isInAnyRectangle(fourFours,
								fourFour.cols(), fourFour.rows(),
								potentialNote.center())) {
					if (!OurUtils.isThereANoteAtThisPosition(
							potentialNote.center(), s)) {
						result.add(potentialNote);
					}
				}
			}
		}
		return result;
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
		Log.i("PROC", "" + lines.size());
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

	/*
	 * private Mat rotateSheetOnStave(Stave s) { Line l = s.topLine(); double
	 * angleToRotate = Math.atan((l.end().y - l.start().y) / (l.end().x -
	 * l.start().x)); Mat rotated = OurUtils.rotateMatrix(
	 * workingSheet.clone().submat(s.yRange(workingSheet.rows()), s.xRange()),
	 * angleToRotate); return rotated; }
	 * 
	 * 
	 * private void correctBeams() { for (int i = 0; i < beams.size(); i++) {
	 * Stave stave = OurUtils.whichStaveDoesAPointBelongTo(beams.get(i)
	 * .start(), staves, workingSheet.rows()); if
	 * (!OurUtils.isABeam(beams.get(i), stave)) { beams.remove(i); i--; } else {
	 * for (Note n : stave.notes()) { if (n.center().x > beams.get(i).start().x
	 * - beamLengthTolerance && n.center().x < beams.get(i).end().x +
	 * beamLengthTolerance) n.setDuration(n.duration() / 2); } } } }
	 */
	/**
	 * Highlights all detected features of the music by writing them on the
	 * image
	 **/
	public Mat print() {
		printStaves(output);
		printFourFour(output);
		printTrebleClefs(output);
		printNotes(output);
		printFlats(output);
		//printSharps(output);
		printDots(output);
		printBeams(output);
		printScale(output);
		printQuaverRests(output);
		printNoteRests(output);
		return output;
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
			s.drawDetailed(sheet, new Scalar(128, 0, 0));
		}
	}

	private void printQuaverRests(Mat sheet) {
		for (Point qr : quaverRests)
			Core.rectangle(sheet, qr, new Point(qr.x + quaverRest.cols(), qr.y
					+ quaverRest.rows()), new Scalar(255, 0, 127), 4);
	}
	
	private void printNoteRests(Mat sheet){
		for (Point nr : noteRests)
			Core.rectangle(sheet, nr, new Point(nr.x + quaverRest.cols(), nr.y
					+ quaverRest.rows()), new Scalar(0, 255, 0), 4);
	}
	
	

	private void printBeams(Mat sheet) {
		for (Line l : beams) {
			Core.line(sheet, l.start(), l.end(), new Scalar(0, 255, 255), 4);
		}
	}

	private void printTrebleClefs(Mat sheet) {
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
													128, 0, 128)
													: n.duration() == 4.0 ? new Scalar(
															0, 255, 255)
															: new Scalar(0,
																	127, 255),
					-1);
	}

	private void printFlats(Mat sheet) {
		for (Point p : flats)
			Core.rectangle(sheet, p, new Point(p.x + flat_on.cols(), p.y
					+ flat_on.rows()), new Scalar(255, 0, 127), 4);
	}

	private void printSharps(Mat sheet) {
		for (Point s : sharps)
			Core.rectangle(sheet, s, new Point(s.x + sharp_on.cols(), s.y
					+ sharp_on.rows()), new Scalar(127, 80, 157), 4);
	}

	private void printDots(Mat sheet) {
		for (Point p : dots.keySet())
			Core.rectangle(sheet, new Point(p.x - dotWidth / 2, p.y - dotHeight
					/ 2), new Point(p.x + dotWidth / 2, p.y + dotHeight / 2),
					new Scalar(0, 255, 128), 6);
	}

}
