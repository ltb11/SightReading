package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import musicdetection.Line;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.sightreading.SightReadingActivity;

import android.os.Environment;
import android.util.Log;

public class Utils {

	private static final int minLineDirectionVectorDiff = 10;
	private static final int minLineGap = 3;
	private static final double horizontalError = 5;
	private static final double staveGapTolerance = 0.2;
	public static final String sdPath = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";

	public static Scalar createHsvColor(float hue, float saturation, float value) {

		int h = (int) ((hue % 1f) * 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		switch (h) {
		case 0:
			return new Scalar(value, t, p);
		case 1:
			return new Scalar(q, value, p);
		case 2:
			return new Scalar(p, value, t);
		case 3:
			return new Scalar(p, q, value);
		case 4:
			return new Scalar(t, p, value);
		case 5:
			return new Scalar(value, p, q);
		default:
			throw new RuntimeException(
					"Something went wrong when converting from HSV to RGB. Input was "
							+ hue + ", " + saturation + ", " + value);
		}

	}

	public boolean isHorizontal(Line l) {
		return (Math.abs(l.directingVector().second) < horizontalError);
	}

	public static void invertColors(Mat mat) {
		Core.bitwise_not(mat, mat);
	}

	/*
	 * This works as so: we check the line to test with every line already in
	 * and test: if the vector directing the line to test is close enough to the
	 * reference line (minLineDirectionVectorDiff) and the point on the test
	 * line with the same x as the start of the reference line is close enough
	 * to this start (minLineGap), the line is considered a duplicate and is
	 * dropped. We need to perform this check on both x and y coordinates
	 * because of horizontal/vertical lines
	 */
	public static boolean areTwoLinesDifferent(Point pt1, Point pt2, Mat lines,
			int i) {
		for (int j = 0; j < i; j++) {
			double[] lineToTest = lines.get(0, j);
			Point ptToTest1 = new Point(lineToTest[0], lineToTest[1]);
			Point ptToTest2 = new Point(lineToTest[2], lineToTest[3]);
			double[] v = new double[] { pt2.x - pt1.x, pt2.y - pt1.y };
			double[] vTest = new double[] { ptToTest2.x - ptToTest1.x,
					ptToTest2.y - ptToTest1.y };
			if ((Math.abs(vTest[1] / vTest[0] - v[1] / v[0]) < minLineDirectionVectorDiff && Math
					.abs(ptToTest1.y - pt1.y + (pt1.x - ptToTest1.x) * vTest[1]
							/ vTest[0]) < minLineGap)
					|| (Math.abs(vTest[0] / vTest[1] - v[0] / v[1]) < minLineDirectionVectorDiff && Math
							.abs(ptToTest1.x - pt1.x + (pt1.y - ptToTest1.y)
									* vTest[0] / vTest[1]) < minLineGap)) {
				return false;
			}
		}
		return true;
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

	/*
	 * PRE: Given a list of horizontal lines of similar length Checks that the
	 * lines are equally spaced
	 */
	public static List<Line> getSpacedLines(List<Line> lines,
			List<Line> actualLines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line line0, Line line1) {
				return (int) (Math.signum((line0.start().y - line1.start().y)));
			}
		});
		// MID: lines is sorted highest to lowest

		Line first = lines.get(0);
		for (int i = 1; i < lines.size(); i++) {
			Line second = lines.get(i);
			double space = second.start().y - first.start().y;
			double pos = second.start().y + space;
			List<Line> result = new LinkedList<Line>();
			result.add(first);
			result.add(second);
			for (int j = i + 1; j < lines.size(); j++) {
				if (Math.abs(lines.get(j).start().y - pos) < space
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
		return new LinkedList<Line>();
	}

	public static void resizeImage(Mat image, double newHeight) {
		double newWidth = newHeight * image.cols() / image.rows();
		Size newSize = new Size(newWidth, newHeight);
		Imgproc.resize(image, image, newSize);

		Utils.writeImage(image, Utils.getPath("output/checkNote.png"));

	}

	public static void printLines(Mat mat, List<Line> lines, Scalar colour) {
		for (int i = 0; i < lines.size(); i++) {
			Core.line(mat, lines.get(i).start(), lines.get(i).end(), colour, 1);
		}
	}

	public static void printMulticolouredLines(Mat mat, List<Line> lines) {
		for (int i = 0; i < lines.size(); i++) {
			Scalar colour = getColour(i);
			Core.line(mat, lines.get(i).start(), lines.get(i).end(), colour, 1);
		}
	}

	public static Scalar getColour(int i) {
		final int adjustBrighter = 50;
		int b = Mod((100 + i) * i, 255);
		int g = Mod((200 + i) * i, 255);
		int r = Mod((300 + i) * i, 255);

		if (b < 10 && g < 10 && r < 10) {
			if (b < g && b < r) {
				b += adjustBrighter;
			} else if (g < r) {
				g += adjustBrighter;
			} else {
				r += adjustBrighter;
			}
		}

		return new Scalar(b, g, r);
	}

	public static int Mod(int numberBeingDivided, int divisor) {
		while (numberBeingDivided > divisor) {
			numberBeingDivided -= divisor;
		}
		return numberBeingDivided;
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

	public static void verticalProjection(Mat mat) {
		Core.reduce(mat, mat, 0, Core.REDUCE_SUM);
	}

	public static void horizontalProjection(Mat mat) {
		Core.reduce(mat, mat, 1, Core.REDUCE_SUM);
	}
	
	public static Mat readImage(String src){
		
		Mat img =  Highgui.imread(src, 0);
		if (img == null)
			Log.i("SightReadingActivity", "There was a problem loading the image " + src);
		return img;
	}
	
	public static void writeImage(Mat src, String dst){
		Highgui.imwrite(dst, src);
	}
	
	//returns the path of a given src image, assuming root directory of DCIM
	public static String getPath(String src){
		return Utils.sdPath + src;
	}
	
	//checks the pixels within a square of side length=radius 
	//of the point where a note is suspected to be. If the area is only black,
	//a note can't exist there, as a note would leave behind a trace in the
	//eroded image
	
	public static boolean isInCircle(Point centre, double radius, Mat ref){
		Point tmp = centre.clone();
		boolean allBlack = true;
		ref.height();
		outerloop:
		for (int i=(int) -radius; i<=radius; i++){
			for (int j=(int) -radius; j<=radius; j++){
				tmp.x = centre.x + i;
				tmp.y = centre.y + j;
				if (tmp.x <0 || tmp.x>ref.width() || tmp.y <0 || tmp.y>ref.width()){
					continue;
				}
				if (ref.get((int) tmp.y, (int) tmp.x)[0] != 0){
					allBlack = false;
					break outerloop;
				}
			}
		}
		Log.v("conrad", String.valueOf(!allBlack));
		return !allBlack;
	}
	
}
