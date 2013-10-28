package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import musicdetection.Clef;
import musicdetection.Line;
import musicdetection.Note;
import musicdetection.Stave;
import musicdetection.StaveLine;
import musicrepresentation.Duration;
import musicrepresentation.NoteName;
import musicrepresentation.PlayedNote;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Environment;
import android.util.Log;

public class Utils {

	private static final double staveGapTolerance = 0.2;
	public static final String sdPath = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
	public static final int totalVerticalSlices = 20;
	public static final double STANDARD_IMAGE_WIDTH = 2500;

	/***************************************
	 ********** MATRIX OPERATIONS **********
	 **************************************/

	public static void invertColors(Mat mat) {
		Core.bitwise_not(mat, mat);
	}

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
				// Mat section = new Mat(sheet, new Range(j,xMax), new
				// Range(i,yMax));
				// Mat section = new Mat(sheet, new Range(0,3000), new
				// Range(0,2000));

				double mean = Core.mean(section).val[0];
				mean = Math.max(Math.min(mean - 20, 255), 0);
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

		Mat newImage = new Mat(newSize, image.type());
		Imgproc.resize(image, newImage, newSize);

		// Utils.writeImage(image, Utils.getPath("output/checkNote.png"));

		return newImage;

	}

	public static void zeroInMatrix(Mat mat, Point start, int width, int height) {
		int startX = (int) start.x;
		int startY = (int) start.y;
		for (int i = -width + 1; i < width; i++) {
			for (int j = -height + 1; j < height; j++) {
				int x = Math.max(startX + i, 0);
				int y = Math.max(startY + j, 0);
				mat.put(y, x, new double[] { 0 });
			}
		}
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
		int length = Math.max(mat.cols(), mat.rows());
		Point p = new Point(length / 2, length / 2);
		Mat rotationMatrix = Imgproc.getRotationMatrix2D(p, angle, 1.0);
		Imgproc.warpAffine(mat, result, rotationMatrix,
				new Size(length, length));
		return result;
	}

	/***************************************
	 ************ CHECK METHODS ************
	 **************************************/

	public static boolean isThereANoteAtThisPosition(Point toCheck,
			List<Note> notes, List<Stave> staves, double staveGap) {
		for (Note n : notes) {
			if (Math.abs(n.center().x - toCheck.x) < 30
					&& whichStaveDoesAPointBelongTo(n.center(), staves,
							staveGap).equals(
							whichStaveDoesAPointBelongTo(toCheck, staves,
									staveGap)))
				return true;
		}
		return false;
	}

	public static boolean isOnBeamLine(Point centre, double noteWidth,
			double staveGap, List<Line> quavers) {
		for (Line l : quavers) {
			if (!(centre.x - noteWidth / 2 > l.end().x
					|| centre.x + noteWidth / 2 < l.start().x
					|| centre.y - staveGap / 2 > l.end().y || centre.y
					+ staveGap / 2 < l.start().y))
				return true;
		}
		return false;
	}

	public static boolean isThereASimilarLine(List<Line> quavers, Line l) {
		for (Line line : quavers) {
			if (Math.abs(line.start().y - l.start().y) < 10) {
				if ((l.start().x >= line.start().x && l.start().x <= line.end().x)
						|| (l.start().x <= line.start().x && l.end().x >= line
								.start().x))
					return true;
			}
		}
		return false;
	}

	public static boolean isABeam(Line line, List<Note> notes, double staveGap) {
		boolean beginning = false;
		boolean end = false;
		for (Note n : notes) {
			if (Math.abs(n.center().x - line.start().x) < 20
					&& Math.abs(n.center().y - line.start().y) > 2 * staveGap
					&& Math.abs(n.center().y - line.start().y) < 4 * staveGap)
				beginning = true;
			else if (Math.abs(n.center().x - line.end().x) < 20
					&& Math.abs(n.center().y - line.end().y) > 2 * staveGap
					&& Math.abs(n.center().y - line.end().y) < 4 * staveGap)
				end = true;
			if (beginning && end)
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
	public static String getPath(String src) {
		return Utils.sdPath + src;
	}

	/*****************************************
	 ************* OTHER METHODS *************
	 ****************************************/

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
				return (int) (Math.signum((line0.toLine().start().y - line1.toLine().start().y)));
			}
		});
		// MID: lines is sorted highest to lowest

		StaveLine first = lines.get(0);
		for (int i = 1; i < lines.size(); i++) {
			StaveLine second = lines.get(i);
			List<StaveLine> result = new LinkedList<StaveLine>();
			result.add(first);
			result.add(second);
			
			double space = second.toLine().start().y - first.toLine().start().y;
			double pos = second.toLine().start().y + space;
			
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
			List<Stave> staves, double staveGap) {
		for (Stave s : staves) {
			if ((new Interval((int) (s.topLine().start().y - 4 * staveGap),
					(int) (s.bottomLine().start().y + 4 * staveGap))
					.contains((int) p.y)))
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
			int[] v = new int[3];
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

	public static Point findNearestNeighbour(Point centre, Mat ref) {
		int x = (int) centre.x;
		int y = (int) centre.y;
		int minX = x;
		int maxX = x;
		int minY = y;
		int maxY = y;
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int j = minY; j <= maxY; j++) {
				if (ref.get(j, maxX + 1)[0] != 0) {
					maxX++;
					changed = true;
				}
			}
			for (int j = minY; j <= maxY; j++) {
				if (ref.get(j, minX - 1)[0] != 0) {
					minX--;
					changed = true;
				}
			}
			for (int i = minX; i <= maxX; i++) {
				if (ref.get(minY - 1, i)[0] != 0) {
					minY--;
					changed = true;
				}
			}
			for (int i = minX; i <= maxX; i++) {
				if (ref.get(maxY + 1, i)[0] != 0) {
					maxY++;
					changed = true;
				}
			}
		}
		Point p = new Point((minX + maxX) / 2, (minY + maxY) / 2);
		return p;
	}

	public static PlayedNote noteToNoteRepresentation(Note n,Mat sheet) {
		Stave s = n.stave();
		Point p = n.center();
		Log.v("Guillaume", Double.toString(p.x)+ "," + Double.toString(p.y));
		// octave in MIDI representation
		int line = getNote(s, p, sheet);
		Clef c = s.getClefAtPos(p);
		return new PlayedNote(getName(c, line), getOctave(c, line),
				 getDuration(n.duration()), 0);
	}

	// TODO: this method is only implemented for treble clef and for octaves 0
	// and 1
	public  static int getOctave(Clef c, int line) {
		if (c == Clef.Treble) {
			if (line >= -2 && line <= 4)
				return 4;
			if (line >= 5 && line <= 11)
				return 5;
		}
		return 10;
	}

	// TODO: this method is only implemented for treble clef
	public  static NoteName getName(Clef c, int line) {
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
		return null;
	}

	// line 0 is the bottom line of the staves
	private static int getNote(Stave s, Point p, Mat sheet) {
		double directingVector = (s.topLine().end().y - s.topLine().start().y)
				/ (s.topLine().end().x - s.topLine().start().x);
		double staveGap = s.staveGap();
		double origin = s.bottomLine().start().y;
		//offset of the point from the start
		double advance = p.x - s.bottomLine().start().x;
		double distance = 10000, prevDistance = 20000;
		double pointY = p.y;
		int currentLine = -10;
		//y is the variable that will change over the loop, augmented by staveGap/2 each time
		double y = origin - advance * directingVector - currentLine * staveGap
				/ 2;
 		while (prevDistance > distance) {
			if (currentLine == 7) {
				Log.v("Guillaume", "CHECK");
			}
			prevDistance = distance;
			currentLine++;
			y -= staveGap / 2;
			distance = Math.abs(y - pointY);
			Core.line(sheet, new Point(p.x, y), new Point (p.x + 10, y), new Scalar (0, 255, 0));
		}
		return currentLine - 1;
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

}
