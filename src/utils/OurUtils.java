package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import musicdetection.Clef;
import musicdetection.Line;
import musicdetection.MusicDetector;
import musicdetection.Note;
import musicdetection.Stave;
import musicdetection.StaveLine;
import musicrepresentation.Duration;
import musicrepresentation.NoteName;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class OurUtils {

	private static final double staveGapTolerance = 0.2;
	public static final String sdPath = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
	public static final int totalVerticalSlices = 30;
	public static final double STANDARD_IMAGE_WIDTH = 2500;

	public static final String DATA_FOLDER = "data";
	public static final String IMAGE_FOLDER = "images";
	public static final String MIDI_FOLDER = "music";

	/***************************************
	 ********** MATRIX OPERATIONS **********
	 **************************************/

	public static void invertColors(Mat mat) {
		Core.bitwise_not(mat, mat);
	}

	/**
	 * Divides the image into 250x250 pixel sections as much as possible, then
	 * for each section it takes the mean pixel intensity, and thresholds the
	 * section based on that value.
	 **/
	public static void thresholdImage(Mat sheet) {

		int width = sheet.cols();
		int height = sheet.rows();
		int sep = 250;
		for (int j = 0; j < width; j += sep) {
			for (int i = 0; i < height; i += sep) {
				int xMax = Math.min(j + sep, width);
				int yMax = Math.min(i + sep, height);
				Mat section = new Mat(sheet, new Range(i, yMax), new Range(j,
						xMax));
				double mean = Core.mean(section).val[0];
				mean = Math.max(Math.min(mean * 0.9, 255), 0);
				Imgproc.threshold(section, section, mean, 256,
						Imgproc.THRESH_BINARY);

				Rect area = new Rect(new Point(j, i), section.size());
				Mat selectedArea = sheet.submat(area);
				section.copyTo(selectedArea);
			}
		}
	}

	public static Mat resizeImage(Mat image, double newHeight) {
		double newWidth = newHeight * image.cols() / image.rows();
		Size newSize = new Size(newWidth, newHeight);

		assert (newHeight > 0 && newWidth > 0);

		Mat newImage = new Mat(newSize, image.type());
		Imgproc.resize(image, newImage, newSize);

		// Utils.writeImage(image, Utils.getPath("output/checkNote.png"));

		return newImage;

	}

	public static void zeroInMatrix(Mat mat, Point start, int width, int height) {
		int startX = (int) Math.max(0, start.x - width);
		int startY = (int) Math.max(0, start.y - height);
		int endX = (int) Math.min(mat.width(), start.x + width);
		int endY = (int) Math.min(mat.height(), start.y + height);
		MusicDetector.zerosForTM.submat(0, endY - startY, 0, endX - startX).copyTo(mat.submat(startY, endY, startX, endX));
	}

	public static void makeColour(Mat sheet, Point topLeft, int width,
			int height, Scalar s) {
		Rect selectedArea = new Rect((int) topLeft.x, (int) topLeft.y, width,
				height);
		Mat colour = new Mat(new Size(width, height), sheet.type(), s);
		colour.copyTo(sheet.submat(selectedArea));
	}

	public static Mat verticalProjection(Mat mat) {
		Mat result = mat.clone();
		Core.reduce(mat, result, 0, Core.REDUCE_AVG, CvType.CV_32S);
		return result;
	}

	public static Mat horizontalProjection(Mat mat) {
		Mat result = mat.clone();
		Core.reduce(mat, result, 1, Core.REDUCE_AVG, CvType.CV_32S);
		return result;
	}

	public static Mat rotateMatrix(Mat mat, double angle) {
		Mat result = mat.clone();
		// int length = Math.max(mat.cols(), mat.rows());
		Point p = new Point(mat.cols() / 2, mat.rows() / 2);
		Mat rotationMatrix = Imgproc.getRotationMatrix2D(p, angle, 1.0);
		Imgproc.warpAffine(mat, result, rotationMatrix, new Size(mat.cols(),
				mat.rows()));
		return result;
	}

	/***************************************
	 ************ CHECK METHODS ************
	 **************************************/

	/**
	 * 
	 * @param toCheck
	 * @param currentStave
	 * @return
	 */
	public static boolean isThereANoteAtThisPosition(Point toCheck,
			Stave currentStave) {
		for (Note n : currentStave.notes()) {
			if (Math.abs(n.center().x - toCheck.x) < MusicDetector.noteMinDistance)
				return true;
		}
		return false;
	}

	public static boolean isOnBeamLine(Point centre, double noteWidth,
			double staveGap, Line line) {
		double slope = (line.end().y - line.start().y)
				/ (line.end().x - line.start().x);
		double beamY = (line.start().y + slope * (centre.x - line.start().x));
		return !(centre.x + noteWidth < line.start().x
				|| centre.x - noteWidth > line.end().x
				|| centre.y + staveGap < beamY || centre.y - staveGap > beamY);
	}

	public static boolean isOnBeamLine(Point centre, double noteWidth,
			double staveGap, List<Line> lines) {
		for (Line l : lines) {
			if (isOnBeamLine(centre, noteWidth, staveGap, l))
				return true;
		}
		return false;
	}

	public static boolean isThereASimilarLine(List<Line> beams, Line l) {
		for (Line line : beams) {
			double slope = (line.end().y - line.start().y)
					/ (line.end().x - line.start().x);
			if (Math.abs((line.start().y + slope
					* (l.start().x - line.start().x))
					- l.start().y) < 10) {
				if ((l.start().x >= line.start().x && l.start().x <= line.end().x)
						|| (l.start().x <= line.start().x && l.end().x >= line
								.start().x))
					return true;
			}
		}
		return false;
	}

	public static boolean isABeam(Line line, Stave s, double staveGap) {
		boolean beginning = false;
		boolean end = false;
		boolean actual = false;
		for (Note n : s.notes()) {
			if (Math.abs(n.center().x - line.start().x) < MusicDetector.beamLengthTolerance)
				beginning = true;
			else if (Math.abs(n.center().x - line.end().x) < MusicDetector.beamLengthTolerance)
				end = true;
			if (beginning && end && actual)
				return true;
		}
		return false;
	}

	public static boolean isInRectangle(Point topLeft, int width, int height,
			Point toCheck) {
		return (toCheck.y >= topLeft.y && toCheck.y <= topLeft.y + height
				&& toCheck.x >= topLeft.x && toCheck.x <= topLeft.x + width);
	}

	public static boolean isInAnyRectangle(List<Point> topLefts, int width,
			int height, Point toCheck) {
		for (Point p : topLefts) {
			if (isInRectangle(p, width, height, toCheck))
				return true;
		}
		return false;
	}

	public static Point isInCircle(Point centre, double radius, Mat ref) {
		// checks the pixels within a square of side length=radius
		// of the point where a note is suspected to be. If the area is only
		// black,
		// a note can't exist there, as a note would leave behind a trace in the
		// eroded image
		Point tmp = centre.clone();
		Interval width = new Interval(0, ref.width());
		Interval height = new Interval(0, ref.height());
		for (int i = (int) -radius; i <= radius; i++) {
			for (int j = (int) -radius; j <= radius; j++) {
				tmp.x = centre.x + i;
				tmp.y = centre.y + j;
				if (!width.contains((int) tmp.x)
						|| !height.contains((int) tmp.y))
					continue;
				if (ref.get((int) tmp.y, (int) tmp.x)[0] != 0)
					return tmp;
			}
		}
		return null;
	}

	/****************************************
	 ************** IO METHODS **************
	 ****************************************/

	public static String getDestImage(String src) {
		String result = "";
		int i = 0;
		while (src.charAt(i) != '.') {
			result += src.charAt(i);
			i++;
		}
		result += "Out.";
		i++;
		while (i < src.length()) {
			result += src.charAt(i);
			i++;
		}
		return result;
	}

	public static String getDestMid(String src) {
		String result = "";
		int i = 0;
		while (src.charAt(i) != '.') {
			result += src.charAt(i);
			i++;
		}
		result += ".mid";
		return result;
	}

	public static Mat readImage(String src) {

		Mat img = Highgui.imread(src, 0);
		if (img == null)
			Log.i("SightReadingActivity",
					"There was a problem loading the image " + src);
		return img;
	}

	public static void writeImage(Mat src, String dst) {
		Highgui.imwrite(dst, src);
	}

	// returns the path of a given src image, assuming root directory of DCIM
	public static String getPath(String folder) {
		return OurUtils.sdPath + "SightReader/" + folder;
	}

	/*****************************************
	 ************* OTHER METHODS *************
	 ****************************************/

	public static Bitmap RotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, true);
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

	public static List<StaveLine> getSpacedLines(List<StaveLine> lines,
			List<StaveLine> actualLines) {
		/*
		 * PRE: Given a list of horizontal lines of similar length Checks that
		 * the lines are equally spaced
		 */
		Collections.sort(lines, new Comparator<StaveLine>() {
			@Override
			public int compare(StaveLine line0, StaveLine line1) {
				return (int) (Math.signum((line0.toLine().start().y - line1
						.toLine().start().y)));
			}
		});
		// MID: lines is sorted highest to lowest
		int maxStaveHeight = 100;

		StaveLine first = lines.get(0);
		for (int i = 1; i < lines.size(); i++) {
			StaveLine second = lines.get(i);
			List<StaveLine> result = new LinkedList<StaveLine>();
			result.add(first);
			result.add(second);

			double space = second.toLine().start().y - first.toLine().start().y;
			double pos = second.toLine().start().y + space;

			if (space > maxStaveHeight)
				continue;

			for (int j = i + 1; j < lines.size(); j++) {
				if (Math.abs(lines.get(j).toLine().start().y - pos) < space
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
		return new LinkedList<StaveLine>();
	}

	public static Point getClefPoint(Stave s, List<Point> trebleClefs,
			double staveGap) {
		int i = 0;
		Point clef = trebleClefs.get(i);
		while (!(new Interval((int) (s.topLine().start().y - 4 * staveGap),
				(int) (s.bottomLine().start().y)).contains((int) clef.y))) {
			i++;
			clef = trebleClefs.get(i);
		}
		return clef;
	}

	public static Stave whichStaveDoesAPointBelongTo(Point p,
			List<Stave> staves, int maxRows) {
		for (Stave s : staves) {
			if (new Interval(s.yRange(maxRows)).contains((int) p.y))
				return s;
		}
		return null;
	}

	public static List<SheetStrip> sliceSheet(Mat sheet, List<Integer> divisions) {
		int totalHorizontalSlices = divisions.size() - 1;
		List<SheetStrip> staveMats = new LinkedList<SheetStrip>();

		int width = sheet.cols();
		int verticalSliceWidth = width / totalVerticalSlices;

		for (int hSlice = 0; hSlice < totalHorizontalSlices; hSlice++) {
			Slice[] slices = new Slice[totalVerticalSlices];
			for (int vSlice = 0; vSlice < totalVerticalSlices; vSlice++) {

				int x0 = vSlice * verticalSliceWidth;
				int x1 = (vSlice + 1) * verticalSliceWidth;
				int y0 = divisions.get(hSlice);
				int y1 = divisions.get(hSlice + 1);

				Mat sliceMat = sheet.submat(new Rect(new Point(x0, y0),
						new Size(verticalSliceWidth, y1 - y0)));
				slices[vSlice] = new Slice(sliceMat, new Point(x0, y0),
						new Point(x1, y1));

				// Utils.writeImage(sliceMat, getPath("output/test" + hSlice +
				// "_"+ vSlice + ".png"));
			}
			staveMats.add(new SheetStrip(slices));
		}
		return staveMats;
	}

	public static LinkedList<Integer> detectDivisions(Mat mat, int threshold) {
		LinkedList<Integer> divisions = new LinkedList<Integer>();
		divisions.add(0);
		int minGapHeight = (int) (mat.height() * 0.05);
		int lastPoint = 0;
		boolean inGap = false;
		for (int i = 0; i < mat.rows(); i++) {
			int[] v = new int[4];
			mat.get(i, 0, v);

			if (inGap) {
				if (v[0] < threshold) {
					// end of gap detected
					int height = i - lastPoint;
					if (height > minGapHeight) {
						divisions.add(lastPoint + height / 2);
					}

					lastPoint = i;
					inGap = false;
				}
			} else {
				if (v[0] > threshold) {
					// start of gap detected
					lastPoint = i;
					inGap = true;
				}
			}
		}
		divisions.add(mat.rows());
		return divisions;
	}

	/*
	 * public static List<BeamDivision> detectBeamDivisions(Mat sheet) { Mat
	 * verticalProj = verticalProjection(sheet); List<BeamDivision>
	 * potentialBeams = new LinkedList<BeamDivision>(); int[] v = new int[4];
	 * boolean in = false; int lastEntry = 0; for (int i = 0; i <
	 * verticalProj.cols(); i++) { verticalProj.get(0, i, v); if (in)
	 * Log.d("Guillaume", "in: " + in); if (in && (v[0] <
	 * MusicDetector.beamVerticalThresholdTolerance)) { if (i - lastEntry >
	 * MusicDetector.beamMinLength) { Log.d("Guillaume",
	 * "New line detected at x: " + lastEntry + "," + i); Mat region =
	 * sheet.submat(new Range(0, sheet.rows()), new Range(lastEntry, i));
	 * writeImage(region, getPath("output/proj" + i + ".jpg")); Mat
	 * horizontalProj = horizontalProjection(region); List<Integer> divs =
	 * detectDivisions(horizontalProj,
	 * MusicDetector.beamHorizontalThresholdTolerance); Point p1 = null, p2 =
	 * null; for (int j = 0; j < divs.size() - 1; j++) { int start =
	 * divs.get(j); int end = divs.get(j + 1); if (end - start > 10) { if
	 * (sheet.get(start, lastEntry)[0] == 255) { p1 = new Point(lastEntry,
	 * start); p2 = new Point(i, end); break; } else if (sheet.get(end - 1,
	 * lastEntry)[0] == 255) { p1 = new Point(lastEntry, end); p2 = new Point(i,
	 * start); break; } Log.e("Guillaume",
	 * "Could not find a valid match for point @position: " + lastEntry + "," +
	 * start + "/" + i + "," + end); } } List<Point> toBeam = new
	 * LinkedList<Point>(); toBeam.add(p1); toBeam.add(p2);
	 * potentialBeams.add(new BeamDivision(toBeam)); } in = false; } else if
	 * (!in && (v[0] > MusicDetector.beamVerticalThresholdTolerance)) { in =
	 * true; lastEntry = i; } } return potentialBeams; }
	 */

	public static Point findNearestNeighbour(Point centre, Mat ref, int width,
			int height) {
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		int centreY = (int) centre.y;
		int centreX = (int) centre.x;
		double x = 0, y = 0;
		Imgproc.findContours(ref.submat(centreY - height, centreY + height,
				centreX - width, centreX + width), contours, new Mat(),
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		List<Moments> mu = new ArrayList<Moments>(contours.size());
		for (int i = 0; i < contours.size(); i++) {
			mu.add(i, Imgproc.moments(contours.get(i), false));
			Moments m = mu.get(i);
			if (m.get_m00() != 0) {
				x = (m.get_m10() / m.get_m00()) + centreX - width;
				y = (m.get_m01() / m.get_m00()) + centreY - height;
				if (Math.abs(centreY - y) < height / 3)
					return new Point(x, y);
			}
		}
		return centre;
	}

	// TODO: this method is only implemented for treble clef and for octaves 0
	// and 1
	public static int getOctave(Clef c, int line) {
		if (c == Clef.Treble) {
			if (line >= -2 && line <= 4)
				return 4;
			if (line >= 5 && line <= 11)
				return 5;
		}
		return 10;
	}

	// TODO: this method is only implemented for treble clef
	public static NoteName getName(Clef c, int line) {
		line = line % 7;
		while (line < 0)
			line += 7;
		if (c == Clef.Treble) {
			switch (line) {
			case 0:
				return NoteName.E;
			case 1:
				return NoteName.F;
			case 2:
				return NoteName.G;
			case 3:
				return NoteName.A;
			case 4:
				return NoteName.B;
			case 5:
				return NoteName.C;
			case 6:
				return NoteName.D;
			}
		}

		if (c == Clef.Bass) {
			return getName(Clef.Treble, line - 2);
		}

		if (c == Clef.Alto) {
			// TODO
			return NoteName.A;
		}
		return null;
	}

	public static Duration getDuration(double duration) {
		if (duration == 1.)
			return Duration.Crotchet;
		if (duration == 2.)
			return Duration.Minim;
		if (duration == 0.5)
			return Duration.Quaver;
		return null;
	}

	public static void saveTempImage(Bitmap bitmap, String fName) {
		String pName = getPath("temp/");
		saveImage(bitmap, pName, fName);

	}

	public static Bitmap loadTempImage(int imageNum)
			throws FileNotFoundException {
		String fName = "page" + (imageNum + 1);
		String pName = getPath("temp/");
		return loadImage(pName, fName);

	}

	public static Mat loadTempMat(int imageNum) throws FileNotFoundException {
		String fName = "page" + (imageNum + 1) + ".png";
		String pName = getPath("temp/");
		Mat mat = readImage(pName + fName);
		if (mat == null)
			throw new FileNotFoundException();
		return mat;

	}

	private static void saveImage(Bitmap bitmap, String pName, String fName) {
		File dir = new File(pName);
		if (!dir.exists())
			dir.mkdirs();
		File file = new File(dir, fName + ".png");

		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Bitmap loadImage(String pName, String fName)
			throws FileNotFoundException {
		File dir = new File(pName);
		File file = new File(dir, fName + ".png");
		FileInputStream fis;

		fis = new FileInputStream(file);
		Bitmap b = BitmapFactory.decodeStream(fis);
		return b;
	}

	public static Line correctLine(Line potentialLine, Mat part, double staveGap) {
		return potentialLine;
	}

	public static double distanceBetweenTwoPoints(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}

	public static List<Point> pointListSubtraction(List<Point> ps1,
			List<Point> ps2, double threshholdDistance) {
		List<Point> output = ps1;
		for (Point x : ps1) {
			if (containsPoint(ps2, x, threshholdDistance) != null) {
				output.remove(x);
			}
		}
		return output;
	}

	public static Point containsPoint(List<Point> ps, Point p,
			double threshholdDistance) {
		for (Point x : ps) {
			if (distanceBetweenTwoPoints(p, x) < threshholdDistance) {
				return x;
			}
		}
		return null;
	}

	public static boolean isAHalfNote(Point p, Mat eroded, int staveGap) {
		int centerX = (int) p.x;
		int centerY = (int) p.y;
		Mat sub = eroded.submat(centerY - 2 * staveGap, centerY - staveGap / 2,
				centerX, centerX + staveGap);
		Mat horizontalProj = horizontalProjection(sub);
		boolean allWhite = true;
		for (int i = 0; i < horizontalProj.rows(); i++) {
			if (horizontalProj.get(i, 0)[0] < 10) {
				allWhite = false;
				break;
			}
		}
		if (allWhite)
			return true;
		sub = eroded.submat(centerY + staveGap / 2, centerY + 2 * staveGap,
				centerX - staveGap, centerX);
		horizontalProj = horizontalProjection(sub);
		allWhite = true;
		for (int i = 0; i < horizontalProj.rows(); i++) {
			if (horizontalProj.get(i, 0)[0] < 10) {
				return false;
			}
		}
		return true;
	}

	/** Use this to save midi images ad .sr connector when files are generated */
	/*
	 * public static void saveSRFiles(MidiFile midi, List<Bitmap> images, String
	 * saveName) { SRFileBuilder builder = new SRFileBuilder(saveName);
	 * Iterator<Bitmap> imagesIterator = images.iterator(); for (int i = 1;
	 * imagesIterator.hasNext(); i++) { saveImage(imagesIterator.next(),
	 * getPath(IMAGE_FOLDER), saveName + i);
	 * builder.addImagePath(getPath(IMAGE_FOLDER) + saveName + i + ".png"); }
	 * Playback.saveMidiFile(midi, saveName); builder.setMidiPath(saveName);
	 * builder.build(); }
	 */
}
